package fr.dragone.drstone.screen;

import fr.dragone.drstone.menu.CampfireMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CampfireScreen extends AbstractContainerScreen<CampfireMenu> {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(
                    "drstone",
                    "textures/gui/campfire_gui.png"
            );

    public CampfireScreen(CampfireMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos  = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(
                BG,
                leftPos,
                topPos,
                0, 0,
                imageWidth,
                imageHeight
        );
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0x404040);
    }
}
