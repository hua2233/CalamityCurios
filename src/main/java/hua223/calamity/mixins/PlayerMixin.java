package hua223.calamity.mixins;

import com.mojang.authlib.GameProfile;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CalamityPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract boolean isSpectator();

    @Shadow @Final private Abilities abilities;
    @Unique
    public CalamityPlayer Calamity$Player;

    @Unique
    private int calamity$AtaraxiaHit;

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level level, BlockPos pos, float yRot, GameProfile gameProfile, CallbackInfo ci) {
        Calamity$Player = new CalamityPlayer((Player) (Object) this);
    }

    @Unique
    public boolean calamity$TargetAtaraxiaHit() {
        //You wouldn't want your eyes to be blinded by the glare...
        if (tickCount > calamity$AtaraxiaHit) {
            calamity$AtaraxiaHit += (tickCount + 12);
            return true;
        }

        return false;
    }

    @Override
    public boolean onClimbable() {
        return (horizontalCollision && Calamity$Player.canClimbable) || super.onClimbable();
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/entity/player/Player;isSprinting()Z", ordinal = 1))
    private boolean calamity$SprintHit(Player instance) {
        return !isSprinting() || Calamity$Player.canSprintingHit;
    }

    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void getScale(float adjustTicks, CallbackInfoReturnable<Float> cir) {
        if (adjustTicks == 0.5f && Calamity$Player.noAttackCooling)
            cir.setReturnValue(1f);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean setSource(Entity instance, DamageSource source, float amount) {
        if (Calamity$Player.noAttackCooling) {
            amount *= (float) (Mth.clamp(calamity$Player.getAttributeValue(Attributes.ATTACK_SPEED), 1, 100));
            instance.invulnerableTime = 0;
        }
        return instance.hurt(source, amount);
    }

    @SuppressWarnings("ALL")
    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
        shift = At.Shift.AFTER), argsOnly = true, remap = false)//
    private DamageSource setCalamity$Source(DamageSource value) {
        return HurtListener.trySetSource(value);
    }

    @Override
    protected float getJumpPower() {
        return (super.getJumpPower()) * Calamity$Player.jumpPower;
    }

    @Override
    public void moveRelative(float amount, @NotNull Vec3 relative) {
        Vec3 initialVelocity = getInputVector(relative, amount, getYRot());
        if (initialVelocity != Vec3.ZERO) {
            if (abilities.flying && Calamity$Player.flySpeedAmplifier > 0)
                initialVelocity = new Vec3(initialVelocity.x * Calamity$Player.flySpeedAmplifier,
                    initialVelocity.y, initialVelocity.z * Calamity$Player.flySpeedAmplifier);
            setDeltaMovement(getDeltaMovement().add(initialVelocity));
        }
    }

    @Override
    public boolean canStandOnFluid(@NotNull FluidState state) {
        return Calamity$Player.fluidStand && !state.isEmpty() && !calamity$Player.isCrouching()
            && calamity$Player.getEyeInFluidType() == ForgeMod.EMPTY_TYPE.get();
    }

    @Override
    public void setPos(double x, double y, double z) {
        if (firstTick || !Calamity$Player.freeze) super.setPos(x, y, z);
    }

    @Override
    public boolean fireImmune() {
        return Calamity$Player.fireImmune;
    }

    @Override
    public boolean dampensVibrations() {
        return CalamityHelp.silent(calamity$Player);
    }
}