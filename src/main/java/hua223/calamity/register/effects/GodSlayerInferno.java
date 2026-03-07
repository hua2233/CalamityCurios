package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class GodSlayerInferno extends CalamityEffect implements IEffectsCallBack {
    public GodSlayerInferno(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(DamageSource.MAGIC, 4 * ++amplifier);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        CalamityHelp.setCalamityFlag(entity, 5, true);
    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        //The second one doesn't seem to be used, maybe I can take advantage of it?!
//        entity.setSharedFlag(2, false);
        CalamityHelp.setCalamityFlag(entity, 5, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("god_slayer_inferno").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
