// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class PopupGuiButton extends Button
{
    IPopupGuiScreen parentScreen;
    
    public PopupGuiButton(final int buttonId, final int x, final int y, final Component buttonText, final Button.OnPress press, final IPopupGuiScreen parentScreen) {
        this(x, y, 200, 20, buttonText, press, parentScreen);
    }
    
    public PopupGuiButton(final int x, final int y, final int widthIn, final int heightIn, final Component buttonText, final Button.OnPress press, final IPopupGuiScreen parentScreen) {
        super(x, y, widthIn, heightIn, buttonText, press);
        this.parentScreen = parentScreen;
    }
    
    public void render(final PoseStack matrixStack, int mouseX, int mouseY, final float partialTicks) {
        final boolean canHover = !this.parentScreen.overPopup(mouseX, mouseY);
        if (!canHover) {
            mouseX = 0;
            mouseY = 0;
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
