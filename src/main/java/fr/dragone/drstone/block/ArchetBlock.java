package fr.dragone.drstone.block;

import fr.dragone.drstone.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ArchetBlock extends Block {

    // ───────── PROPERTIES ─────────
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);
    public static final IntegerProperty TIMER = IntegerProperty.create("timer", 0, 900);

    private static final VoxelShape COLLISION_SHAPE =
            Block.box(1, 0, 1, 15, 3, 15);

    public ArchetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(STAGE, 0)
                        .setValue(TIMER, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, TIMER);
    }

    // ───────── INTERACTION ─────────
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {

        int stage = state.getValue(STAGE);
        ItemStack stack = player.getMainHandItem();

        // STAGE 0 → STICK
        if (stage == 0 && stack.is(Items.STICK)) {
            consume(player, stack);
            level.setBlock(pos, state.setValue(STAGE, 1), 3);
            return InteractionResult.CONSUME;
        }

        // STAGE 1 → BOW
        if (stage == 1 && stack.is(Items.BOW)) {
            consume(player, stack);
            level.setBlock(pos, state.setValue(STAGE, 2), 3);
            return InteractionResult.CONSUME;
        }

        // STAGE 2 → CALLIOU (allumage)
        if (stage == 2 && stack.is(ModItems.CALLIOU.get())) {
            consume(player, stack);

            int time = 600 + level.random.nextInt(301); // 30–45s
            level.setBlock(
                    pos,
                    state.setValue(STAGE, 3).setValue(TIMER, time),
                    3
            );

            level.scheduleTick(pos, this, 20);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    // ───────── TICK SERVEUR ─────────
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if (state.getValue(STAGE) != 3) return;

        int timer = state.getValue(TIMER);

        // ───── PARTICULES (fumée + étincelles) ─────
        level.sendParticles(
                ParticleTypes.SMOKE,
                pos.getX() + 0.5,
                pos.getY() + 0.25,
                pos.getZ() + 0.5,
                2,
                0.12, 0.04, 0.12,
                0.005
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                pos.getX() + 0.5,
                pos.getY() + 0.3,
                pos.getZ() + 0.5,
                4,
                0.15, 0.05, 0.15,
                0.02
        );

        // ───── TIMER LOGIC (SAFE) ─────
        if (timer > 20) {
            level.setBlock(
                    pos,
                    state.setValue(TIMER, timer - 20),
                    3
            );
            level.scheduleTick(pos, this, 20);
        } else {
            // FIN DU PROCESS
            level.setBlock(
                    pos,
                    state.setValue(STAGE, 4).setValue(TIMER, 0),
                    3
            );
        }
    }

    // ───────── UTIL ─────────
    private void consume(Player player, ItemStack stack) {
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                        BlockPos pos, CollisionContext ctx) {
        return COLLISION_SHAPE;
    }
}
