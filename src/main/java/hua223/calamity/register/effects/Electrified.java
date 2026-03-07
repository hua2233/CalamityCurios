package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Electrified extends CalamityEffect {
    protected Electrified(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int pAmplifier) {
        if (!entity.level.isClientSide) {
            float amount = 1f;
            if (entity.isInWaterOrRain()) amount += 3f;
            //it hurts when you move.
            if (entity.walkDist != entity.walkDistO) amount += 3f;
            entity.hurt(DamageSource.LIGHTNING_BOLT, amount);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("electrified").withStyle(ChatFormatting.YELLOW));
    }
}
