// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiMinimapControls extends GuiScreenMinimap
{
    private Screen parentScreen;
    protected String screenTitle;
    private MapSettingsManager options;
    public KeyMapping buttonId;
    
    public GuiMinimapControls(final Screen par1GuiScreen, final IVoxelMap master) {
        this.screenTitle = "Controls";
        this.buttonId = null;
        this.parentScreen = par1GuiScreen;
        this.options = master.getMapOptions();
    }
    
    private int getLeftBorder() {
        return this.getWidth() / 2 - 155;
    }
    
    public void init() {
        final int left = this.getLeftBorder();
        for (int t = 0; t < this.options.keyBindings.length; ++t) {
            final int id = t;
            this.addRenderableWidget(new Button(left + t % 2 * 160, this.getHeight() / 6 + 24 * (t >> 1), 70, 20, this.options.getKeybindDisplayString(t), button -> this.controlButtonClicked(id)));
        }
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parentScreen)));
        this.screenTitle = I18nUtils.getString("controls.minimap.title", new Object[0]);
    }
    
    protected void controlButtonClicked(final int id) {
        for (int buttonListIndex = 0; buttonListIndex < this.options.keyBindings.length; ++buttonListIndex) {
            ((Button)this.getButtonList().get(buttonListIndex)).setMessage(this.options.getKeybindDisplayString(buttonListIndex));
        }
        this.buttonId = this.options.keyBindings[id];
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        if (this.buttonId != null) {
            this.options.setKeyMapping(this.buttonId, InputConstants.Type.MOUSE.getOrCreate(mouseButton));
            this.buttonId = null;
            KeyMapping.resetMapping();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        if (this.buttonId != null) {
            if (keysm == 256) {
                this.options.setKeyMapping(this.buttonId, InputConstants.UNKNOWN);
            }
            else {
                this.options.setKeyMapping(this.buttonId, InputConstants.getKey(keysm, scancode));
            }
            this.buttonId = null;
            KeyMapping.resetMapping();
            return true;
        }
        return super.keyPressed(keysm, scancode, b);
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        final int leftBorder = this.getLeftBorder();
        for (int keyCounter = 0; keyCounter < this.options.keyBindings.length; ++keyCounter) {
            boolean keycodeCollision = false;
            final KeyMapping keyBinding = this.options.keyBindings[keyCounter];
            for (int compareKeyCounter = 0; compareKeyCounter < this.options.game.options.keyMappings.length; ++compareKeyCounter) {
                if (compareKeyCounter < this.options.keyBindings.length) {
                    final KeyMapping compareBinding = this.options.keyBindings[compareKeyCounter];
                    if (keyBinding != compareBinding && keyBinding.equals(compareBinding)) {
                        keycodeCollision = true;
                        break;
                    }
                }
                if (compareKeyCounter < this.options.game.options.keyMappings.length) {
                    final KeyMapping compareBinding = this.options.game.options.keyMappings[compareKeyCounter];
                    if (keyBinding != compareBinding && keyBinding.equals(compareBinding)) {
                        keycodeCollision = true;
                        break;
                    }
                }
            }
            if (this.buttonId == this.options.keyBindings[keyCounter]) {
                ((Button)this.getButtonList().get(keyCounter)).setMessage(new TextComponent("> ").append(new TextComponent("???").copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
            }
            else if (keycodeCollision) {
                ((Button)this.getButtonList().get(keyCounter)).setMessage(this.options.getKeybindDisplayString(keyCounter).copy().withStyle(ChatFormatting.RED));
            }
            else {
                ((Button)this.getButtonList().get(keyCounter)).setMessage(this.options.getKeybindDisplayString(keyCounter));
            }
            drawString(matrixStack, this.getFontRenderer(), this.options.getKeyMappingDescription(keyCounter), leftBorder + keyCounter % 2 * 160 + 70 + 6, this.getHeight() / 6 + 24 * (keyCounter >> 1) + 7, -1);
        }
        drawCenteredString(matrixStack, this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind1", new Object[0]), this.getWidth() / 2, this.getHeight() / 6 + 115, 16777215);
        drawCenteredString(matrixStack, this.getFontRenderer(), I18nUtils.getString("controls.minimap.unbind2", new Object[0]), this.getWidth() / 2, this.getHeight() / 6 + 129, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
