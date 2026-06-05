package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import hua223.calamity.util.damage.DamageTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class HurtListener extends BaseListener<LivingHurtEvent> {
    private static DamageSource replace;
    public final boolean isTriggerByLiving;
    public final DamageSource source;
    public final LivingEntity entity;
    public final ServerPlayer player;
    public final float baseAmount;
    public float amplifier = 1f;
    private float finalAmount;
    public float floating;

    @EventConstructor
    public HurtListener(LivingHurtEvent event, ServerPlayer player) {
        super(event);
        this.player = player;
        baseAmount = event.getAmount();
        source = event.getSource();

        if (source.getEntity() instanceof LivingEntity living) {
            isTriggerByLiving = true;
            this.entity = living;
        } else {
            entity = null;
            isTriggerByLiving = false;
        }
    }

    protected HurtListener(ServerPlayer player, LivingHurtEvent event) {
        super(event);

        this.player = player;
        baseAmount = event.getAmount();
        source = event.getSource();
        isTriggerByLiving = true;
        this.entity = event.getEntity();
    }

    public boolean isFarAttack() {
        return source.is(DamageTypeTags.IS_PROJECTILE) &&
            source.getDirectEntity() instanceof Projectile;
    }


    public boolean isSpell() {
        return source.is(DamageTags.CALAMITY_MAGIC.tag);
    }

    public Projectile getProjectile() {
        return (Projectile) source.getDirectEntity();
    }

    public void setFinalAmount(float amount) {
        finalAmount = amount;
    }

    public static DamageSource trySetSource(DamageSource source) {
        if (replace != null) {
            source = replace;
            replace = null;
        }

        return source;
    }

    public void setSource(DamageSource source) {
        replace = source;
    }

    public float getCorrectionValue() {
        if (finalAmount > 0f) return finalAmount;
        else return baseAmount * amplifier + floating;
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
