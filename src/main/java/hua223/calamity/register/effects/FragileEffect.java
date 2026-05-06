package hua223.calamity.register.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FragileEffect extends CalamityEffect implements IEffectsCallBack {
    public FragileEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    protected abstract float getFragileAmplifier(@NotNull MobEffectInstance effect, @NotNull LivingEntity entity, @Nullable Entity source);

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        entity.calamity$EffectFragile += getFragileAmplifier(effect, entity, source);
    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        entity.calamity$EffectFragile -= getFragileAmplifier(effect, entity, null);
    }
}
