package hua223.calamity.register.items;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CMLangUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@JeiInfo(zh_cn = "处于满月时击杀处于寒冷群系的亡灵生物掉落")
public class EndothermicEnergy extends Item {
    public EndothermicEnergy() {
        super(RegisterList.ITEM_UNCOMMON);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide && entity.getAge() != 0 &&  entity.getAge() % 20 == 0) {
            CompoundTag tag = updateState(level, entity);
            if (tag.contains("index", 11)) {
                byte energy;
                if (tag.contains("energy", 1)) energy = tag.getByte("energy");
                else tag.putByte("energy", energy = 10);

                int[] index = tag.getIntArray("index");
                int[] info = tag.getIntArray("fluids");
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                int informationBits = index.length - 1;
                int end = index[informationBits] + 2;
                ServerLevel serverLevel = (ServerLevel) level;
                for (; index[informationBits] < end; index[informationBits]++) {
                    if (index[informationBits] == informationBits - 1) {
                        tag.remove("index");
                        tag.remove("fluids");
                        break;
                    }

                    int startIndex = index[index[informationBits]] * 3;
                    mutable.set(info[startIndex++], info[startIndex++], info[startIndex]);
                    FluidState fluidState = level.getFluidState(mutable);
                    if (!fluidState.isEmpty()) {
                        byte depletion = !fluidState.is(FluidTags.LAVA) ? 1 : (byte) (level.random.nextFloat() < .3f ? 3 : 2);
                        Block transformation = switch (depletion) {
                            case 1 : yield Blocks.ICE;
                            case 2 : yield Blocks.STONE;
                            case 3 : yield Blocks.OBSIDIAN;
                            default: yield Blocks.AIR;
                        };

                        level.setBlock(mutable, transformation.defaultBlockState(), 18);
                        serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, mutable.getX() + 0.5,
                            mutable.getY() + 1.0, mutable.getZ() + 0.5, 5, 0.3, 0.2, 0.3, 0.05);

                        if ((energy -= depletion) < 1) {
                            entity.discard();
                            return true;
                        } else tag.putByte("energy", energy);
                    }
                }
            }
        }

        return false;
    }

    private static CompoundTag updateState(Level level, ItemEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        if (!tag.contains("fluids") || entity.walkDist != entity.walkDistO) {
            IntList list = new IntArrayList();
            BlockPos pos = entity.blockPosition();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int i = -4; i < 4; i++) {
                mutable.setX(pos.getX() + i);
                for (int k = -4; k < 4; k++) {
                    mutable.setZ(pos.getZ() + k);
                    for (int j = -2; j < 2; j++) {
                        if (!level.getFluidState(mutable.setY(pos.getY() + j)).isEmpty()) {
                            list.add(mutable.getX());
                            list.add(mutable.getY());
                            list.add(mutable.getZ());
                        }
                    }
                }
            }

            if (!list.isEmpty()) {
                int[] info = list.toIntArray();
                tag.putIntArray("fluids", info);

                list.clear();
                for (int i = 0; i < info.length / 3; i++)
                    list.add(i);

                Collections.shuffle(list);
                list.add(0);
                tag.putIntArray("index", list.toIntArray());
            }
        }

        return tag;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return 4000;
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }

    @ApplyGlobalLoot
    public void onEntityDrop(EntitiesLootContext context) {
        Entity entity = context.entity;
        Level level = entity.level();
        if (entity instanceof LivingEntity living && living.getMobType() == MobType.UNDEAD && level.dimensionType().moonPhase(
            level.getDayTime()) == 0 && level.getBiome(living.getOnPos()).is(Tags.Biomes.IS_COLD)) context.addLoot(this, context.getRandomCount(0, 2));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag) {
        tooltips.add(CMLangUtil.getTranslatable("energy").withStyle(ChatFormatting.GOLD));
    }
}
