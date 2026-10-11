package hua223.calamity.register.effects;

import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Bane extends CountFactorEffects {
    protected Bane() {
        super(MobEffectCategory.HARMFUL, 16777045);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void applyServerTick(@NotNull LivingEntity target, int a) {
        CountFactor factor = target.getEffect(this).calamity$GetUniversalFactor(this);
        LivingEntity applier = factor.getOwner();
        float hurt = 3f;
        if (applier != null) {
            double amplifier = applier.getAttributeValue(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get()) - 1;
            if (amplifier > 0) hurt += hurt * (float) (amplifier * factor.getFactor()[0]);
        }

        target.hurt(target.level().damageSources().magic(), hurt);
    }

    @Override
    protected CountFactor factory() {
        return new CountFactor(1);
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("bane").withStyle(ChatFormatting.YELLOW));
    }
}
