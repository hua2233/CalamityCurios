package hua223.calamity.mixins;

import com.mojang.authlib.GameProfile;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.effects.Bounding;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract boolean isSpectator();

    @Unique
    public boolean calamity$IsFreeze;

    @Unique
    //1 verticalSpeed, 2, extraFlyTime 3, flyTimeAmplifier
    public float[] calamity$WingsExpand;

    @Unique
    private int calamity$AtaraxiaHit;

    @Unique
    public float calamity$Invisible;

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(DDD)V"))
    private void verticalSpeed(Player instance, double x, double y, double z) {
        if (y > 0) y *= calamity$WingsExpand[0];
        setDeltaMovement(x, y, z);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initFiled(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile, CallbackInfo ci) {
        calamity$WingsExpand = new float[] {1, 0, 1};
    }

    //No need to apply Mixin annotation, just let Mixin forcefully insert it
    //Even in the development environment, the source code of the pile method will be replaced, while in the formal environment,
    //it will be directly injected, No need for complex Gradle tasks to dynamically handle Overwrite and Unique
    public boolean calamity$TargetAtaraxiaHit() {
        //You wouldn't want your eyes to be blinded by the glare...
        if (tickCount > calamity$AtaraxiaHit) {
            calamity$AtaraxiaHit += (tickCount + 12);
            return true;
        }

        return false;
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/entity/player/Player;isSprinting()Z", ordinal = 1))
    private boolean calamity$SprintHit(Player instance) {
        return !instance.isSprinting() || CalamityHelp.getCalamityFlag(instance, 9);
    }

    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void getScale(float adjustTicks, CallbackInfoReturnable<Float> cir) {
        if (adjustTicks == 0.5f && CalamityHelp.getCalamityFlag(calamity$Player, 6))
            cir.setReturnValue(1f);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean setSource(Entity instance, DamageSource source, float amount) {
        if (CalamityHelp.getCalamityFlag(calamity$Player, 6)) {
            amount *= (float) (Mth.clamp(calamity$Player.getAttributeValue(Attributes.ATTACK_SPEED), 1, 100));
            instance.invulnerableTime = 0;
        }
        return instance.hurt(source, amount);
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
        shift = At.Shift.AFTER), argsOnly = true, remap = false)
    private DamageSource setCalamity$Source(DamageSource value) {
        return HurtListener.trySetSource(value);
    }

    @Override
    protected float getJumpPower() {
        return (super.getJumpPower()) * Bounding.jumpPower;
    }

    @Override
    public boolean canStandOnFluid(@NotNull FluidState state) {
        return calamity$Player.isLocalPlayer() && !state.isEmpty() &&
            !calamity$Player.isCrouching() &&
            CalamityHelp.getCalamityFlag(calamity$Player, 8) &&
            calamity$Player.getEyeInFluidType() == ForgeMod.EMPTY_TYPE.get();
    }

    @Override
    public void setPos(double x, double y, double z) {
        if (!calamity$IsFreeze) {
            setPosRaw(x, y, z);
            setBoundingBox(makeBoundingBox());
        }
    }

    @Override
    public boolean fireImmune() {
        return CalamityHelp.getCalamityFlag(calamity$Player, 7);
    }

    @Override
    public boolean dampensVibrations() {
        return CalamityHelp.silent(calamity$Player);
    }
}