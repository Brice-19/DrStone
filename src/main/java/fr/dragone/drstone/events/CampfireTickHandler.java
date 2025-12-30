package fr.dragone.drstone.events;

import fr.dragone.drstone.DrStone;
import fr.dragone.drstone.data.CampfireSavedData;
import fr.dragone.drstone.menu.CampfireMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(modid = DrStone.MODID)
public class CampfireTickHandler {

    private static final int CHARCOAL_CHANCE = 1;

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        // ⏱️ 1 fois par seconde
        if (level.getGameTime() % 20 != 0) return;

        CampfireSavedData data = CampfireSavedData.get(level);
        List<BlockPos> toRemove = new ArrayList<>();

        data.forEachCampfire((pos, state) -> {

            BlockState bs = level.getBlockState(pos);
            if (!bs.is(Blocks.CAMPFIRE)) {
                toRemove.add(pos);
                return;
            }

            /* =========================
               🔥 FEU EN COURS
               ========================= */
            if (state.burnTime > 0) {
                state.burnTime--;
                data.setDirty();
            }

            /* =========================
               🔁 NOUVEAU CYCLE → 1 LOG CONSOMMÉ
               ========================= */
            if (state.burnTime <= 0 && !state.fuel.isEmpty()) {

                boolean wasLog = state.fuel.is(ItemTags.LOGS);

                // ✅ 1 LOG = 1 CYCLE
                state.fuel.shrink(1);
                state.burnTime = CampfireSavedData.MAX_BURN;
                data.setDirty();

                // 🔥 chance de charbon
                if (wasLog && level.random.nextInt(CHARCOAL_CHANCE) == 0) {
                    if (state.result1.isEmpty()) {
                        state.result1 = new ItemStack(Items.CHARCOAL);
                    } else if (state.result2.isEmpty()) {
                        state.result2 = new ItemStack(Items.CHARCOAL);
                    }
                }

                // 🔄 SYNC CLIENT (menus ouverts)
                for (var player : level.players()) {
                    if (player.containerMenu instanceof CampfireMenu menu) {
                        if (menu.getPos().equals(pos)) {
                            menu.syncFromState();
                        }
                    }
                }
            }

            /* =========================
               🔥 ÉTAT DU FEU
               ========================= */
            if (state.burnTime > 0 && !bs.getValue(CampfireBlock.LIT)) {
                level.setBlock(pos, bs.setValue(CampfireBlock.LIT, true), 3);
            }

            /* =========================
               🧯 EXTINCTION FINALE
               ========================= */
            // 🧯 EXTINCTION FINALE → DROP DU CONTENU
            if (state.burnTime <= 0 && state.fuel.isEmpty()) {

                // 🔽 drop fuel restant (sécurité)
                drop(level, pos, state.fuel);

                // 🔽 drop résultats
                drop(level, pos, state.result1);
                drop(level, pos, state.result2);

                // 🧹 clear state pour éviter duplication
                state.fuel = ItemStack.EMPTY;
                state.result1 = ItemStack.EMPTY;
                state.result2 = ItemStack.EMPTY;

                if (bs.getValue(CampfireBlock.LIT)) {
                    level.setBlock(pos, bs.setValue(CampfireBlock.LIT, false), 3);
                }

                toRemove.add(pos);
            }

        });

        toRemove.forEach(data::remove);
        if (!toRemove.isEmpty()) data.setDirty();
    }

    private static void drop(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        level.addFreshEntity(
                new net.minecraft.world.entity.item.ItemEntity(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        stack.copy()
                )
        );
    }

}
