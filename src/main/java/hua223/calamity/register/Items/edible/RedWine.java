package hua223.calamity.register.Items.edible;

import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedWine extends Beer {
    public RedWine(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 300), 1f)
            .build()));
    }

    @Override
    protected void endOfUse(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            if (entity.hasEffect(CalamityEffects.BAGUETTE.get())) {
                entity.heal(18f);
                entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 1));
            } else entity.heal(12f);
        }
    }

    @Override
    protected int getCooldown() {
        return 700;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag isAdvanced) {
        tooltips.add(CMLangUtil.getTranslatable("red_wine").withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, level, tooltips, isAdvanced);
    }
}
