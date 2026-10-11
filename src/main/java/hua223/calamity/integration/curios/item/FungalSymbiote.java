package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class FungalSymbiote extends BaseCurio implements ICuriosStorage {
    public FungalSymbiote(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.fungalSymbiosis = true;
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.fungalSymbiosis = false;
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 60)) {
            Level level = player.level();
            BlockPos pos = player.getOnPos();
            BlockPos blockPos = pos.atY(pos.getY() + 1);
            if (level.getBlockState(pos).is(BlockTags.MUSHROOM_GROW_BLOCK) && level.getBlockState(blockPos).isAir()) {
                if (player.getRandom().nextDouble() < 0.3) level.setBlock(blockPos, Blocks.RED_MUSHROOM.defaultBlockState(), 1 | 2 | 16);
                else level.setBlock(blockPos, Blocks.BROWN_MUSHROOM.defaultBlockState(), 1 | 2 | 16);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips , ChatFormatting.GOLD,"fungal_symbiote", 1, 2);
        return tooltips;
    }
}
