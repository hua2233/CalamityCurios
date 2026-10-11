package hua223.calamity.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public interface IServantsReinforcement {
    void modifyServant(boolean apply, ServerPlayer player, LivingEntity servant);

    default boolean onlyTransientModifier() {
        return true;
    }
}
