package hua223.calamity.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.UUID;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConflictChain {
    //Indicate the root conflict path that this accessory relies on
    Class<?> value();

    boolean isRoot() default false;

    Class<?> node() default Conflict.class;

    final class Conflict {
        private final static Object2ObjectOpenHashMap<Class<?>, Set<UUID>>
            GLOBAL_CONFLICT_CHAIN = new Object2ObjectOpenHashMap<>(16);

        //When attempting equipment, it should only be read-only to prevent accidental errors such as slot being full
        public static boolean noOccupied(ConflictChain link, LivingEntity entity) {
            Class<?> root = link.value();
            Set<UUID> result = GLOBAL_CONFLICT_CHAIN.get(root);
            if (result == null) return false;
            UUID uuid = entity.getUUID();
            boolean occ = result.contains(uuid);

            Class<?> node = link.node();
            if (node != Conflict.class)
                return !occ && !GLOBAL_CONFLICT_CHAIN.get(node).contains(uuid);
            return !occ;
        }

        //In theory, it only serves the Player service, and the reason for
        //use LivingEntity is that many entities in the Curio context are Living
        public static void lockInConflict(ConflictChain chain, LivingEntity entity) {
            Class<?> root = chain.value();
            UUID uuid = entity.getUUID();
            GLOBAL_CONFLICT_CHAIN.get(root).add(uuid);

            if (chain.node() != Conflict.class)
                GLOBAL_CONFLICT_CHAIN.get(chain.node()).add(uuid);
        }

        public static void unLockConflict(ConflictChain chain, LivingEntity entity) {
            UUID uuid = entity.getUUID();
            GLOBAL_CONFLICT_CHAIN.get(chain.value()).remove(uuid);
            if (chain.node() != Conflict.class)
                GLOBAL_CONFLICT_CHAIN.get(chain.node()).remove(uuid);
        }

        public static void registerRootToLink(Class<?> curio) {
            ConflictChain chain = curio.getAnnotation(ConflictChain.class);
            Class<?> link = chain.isRoot() ? chain.value() :
                chain.node() != Conflict.class ? chain.node() : null;
            if (link != null) {
                if (Conflict.GLOBAL_CONFLICT_CHAIN.containsKey(link))
                    throw new IllegalArgumentException("Duplicate root or node path!");

                Conflict.GLOBAL_CONFLICT_CHAIN.put(link, new ObjectOpenHashSet<>(2));
            }
        }

        public static void delete(ServerPlayer player) {
            UUID playerId = player.getUUID();
            for (Set<UUID> chain : GLOBAL_CONFLICT_CHAIN.values()) {
                chain.remove(playerId);
            }
        }
    }
}
