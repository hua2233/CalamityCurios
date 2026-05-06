package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LifeOppress extends FragileEffect {
    public LifeOppress(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MAX_HEALTH, "123e4567-e89b-12d3-a456-426614174030",
            -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity target, int amplifier) {
        if (!target.level().isClientSide)
            target.hurt(target.damageSources().magic(), target.getPersistentData().getFloat("LifeOppress") );
    }

    @Override
    public double getAttributeModifierValue(int amplifier, @NotNull AttributeModifier modifier) {
        return modifier.getAmount() * Math.min(3, amplifier + 1);
    }

    @Override
    protected float getFragileAmplifier(@NotNull MobEffectInstance effect, @NotNull LivingEntity entity, @Nullable Entity source) {
        return effect.getAmplifier() * 0.2f;
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        super.onAdd(effect, entity, source);
        entity.getPersistentData().putFloat("LifeOppress",
            entity.getMaxHealth() * ((effect.getAmplifier() + 1) * 0.05f));
        inactivationEffect(entity, true);
    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        super.onRemove(effect, entity);
        entity.getPersistentData().remove("LifeOppress");
        inactivationEffect(entity, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("life_oppress").withStyle(ChatFormatting.DARK_RED));
    }
}
