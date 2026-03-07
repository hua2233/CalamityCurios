package hua223.calamity.register.effects;

import hua223.calamity.register.entity.TeslaAura;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Tesla extends CalamityEffect implements IEffectsCallBack {
    public Tesla(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer && !entity.hasEffect(this))
            TeslaAura.create(entity, false);
    }

    @Override
    public void onLoad(MobEffectInstance instance, LivingEntity entity) {
        //When re entering the world, callbacks occur after the MobEffectInstance is loaded
        if (entity.calamity$IsPlayer) TeslaAura.create(entity, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("tesla").withStyle(ChatFormatting.AQUA));
    }
}
