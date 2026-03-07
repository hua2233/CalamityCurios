package hua223.calamity.register.Items.edible;

import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CinnamonSolyn extends Item {
    public CinnamonSolyn(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .meat().alwaysEat().saturationMod(20).nutrition(30)
            .effect(() -> new MobEffectInstance(CalamityEffects.STAR_STRIKINGLY_SATIATED.get(),
                1200, 0), 1f).build()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("cinnamon_solyn")
            .setStyle(Style.EMPTY.withColor(0xF38BBC)));
    }
}
