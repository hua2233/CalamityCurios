package hua223.calamity.register.effects;

import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MagicBurn extends CountFactorEffects {
    protected MagicBurn(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public BiConsumer<MobEffectInstance, CountFactor> createFactorUpdater() {
        return null;
    }

    @Override
    public Function<MobEffectInstance, float[]> initFactorData() {
        return instance -> new float[]{instance.getDuration()};
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide) {
            MobEffectInstance instance = entity.getEffect(this);
            float intensity = instance.calamity$GetUniversalFactor(this).getRatio(instance.getDuration(), 0);
            float heal = entity.getMaxHealth() * 0.1f;
            entity.hurt(DamageSource.MAGIC.bypassArmor(), (float) (Math.sqrt(intensity) * Math.pow(heal, 1 + intensity)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("mana_burn").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.RED));
    }
}
