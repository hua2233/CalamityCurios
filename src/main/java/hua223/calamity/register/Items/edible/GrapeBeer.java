package hua223.calamity.register.Items.edible;

import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GrapeBeer extends Beer {
    public GrapeBeer(Properties properties) {
        super(properties.stacksTo(16).food(new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(CalamityEffects.GRAPE_BEER.get(), 300), 1f)
            .build()));
    }

    @Override
    protected void endOfUse(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.heal(7);
            CalamityCapProvider.safetyRunCalamityMagic(entity,
                expand -> expand.calamity$ChangeMana(100, true));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag isAdvanced) {
        tooltips.add(CMLangUtil.getTranslatable("grape_beer", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("grape_beer", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, level, tooltips, isAdvanced);
    }
}
