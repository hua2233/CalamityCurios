package hua223.calamity.loots;

import hua223.calamity.main.CalamityCurios;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

//Global loot function, used to manipulate all loot types in the context environment set.
//If simple modification or triggering of types is required, please use CalamityEventHandler to register curios events
public enum GlobalLoot {
    CHESTS_LOOTS {
        @Override
        public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ServerPlayer player = (ServerPlayer) context.getParam(LootContextParams.THIS_ENTITY);
            ChestLootContext lootContext = new ChestLootContext(generatedLoot, context, player, player.getRandom());
            onDrops(lootContext);
            return generatedLoot;
        }
    },

//    BLOCK_LOOTS {
//        @Override
//        public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
//            return null;
//        }
//    },

    ENTITY_LOOTS {
        @Override
        public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            Entity killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
            if (entity == null || killer == null || killer.getType() != EntityType.PLAYER) return generatedLoot;
            ServerPlayer player = (ServerPlayer) killer;
            onDrops(new EntitiesLootContext(generatedLoot, context, player.getRandom(), entity, player));
            return generatedLoot;
        }
    };

    public abstract ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);
    private final ArrayList<LootCall> globalLootFunctions = new ArrayList<>();

    //If the class is in singleton mode, you can also mount it in the constructor
    public static void mountTo(Object target) {
        Set<GlobalLoot> loots = EnumSet.noneOf(GlobalLoot.class);
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.getModifiers() != 17 || !method.isAnnotationPresent(ApplyGlobalLoot.class)) continue;

            Class<?> clazz = method.getParameterTypes()[0];
            if (BaseLootContextPacker.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(BaseLootContextPacker.GlobalLootType.class)) {
                GlobalLoot globalLoot = clazz.getAnnotation(BaseLootContextPacker.GlobalLootType.class).value();
                try {
                    if (!loots.contains(globalLoot)) {
                        MethodHandles.Lookup lookup = MethodHandles.lookup();
                        globalLoot.globalLootFunctions.add(toLambda(lookup, target, method.getName(), clazz));
                        loots.add(globalLoot);
                    } else CalamityCurios.LOGGER.warn("Duplicate apply type {} found in {} class", target.getClass(), globalLoot);
                } catch (Throwable exception) {
                    CalamityCurios.LOGGER.error("An unexpected error occurred while converting the method! method name: {}", method.getName(), exception);
                }
            } else CalamityCurios.LOGGER.warn("This type or method is not Mountable {}!", method.getName());
        }
    }

    //Unlike dynamically added event calls, these are persistent。
    //Convert it into a lambda object without variable capture, achieving hard coded efficiency in performance
    private static LootCall toLambda(MethodHandles.Lookup lookup, Object lootAble, String methodName, Class<?> covariantParameter) throws Throwable {
        Class<?> lootClass = lootAble.getClass();
        MethodType realType = MethodType.methodType(void.class, covariantParameter);
        CallSite site = LambdaMetafactory.metafactory(lookup, "doApply",
            MethodType.methodType(LootCall.class, lootClass), MethodType.methodType(void.class, BaseLootContextPacker.class),
            lookup.findVirtual(lootClass, methodName, realType), realType);
        //Allow it to detect the true object type
        return (LootCall) site.getTarget().invoke(lootAble);
    }

    @ApiStatus.Internal
    public void mountDynamic(Consumer<BaseLootContextPacker> call) {
        globalLootFunctions.add(new LootCall() {
            @Override
            public void doApply(BaseLootContextPacker context) {
                call.accept(context);
            }

            @Override
            public boolean isDynamic() {
                return true;
            }
        });
    }

    public void removeDynamic() {
        globalLootFunctions.removeIf(LootCall::isDynamic);
    }

    protected final void onDrops(BaseLootContextPacker packer) {
        for (LootCall handle : globalLootFunctions) {
            handle.doApply(packer);
            if (packer.isCancelDrop()) return;
        }
    }

    @FunctionalInterface
    private interface LootCall {
        void doApply(BaseLootContextPacker context);

        default boolean isDynamic() {
            return false;
        }
    }
}
