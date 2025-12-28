package fr.dragone.drstone.item;

import fr.dragone.drstone.DrStone;
import fr.dragone.drstone.block.ModBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, DrStone.MODID);

    // 🧱 BlockItem : Pierre
    public static final RegistryObject<Item> PIERRE =
            ITEMS.register("pierre",
                    () -> new BlockItem(ModBlock.PIERRE.get(), new Item.Properties()));

    public static final RegistryObject<Item> ARCHET_ITEM = ITEMS.register(
            "archet",
            () -> new BlockItem(ModBlock.ARCHET.get(), new Item.Properties())
    );

    // 📦 Item simple : Calliou
    public static final RegistryObject<Item> CALLIOU =
            ITEMS.register("calliou",
                    () -> new Item(new Item.Properties()));
}
