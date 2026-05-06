package hua223.calamity.register.Items.edible;

import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Baguette extends Item {
    public Baguette(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(3)
            .effect(() -> new MobEffectInstance(CalamityEffects.BAGUETTE.get(), 1200), 1f)
            .build()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level pLevel, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("baguette", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("baguette", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("baguette", 3).withStyle(ChatFormatting.YELLOW));
    }
}
