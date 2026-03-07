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

public class Baguette extends CalamityEffect implements IEffectsCallBack {
    public Baguette(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (!entity.hasEffect(CalamityEffects.WELL_FED.get()))
            entity.addEffect(new MobEffectInstance(CalamityEffects.WELL_FED.get(), effect.getDuration()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("baguette").withStyle(ChatFormatting.YELLOW));
    }
}
