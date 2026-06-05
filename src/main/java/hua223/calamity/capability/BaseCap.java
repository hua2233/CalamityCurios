package hua223.calamity.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface BaseCap {
    void save(CompoundTag tag);

    void load(CompoundTag tag);

    void onClone(Player old, boolean isDeath);

    void syncData();
}
