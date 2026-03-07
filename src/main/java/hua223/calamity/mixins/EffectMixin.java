package hua223.calamity.mixins;

import hua223.calamity.integration.curios.item.BloodGodChalice;
import hua223.calamity.mixed.ISelfCast;
import hua223.calamity.register.effects.factor.UniversalFactorEffect;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.GlobalCuriosStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MobEffect.class)
public abstract class EffectMixin {
    @Shadow
    @Final
    private MobEffectCategory category;

    @Redirect(method = "applyInstantenousEffect", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    private void instantenous(LivingEntity instance, float healAmount) {
        float[] count = GlobalCuriosStorage.getCountStorages(instance, BloodGodChalice.class);
        if (count != null) {
            healAmount *= 1.25f;
            count[0] -= healAmount / 2;
            if (count[0] < 0) count[0] = 0;
        } else instance.heal(healAmount);
    }

    @Inject(at = @At("HEAD"), method = "applyEffectTick", cancellable = true)
    private void injectTick(LivingEntity entity, int amplifier, CallbackInfo info) {
        if (category != MobEffectCategory.BENEFICIAL && entity instanceof Player player) {
            if (CalamityHelp.getCalamityFlag(player, 0)) {
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addAttributeModifiers", cancellable = true)
    private void injectAddModifier(LivingEntity entity, AttributeMap pAttributeMap, int pAmplifier, CallbackInfo info) {
        if (category != MobEffectCategory.BENEFICIAL && entity instanceof Player player) {
            if (CalamityHelp.getCalamityFlag(player, 0)) {
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "applyInstantenousEffect", cancellable = true)
    private void injectInstantenous(Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, CallbackInfo info) {
        if (category != MobEffectCategory.BENEFICIAL && livingEntity instanceof Player player) {
            if (CalamityHelp.getCalamityFlag(player, 0)) {
                info.cancel();
            }
        }
    }

    @Mixin({MobEffectInstance.class})//@Implements(@Interface(iface = IEffect.class, prefix = "calamity$"))
    public abstract static class EffectInstance implements ISelfCast<MobEffectInstance> {
        @Shadow
        int duration;
        @Shadow
        private Optional<MobEffectInstance.FactorData> factorData;
        @Shadow
        @Final
        private MobEffect effect;

        @Unique
        private boolean calamity$Factor;
        @Unique
        public boolean calamity$NoFlicker;

        @Redirect(method = "loadSpecifiedEffect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z", ordinal = 3))
        private static boolean loadUniversalFactor(CompoundTag instance, String key, int tagType) {
            return !instance.contains("UniversalFactor", tagType) && instance.contains(key, tagType);
        }

        @Inject(method = "readCurativeItems", at = @At("HEAD"), remap = false)
        private static void readUniversalFactor(MobEffectInstance effect, CompoundTag nbt, CallbackInfoReturnable<MobEffectInstance> cir) {
            if (nbt.contains("UniversalFactor", 10)) {
                EffectInstance mixin = ((EffectInstance) (Object) effect);
                mixin.factorData = (Optional<MobEffectInstance.FactorData>) UniversalFactorEffect.fromNbt(nbt.getCompound("UniversalFactor"));
                //make sure it can be reloaded
                mixin.calamity$Factor = true;
            }
        }

        @Inject(method = "<init>(Lnet/minecraft/world/effect/MobEffect;IIZZZLnet/minecraft/world/effect/MobEffectInstance;Ljava/util/Optional;)V",
            at = @At(value = "RETURN"))
        private void setData(MobEffect effect, int duration, int amplifier, boolean ambient, boolean visible,
                             boolean showIcon, MobEffectInstance hiddenEffect, Optional factorData, CallbackInfo ci) {
            if (factorData.isPresent() && factorData.get() instanceof UniversalFactorEffect.UniversalFactor<?> factor) {
                calamity$Factor = true;
                factor.initFactorData(cast());
            }
        }

        @Inject(method = "<init>(Lnet/minecraft/world/effect/MobEffectInstance;)V",
            at = @At(value = "RETURN"))
        private void setEffect(MobEffectInstance other, CallbackInfo ci) {
            if (factorData.isPresent() && factorData.get() instanceof UniversalFactorEffect.UniversalFactor<?> factor) {
                calamity$Factor = true;
                factor.initFactorData(cast());
            }
        }

        @Inject(method = "writeDetailsTo", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/effect/MobEffectInstance;writeCurativeItems(Lnet/minecraft/nbt/CompoundTag;)V",
            shift = At.Shift.AFTER), cancellable = true)//, remap = false
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

        public void calamity$SetDuration(int duration) {
            this.duration = duration;
        }

        public <T extends UniversalFactorEffect.UniversalFactor<?>> T calamity$GetUniversalFactor(UniversalFactorEffect<?, T> effect) {
            return (T) factorData.get();
        }
    }
}
