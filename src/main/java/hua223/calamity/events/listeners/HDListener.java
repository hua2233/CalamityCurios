package hua223.calamity.events.listeners;

import hua223.calamity.register.damage.DamageSupplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingEvent;

//TODO rewrite the entire damage frame listener system, and also solve the legacy issues of other single events
public abstract sealed class HDListener <T extends LivingEvent> extends BaseListener<T> permits DamageListener, HurtListener {
    public final float baseAmount;
    public final DamageSource source;
    public final LivingEntity entity;
    public final ServerPlayer player;
    public final boolean isTriggerByLiving;

    public float floating;
    public float amplifier = 1f;
    private float finalAmount = -1;

    protected HDListener(T event, ServerPlayer player, float baseAmount, DamageSource source) {
        super(event);
        this.player = player;
        this.baseAmount = baseAmount;
        this.source = source;

        if (source.getEntity() instanceof LivingEntity living) {
            entity = living;
            isTriggerByLiving = true;
            //禁用无敌帧增长，防止事件回调时再次触发伤害设置无敌帧导致的骗伤
            entity.calamity$DisableInvulnerable = true;
        } else {
            entity = null;
            isTriggerByLiving = false;
        }
    }

    protected HDListener(ServerPlayer player, T event, float baseAmount, DamageSource source) {
        super(event);
        this.source = source;
        this.player = player;
        isTriggerByLiving = true;
        this.baseAmount = baseAmount;
        this.entity = event.getEntity();
        entity.calamity$DisableInvulnerable = true;
    }

    public boolean isFarAttack() {
        return source.is(DamageTypeTags.IS_PROJECTILE) &&
            source.getDirectEntity() instanceof Projectile;
    }

    public boolean isSpell() {
        return source.is(DamageSupplier.CALAMITY_MAGIC);
    }

    public Projectile getProjectile() {
        return (Projectile) source.getDirectEntity();
    }

    public void setFinalAmount(float amount) {
        if (finalAmount < 0 || amount < finalAmount) finalAmount = amount;
    }

    public float getCorrectionValue() {
        if (finalAmount > 0f) return finalAmount;
        else return baseAmount * amplifier + floating;
    }

    public float settlement() {
        if (isTriggerByLiving) entity.calamity$DisableInvulnerable = false;
        return getCorrectionValue();
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
        if (isTriggerByLiving) entity.calamity$DisableInvulnerable = false;
    }
}
