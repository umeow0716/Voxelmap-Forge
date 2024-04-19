// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiRadarOptions extends GuiScreenMinimap
{
    private static final EnumOptionsMinimap[] relevantOptionsFull;
    private static final EnumOptionsMinimap[] relevantOptionsSimple;
    private static EnumOptionsMinimap[] relevantOptions;
    private final Screen parent;
    private final RadarSettingsManager options;
    protected TranslatableComponent screenTitle;
    
    public GuiRadarOptions(final Screen parent, final IVoxelMap master) {
        this.parent = parent;
        this.options = master.getRadarOptions();
    }
    
    public void init() {
        this.getButtonList().clear();
        this.children().clear();
        int var2 = 0;
        this.screenTitle = new TranslatableComponent("options.minimap.radar.title");
        final int radarMode = this.options.radarMode;
        this.options.getClass();
        if (radarMode == 2) {
            GuiRadarOptions.relevantOptions = GuiRadarOptions.relevantOptionsFull;
        }
        else {
            GuiRadarOptions.relevantOptions = GuiRadarOptions.relevantOptionsSimple;
        }
        for (int t = 0; t < GuiRadarOptions.relevantOptions.length; ++t) {
            final EnumOptionsMinimap option = GuiRadarOptions.relevantOptions[t];
            final GuiOptionButtonMinimap optionButton = new GuiOptionButtonMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, new TextComponent(this.options.getKeyText(option)), button -> this.optionClicked(button));
            this.addRenderableWidget(optionButton);
            ++var2;
        }
        for (final Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                final GuiOptionButtonMinimap button = (GuiOptionButtonMinimap)buttonObj;
                if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWRADAR)) {
                    button.active = this.options.showRadar;
                }
                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERS)) {
                    button.active = (button.active && (this.options.radarAllowed || this.options.radarPlayersAllowed));
                }
                else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWNEUTRALS) || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWHOSTILES)) {
                    button.active = (button.active && (this.options.radarAllowed || this.options.radarMobsAllowed));
                }
                else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERHELMETS) || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERNAMES)) {
                    button.active = (button.active && this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed));
                }
                else {
                    if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWMOBHELMETS)) {
                        continue;
                    }
                    button.active = (button.active && (this.options.showNeutrals || this.options.showHostiles) && (this.options.radarAllowed || this.options.radarMobsAllowed));
                }
            }
        }
        final int radarMode2 = this.options.radarMode;
        this.options.getClass();
        if (radarMode2 == 2) {
            this.addRenderableWidget(new Button(this.getWidth() / 2 - 155, this.getHeight() / 6 + 144 - 6, 150, 20, new TranslatableComponent("options.minimap.radar.selectmobs"), button -> this.getMinecraft().setScreen((Screen)new GuiMobs(this, this.options))));
        }
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parent)));
    }
    
    protected void optionClicked(final Button buttonClicked) {
        final EnumOptionsMinimap option = ((GuiOptionButtonMinimap)buttonClicked).returnEnumOptions();
        this.options.setOptionValue(option);
        if (((GuiOptionButtonMinimap)buttonClicked).returnEnumOptions().equals(EnumOptionsMinimap.RADARMODE)) {
            this.init();
            return;
        }
        buttonClicked.setMessage(new TextComponent(this.options.getKeyText(option)));
        for (final Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionButtonMinimap) {
                final GuiOptionButtonMinimap button = (GuiOptionButtonMinimap)buttonObj;
                if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWRADAR)) {
                    button.active = this.options.showRadar;
                }
                if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERS)) {
                    button.active = (button.active && (this.options.radarAllowed || this.options.radarPlayersAllowed));
                }
                else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWNEUTRALS) || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWHOSTILES)) {
                    button.active = (button.active && (this.options.radarAllowed || this.options.radarMobsAllowed));
                }
                else if (button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERHELMETS) || button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWPLAYERNAMES)) {
                    button.active = (button.active && this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed));
                }
                else {
                    if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWMOBHELMETS)) {
                        continue;
                    }
                    button.active = (button.active && (this.options.showNeutrals || this.options.showHostiles) && (this.options.radarAllowed || this.options.radarMobsAllowed));
                }
            }
        }
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    static {
        relevantOptionsFull = new EnumOptionsMinimap[] { EnumOptionsMinimap.SHOWRADAR, EnumOptionsMinimap.RADARMODE, EnumOptionsMinimap.SHOWHOSTILES, EnumOptionsMinimap.SHOWNEUTRALS, EnumOptionsMinimap.SHOWPLAYERS, EnumOptionsMinimap.SHOWPLAYERNAMES, EnumOptionsMinimap.SHOWPLAYERHELMETS, EnumOptionsMinimap.SHOWMOBHELMETS, EnumOptionsMinimap.RADARFILTERING, EnumOptionsMinimap.RADAROUTLINES };
        relevantOptionsSimple = new EnumOptionsMinimap[] { EnumOptionsMinimap.SHOWRADAR, EnumOptionsMinimap.RADARMODE, EnumOptionsMinimap.SHOWHOSTILES, EnumOptionsMinimap.SHOWNEUTRALS, EnumOptionsMinimap.SHOWPLAYERS, EnumOptionsMinimap.SHOWFACING };
    }
}
