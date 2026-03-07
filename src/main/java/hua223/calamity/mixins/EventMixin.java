package hua223.calamity.mixins;

import hua223.calamity.util.CalamityDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public class EventMixin {
    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sourceCheck(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Float> cir) {
        if (src instanceof CalamityDamageSource source && source.isNotTriggerEvent()) cir.setReturnValue(amount);
    }

    @Inject(method = "onLivingDamage", at = @At("RETURN"), cancellable = true, remap = false)
    private static void noDecay(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Float> cir) {
        if (src instanceof CalamityDamageSource source && source.isNoDecay()
            && source.getRealAmount() > cir.getReturnValue())
            cir.setReturnValue(source.getRealAmount());
    }
}
