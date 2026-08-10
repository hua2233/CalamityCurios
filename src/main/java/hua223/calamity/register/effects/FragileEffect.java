package hua223.calamity.register.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public abstract class FragileEffect extends CalamityEffect implements IEffectsCallBack {
    public FragileEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    protected abstract float getFragileAmplifier(int amplifier, @NotNull LivingEntity entity);

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        entity.calamity$EffectFragile += getFragileAmplifier(effect.getAmplifier(), entity);
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        entity.calamity$EffectFragile -= getFragileAmplifier(amplifier, entity);
    }
}
