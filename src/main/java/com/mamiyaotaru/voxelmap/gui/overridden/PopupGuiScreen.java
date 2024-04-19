// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class PopupGuiScreen extends GuiScreenMinimap implements IPopupGuiScreen
{
    private ArrayList<Popup> popups;
    
    public PopupGuiScreen() {
        this.popups = new ArrayList<Popup>();
    }
    
    public void drawMap() {
    }
    
    @Override
    public void removed() {
    }
    
    public void createPopup(final int x, final int y, final int directX, final int directY, final ArrayList<Popup.PopupEntry> entries) {
        this.popups.add(new Popup(x, y, directX, directY, entries, this));
    }
    
    public void clearPopups() {
        this.popups.clear();
    }
    
    public boolean clickedPopup(final double x, final double y) {
        boolean clicked = false;
        final ArrayList<Popup> deadPopups = new ArrayList<Popup>();
        for (final Popup popup : this.popups) {
            final boolean clickedPopup = popup.clickedMe(x, y);
            if (!clickedPopup) {
                deadPopups.add(popup);
            }
            else if (popup.shouldClose()) {
                deadPopups.add(popup);
            }
            clicked = (clicked || clickedPopup);
        }
        this.popups.removeAll(deadPopups);
        return clicked;
    }
    
    @Override
    public boolean overPopup(final int x, final int y) {
        boolean over = false;
        for (final Popup popup : this.popups) {
            final boolean overPopup = popup.overMe(x, y);
            over = (over || overPopup);
        }
        return over;
    }
    
    @Override
    public boolean popupOpen() {
        return this.popups.size() > 0;
    }
    
    @Override
    public void render(final PoseStack matrixStack, final int x, final int y, final float dunno) {
        super.render(matrixStack, x, y, dunno);
        for (final Popup popup : this.popups) {
            popup.drawPopup(matrixStack, x, y);
        }
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        return !this.clickedPopup(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
