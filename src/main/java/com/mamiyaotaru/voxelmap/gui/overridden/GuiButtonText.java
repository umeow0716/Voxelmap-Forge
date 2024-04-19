// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class GuiButtonText extends Button
{
    private boolean editing;
    private EditBox textField;
    
    public GuiButtonText(final Font fontRenderer, final int x, final int y, final Component buttonText, final Button.OnPress press) {
        this(fontRenderer, x, y, 200, 20, buttonText, press);
    }
    
    public GuiButtonText(final Font fontRenderer, final int x, final int y, final int widthIn, final int heightIn, final Component buttonText, final Button.OnPress press) {
        super(x, y, widthIn, heightIn, buttonText, press);
        this.editing = false;
        this.textField = new EditBox(fontRenderer, x + 1, y + 1, widthIn - 2, heightIn - 2, (Component)null);
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        if (!this.editing) {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        else {
            this.textField.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        final boolean pressed = super.mouseClicked(mouseX, mouseY, mouseButton);
        this.setEditing(pressed);
        return pressed;
    }
    
    public void setEditing(final boolean editing) {
        this.editing = editing;
        if (editing) {
            this.setFocused(true);
        }
        this.textField.setFocus(editing);
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        boolean ok = false;
        if (this.editing) {
            if (keysm == 257 || keysm == 335 || keysm == 258) {
                this.setEditing(false);
            }
            else {
                ok = this.textField.keyPressed(keysm, scancode, b);
            }
        }
        else {
            ok = super.keyPressed(keysm, scancode, b);
        }
        return ok;
    }
    
    public boolean charTyped(final char character, final int keycode) {
        boolean ok = false;
        if (this.editing) {
            if (character == '\r') {
                this.setEditing(false);
            }
            else {
                ok = this.textField.charTyped(character, keycode);
            }
        }
        else {
            ok = super.charTyped(character, keycode);
        }
        return ok;
    }
    
    public boolean isEditing() {
        return this.editing;
    }
    
    public void tick() {
        this.textField.tick();
    }
    
    public void setText(final String textIn) {
        this.textField.setValue(textIn);
    }
    
    public String getText() {
        return this.textField.getValue();
    }
}
