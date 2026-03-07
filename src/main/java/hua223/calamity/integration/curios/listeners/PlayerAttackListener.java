package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PlayerAttackListener extends HurtListener {
    @EventConstructor
    public PlayerAttackListener(ServerPlayer player, LivingHurtEvent event) {
        super(player, event);
    }
}