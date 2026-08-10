package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Enrage extends CalamityEffect implements IEffectsCallBack {
    public Enrage(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        entity.calamity$EffectFragile += ((effect.getAmplifier() + 1) * 0.25f);
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        entity.calamity$EffectFragile -= ((amplifier + 1) * 0.25f);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("enrage").withStyle(ChatFormatting.DARK_RED));
    }
}
