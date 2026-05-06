package hua223.calamity.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class PotionCloudThrownItem extends ThrownPotion {
    private final int color;
    private final MobEffectInstance[] effects;
    private final LivingEntity entity;
    public PotionCloudThrownItem(LivingEntity shooter, int color, MobEffectInstance... instances) {
        super(shooter.level(), shooter);
        entity = shooter;
        effects = instances;
        this.color = color;
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (!level().isClientSide) {
            new FriendlyEffectCloudBuilder(entity, position(), 200, 3f)
                .setEffects(effects)
                .setCustomColor(color)
                .build();
            discard();
        }
    }
}
