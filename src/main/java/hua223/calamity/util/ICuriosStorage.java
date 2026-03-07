package hua223.calamity.util;

import net.minecraft.util.Tuple;
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

    default Tuple<float[], UUID[]> createStorage() {
        int size = getCountSize();
        Tuple<float[], UUID[]> pair = new Tuple<>(null, null);

        if (storageCount()) pair.setA(new float[size]);
        if (storageID()) pair.setB(new UUID[size]);
        return pair;
    }

    default float addCount(Player player, int index) {
        return ++getCount(player)[index];
    }

    default boolean storageID() {
        return false;
    }

    default boolean storageCount() {
        return true;
    }

    default boolean reduceCount(Player player, int index) {
        return !(getCount(player)[index]-- > 0);
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
        return GlobalCuriosStorage.getStorage(this, player).getA();
    }

    default UUID[] getUUID(LivingEntity player) {
        return GlobalCuriosStorage.getStorage(this, player).getB();
    }

    default UUID getFirstUUID(LivingEntity player) {
        return GlobalCuriosStorage.getStorage(this, player).getB()[0];
    }

    default Tuple<float[], UUID[]> getPair(Player player) {
        return GlobalCuriosStorage.getStorage(this, player);
    }

    default void removeStorage(Player player) {
        GlobalCuriosStorage.unEquipSafeRemove(player, this);
    }

    default void addToStorage(Player player) {
        GlobalCuriosStorage.addCurioStorage(player, this, createStorage());
    }
}
