package fr.dragone.drstone.block;

import fr.dragone.drstone.item.ModItems;
import fr.dragone.drstone.util.FireScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ArchetBlock extends Block {

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 2);
    public static final IntegerProperty PROGRESS = IntegerProperty.create("progress", 0, 40);

    public ArchetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(STAGE, 0)
                        .setValue(PROGRESS, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, PROGRESS);
    }

    @Mod.EventBusSubscriber
    public static class ArchetEvent {

        @SubscribeEvent
        public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {

            if (event.getHand() != InteractionHand.MAIN_HAND) return;

            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            Player player = event.getEntity();
            ItemStack held = player.getItemInHand(event.getHand());

            if (!(state.getBlock() instanceof ArchetBlock)) return;

            int stage = state.getValue(STAGE);
            int progress = state.getValue(PROGRESS);

            // Étape 1 : arc
            if (stage == 0 && held.is(Items.BOW)) {
                consume(player, held);
                level.setBlock(pos, state.setValue(STAGE, 1), 3);
                cancel(event);
                return;
            }

            // Étape 2 : caillou
            if (stage == 1 && held.is(ModItems.CALLIOU.get())) {
                consume(player, held);
                level.setBlock(pos, state.setValue(STAGE, 2).setValue(PROGRESS, 0), 3);
                cancel(event);
                return;
            }

            // Étape 3 : frottement main nue
            if (stage == 2 && held.isEmpty() && level instanceof ServerLevel server) {

                server.sendParticles(
                        ParticleTypes.SMOKE,
                        pos.getX() + 0.5,
                        pos.getY() + 0.4,
                        pos.getZ() + 0.5,
                        2,
                        0.02, 0.05, 0.02,
                        0.0
                );

                int newProgress = progress + 1;

                if (newProgress >= 40) {
                    level.setBlock(
                            pos,
                            ModBlock.BOIS.get()
                                    .defaultBlockState()
                                    .setValue(BoisBlock.STAGE, 0)
                                    .setValue(BoisBlock.FROM_ARCHET, true),
                            3
                    );

                    // ✅ SEULE AJOUT : timer pour retirer FROM_ARCHET après 1 minute
                    FireScheduler.schedule(level, pos);

                } else {
                    level.setBlock(pos, state.setValue(PROGRESS, newProgress), 3);
                }

                cancel(event);
            }
        }

        private static void cancel(PlayerInteractEvent.RightClickBlock event) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }

        private static void consume(Player player, ItemStack stack) {
            if (!player.isCreative()) stack.shrink(1);
        }
    }
}
