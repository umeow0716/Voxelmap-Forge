// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMapOptions;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiMinimapOptions extends GuiScreenMinimap
{
    private Screen parent;
    private IVoxelMap master;
    private static EnumOptionsMinimap[] relevantOptions;
    private final MapSettingsManager options;
    protected String screenTitle;
    
    public GuiMinimapOptions(final Screen parent, final IVoxelMap master) {
        this.screenTitle = "Minimap Options";
        this.parent = parent;
        this.master = master;
        this.options = master.getMapOptions();
    }
    
    public void init() {
        GuiMinimapOptions.relevantOptions = new EnumOptionsMinimap[] { EnumOptionsMinimap.COORDS, EnumOptionsMinimap.HIDE, EnumOptionsMinimap.LOCATION, EnumOptionsMinimap.SIZE, EnumOptionsMinimap.SQUARE, EnumOptionsMinimap.ROTATES, EnumOptionsMinimap.BEACONS, EnumOptionsMinimap.CAVEMODE };
        int var2 = 0;
        this.screenTitle = I18nUtils.getString("options.minimap.title", new Object[0]);
        for (int t = 0; t < GuiMinimapOptions.relevantOptions.length; ++t) {
            final EnumOptionsMinimap option = GuiMinimapOptions.relevantOptions[t];
            final GuiOptionButtonMinimap optionButton = new GuiOptionButtonMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, new TextComponent(this.options.getKeyText(option)), button -> this.optionClicked(button));
            this.addRenderableWidget(optionButton);
            if (option.equals(EnumOptionsMinimap.CAVEMODE)) {
                optionButton.active = this.options.cavesAllowed;
            }
            ++var2;
        }
        final Button radarOptionsButton = new Button(this.getWidth() / 2 - 155, this.getHeight() / 6 + 120 - 6, 150, 20, new TranslatableComponent("options.minimap.radar"), button -> this.getMinecraft().setScreen(new GuiRadarOptions(this, this.master)));
        radarOptionsButton.active = (this.master.getRadarOptions().radarAllowed || this.master.getRadarOptions().radarMobsAllowed || this.master.getRadarOptions().radarPlayersAllowed);
        this.addRenderableWidget(radarOptionsButton);
        this.addRenderableWidget(new Button(this.getWidth() / 2 + 5, this.getHeight() / 6 + 120 - 6, 150, 20, new TranslatableComponent("options.minimap.detailsperformance"), button -> this.getMinecraft().setScreen(new GuiMinimapPerformance(this, this.master))));
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 155, this.getHeight() / 6 + 144 - 6, 150, 20, new TranslatableComponent("options.controls"), button -> this.getMinecraft().setScreen(new GuiMinimapControls(this, this.master))));
        this.addRenderableWidget(new Button(this.getWidth() / 2 + 5, this.getHeight() / 6 + 144 - 6, 150, 20, new TranslatableComponent("options.minimap.worldmap"), button -> this.getMinecraft().setScreen(new GuiPersistentMapOptions(this, this.master))));
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parent)));
    }
    
    protected void optionClicked(final Button par1GuiButton) {
        final EnumOptionsMinimap option = ((GuiOptionButtonMinimap)par1GuiButton).returnEnumOptions();
        this.options.setOptionValue(option);
        par1GuiButton.setMessage(new TextComponent(this.options.getKeyText(option)));
        if (option == EnumOptionsMinimap.OLDNORTH) {
            this.master.getWaypointManager().setOldNorth(this.options.oldNorth);
        }
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
