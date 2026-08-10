package hua223.calamity.register.items.edible;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class EnchantedStarfish extends Item {
    public EnchantedStarfish(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(6f)
            .meat()
            .alwaysEat()
            .build()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, LivingEntity entity) {
        if (entity.calamity$IsPlayer && !level.isClientSide) {
            entity.calamity$Player.Calamity$Player.tryUseEnchantedStarfish();
            stack.shrink(1);
        }
        return stack;
    }
}
