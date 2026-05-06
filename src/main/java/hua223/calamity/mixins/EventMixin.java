package hua223.calamity.mixins;

import hua223.calamity.util.damage.DamageTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public class EventMixin {
    @Inject(method = "onLivingAttack", at = @At("HEAD"))
    private static void setRealAmount(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (src.is(DamageTags.NO_DECAY.tag)) src.calamity$RealAmount = amount;
    }

    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true)
    private static void not(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Float> cir) {
        if (src.is(DamageTags.NOT_TRIGGER_EVENT.tag)) cir.setReturnValue(amount);
    }
    @Inject(method = "onLivingDamage", at = @At("RETURN"), cancellable = true)
    private static void noDecay(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Float> cir) {
        if (src.is(DamageTags.NO_DECAY.tag) && src.calamity$RealAmount > cir.getReturnValue())
            cir.setReturnValue(src.calamity$RealAmount);
    }
}
