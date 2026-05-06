package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

public class ProjectileSpawnListener extends BaseListener<EntityJoinLevelEvent> {
    public final boolean isArrow;
    public final AbstractArrow arrow;
    public final Projectile projectile;
    public final ServerPlayer player;
    public double speedVectorAmplifier = 1;
    public double hurtAmplifier = 1;

    @EventConstructor
    public ProjectileSpawnListener(EntityJoinLevelEvent event, ServerPlayer player, Projectile projectile) {
        super(event);
        this.projectile = projectile;
        this.player = player;
        if (projectile instanceof AbstractArrow) {
            isArrow = true;
            arrow = (AbstractArrow) projectile;
        } else {
            isArrow = false;
            arrow = null;
        }
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
