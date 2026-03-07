package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Photosynthesis extends CalamityEffect {
    public Photosynthesis(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        Level level = target.level;
        if (!level.isClientSide && target.calamity$IsPlayer && level.isDay() && !level.isThundering()) {
            BlockPos pos = new BlockPos(target.getEyePosition());
            if (!level.isRainingAt(pos) && level.getBrightness(LightLayer.SKY, pos) > 10) {
                FoodData data = target.calamity$Player.getFoodData();
                data.setFoodLevel(data.getFoodLevel() + 2);
                data.setSaturation(data.getSaturationLevel() + 1);

                target.calamity$Player.heal(2);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("photosynthesis").withStyle(ChatFormatting.YELLOW));
    }
}
