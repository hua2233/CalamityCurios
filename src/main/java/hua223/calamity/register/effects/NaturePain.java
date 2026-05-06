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

public class NaturePain extends FragileEffect {
    protected NaturePain(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "123e4567-e89b-12d3-a456-426614174023",
            -0.2, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    protected float getFragileAmplifier(@NotNull MobEffectInstance effect, @NotNull LivingEntity entity, @Nullable Entity source) {
        return (effect.getAmplifier() + 1) * 0.1f;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide)
            pLivingEntity.hurt(pLivingEntity.damageSources().magic(), 3f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("nature_pain").withStyle(ChatFormatting.DARK_GREEN));
    }
}
