package hua223.calamity.register.effects;

import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

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
    public void applyEffectTick(@NotNull LivingEntity entity, int pAmplifier) {
        if (entity.level().isClientSide || CalamityItems.INFECTED_JEWEL.isEquip(entity)) return;
        entity.hurt(entity.damageSources().magic(), 3f);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        inactivationEffect(entity, true);
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        inactivationEffect(entity, false);
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("astral_infection"));
    }
}
