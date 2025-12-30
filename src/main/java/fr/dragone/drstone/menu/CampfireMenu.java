package fr.dragone.drstone.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CampfireMenu extends AbstractContainerMenu {

    private final SimpleContainer container = new SimpleContainer(3);

    public CampfireMenu(int id, Inventory playerInv) {
        super(ModMenus.CAMPFIRE_MENU.get(), id);

        // 🔥 SLOTS CAMPFIRE (DESCENDUS VISUELLEMENT)
        this.addSlot(new Slot(container, 0, 62, 60));  // entrée bois
        this.addSlot(new Slot(container, 1, 98, 60));  // sortie
        this.addSlot(new Slot(container, 2, 116, 78)); // bonus

        int invStartY = 84;

        // Inventaire
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        invStartY + row * 18
                ));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInv,
                    col,
                    8 + col * 18,
                    invStartY + 58
            ));
        }
    }

    // ───── CLIENT
    public CampfireMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
