package fr.dragone.drstone.menu;

import fr.dragone.drstone.data.CampfireSavedData;
import fr.dragone.drstone.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CampfireMenu extends AbstractContainerMenu {

    /**
     * Slots :
     * 0 = fuel
     * 1 = résultat (charbon)
     * 2 = fallback
     */
    private final SimpleContainer container = new SimpleContainer(3);

    /** 🔄 Sync burn / max */
    private final ContainerData sync = new SimpleContainerData(2);

    /** 📦 State serveur */
    private final CampfireSavedData.CampfireState state;

    /** 📍 Position du feu */
    private final BlockPos pos;

    /** 👤 Joueur */
    private final Player player;

    /** ✅ Accès vanilla (ANTI FERMETURE MENU) */
    private final ContainerLevelAccess access;

    /* =========================
       SERVEUR
       ========================= */
    public CampfireMenu(int id, Inventory inv, ServerLevel level, BlockPos pos) {
        super(ModMenus.CAMPFIRE_MENU.get(), id);

        this.pos = pos;
        this.player = inv.player;
        this.access = ContainerLevelAccess.create(level, pos);

        CampfireSavedData data = CampfireSavedData.get(level);
        this.state = data.getState(pos);

        // 🔄 restauration des données
        container.setItem(0, state.fuel.copy());
        container.setItem(1, state.result1.copy());
        container.setItem(2, state.result2.copy());

        /* =========================
           SLOT FUEL (0)
           ========================= */
        this.addSlot(new Slot(container, 0, 62, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isFuel(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                state.fuel = container.getItem(0).copy();

                if (!state.fuel.isEmpty() && state.burnTime <= 0) {
                    state.burnTime = CampfireSavedData.MAX_BURN;
                }

                data.setDirty();
            }
        });

        /* =========================
           SLOT RÉSULTAT (1)
           ========================= */
        this.addSlot(new Slot(container, 1, 134, 26) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void setChanged() {
                super.setChanged();
                state.result1 = container.getItem(1).copy();
                data.setDirty();
            }
        });

        /* =========================
           SLOT FALLBACK (2)
           ========================= */
        this.addSlot(new Slot(container, 2, 134, 48) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void setChanged() {
                super.setChanged();
                state.result2 = container.getItem(2).copy();
                data.setDirty();
            }
        });

        addPlayerInventory(inv);

        addDataSlots(sync);
        sync.set(0, state.burnTime);
        sync.set(1, CampfireSavedData.MAX_BURN);
    }

    /* =========================
       CLIENT
       ========================= */
    public CampfireMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(ModMenus.CAMPFIRE_MENU.get(), id);

        this.pos = BlockPos.ZERO; // ✅ PLUS DE LECTURE BUFFER
        this.player = inv.player;
        this.state = null;
        this.access = ContainerLevelAccess.NULL;

        this.addSlot(new Slot(container, 0, 62, 26) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(container, 1, 134, 26) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(container, 2, 134, 48) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        addPlayerInventory(inv);
        addDataSlots(sync);
    }


    public boolean consumeOneFuel() {
        ItemStack fuel = container.getItem(0);

        if (fuel.isEmpty()) return false;

        fuel.shrink(1);
        container.setItem(0, fuel);

        state.fuel = fuel.copy();
        CampfireSavedData.get((ServerLevel) player.level()).setDirty();

        return true;
    }

    /* =========================
       SYNC
       ========================= */
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (state == null) return;

        sync.set(0, state.burnTime);
        sync.set(1, CampfireSavedData.MAX_BURN);
    }

    /* =========================
       VALIDITÉ (VANILLA)
       ========================= */
    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(
                this.access,
                player,
                Blocks.CAMPFIRE
        );
    }

    /* =========================
       GUI HELPERS
       ========================= */

    /** 🔥 flammes : 100% → 0% */
    public float getFlameProgress() {
        int burn = sync.get(0);
        int max  = sync.get(1);
        if (burn <= 0 || max <= 0) return 0f;
        return burn / (float) max;
    }


    /** ➡️ flèche : 0% → 100% */
    public float getArrowProgress() {
        int burn = sync.get(0);
        int max  = sync.get(1);

        if (burn <= 0 || max <= 0) return 0f;
        if (container.getItem(0).isEmpty()) return 0f;

        return 1f - (burn / (float) max);
    }

    /* =========================
       INVENTAIRE JOUEUR
       ========================= */
    private void addPlayerInventory(Inventory inv) {
        int y = 84;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, y + r * 18));

        for (int c = 0; c < 9; c++)
            addSlot(new Slot(inv, c, 8 + c * 18, y + 58));
    }

    private boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL)
                || stack.is(Items.CHARCOAL)
                || stack.is(ItemTags.LOGS)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ModItems.BOIS.get());
    }

    /* =========================
       SHIFT-CLICK
       ========================= */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == 1 || index == 2 || index == 0) {
            if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (isFuel(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }


    public boolean isBurning() {
        return sync.get(0) > 0;
    }

    public boolean hasFuel() {
        return !container.getItem(0).isEmpty();
    }
    public BlockPos getPos() {
        return pos;
    }

    public void syncFromState() {
        container.setItem(0, state.fuel.copy());
        container.setItem(1, state.result1.copy());
        container.setItem(2, state.result2.copy());

        slotsChanged(container);
        broadcastChanges();
    }

}
