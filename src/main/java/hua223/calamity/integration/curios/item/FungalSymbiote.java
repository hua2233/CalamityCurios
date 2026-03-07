package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class FungalSymbiote extends BaseCurio implements ICuriosStorage {
    public FungalSymbiote(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) > 60) {
            zeroCount(player, 0);

            Level level = player.level;
            BlockPos pos = player.getOnPos();
            BlockPos blockPos = pos.atY(pos.getY() + 1);
            if (level.getBlockState(pos).is(BlockTags.MUSHROOM_GROW_BLOCK) && level.getBlockState(blockPos).isAir()) {
                if (player.getRandom().nextDouble() < 0.3) {
                    level.setBlock(blockPos, Blocks.RED_MUSHROOM.defaultBlockState(), 1 | 2 | 16);
                } else {
                    level.setBlock(blockPos, Blocks.BROWN_MUSHROOM.defaultBlockState(), 1 | 2 | 16);
                }
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("fungal_symbiote", 1));
        tooltips.add(CMLangUtil.getTranslatable("fungal_symbiote", 2));
        return tooltips;
    }
}
