package hua223.calamity.main;

import com.google.common.base.CaseFormat;
import hua223.calamity.events.LogoutRelease;
import hua223.calamity.integration.jei.CalamityJEIPlugin;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.loots.GlobalLootModifier;
import hua223.calamity.net.NetMessages;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.entity.AutoEntityRegister;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.util.CuriosConflictMap;
import hua223.calamity.util.ITransaction;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.Consumer;

// I made this mod simply because I love both games
// I'm not a professional game developer, and this was my first time working with modding
// I was not good at math, and I was the only one on the development team, so i can only do my best
@Mod(CalamityCurios.MODID)
public class CalamityCurios {
    // Define mod getId in a common place for everything to reference
    public static final String MODID = "calamity_curios";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private static final Map<Class<?>, EntityType<?>> ENTITY_TYPE_MAP = new Object2ObjectOpenHashMap<>();
    public static ResourceLocation ModResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation ResourceOf(String modId, String path) {
        return ResourceLocation.fromNamespaceAndPath(modId, path);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.parse(path);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityType<T> getEntityType(Class<T> caller) {
        return (EntityType<T>) ENTITY_TYPE_MAP.get(caller);
    }

    public CalamityCurios(final FMLJavaModLoadingContext context) {
        //Ensure that all requirement classes have been initialized
        AnnotationProcessor processor = new AnnotationProcessor(context);
        NetMessages.registerNetPack(processor);
        CalamityJEIPlugin.findJeiInfo(processor);
        CalamityConfig.registerConfig(processor);
        GlobalLoot.findLootFunction(processor);
        ITransaction.findTransactionList(processor);
        DamageSupplier.findRequester(processor);
        CuriosConflictMap.createConflictMap(processor);
        registerEntity(processor);
        findLogOutFunction(processor);
        processor.process();

        RegisterList.build(processor.modBus);
        ParticleRegister.register(processor.modBus);
        GlobalLootModifier.register(processor.modBus);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerEntity(AnnotationProcessor annotationProcessor) {
        final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
        ENTITIES.register(annotationProcessor.modBus);
        //pre
        final Map<EntityType<?>, Class<?>> renderMap = FMLEnvironment.dist.isClient() ?  new Object2ObjectOpenHashMap<>() : null;

        MethodType constructorType = MethodType.methodType(void.class, EntityType.class, Level.class);
        MethodType factoryType = MethodType.methodType(EntityType.EntityFactory.class);
        MethodType interfaceType = MethodType.methodType(Entity.class, EntityType.class, Level.class);
//        final String methodName = FMLEnvironment.production ? "m_20721_" : "create";
        final String methodName = EntityType.EntityFactory.class.getMethods()[0].getName();

        annotationProcessor.addStartProcessingEntries(AutoEntityRegister.class, processor -> {
            Class<?> clazz = processor.getDataClass();
            processor.analyzeDefaultData(AutoEntityRegister.class);

            MethodHandle ctorHandle = processor.lookup.findConstructor(clazz, constructorType);
            EntityType.EntityFactory<?> factory = (EntityType.EntityFactory<?>) LambdaMetafactory.metafactory(
                processor.lookup, methodName, factoryType, interfaceType, ctorHandle,
                MethodType.methodType(clazz, EntityType.class, Level.class)).getTarget().invokeExact();

            EntityType.Builder<?> builder = EntityType.Builder.of(factory, processor.parser("category"));
            if (processor.parser("noSummon")) builder.noSummon();
            if (processor.parser("noSave")) builder.noSave();
            float[] sized = processor.parser("sized");
            builder.setShouldReceiveVelocityUpdates(processor.parser("velocityUpdates"))
                .updateInterval(processor.parser("updateInterval"))
                .clientTrackingRange(processor.parser("trackingRange"))
                .sized(sized[0], sized[1]);

            String raw = processor.parser("name");
            final String name = raw.isEmpty() ? CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()) : raw;
            final Class<?> renderClass;
            if (renderMap != null) {
                Class<?> c = processor.parser("renderClass");
                renderClass = c == AutoEntityRegister.class ? processor.modLoader.loadClass(clazz.getName() + "$Renderer") : c;
            } else renderClass = null;

            ENTITIES.register(name, () -> {
                EntityType<?> type = builder.build(name);
                if (renderMap != null) renderMap.put(type, renderClass);
                ENTITY_TYPE_MAP.put(clazz, type);

                return type;
            });
        });

        if (renderMap != null) annotationProcessor.addPostProcessor(() -> {
            Consumer<EntityRenderersEvent.RegisterRenderers> consumer = event -> {
                MethodType constructorType2 = MethodType.methodType(void.class, EntityRendererProvider.Context.class);
                MethodType factoryType2 = MethodType.methodType(EntityRendererProvider.class);
                MethodType interfaceType2 = MethodType.methodType(EntityRenderer.class, EntityRendererProvider.Context.class);

                try {
                    final String name = EntityRendererProvider.class.getMethods()[0].getName();
                    for (Map.Entry<EntityType<?>, Class<?>> entry : renderMap.entrySet()) {
                        event.registerEntityRenderer(entry.getKey(), (EntityRendererProvider) LambdaMetafactory.metafactory(
                            annotationProcessor.lookup, name, factoryType2, interfaceType2, annotationProcessor
                                .lookup.findConstructor(entry.getValue(), constructorType2), interfaceType2).getTarget().invokeExact());
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };

            annotationProcessor.modBus.addListener(consumer);
        });
    }

    private static void findLogOutFunction(AnnotationProcessor annotationProcessor) {
        ArrayList<Object> s = new ArrayList<>();
        ArrayList<Object> c = FMLEnvironment.dist.isClient() ? new ArrayList<>() : null;
        annotationProcessor.addStartProcessingEntries(LogoutRelease.class, processor -> {
            MethodType type = processor.fromDescriptorGetType();
            List<Object> list = null;
            if (type.parameterCount() == 1) {
                Class<?> param = type.parameterType(0);
                if (param == ServerPlayer.class) list = s;
                else if (c != null && param == LocalPlayer.class) list = c;
            }

            Class<?> target = processor.getDataClass();
            String name = processor.getMethodName();
            if (list != null) {
                list.add(target);
                list.add(name);
            } else throw new IllegalStateException(String.format("Invalid player logout function %s was found in class %s", target, name));
        });
        annotationProcessor.addPostProcessor(() -> {
            Object[] objects = s.toArray();
            Consumer<PlayerEvent.PlayerLoggedOutEvent> consumer = event -> handlerLogOut(event.getEntity(), objects);
            annotationProcessor.forgeBus.addListener(consumer);

            if (c != null) {
                Object[] client = c.toArray();
                Consumer<ClientPlayerNetworkEvent.LoggingOut> clientConsumer = event -> handlerLogOut(event.getPlayer(), client);
                annotationProcessor.forgeBus.addListener(clientConsumer);
            }
        });
    }

    private static void handlerLogOut(Player player, Object[] info) {
        if (player == null) return;
        MethodHandles.Lookup handle = MethodHandles.publicLookup();
        MethodType methodType = MethodType.methodType(void.class, player.getClass());
        try {
            for (int i = 0; i < info.length; i++)
                handle.findStatic((Class<?>) info[i], (String) info[++i], methodType).invoke(player);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
