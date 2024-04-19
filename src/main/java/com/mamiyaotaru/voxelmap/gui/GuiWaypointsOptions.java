// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionSliderMinimap;
import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class GuiWaypointsOptions extends GuiScreenMinimap
{
    private static final EnumOptionsMinimap[] relevantOptions;
    private final Screen parent;
    private final MapSettingsManager options;
    protected TranslatableComponent screenTitle;
    
    public GuiWaypointsOptions(final Screen parent, final MapSettingsManager options) {
        this.parent = parent;
        this.options = options;
    }
    
    public void init() {
        int var2 = 0;
        this.screenTitle = new TranslatableComponent("options.minimap.waypoints.title");
        for (int t = 0; t < GuiWaypointsOptions.relevantOptions.length; ++t) {
            final EnumOptionsMinimap option = GuiWaypointsOptions.relevantOptions[t];
            if (option.isFloat()) {
                float distance = this.options.getOptionFloatValue(option);
                if (distance < 0.0f) {
                    distance = 10001.0f;
                }
                distance = (distance - 50.0f) / 9951.0f;
                this.addRenderableWidget(new GuiOptionSliderMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, distance, this.options));
            }
            else {
                final GuiOptionButtonMinimap optionButton = new GuiOptionButtonMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, new TextComponent(this.options.getKeyText(option)), button -> this.optionClicked(button));
                this.addRenderableWidget(optionButton);
            }
            ++var2;
        }
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parent)));
    }
    
    protected void optionClicked(final Button par1GuiButton) {
        final EnumOptionsMinimap option = ((GuiOptionButtonMinimap)par1GuiButton).returnEnumOptions();
        this.options.setOptionValue(option);
        par1GuiButton.setMessage(new TextComponent(this.options.getKeyText(option)));
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    static {
        relevantOptions = new EnumOptionsMinimap[] { EnumOptionsMinimap.WAYPOINTDISTANCE, EnumOptionsMinimap.DEATHPOINTS };
    }
}
