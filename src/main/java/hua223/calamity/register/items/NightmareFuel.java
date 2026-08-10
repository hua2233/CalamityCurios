package hua223.calamity.register.items;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@JeiInfo(zh_cn = "在下界堡垒中可以被找到")
public class NightmareFuel extends Item {
    public NightmareFuel() {
        super(RegisterList.ITEM_UNCOMMON);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 3200;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.onGround() && entity.getAge() % 5 == 0) {
            Level level = entity.level();
            BlockPos pos = entity.getOnPos().above();
            if (BaseFireBlock.canBePlacedAt(level, pos, Direction.NORTH)) {
                BlockState fire = BaseFireBlock.getState(level, pos);
                level.setBlock(pos, fire, 11);
            }
        }
        return false;
    }

    @ApplyGlobalLoot
    public void onChestLoot(ChestLootContext context) {
        if (context.fromSpecificName("nether_bridge") && context.chance(0.3f))
            context.addLoot(this, context.getRandomCount(2, 7));
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag) {
        tooltips.add(CMLangUtil.getTranslatable("nightmare_fuel").withStyle(ChatFormatting.GOLD));
    }
}
