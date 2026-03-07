package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DragonBurn extends CalamityEffect implements IEffectsCallBack {
    protected DragonBurn(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 40 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level.isClientSide) {
            for (int i = 0; i < 5; i++) {
                pLivingEntity.level.addParticle(ParticleTypes.SOUL, pLivingEntity.getRandomX(0.5),
                    pLivingEntity.getRandomY(), pLivingEntity.getRandomZ(0.5), 0, 0.1, 0);
            }
        } else pLivingEntity.hurt(DamageSource.LAVA, 6f + pAmplifier);
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
        tooltips.add(CMLangUtil.getEffectTranslatable("dragon_burn").withStyle(ChatFormatting.GOLD));
    }
}
