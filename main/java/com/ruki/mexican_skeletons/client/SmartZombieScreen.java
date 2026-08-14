package com.ruki.mexican_skeletons.client;

import com.ruki.mexican_skeletons.menu.SmartZombieMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SmartZombieScreen
        extends AbstractContainerScreen<SmartZombieMenu> {

    public SmartZombieScreen(
            SmartZombieMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);

        // Temporary dimensions only.
        this.imageWidth = 230;
        this.imageHeight = 224;

        this.titleLabelX = 8;
        this.titleLabelY = 6;

        this.inventoryLabelX = SmartZombieMenu.PLAYER_X;
        this.inventoryLabelY = 128;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left = this.leftPos;
        int top = this.topPos;

        // Temporary ugly-but-functional backing panel.
        graphics.fill(
                left,
                top,
                left + this.imageWidth,
                top + this.imageHeight,
                0xFF202020
        );

        graphics.fill(
                left + 4,
                top + 4,
                left + this.imageWidth - 4,
                top + 120,
                0xFFC6C6C6
        );

        graphics.fill(
                left + 4,
                top + 124,
                left + this.imageWidth - 4,
                top + this.imageHeight - 4,
                0xFFC6C6C6
        );

        // Draw simple temporary slot boxes behind every logical slot.
        for (Slot slot : this.menu.slots) {
            int x = left + slot.x;
            int y = top + slot.y;

            graphics.fill(
                    x - 1,
                    y - 1,
                    x + 17,
                    y + 17,
                    0xFF373737
            );

            graphics.fill(
                    x,
                    y,
                    x + 16,
                    y + 16,
                    0xFF8B8B8B
            );
        }
    }
}
