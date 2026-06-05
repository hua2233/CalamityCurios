package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PlayerAttackListener extends HurtListener {
    public final Boolean isCalamityCriticalHits;
    @EventConstructor
    public PlayerAttackListener(ServerPlayer player, LivingHurtEvent event, Boolean criticalHits) {
        super(player, event);
        isCalamityCriticalHits = criticalHits;
    }
}