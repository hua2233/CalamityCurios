package hua223.calamity.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;

public class GlobalCuriosStorage {
    private static final Map<UUID, Map<Class<? extends ICuriosStorage>, Tuple<float[], UUID[]>>> STORAGES =
        new Object2ObjectOpenHashMap<>(2);

    public static Tuple<float[], UUID[]> getStorage(ICuriosStorage storage, LivingEntity player) {
        Map<Class<? extends ICuriosStorage>, Tuple<float[], UUID[]>> map = STORAGES.get(player.getUUID());
        if (map != null) return map.get(storage.getClass());

        return null;
    }

    public static void unEquipSafeRemove(Entity entity, ICuriosStorage storage) {
        UUID uuid = entity.getUUID();
        if (STORAGES.containsKey(uuid)) {
            Map<Class<? extends ICuriosStorage>, Tuple<float[], UUID[]>> map = STORAGES.get(uuid);
            map.remove(storage.getClass());
            if (map.isEmpty()) STORAGES.remove(uuid);
        }
    }

    public static float[] getCountStorages(Entity player, Class<? extends ICuriosStorage> storage) {
        UUID id = player.getUUID();
        if (STORAGES.containsKey(id)) {
            Tuple<float[], UUID[]> tuple = STORAGES.get(player.getUUID()).get(storage);
            if (tuple != null) return tuple.getA();
        }
        return null;
    }

    public static void removePlayerStorage(Player player) {
        STORAGES.remove(player.getUUID());
    }

    public static void addCurioStorage(Player player, ICuriosStorage storage, Tuple<float[], UUID[]> pair) {
        STORAGES.computeIfAbsent(player.getUUID(), k -> new Object2ObjectOpenHashMap<>(8))
            .put(storage.getClass(), pair);
    }

    public static void removeCurioStorage(Player player, ICuriosStorage storage) {
        STORAGES.get(player.getUUID()).remove(storage.getClass());
    }
}
