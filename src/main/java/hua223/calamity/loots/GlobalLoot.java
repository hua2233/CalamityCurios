package hua223.calamity.loots;

import hua223.calamity.main.CalamityCurios;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

//Global loot function, used to manipulate all loot types in the context environment set.
//If simple modification or triggering of types is required, please use CuriosEventHandler to register curios events
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
    private final ArrayList<MethodHandle> globalLootFunction = new ArrayList<>();

    //When a class is created, it is mounted through static code blocks.
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
                        MethodHandle handle = MethodHandles.publicLookup().unreflect(method).bindTo(target);
                        globalLoot.globalLootFunction.add(handle.asType(MethodType.methodType(void.class, BaseLootContextPacker.class)));
                        loots.add(globalLoot);
                    } else CalamityCurios.LOGGER.warn("Duplicate apply type {} found in {} class", target.getClass(), globalLoot);
                } catch (IllegalAccessException exception) {
                    CalamityCurios.LOGGER.error("An unexpected error occurred while converting the method! method name: {}", method.getName(), exception);
                }
            } else CalamityCurios.LOGGER.warn("This type or method is not Mountable {}!", method.getName());
        }
    }

    protected final void onDrops(BaseLootContextPacker packer) {
        try {
            for (MethodHandle handle : globalLootFunction) {
                handle.invokeExact(packer);
                if (packer.isCancelDrop()) return;
            }
        } catch (Throwable e) {
            CalamityCurios.LOGGER.error("{} a fatal error occurred while processing global tasks with the type!", this.name(), e);
        }
    }
}
