package fr.dragone.drstone.events;

import fr.dragone.drstone.DrStone;
import fr.dragone.drstone.data.CampfireSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DrStone.MODID)
public class CampfireBreakHandler {

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {

        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos();

        // ✅ uniquement pour feu de camp
        if (!level.getBlockState(pos).is(Blocks.CAMPFIRE)) return;

        CampfireSavedData data = CampfireSavedData.get(level);
        CampfireSavedData.CampfireState state = data.getState(pos);

        // 🔽 drop fuel
        drop(level, pos, state.fuel);

        // 🔽 drop résultats
        drop(level, pos, state.result1);
        drop(level, pos, state.result2);

        // 🧹 nettoyage
        data.remove(pos);
        data.setDirty();
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
