package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class DeathListener extends BaseListener<LivingDeathEvent> {
    public final DamageSource source;
    public final LivingEntity entity;
    public final Boolean isPlayerDeath;
    public ServerPlayer player;

    @EventConstructor
    public DeathListener(LivingDeathEvent event, ServerPlayer player, Boolean isPlayerDeath) {
        super(event);
        source = event.getSource();
        this.isPlayerDeath = isPlayerDeath;
        if (isPlayerDeath) {
            this.player = player;
            if (event.getSource().getEntity() instanceof LivingEntity entity) {
                this.entity = entity;
            } else this.entity = null;
        } else {
            entity = event.getEntity();
            this.player = player;
        }
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
