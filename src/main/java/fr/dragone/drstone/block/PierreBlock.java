package fr.dragone.drstone.block;

import fr.dragone.drstone.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PierreBlock extends Block {

    // ───────── BLOCK STATES ─────────
    // 5 stages : 4 → 3 → 2 → 1 → 0
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);

    // Rotation horizontale
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // ───────── COLLISION (TRÈS PETITE) ─────────
    private static final VoxelShape COLLISION_SHAPE =
            Block.box(6, 0, 6, 10, 1, 10);

    // ───────── CONSTRUCTEUR ─────────
    public PierreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(STAGE, 4)
                        .setValue(FACING, Direction.NORTH)
        );
    }

    // ───────── BLOCKSTATE REGISTRY ─────────
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, FACING);
    }

    // ───────── ROTATION ALÉATOIRE AU PLACEMENT ─────────
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction randomDir =
                Direction.Plane.HORIZONTAL.getRandomDirection(context.getLevel().random);

        return this.defaultBlockState()
                .setValue(STAGE, 4)
                .setValue(FACING, randomDir);
    }

    // ───────── INTERACTION (CLIC DROIT) ─────────
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {

        if (!level.isClientSide) {

            int stage = state.getValue(STAGE);

            popResource(level, pos, new ItemStack(ModItems.CALLIOU.get(), 1));

            if (stage > 0) {
                level.setBlock(pos, state.setValue(STAGE, stage - 1), 3);
            } else {
                level.destroyBlock(pos, false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    // ───────── SURVIE (WORLDGEN FIX) ─────────
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());

        return below.is(net.minecraft.tags.BlockTags.DIRT)
                || below.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD);
    }

    // ───────── SHAPES ─────────
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return Block.box(0, 0, 0, 16, 2, 16);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        if (state.getValue(STAGE) == 0) {
            return Shapes.empty();
        }
        return COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos,
                                     CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }
}
