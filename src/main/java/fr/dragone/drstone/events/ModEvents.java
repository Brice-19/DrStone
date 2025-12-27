package fr.dragone.drstone.events;

import fr.dragone.drstone.DrStone;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DrStone.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player == null) return;

        // Créatif autorisé
        if (player.isCreative()) return;

        BlockState state = event.getState();
        ItemStack held = player.getMainHandItem();

        if (
                state.is(Blocks.SHORT_GRASS) ||
                        state.is(Blocks.TALL_GRASS) ||
                        state.is(Blocks.FERN) ||
                        state.is(Blocks.LARGE_FERN) ||
                        state.is(BlockTags.FLOWERS) ||
                        state.is(BlockTags.TALL_FLOWERS) ||
                        state.is(BlockTags.SAPLINGS)
        ) {
            return;
        }

        boolean needsTool =
                state.is(BlockTags.MINEABLE_WITH_AXE) ||
                        state.is(BlockTags.MINEABLE_WITH_PICKAXE) ||
                        state.is(BlockTags.MINEABLE_WITH_SHOVEL);

        if (!needsTool) return;


        if (held.isEmpty() || !held.isCorrectToolForDrops(state)) {
            event.setNewSpeed(-1.0f);

            player.displayClientMessage(
                    Component.literal("§cTu dois utiliser le bon outil pour casser ce bloc !"),
                    true
            );
        }
    }
}
