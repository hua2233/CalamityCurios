package hua223.calamity.register.items.edible;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.FishingLootContext;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@JeiInfo(zh_cn = "在夜晚钓鱼且魔力值大于一半以上时可以获得，会消耗一半魔力。每点魔力上限提供0.05%的捕获概率")
public class EnchantedStarfish extends Item {
    public EnchantedStarfish() {
        super(new Properties().rarity(Rarity.UNCOMMON).stacksTo(16).food(
            new FoodProperties.Builder()
                .nutrition(6)
                .saturationMod(6f)
                .meat()
                .alwaysEat()
                .build()));
    }

    @ApplyGlobalLoot
    public void onFishing(FishingLootContext context) {
        if (context.player.level().isNight()) {
            float maxMana = (float) context.player.getAttributeValue(AttributeRegistry.MAX_MANA.get());

            if (context.chance(maxMana * 0.0005f) &&
                context.player.Calamity$Player.consumeMana(maxMana / 2)) context.addLoot(this);
        }
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
