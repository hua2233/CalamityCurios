package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHealEvent;

public class PlayerHealListener extends BaseListener<LivingHealEvent> {
    public final ServerPlayer player;
    public float amplification = 1f;
    public float bonus;
    public final float healAmount;

    @EventConstructor
    public PlayerHealListener(LivingHealEvent event, ServerPlayer player) {
        super(event);
        this.player = player;
        healAmount = event.getAmount();
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
