package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.ProjectileImpactEvent;

public class ProjectileHitListener extends BaseListener<ProjectileImpactEvent> {
    public final ServerPlayer player;
    public final Projectile projectile;
    public final LivingEntity target;
    private static boolean daawnlight;

    @EventConstructor
    public ProjectileHitListener(ProjectileImpactEvent event, ServerPlayer player, LivingEntity target) {
        super(event);
        this.player = player;
        projectile = event.getProjectile();
        this.target = target;
    }

    public static boolean isSpiritOriginCritical() {
        if (daawnlight) {
            daawnlight = false;
            return true;
        }

        return false;
    }

    public void setDaawnlight() {
        daawnlight = true;
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
        event.getProjectile().discard();
    }
}
