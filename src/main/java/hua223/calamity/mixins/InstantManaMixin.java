package hua223.calamity.mixins;

import hua223.calamity.register.effects.CalamityEffects;
import io.redspace.ironsspellbooks.effect.InstantManaEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InstantManaEffect.class, remap = false)
public class InstantManaMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "applyInstantenousEffect", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lio/redspace/ironsspellbooks/setup/PacketDistributor;sendToPlayer(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Object;)V"))
    private void manaSickness(Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, CallbackInfo ci) {
        if (amplifier != 7) {
            MobEffect effect = CalamityEffects.MANA_SICKNESS.get();
            if (livingEntity.hasEffect(effect)) {
                MobEffectInstance instance = livingEntity.getEffect(effect);
                instance.calamity$SetProperties(Math.min(instance.getDuration() + 200, 1200), instance.getAmplifier(), livingEntity);
            } else livingEntity.addEffect(new MobEffectInstance(effect, 200));
        }
    }
}
