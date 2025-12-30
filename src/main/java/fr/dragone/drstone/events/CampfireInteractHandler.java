package fr.dragone.drstone.events;

import fr.dragone.drstone.menu.CampfireMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CampfireInteractHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();

        if (level.isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack held = player.getMainHandItem();
        BlockState state = level.getBlockState(pos);

        // 🔥 UNIQUEMENT feu de camp VANILLA
        if (!state.is(Blocks.CAMPFIRE)) return;

        // 🔥 UNIQUEMENT SI ALLUMÉ
        if (!state.getValue(CampfireBlock.LIT)) return;

        // ✋ main vide obligatoire
        if (!held.isEmpty()) return;

        // 🚫 bloque interaction vanilla sans cancel l’event
        event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
        event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // ✅ ouvre le menu
        ((ServerPlayer) player).openMenu(
                new SimpleMenuProvider(
                        (id, inv, p) -> new CampfireMenu(id, inv),
                        Component.literal("Feu de camp")
                )
        );
    }
}
