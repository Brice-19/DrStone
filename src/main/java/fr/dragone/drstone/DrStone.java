package fr.dragone.drstone;

import fr.dragone.drstone.item.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


import fr.dragone.drstone.block.ModBlock;

@Mod(DrStone.MODID)
public class DrStone {

    public static final String MODID = "drstone";

    public DrStone(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModBlock.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
    }
}
