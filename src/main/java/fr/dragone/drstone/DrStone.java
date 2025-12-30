package fr.dragone.drstone;

import fr.dragone.drstone.block.ModBlock;
import fr.dragone.drstone.item.ModItems;
import fr.dragone.drstone.menu.ModMenus;
import fr.dragone.drstone.screen.CampfireScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DrStone.MODID)
public class DrStone {

    public static final String MODID = "drstone";

    public DrStone(FMLJavaModLoadingContext context) {

        IEventBus modEventBus = context.getModEventBus();

        ModBlock.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(
                    ModMenus.CAMPFIRE_MENU.get(),
                    CampfireScreen::new
            );
        });
    }
}
