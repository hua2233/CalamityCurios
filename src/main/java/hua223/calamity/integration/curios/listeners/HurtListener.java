package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import io.redspace.ironsspellbooks.damage.ISpellDamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;


public class HurtListener extends BaseListener<LivingHurtEvent> {
    public final boolean isTriggerByLiving;
    public final DamageSource source;
    public final LivingEntity entity;
    public final ServerPlayer player;
    public final float baseAmount;
    public float amplifier = 1f;
    private float finalAmount;
    public final Projectile projectile;
    public float floating;
    public final boolean isMagic;

    @EventConstructor
    public HurtListener(LivingHurtEvent event, ServerPlayer player) {
        super(event);
        this.player = player;
        baseAmount = event.getAmount();
        source = event.getSource();
        isMagic = source instanceof ISpellDamageSource;

        if (source.getEntity() instanceof LivingEntity living) {
            isTriggerByLiving = true;
            this.entity = living;
        } else {
            entity = null;
            isTriggerByLiving = false;
        }

        if (source.isProjectile()) projectile = (Projectile) source.getDirectEntity();
        else projectile = null;
    }

    protected HurtListener(ServerPlayer player, LivingHurtEvent event) {
        super(event);

        this.player = player;
        baseAmount = event.getAmount();
        source = event.getSource();
        isTriggerByLiving = true;
        this.entity = event.getEntity();
        isMagic = source instanceof ISpellDamageSource;

        if (source.isProjectile()) projectile = (Projectile) source.getDirectEntity();
        else projectile = null;
    }

    public boolean isFarAttack() {
        return projectile != null;
    }

    public void setFinalAmount(float amount) {
        finalAmount = amount;
    }

    public float getCorrectionValue() {
        if (finalAmount > 0f) return finalAmount;
        else return event.getAmount() * amplifier + floating;
    }

    @Override
    public void canceledEvent() {
        event.setCanceled(true);
    }
}
