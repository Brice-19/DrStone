package fr.dragone.drstone.events;

import fr.dragone.drstone.DrStone;
import fr.dragone.drstone.menu.CampfireMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DrStone.MODID)
public class CampfireInteractHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        if (event.getLevel().isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.CAMPFIRE)) return;
        if (!state.getValue(CampfireBlock.LIT)) return;

        // 🍖 si nourriture en main → laisser le comportement vanilla
        if (player.getMainHandItem().has(DataComponents.FOOD)) {
            return;
        }

        // 🔥 DEBUG
        System.out.println("CAMPFIRE CLICK OK");

        // bloque interaction vanilla
        event.setCanceled(true);

        ((ServerPlayer) player).openMenu(
                new SimpleMenuProvider(
                        (id, inv, p) -> new CampfireMenu(
                                id,
                                inv,
                                (ServerLevel) level,
                                pos
                        ),
                        Component.literal("Feu de camp")
                ),
                buf -> buf.writeBlockPos(pos)
        );
    }
}
