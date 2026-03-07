package hua223.calamity.register.entity.projectiles;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

import static net.minecraftforge.event.ForgeEventFactory.onProjectileImpact;

//This was written when I first started studying, and it was very... Well, I should refactor it
public abstract class BaseProjectile extends Projectile {
    protected int lifeTime = 200;
    protected float damage;

    protected BaseProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    protected void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return super.canHitEntity(pTarget) && pTarget != this.getOwner();
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount++ >= lifeTime) {
            discard();
            return;
        }

        if (level.isClientSide) return;

        logic();

        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);

        if (hitResult.getType() != HitResult.Type.MISS && !onProjectileImpact(this, hitResult)) {
            onHit(hitResult);
        }

        Vec3 vec3 = getDeltaMovement();
        setPos(position().add(vec3));
        setDeltaMovement(vec3.scale(0.96));
        if (!isNoGravity()) {
            Vec3 vector = getDeltaMovement();
            setDeltaMovement(vector.x, vector.y - 0.02, vector.z);
        }
    }

    protected void logic() {
    }

    protected void superTick() {
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (pResult.getEntity() instanceof LivingEntity livingEntity) {
            attack(livingEntity);
            discard();
        }
    }

    protected abstract void attack(LivingEntity target);

    @Override
    public boolean shouldBeSaved() {
        return super.shouldBeSaved() && !Objects.equals(getRemovalReason(), RemovalReason.UNLOADED_TO_CHUNK);
    }
}
