package fr.dragone.drstone.block;

import fr.dragone.drstone.item.ModItems;
import fr.dragone.drstone.menu.CampfireMenu;
import fr.dragone.drstone.util.FireScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

    /* =====================================================
       🔥 INTERACTION AVEC ITEM (Forge 1.21)
       ===================================================== */
    @Override
    public ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        // ⚠️ le client doit juste ACK, JAMAIS ouvrir un menu
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int stage = state.getValue(STAGE);

        /* =========================
           🪵 AJOUT DE BOIS
           ========================= */
        if (stack.is(ModItems.BOIS.get()) && stage < 3) {
            consume(player, stack);
            level.setBlock(pos, state.setValue(STAGE, stage + 1), 3);
            return ItemInteractionResult.CONSUME;
        }

        /* =========================
           🔥 ALLUMAGE AU CHARBON
           ========================= */
        if ((stack.is(Items.CHARCOAL) || stack.is(Items.COAL)) && stage == 3) {

            boolean fromArchet = state.getValue(FROM_ARCHET);
            consume(player, stack);

            BlockState campfire = Blocks.CAMPFIRE.defaultBlockState()
                    .setValue(CampfireBlock.LIT, fromArchet);

            level.setBlock(pos, campfire, 3);

            if (fromArchet) {
                FireScheduler.schedule(level, pos);
            }

            if (level instanceof ServerLevel serverLevel) {
                player.openMenu(new MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return net.minecraft.network.chat.Component.literal("Feu de camp");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                        return new CampfireMenu(id, inv, serverLevel, pos);
                    }
                });
            }


            return ItemInteractionResult.CONSUME;
        }

        // équivalent PASS en 1.21
        return ItemInteractionResult.FAIL;
    }

    /* =====================================================
       📦 DROPS
       ===================================================== */
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

    /* =====================================================
       🔥 PARTICULES
       ===================================================== */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FROM_ARCHET)) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.6;
        double z = pos.getZ() + 0.5;

        level.addParticle(ParticleTypes.FLAME,
                x + (random.nextDouble() - 0.5) * 0.3, y,
                z + (random.nextDouble() - 0.5) * 0.3,
                0.0, 0.01, 0.0);

        level.addParticle(ParticleTypes.SMOKE,
                x, y + 0.1, z,
                0.0, 0.02, 0.0);
    }

    /* =====================================================
       🏹 TRANSFORMATION EN ARCHET
       ===================================================== */
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

                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}
