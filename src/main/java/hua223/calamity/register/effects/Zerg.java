package hua223.calamity.register.effects;

import hua223.calamity.register.config.CalamityConfigHelper;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Zerg extends CalamityEffect implements IEffectsCallBack {
    public Zerg(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) CalamityConfigHelper.zergState(true);
    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        if (entity.calamity$IsPlayer) CalamityConfigHelper.zergState(false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("zerg").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
