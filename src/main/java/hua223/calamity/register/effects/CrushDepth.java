package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class CrushDepth extends CalamityEffect implements IEffectsCallBack {
    protected CrushDepth(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level.isClientSide) {
            if (pLivingEntity.isInWater()) {
                pLivingEntity.setAirSupply(pLivingEntity.getAirSupply() - 10 * pAmplifier);
                if (pLivingEntity.getAirSupply() <= 0) {
                    pLivingEntity.hurt(DamageSource.DROWN, 8f);
                    return;
                }
            }

            pLivingEntity.hurt(DamageSource.DROWN, 4f);
        }
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        inactivationEffect(entity, true);
    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        inactivationEffect(entity, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("crush_depth").withStyle(ChatFormatting.DARK_BLUE));
    }
}
