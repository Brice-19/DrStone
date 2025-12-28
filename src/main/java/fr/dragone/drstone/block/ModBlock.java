package fr.dragone.drstone.block;

import fr.dragone.drstone.DrStone;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModBlock {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DrStone.MODID);

    public static final RegistryObject<Block> PIERRE = BLOCKS.register(
            "pierre",
            () -> new PierreBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(50.0F, 1200.0F)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
            )
    );
    public static final RegistryObject<Block> ARCHET = BLOCKS.register(
            "archet",
            () -> new ArchetBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5F)
                            .noOcclusion()
            )
    );

}
