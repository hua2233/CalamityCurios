package hua223.calamity.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
public interface ICuriosStorage {
    static float getReducedValue(float[] count, int index, float reduceNumber) {

        float value = count[index];
        if (value == 0) return 0;

        float actualReduce = Math.min(value, reduceNumber);
        count[index] = value - actualReduce;
        return actualReduce;
    }

    int getCountSize();

    default float addCount(Player player, int index) {
        return ++getCount(player)[index];
    }

    default boolean storageID() {
        return false;
    }

    default boolean storageCount() {
        return true;
    }

    default Class<?>[] defineStorageType() {
        return null;
    }

    default boolean resetOrUpdate(Player player, int index, int max) {
        float[] count = getCount(player);

        if (++count[index] >= max) {
            count[index] = 0;
            return false;
        }

        return true;
    }

    default void zeroCount(Player player, int index) {
        getCount(player)[index] = 0;
    }

    default float[] getCount(Player player) {
        GlobalCuriosStorage.CuriosMemory memory = GlobalCuriosStorage.getStorage(this, player);
        return memory != null ? memory.count : null;
    }

    default UUID[] getUUID(LivingEntity player) {
        GlobalCuriosStorage.CuriosMemory memory = GlobalCuriosStorage.getStorage(this, player);
        return memory != null ? memory.uuids : null;
    }

    @SuppressWarnings("ConstantConditions")
    default UUID getFirstUUID(LivingEntity player) {
        if (storageID()) {
            GlobalCuriosStorage.CuriosMemory memory = GlobalCuriosStorage.getStorage(this, player);
            return memory != null ? memory.uuids[0] : null;
        }

        return null;
    }

    default GlobalCuriosStorage.CuriosMemory getMemory(LivingEntity player) {
        return GlobalCuriosStorage.getStorage(this, player);
    }

    default void removeStorage(Player player) {
        GlobalCuriosStorage.unEquipSafeRemove(player, this);
    }

    default void addToStorage(Player player) {
        GlobalCuriosStorage.addCurioStorage(player, this);
    }
}
