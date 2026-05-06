package hua223.calamity.register.Items.edible;

import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlasphemousDonut extends Item {
    public BlasphemousDonut(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(CalamityEffects.PLENTY_SATISFIED.get(), 12000), 1f)
            .nutrition(18).saturationMod(12).build()));
        GlobalLoot.mountTo(this);
    }

    @ApplyGlobalLoot
    public final void onGetChestLoot(ChestLootContext context) {
        if (context.fromSpecificName("catacombs/hidden_trough_treasure") && context.chance(0.1f))
            context.addLoot(this, context.getRandomCount(1, 3));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("blasphemous_donut").withStyle(ChatFormatting.GOLD));
    }
}
