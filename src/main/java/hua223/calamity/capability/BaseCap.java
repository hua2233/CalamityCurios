package hua223.calamity.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface BaseCap<T> {
    void save(CompoundTag tag);

    void load(CompoundTag tag);

    void deathActivation(T old, ServerPlayer _new);

    void syncData(ServerPlayer player);
}
