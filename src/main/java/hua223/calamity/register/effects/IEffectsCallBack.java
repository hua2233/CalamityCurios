package hua223.calamity.register.effects;

import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface IEffectsCallBack extends IMobEffectEndCallback {
    default void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {}

    @Override
    default void onEffectRemoved(LivingEntity entity, int amplifier) {}

    default void inactivationEffect(LivingEntity entity, boolean isApply) {
        if (isApply) entity.calamity$InactivationCount++;
        else entity.calamity$InactivationCount--;
    }
}
