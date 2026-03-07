package hua223.calamity.register.Items.edible;

import hua223.calamity.capability.CalamityCapProvider;
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity.calamity$IsPlayer && !entity.calamity$Player.isLocalPlayer()) {
            CalamityCapProvider.safetyRunCalamityMagic(entity,
                expand -> {
                    expand.calamity$TryUseEnchantedStarfish(entity, 20);
                    stack.shrink(1);
                });
            return stack;
        }
        return stack;
    }
}
