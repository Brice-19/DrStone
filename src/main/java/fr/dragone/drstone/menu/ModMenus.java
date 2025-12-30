package fr.dragone.drstone.menu;

import fr.dragone.drstone.DrStone;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, DrStone.MODID);

    public static final RegistryObject<MenuType<CampfireMenu>> CAMPFIRE_MENU =
            MENUS.register(
                    "campfire_menu",
                    () -> IForgeMenuType.create(CampfireMenu::new)
            );
}
