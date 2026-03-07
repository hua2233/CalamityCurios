package hua223.calamity.register.effects;

import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public class AstralInfection extends CalamityEffect implements IEffectsCallBack {
    protected AstralInfection(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        addAttributeModifier(Attributes.ATTACK_DAMAGE,
            "123e4567-e89b-12d3-a456-426614174026", -0.1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 40 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level.isClientSide || CalamityItems.INFECTED_JEWEL.isEquip(pLivingEntity)) return;
        pLivingEntity.hurt(DamageSource.MAGIC, 3f);
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
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("astral_infection"));
    }
}
