package hua223.calamity.mixins;

import hua223.calamity.register.damage.DamageSupplier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public class EventMixin {
    @Inject(method = "onLivingAttack", at = @At("HEAD"), cancellable = true)
    private static void setRealAmount(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (src.is(DamageSupplier.NOT_TRIGGER_EVENT)) cir.setReturnValue(false);
    }

    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true)
    private static void not(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Float> cir) {
        if (src.is(DamageSupplier.NOT_TRIGGER_EVENT)) cir.setReturnValue(amount);
    }
}
