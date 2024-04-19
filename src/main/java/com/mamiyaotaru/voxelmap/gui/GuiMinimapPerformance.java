// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiButtonText;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiMinimapPerformance extends GuiScreenMinimap
{
	private final Minecraft game = Minecraft.getInstance();
    private static final EnumOptionsMinimap[] relevantOptions;
    private GuiButtonText worldSeedButton;
    private GuiOptionButtonMinimap slimeChunksButton;
    private Screen parentScreen;
    protected String screenTitle;
    private MapSettingsManager options;
    IVoxelMap master;
    
    public GuiMinimapPerformance(final Screen par1GuiScreen, final IVoxelMap master) {
        this.screenTitle = "Details / Performance";
        this.parentScreen = par1GuiScreen;
        this.options = master.getMapOptions();
        this.master = master;
    }
    
    private int getLeftBorder() {
        return this.getWidth() / 2 - 155;
    }
    
    public void init() {
        this.screenTitle = I18nUtils.getString("options.minimap.detailsperformance", new Object[0]);
        this.game.keyboardHandler.setSendRepeatsToGui(true);
        final int leftBorder = this.getLeftBorder();
        int var2 = 0;
        for (int t = 0; t < GuiMinimapPerformance.relevantOptions.length; ++t) {
            final EnumOptionsMinimap option = GuiMinimapPerformance.relevantOptions[t];
            String text = this.options.getKeyText(option);
            if ((option == EnumOptionsMinimap.WATERTRANSPARENCY || option == EnumOptionsMinimap.BLOCKTRANSPARENCY || option == EnumOptionsMinimap.BIOMES) && !this.options.multicore && this.options.getOptionBooleanValue(option)) {
                text = "§c" + text;
            }
            final GuiOptionButtonMinimap optionButton = new GuiOptionButtonMinimap(leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, new TextComponent(text), button -> this.optionClicked(button));
            this.addRenderableWidget(optionButton);
            ++var2;
            if (optionButton.returnEnumOptions().equals(EnumOptionsMinimap.SLIMECHUNKS)) {
                this.slimeChunksButton = optionButton;
                this.slimeChunksButton.active = (this.getMinecraft().isLocalServer() || !this.master.getWorldSeed().equals(""));
            }
        }
        String worldSeedDisplay = this.master.getWorldSeed();
        if (worldSeedDisplay.equals("")) {
            worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown", new Object[0]);
        }
        final String buttonText = I18nUtils.getString("options.minimap.worldseed", new Object[0]) + ": " + worldSeedDisplay;
        (this.worldSeedButton = new GuiButtonText(this.getFontRenderer(), leftBorder + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), 150, 20, new TextComponent(buttonText), button -> this.worldSeedButton.setEditing(true))).setText(this.master.getWorldSeed());
        this.worldSeedButton.active = !this.getMinecraft().isLocalServer();
        this.addRenderableWidget(this.worldSeedButton);
        ++var2;
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parentScreen)));
    }
    
    @Override
    public void removed() {
        this.game.keyboardHandler.setSendRepeatsToGui(false);
    }
    
    protected void optionClicked(final Button par1GuiButton) {
        final EnumOptionsMinimap option = ((GuiOptionButtonMinimap)par1GuiButton).returnEnumOptions();
        this.options.setOptionValue(option);
        String perfBomb = "";
        if ((option == EnumOptionsMinimap.WATERTRANSPARENCY || option == EnumOptionsMinimap.BLOCKTRANSPARENCY || option == EnumOptionsMinimap.BIOMES) && !this.options.multicore && this.options.getOptionBooleanValue(option)) {
            perfBomb = "§c";
        }
        par1GuiButton.setMessage(new TextComponent(perfBomb + this.options.getKeyText(option)));
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        if (keysm == 258) {
            this.worldSeedButton.keyPressed(keysm, scancode, b);
        }
        if ((keysm == 257 || keysm == 335) && this.worldSeedButton.isEditing()) {
            this.newSeed();
        }
        return super.keyPressed(keysm, scancode, b);
    }
    
    public boolean charTyped(final char character, final int keycode) {
        final boolean OK = super.charTyped(character, keycode);
        if (character == '\r' && this.worldSeedButton.isEditing()) {
            this.newSeed();
        }
        return OK;
    }
    
    private void newSeed() {
        final String newSeed = this.worldSeedButton.getText();
        this.master.setWorldSeed(newSeed);
        String worldSeedDisplay = this.master.getWorldSeed();
        if (worldSeedDisplay.equals("")) {
            worldSeedDisplay = I18nUtils.getString("selectWorld.versionUnknown", new Object[0]);
        }
        final String buttonText = I18nUtils.getString("options.minimap.worldseed", new Object[0]) + ": " + worldSeedDisplay;
        this.worldSeedButton.setMessage(new TextComponent(buttonText));
        this.worldSeedButton.setText(this.master.getWorldSeed());
        this.master.getMap().forceFullRender(true);
        this.slimeChunksButton.active = (this.getMinecraft().isLocalServer() || !this.master.getWorldSeed().equals(""));
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    public void tick() {
        this.worldSeedButton.tick();
    }
    
    static {
        relevantOptions = new EnumOptionsMinimap[] { EnumOptionsMinimap.LIGHTING, EnumOptionsMinimap.TERRAIN, EnumOptionsMinimap.WATERTRANSPARENCY, EnumOptionsMinimap.BLOCKTRANSPARENCY, EnumOptionsMinimap.BIOMES, EnumOptionsMinimap.FILTERING, EnumOptionsMinimap.CHUNKGRID, EnumOptionsMinimap.BIOMEOVERLAY, EnumOptionsMinimap.SLIMECHUNKS };
    }
}
