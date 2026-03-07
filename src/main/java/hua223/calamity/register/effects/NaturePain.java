package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class NaturePain extends CalamityEffect {
    protected NaturePain(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "123e4567-e89b-12d3-a456-426614174023",
            -0.2, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level.isClientSide)
            pLivingEntity.hurt(DamageSource.MAGIC, 3f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("nature_pain").withStyle(ChatFormatting.DARK_GREEN));
    }
}
