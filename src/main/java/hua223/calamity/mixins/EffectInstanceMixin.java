package hua223.calamity.mixins;

import hua223.calamity.mixed.ISelfCast;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.packets.EffectSync;
import hua223.calamity.register.effects.factor.UniversalFactorEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin({MobEffectInstance.class})
public abstract class EffectInstanceMixin implements ISelfCast<MobEffectInstance> {
    @Shadow
    private int duration;

    @Shadow @Mutable
    @Final
    @SuppressWarnings("ALL")
    private Optional<MobEffectInstance.FactorData> factorData;
    @Shadow
    @Final
    private MobEffect effect;

    @Shadow private int amplifier;

    @Shadow public abstract boolean equals(Object pOther);

    @Unique
    private boolean calamity$Factor;

    @Unique
    public boolean calamity$NoFlicker;

    @Redirect(method = "loadSpecifiedEffect", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z", ordinal = 3))
    private static boolean loadUniversalFactor(CompoundTag instance, String key, int tagType) {
        return !instance.contains("UniversalFactor", tagType) && instance.contains(key, tagType);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/effect/MobEffectInstance;applyEffect(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void radianceHook(MobEffectInstance instance, LivingEntity entity) {
        if (!entity.calamity$IsPlayer || effect.getCategory() == MobEffectCategory.BENEFICIAL
            || !entity.calamity$Player.Calamity$Player.hasRadianceEffect) instance.applyEffect(entity);
    }

    @Inject(method = "readCurativeItems", at = @At("HEAD"), remap = false)
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private static void readUniversalFactor(MobEffectInstance effect, CompoundTag nbt, CallbackInfoReturnable<MobEffectInstance> cir) {
        if (nbt.contains("UniversalFactor", 10)) {
            EffectInstanceMixin mixin = ((EffectInstanceMixin) (Object) effect);
            mixin.factorData = (Optional<MobEffectInstance.FactorData>) UniversalFactorEffect.fromNbt(nbt.getCompound("UniversalFactor"));
            //make sure it can be reloaded
            mixin.calamity$Factor = true;
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "<init>(Lnet/minecraft/world/effect/MobEffect;IIZZZLnet/minecraft/world/effect/MobEffectInstance;Ljava/util/Optional;)V", at = @At(value = "RETURN"))
    private void setData(MobEffect effect, int duration, int amplifier, boolean ambient, boolean visible,
                         boolean showIcon, MobEffectInstance hiddenEffect, Optional factorData, CallbackInfo ci) {
        if (factorData.isPresent() && factorData.get() instanceof UniversalFactorEffect.UniversalFactor<?> factor) {
            calamity$Factor = true;
            factor.initFactorData(cast());
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/effect/MobEffectInstance;)V", at = @At(value = "RETURN"))
    private void setEffect(MobEffectInstance other, CallbackInfo ci) {
        if (factorData.isPresent() && factorData.get() instanceof UniversalFactorEffect.UniversalFactor<?> factor) {
            calamity$Factor = true;
            factor.initFactorData(cast());
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "writeDetailsTo", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/effect/MobEffectInstance;writeCurativeItems(Lnet/minecraft/nbt/CompoundTag;)V",
        shift = At.Shift.AFTER), cancellable = true, remap = false)//
    private void saveUniversalFactor(CompoundTag nbt, CallbackInfo ci) {
        if (calamity$Factor) {
            nbt.put("UniversalFactor", ((UniversalFactorEffect<?, ?>) effect).toNbt(factorData.get()));
            ci.cancel();
        }
    }

    /**
     * @author hua223
     * @reason Prevent these factor data from contaminating each other
     */
    @Overwrite
    public Optional<MobEffectInstance.FactorData> getFactorData() {
        return calamity$Factor ? Optional.empty() : factorData;
    }

    @Unique
    public void calamity$SetProperties(int duration, int amplifier, @Nullable LivingEntity owner) {
        if (owner != null) {
            this.duration = duration;
            if (this.amplifier != amplifier) {
                AttributeMap map = owner.getAttributes();
                effect.removeAttributeModifiers(owner, map, amplifier);
                effect.addAttributeModifiers(owner, map, amplifier);
                this.amplifier = amplifier;
            }

            if (owner.calamity$IsPlayer) NetMessages.sendToTrackingAndSelf(owner.calamity$Player.Calamity$Player.getPlayer(), new EffectSync(owner.getId(), cast()));
            else NetMessages.sendToTracking(owner, new EffectSync(owner.getId(), cast()));
        }
    }

    @Unique
    @SuppressWarnings("all")
    public <T extends UniversalFactorEffect.UniversalFactor<?>> T calamity$GetUniversalFactor(UniversalFactorEffect<?, T> effect) {
        return (T) factorData.get();
    }
}
