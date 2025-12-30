package fr.dragone.drstone.block;

import fr.dragone.drstone.item.ModItems;
import fr.dragone.drstone.util.FireScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class BoisBlock extends Block {

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    public static final BooleanProperty FROM_ARCHET = BooleanProperty.create("from_archet");

    public BoisBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(STAGE, 0)
                        .setValue(FROM_ARCHET, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, FROM_ARCHET);
    }

    /* ===================== INTERACTIONS ===================== */

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack held = player.getMainHandItem();
        int stage = state.getValue(STAGE);

        // Ajouter du bois
        if (held.is(ModItems.BOIS.get()) && stage < 3) {
            consume(player, held);
            level.setBlock(pos, state.setValue(STAGE, stage + 1), 3);
            return InteractionResult.CONSUME;
        }

        // Charbon / charbon de bois
        if ((held.is(Items.CHARCOAL) || held.is(Items.COAL)) && stage == 3) {

            boolean fromArchet = state.getValue(FROM_ARCHET);
            consume(player, held);

            BlockState campfire = Blocks.CAMPFIRE.defaultBlockState()
                    .setValue(CampfireBlock.LIT, fromArchet);

            level.setBlock(pos, campfire, 3);

// 🔥 feu temporaire UNIQUEMENT si bois issu de l’archet
            if (fromArchet) {
                FireScheduler.schedule(level, pos);

            }


            return InteractionResult.CONSUME;
        }

        // Retirer du bois à la main
        if (held.isEmpty()) {

            if (stage == 0) return InteractionResult.SUCCESS;

            level.setBlock(pos, state.setValue(STAGE, stage - 1), 3);
            popResource(level, pos, new ItemStack(ModItems.BOIS.get()));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    /* ===================== DROPS ===================== */

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i <= state.getValue(STAGE); i++) {
            drops.add(new ItemStack(ModItems.BOIS.get()));
        }
        return drops;
    }

    private void consume(Player player, ItemStack stack) {
        if (!player.isCreative()) stack.shrink(1);
    }

    /* ===================== PARTICULES 🔥 ===================== */

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

        if (!state.getValue(FROM_ARCHET)) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.6;
        double z = pos.getZ() + 0.5;

        level.addParticle(
                ParticleTypes.FLAME,
                x + (random.nextDouble() - 0.5) * 0.3,
                y,
                z + (random.nextDouble() - 0.5) * 0.3,
                0.0, 0.01, 0.0
        );

        level.addParticle(
                ParticleTypes.SMOKE,
                x,
                y + 0.1,
                z,
                0.0, 0.02, 0.0
        );
    }

    /* ===================== FEU TEMPORAIRE ===================== */

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if (state.getBlock() instanceof CampfireBlock
                && state.getValue(CampfireBlock.LIT)) {

            level.setBlock(
                    pos,
                    state.setValue(CampfireBlock.LIT, false),
                    3
            );
        }
    }

    /* ===================== EVENT : BOIS → ARCHET ===================== */

    @Mod.EventBusSubscriber
    public static class BoisRightClickEvent {

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

            if (event.getHand() != InteractionHand.MAIN_HAND) return;

            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            Player player = event.getEntity();
            ItemStack held = player.getItemInHand(event.getHand());

            if (state.getBlock() != ModBlock.BOIS.get()) return;

            if (held.is(Items.STICK)) {

                if (!level.isClientSide) {
                    if (!player.isCreative()) held.shrink(1);

                    level.setBlock(
                            pos,
                            ModBlock.ARCHET.get()
                                    .defaultBlockState()
                                    .setValue(ArchetBlock.STAGE, 0)
                                    .setValue(ArchetBlock.PROGRESS, 0),
                            3
                    );
                }

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}
