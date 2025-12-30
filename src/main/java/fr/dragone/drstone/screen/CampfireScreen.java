package fr.dragone.drstone.screen;

import fr.dragone.drstone.menu.CampfireMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CampfireScreen extends AbstractContainerScreen<CampfireMenu> {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("drstone", "textures/gui/campfire_gui.png");

    private static final ResourceLocation SLOT =
            ResourceLocation.fromNamespaceAndPath("drstone", "textures/gui/slot.png");

    private static final ResourceLocation ARROW =
            ResourceLocation.fromNamespaceAndPath("drstone", "textures/gui/arrow.png");

    private static final ResourceLocation ARROW_FULL =
            ResourceLocation.fromNamespaceAndPath("drstone", "textures/gui/arrow_full.png");

    private static final ResourceLocation FLAME_EMPTY =
            ResourceLocation.fromNamespaceAndPath("drstone", "textures/gui/flame.png");

    private static final ResourceLocation FLAME_FULL =
            ResourceLocation.fromNamespaceAndPath("drstone", "textures/gui/flame_full.png");

    public CampfireScreen(CampfireMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    /* =========================
       TOOLTIP (noms des items)
       ========================= */
    private int noFireTicks = 0;
    private boolean closed = false;

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        boolean noFire = menu.getFlameProgress() <= 0f;
        boolean noFuel = menu.getSlot(0).getItem().isEmpty();

// ❗ fermeture UNIQUEMENT si flamme à 0 ET aucun combustible
        if (noFire && noFuel) {
            noFireTicks++;
        } else {
            noFireTicks = 0;
        }



        // ⏱️ délai de sécurité (2 secondes)
        if (!closed && noFireTicks > 40) {
            closed = true;
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
                return;
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }






    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {

        g.blit(BG, leftPos, topPos, 0, 0, 0, imageWidth, imageHeight, 176, 166);

        /* =========================
           SLOTS VISUELS UNIQUEMENT
           ========================= */
        drawSlot(g, 62, 26);    // entrée
        drawSlot(g, 134, 26);   // résultat
        drawSlot(g, 134, 48);   // secondaire

        drawArrow(g);
        drawFlames(g);
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.blit(
                SLOT,
                leftPos + x - 1,
                topPos + y - 1,
                0, 0, 0,
                18, 18,
                18, 18
        );
    }

    /* =========================
       ➡️ FLÈCHE (0 % → 100 %)
       ========================= */
    private void drawArrow(GuiGraphics g) {

        int width = 24;
        int height = 17;

        int x = 86;
        int y = 26 + (18 / 2) - (height / 2);

        g.blit(
                ARROW,
                leftPos + x,
                topPos + y,
                0, 0, 0,
                width, height,
                width, height
        );

        float progress = menu.getArrowProgress();
        int filled = (int) (progress * width);

        if (filled > 0) {
            g.blit(
                    ARROW_FULL,
                    leftPos + x,
                    topPos + y,
                    0, 0, 0,
                    filled,
                    height,
                    width,
                    height
            );
        }
    }

    /* =========================
       🔥 FLAMMES (100 % → 0 %)
       ========================= */
    private void drawFlames(GuiGraphics g) {

        int flameSize = 8;
        int columns = 2;
        int rows = 5;

        int startX = 8 + 9 - (columns * flameSize) / 2;
        int startY = 84 - 18 - 10;

        float progress = menu.getFlameProgress();

        int totalPixels = rows * flameSize;
        int visiblePixels = (int) (progress * totalPixels);

        for (int col = 0; col < columns; col++) {

            int pixelsLeft = visiblePixels;

            for (int row = 0; row < rows; row++) {

                if (pixelsLeft <= 0) break;

                int drawHeight = Math.min(flameSize, pixelsLeft);

                int x = startX + col * flameSize;
                int y = startY - row * flameSize + (flameSize - drawHeight);

                g.blit(
                        FLAME_FULL,
                        leftPos + x,
                        topPos + y,
                        0,
                        flameSize - drawHeight,
                        0,
                        flameSize,
                        drawHeight,
                        flameSize,
                        flameSize
                );

                pixelsLeft -= drawHeight;
            }
        }

        // fond vide
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {

                int x = startX + col * flameSize;
                int y = startY - row * flameSize;

                g.blit(
                        FLAME_EMPTY,
                        leftPos + x,
                        topPos + y,
                        0, 0, 0,
                        flameSize,
                        flameSize,
                        flameSize,
                        flameSize
                );
            }
        }
    }
}
