package hua223.calamity.register.Items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.render.Item.CrusherRender;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CrystylCrusher extends PickaxeItem {
    public CrystylCrusher(Item.Properties properties) {
        super(RegisterList.CRYSTYL_TIER, -1, -2, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 600;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            if (level.isClientSide) CrusherRender.start((AbstractClientPlayer) player);
            else player.startUsingItem(hand);
        }

        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    public void destroyBlock(Level level, BlockPos target, Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    tryDestroy(level, target.offset(-1 + i, 0, -1 + j));
                }
            }
        } else if (side == Direction.EAST || side == Direction.WEST) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    tryDestroy(level, target.offset(0, -1 + i, -1 + j));
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    tryDestroy(level, target.offset(-1 + j, -1 + i, 0));
                }
            }
        }
    }

    private void tryDestroy(Level level, BlockPos pos) {
        BlockState block = level.getBlockState(pos);
        if (block.isAir() || block.getDestroySpeed(level, pos) < 0) return;
        level.destroyBlock(pos, true);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        Level level = player.level;
        if (!level.isClientSide) {
            Vec3 start = player.getEyePosition();
            Vec3 end = player.getLookAngle().normalize().scale(16f).add(start);
            BlockHitResult result = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            if (result.getType() != HitResult.Type.MISS) {
                destroyBlock(level, result.getBlockPos(), result.getDirection());
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int pTimeCharged) {
        if (entity.calamity$IsPlayer && level.isClientSide)
            CrusherRender.stop((AbstractClientPlayer) entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        tooltips.add(CMLangUtil.getTranslatable("crystyl_crusher").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return CrusherRender.isRendering ? RenderUtil.HOLD_POSE : null;
            }
        });
    }
}
