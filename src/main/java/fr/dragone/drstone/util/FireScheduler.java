package fr.dragone.drstone.util;

import fr.dragone.drstone.DrStone;
import fr.dragone.drstone.block.BoisBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = DrStone.MODID)
public class FireScheduler {

    private static final Map<FirePos, Integer> TIMERS = new ConcurrentHashMap<>();

    public static void schedule(Level level, BlockPos pos) {
        TIMERS.put(
                new FirePos(level.dimension(), pos.immutable()),
                1200 // 1 minute
        );
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<FirePos, Integer>> it = TIMERS.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<FirePos, Integer> entry = it.next();
            FirePos fire = entry.getKey();
            int time = entry.getValue() - 1;

            if (time <= 0) {
                Level level = server.getLevel(fire.dimension());
                if (level != null) {
                    BlockState state = level.getBlockState(fire.pos());

                    // 🔥 ON NE TOUCHE QU’AU BOIS
                    if (state.getBlock() instanceof BoisBlock
                            && state.getValue(BoisBlock.FROM_ARCHET)) {

                        level.setBlock(
                                fire.pos(),
                                state.setValue(BoisBlock.FROM_ARCHET, false),
                                3
                        );
                    }
                }
                it.remove();
            } else {
                entry.setValue(time);
            }
        }
    }

    private record FirePos(ResourceKey<Level> dimension, BlockPos pos) {}
}
