package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public non-sealed class DamageListener extends HDListener<LivingDamageEvent> {
    @EventConstructor
    public DamageListener(LivingDamageEvent event, ServerPlayer player) {
        super(event, player, event.getAmount(), event.getSource());
    }

    protected DamageListener(ServerPlayer player, LivingDamageEvent event) {
        super(player, event, event.getAmount(), event.getSource());
    }
}
