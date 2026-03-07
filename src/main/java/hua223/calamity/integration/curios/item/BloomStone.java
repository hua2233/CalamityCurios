package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class BloomStone extends BaseCurio implements ICuriosStorage {
    public BloomStone(Properties pProperties) {
        super(pProperties);
    }

    private static void spawn(Player player) {
        if (player.isOnGround()) {
            ServerLevel serverLevel = (ServerLevel) player.level;
            BlockPos blockPos = player.getOnPos();
            if (!serverLevel.getBlockState(blockPos).is(Blocks.GRASS_BLOCK)) return;
            BlockPos abovePos = blockPos.above();
            RandomSource random = serverLevel.random;
            for (BlockPos aroundPos : BlockPos.betweenClosed(abovePos.offset(-1, 0, -1), abovePos.offset(1, 0, 1))) {
                if (!serverLevel.getBlockState(aroundPos.below()).is(Blocks.GRASS_BLOCK)) continue;
                BlockState spawnBlock = serverLevel.getBlockState(aroundPos);
                if (spawnBlock.isCollisionShapeFullBlock(serverLevel, aroundPos)) continue;

                if (random.nextFloat() < 0.3F && spawnBlock.isAir()) {
                    List<ConfiguredFeature<?, ?>> list = serverLevel.getBiome(aroundPos).value()
                        .getGenerationSettings().getFlowerFeatures();
                    if (list.isEmpty()) continue;
                    ((RandomPatchConfiguration) list.get(0).config()).feature().value()
                        .place(serverLevel, serverLevel.getChunkSource().getGenerator(), random, aroundPos);
                }
            }
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setTheValueInTime(player.level, getCount(player));
    }

    @Override
    public int getCountSize() {
        return 4;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);

        if (++count[0] > 1200) {
            count[0] = 0;
            Level level = player.level;
            setTheValueInTime(level, count);
        }

        if (++count[1] > 100) {
            count[1] = 0;
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(count[3]);
            }
        }

        if (++count[2] > 60) {
            count[2] = 0;
            spawn(player);
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    private void setTheValueInTime(Level level, float[] count) {
        long tick = level.getDayTime();

        if (tick > 18000) {
            count[3] = 1;
        } else if (tick > 12000) {
            count[3] = 3;
        } else if (tick > 6000) {
            count[3] = 4;
        } else count[3] = 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("bloom_stone", 2));
            tooltips.add(CMLangUtil.getTranslatable("bloom_stone", 3));
            tooltips.add(CMLangUtil.getTranslatable("bloom_stone", 4));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("bloom_stone", 1));
        }
        return tooltips;
    }
}
