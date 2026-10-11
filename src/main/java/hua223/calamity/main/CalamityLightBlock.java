package hua223.calamity.main;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static hua223.calamity.main.CalamityCurios.MODID;

//I don't want it to have a bunch of dependencies mods，
//this will make people feel very troublesome, I believe it is the same for anyone
public class CalamityLightBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static RegistryObject<Block> LIGHT_BLOCK;
    public static RegistryObject<Block> INFERNAL_SUEVITE_ORE;
    public static RegistryObject<Block> EXODIUM_CLUSTER_ORE;
    protected static RegistryObject<BlockEntityType<LightBlockEntity>> LIGHT_ENTITY;

    public CalamityLightBlock() {
        super(BlockBehaviour.Properties.of().noLootTable().noCollission()
            .noOcclusion().lightLevel(state -> state.getValue(BlockStateProperties.LEVEL)));
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(BlockStateProperties.AGE_5, 0)
            .setValue(BlockStateProperties.WATERLOGGED, false)
            .setValue(BlockStateProperties.LEVEL, 0));
    }

    public static void registerBlock(IEventBus bus) {
        DeferredRegister<Block> blockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
        LIGHT_BLOCK = blockRegister.register("light", CalamityLightBlock::new);

        blockRegister.register("lumenyl_druse", LumenylDruse::new);

        BlockBehaviour.Properties properties = BlockBehaviour.Properties.copy(Blocks.ANCIENT_DEBRIS);

        INFERNAL_SUEVITE_ORE = blockRegister.register("infernal_suevite_ore", () -> new Block(properties));

        EXODIUM_CLUSTER_ORE = blockRegister.register("exodium_cluster_ore", () -> new Block(properties));

        blockRegister.register(bus);

        DeferredRegister<BlockEntityType<?>> blockEntity = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

        LIGHT_ENTITY = blockEntity.register("light",
            () -> BlockEntityType.Builder.of(LightBlockEntity::new, LIGHT_BLOCK.get()).build(null));

        blockEntity.register(bus);
    }

    //Fixed the issue of incorrect placement of blocks during swimming
    private static BlockPos canPlace(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        BlockPos headPos = new BlockPos((int) eyePos.x, (int) eyePos.y, (int) eyePos.z);
        BlockState state = level.getBlockState(headPos);
        if (state.isAir() || state.getFluidState().getType() == Fluids.WATER) return headPos;

        BlockPos footPos = headPos.below();
        BlockState footState = level.getBlockState(footPos);
        if (footState.isAir() || footState.getFluidState().getType() == Fluids.WATER) return footPos;

        return null;
    }

    public static void placePlayerDynamicLightSource(Player player, int lightLevel) {
        Level level = player.level();
        BlockPos placePos = canPlace(level, player);
        if (placePos != null) {
            BlockState state = LIGHT_BLOCK.get().defaultBlockState();
            level.setBlock(placePos, state.setValue(BlockStateProperties.LEVEL, lightLevel), 18);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LEVEL);
        builder.add(BlockStateProperties.AGE_5);
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, LIGHT_ENTITY.get(), LightBlockEntity::tick);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LightBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @OnlyIn(Dist.CLIENT)
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("all")
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return state.isAir();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() != Fluids.WATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            Fluid fluid = state.getFluidState().getType();
            level.scheduleTick(currentPos, fluid, fluid.getTickDelay(level));
        }

        return state;
    }
}

class LightBlockEntity extends BlockEntity {
    public LightBlockEntity(BlockPos pos, BlockState state) {
        super(CalamityLightBlock.LIGHT_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LightBlockEntity entity) {
        int lifeCycle = state.getValue(BlockStateProperties.AGE_5);

        if (lifeCycle < 5) level.setBlock(pos, state.setValue(BlockStateProperties.AGE_5, ++lifeCycle), 18);
        else level.setBlock(pos, level.getFluidState(pos).createLegacyBlock(), 3);
    }
}
