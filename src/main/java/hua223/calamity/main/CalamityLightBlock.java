package hua223.calamity.main;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import static hua223.calamity.main.CalamityCurios.MODID;

public class CalamityLightBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static RegistryObject<Block> LIGHT_BLOCK;
    private static RegistryObject<BlockEntityType<LightBlockEntity>> LIGHT_ENTITY;
    public CalamityLightBlock() {
        super(BlockBehaviour.Properties.of(Material.AIR)
            .air().noLootTable().noCollission()
            .noOcclusion().lightLevel(state -> state.getValue(BlockStateProperties.LEVEL)));
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(BlockStateProperties.AGE_5, 0)
            .setValue(BlockStateProperties.WATERLOGGED, false)
            .setValue(BlockStateProperties.LEVEL, 0));
    }

    public static void registerBlock(IEventBus bus) {
        DeferredRegister<Block> blockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
        LIGHT_BLOCK = blockRegister.register("light", CalamityLightBlock::new);
        blockRegister.register(bus);

        DeferredRegister<BlockEntityType<?>> blockEntity = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
        LIGHT_ENTITY = blockEntity.register("light",
            () -> BlockEntityType.Builder.of(LightBlockEntity::new, LIGHT_BLOCK.get()).build(null));
        blockEntity.register(bus);
    }

    //Fixed the issue of incorrect placement of blocks during swimming
    private static BlockPos canPlace(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        BlockPos headPos = new BlockPos(eyePos.x, eyePos.y, eyePos.z);
        BlockState state = level.getBlockState(headPos);
        if (state.isAir() || !state.getFluidState().isEmpty()) return headPos;

        BlockPos footPos = headPos.below();
        BlockState footState = level.getBlockState(footPos);
        if (footState.isAir() || !footState.getFluidState().isEmpty()) return footPos;

        return null;
    }

    public static void placePlayerDynamicLightSource(Player player, int lightLevel) {
        Level level = player.level;
        BlockPos placePos = canPlace(level, player);
        if (placePos != null) {
            BlockState state = LIGHT_BLOCK.get().defaultBlockState();
            level.setBlock(placePos, state.setValue(BlockStateProperties.LEVEL, lightLevel), 18);
        }
    }

    public static BlockEntityType<LightBlockEntity> getEntity() {
        return LIGHT_ENTITY.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AGE_5);
        builder.add(BlockStateProperties.WATERLOGGED);
        builder.add(BlockStateProperties.LEVEL);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, LIGHT_ENTITY.get(), LightBlockEntity::tick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightBlockEntity(LIGHT_ENTITY.get(), pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED))
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return state;
    }
}

class LightBlockEntity extends BlockEntity {
    public LightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public LightBlockEntity(BlockPos pos, BlockState state) {
        super(CalamityLightBlock.getEntity(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LightBlockEntity entity) {
        int lifeCycle = state.getValue(BlockStateProperties.AGE_5);

        if (lifeCycle < 5) {
            level.setBlock(pos, state.setValue(BlockStateProperties.AGE_5, ++lifeCycle), 18);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
        }
    }
}
