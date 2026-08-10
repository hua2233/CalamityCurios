package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@JeiInfo(zh_cn = "可以从雪原小屋的地下室宝箱中发现")
public class IceSnowMirror extends Item {
    public IceSnowMirror() {
        super(RegisterList.CURIOS_COMMON);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!level.isClientSide && usedHand == InteractionHand.MAIN_HAND && !player.getCooldowns().isOnCooldown(this))
            player.startUsingItem(usedHand);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && remainingUseDuration % 5 == 0) {
            Vec3 eyesPos = livingEntity.getEyePosition();
            RandomSource source = level.random;
            for (int i = 0; i < source.nextInt(4, 9); i++)
                level.addParticle(source.nextFloat() < 0.4 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.SNOWFLAKE,
                    eyesPos.x + (0.5f - source.nextFloat()), eyesPos.y + (0.5f - source.nextFloat()),
                    eyesPos.z + (0.5f - source.nextFloat()), 0.03f - source.nextFloat() * 0.06f, 0, 0.03f - source.nextFloat() * 0.06f);
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (!entity.level().isClientSide && entity.calamity$IsPlayer) {
            ServerPlayer player = toSpawnPoint((ServerPlayer) entity);
            player.getCooldowns().addCooldown(this, 60);
            player.playSound(CalamitySounds.MIRROR_TELEPORT.get());
        }
        return stack;
    }

    @SuppressWarnings("ConstantConditions")
    public static ServerPlayer toSpawnPoint(ServerPlayer player) {
        ServerLevel level = player.getServer().getLevel(player.getRespawnDimension());
        Tuple<BlockState, Vec3> value = findSpawnPoint(level, player.getRespawnPosition(), player.getRespawnAngle());
        level = value.getB() != null ? level : player.getServer().overworld();

        if (level != player.level()) {
            ServerPlayer _new = (ServerPlayer) player.changeDimension(level);
            if (_new == null) {
                player.displayClientMessage(CMLangUtil.getTranslatable("teleport_fail"), false);
                return player;
            }

            player = _new;
        }

        if (value.getB() != null) {
            Vec3 vec3 = value.getB();
            float yRot;
            if (value.getA().is(BlockTags.BEDS)) {
                Vec3 vec = Vec3.atBottomCenterOf(player.getRespawnPosition()).subtract(vec3).normalize();
                yRot = (float) Mth.wrapDegrees(Mth.atan2(vec.z, vec.x) * (180f / Math.PI) - 90.0f);
            } else yRot = player.getRespawnAngle();

            player.connection.teleport(vec3.x, vec3.y, vec3.z, yRot, 0.0F);
        } else {
            BlockPos spawnPos = level.getSharedSpawnPos();
            player.connection.teleport(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), level.getSharedSpawnAngle(), 0.0F);
        }

        return player;
    }

    @SuppressWarnings("all")
    private static Tuple<BlockState, Vec3> findSpawnPoint(Level level, BlockPos pos, float orientation) {
        Tuple<BlockState, Vec3> value = new Tuple<>(null, null);
        if (level != null && pos != null) {
            BlockState blockstate = level.getBlockState(pos);

            value.setA(blockstate);
            Block block = blockstate.getBlock();

            Optional<Vec3> optional = Optional.empty();
            if (block instanceof RespawnAnchorBlock && blockstate.getValue(RespawnAnchorBlock.CHARGE) > 0 && RespawnAnchorBlock.canSetSpawn(level))
                optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos);
            else if (block instanceof BedBlock && BedBlock.canSetSpawn(level))
                optional = BedBlock.findStandUpPosition(EntityType.PLAYER, level, pos, blockstate.getValue(BedBlock.FACING), orientation);

            optional.ifPresent(value::setB);
        }

        return value;
    }

    @ApplyGlobalLoot
    public void onChestLoot(ChestLootContext context) {
        if (context.fromSpecificName("igloo_chest") && context.chance(.3f))
            context.addLoot(this, 1);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 60;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("ice_mirror").withStyle(ChatFormatting.AQUA));
    }
}
