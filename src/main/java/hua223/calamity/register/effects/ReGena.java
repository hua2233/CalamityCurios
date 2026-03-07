package hua223.calamity.register.effects;

import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ReGena extends CalamityEffect {
    public ReGena(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        addAttributeModifier(CalamityAttributes.DAMAGE_UP.get(),
            "123e4567-e89b-12d3-a456-426614174023", 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(CalamityAttributes.INJURY_OFFSET.get(),
            "123e4567-e89b-12d3-a456-426614174023", 0.07, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level.isClientSide) return;

        if (pLivingEntity instanceof Enemy) {
            pLivingEntity.hurt(new DamageSource("regena"), pAmplifier);
        } else pLivingEntity.heal(pAmplifier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("re_gena").withStyle(ChatFormatting.GREEN));
    }
}
