// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionSliderMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiPersistentMapOptions extends GuiScreenMinimap
{
    private final Screen parent;
    private static EnumOptionsMinimap[] relevantOptions;
    private final PersistentMapSettingsManager options;
    protected String screenTitle;
    protected String cacheSettings;
    protected String warning;
    private static EnumOptionsMinimap[] relevantOptions2;
    
    public GuiPersistentMapOptions(final Screen parent, final IVoxelMap master) {
        this.screenTitle = "Worldmap Options";
        this.cacheSettings = "Zoom/Cache Settings";
        this.warning = "Edit at your own risk";
        this.parent = parent;
        this.options = master.getPersistentMapOptions();
    }
    
    public void init() {
        GuiPersistentMapOptions.relevantOptions = new EnumOptionsMinimap[] { EnumOptionsMinimap.SHOWWAYPOINTS, EnumOptionsMinimap.SHOWWAYPOINTNAMES };
        this.screenTitle = I18nUtils.getString("options.worldmap.title", new Object[0]);
        this.cacheSettings = I18nUtils.getString("options.worldmap.cachesettings", new Object[0]);
        this.warning = I18nUtils.getString("options.worldmap.warning", new Object[0]);
        int var2 = 0;
        for (int t = 0; t < GuiPersistentMapOptions.relevantOptions.length; ++t) {
            final EnumOptionsMinimap option = GuiPersistentMapOptions.relevantOptions[t];
            this.addRenderableWidget(new GuiOptionButtonMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, new TextComponent(this.options.getKeyText(option)), button -> this.optionClicked(button)));
            ++var2;
        }
        GuiPersistentMapOptions.relevantOptions2 = new EnumOptionsMinimap[] { EnumOptionsMinimap.MINZOOM, EnumOptionsMinimap.MAXZOOM, EnumOptionsMinimap.CACHESIZE };
        var2 += 2;
        for (int t = 0; t < GuiPersistentMapOptions.relevantOptions2.length; ++t) {
            final EnumOptionsMinimap option = GuiPersistentMapOptions.relevantOptions2[t];
            if (option.isFloat()) {
                final float sValue = this.options.getOptionFloatValue(option);
                float fValue = 0.0f;
                switch (option) {
                    case MINZOOM: {
                        final float n = sValue;
                        this.options.getClass();
                        final float n2 = n + 3.0f;
                        this.options.getClass();
                        final int n3 = 5;
                        this.options.getClass();
                        fValue = n2 / (n3 + 3);
                        break;
                    }
                    case MAXZOOM: {
                        final float n4 = sValue;
                        this.options.getClass();
                        final float n5 = n4 + 3.0f;
                        this.options.getClass();
                        final int n6 = 5;
                        this.options.getClass();
                        fValue = n5 / (n6 + 3);
                        break;
                    }
                    case CACHESIZE: {
                        final float n7 = sValue;
                        this.options.getClass();
                        fValue = n7 / 5000.0f;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)");
                    }
                }
                this.addRenderableWidget(new GuiOptionSliderMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, fValue, this.options));
            }
            else {
                this.addRenderableWidget(new GuiOptionButtonMinimap(this.getWidth() / 2 - 155 + var2 % 2 * 160, this.getHeight() / 6 + 24 * (var2 >> 1), option, new TextComponent(this.options.getKeyText(option)), button -> this.optionClicked(button)));
            }
            ++var2;
        }
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, 200, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parent)));
        
//        int times = 0;
//        try {
//        	for (final Object buttonObj : this.getButtonList()) {
//        		times++;
//        		if(times > 100) break;
//            	if (buttonObj instanceof GuiOptionButtonMinimap) {
//                	final GuiOptionButtonMinimap button = (GuiOptionButtonMinimap) buttonObj;
//                	if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
//                    	continue;
//                	}
//                	button.active = this.options.showWaypoints;
//            	}
//        	}
//        } catch(Exception e) {
//        	e.printStackTrace();
//        }
    }
    
    protected void optionClicked(final Button par1GuiButton) {
        final EnumOptionsMinimap option = ((GuiOptionButtonMinimap)par1GuiButton).returnEnumOptions();
        this.options.setOptionValue(option);
        par1GuiButton.setMessage(new TextComponent(this.options.getKeyText(option)));
//        for (final Object buttonObj : this.getButtonList()) {
//            if (buttonObj instanceof GuiOptionButtonMinimap) {
//                final GuiOptionButtonMinimap button = (GuiOptionButtonMinimap)buttonObj;
//                if (!button.returnEnumOptions().equals(EnumOptionsMinimap.SHOWWAYPOINTNAMES)) {
//                    continue;
//                }
//                button.active = this.options.showWaypoints;
//            }
//        }
    }
    
    public void render(final PoseStack matrixStack, final int par1, final int par2, final float par3) {
        for (final Object buttonObj : this.getButtonList()) {
            if (buttonObj instanceof GuiOptionSliderMinimap) {
                final GuiOptionSliderMinimap slider = (GuiOptionSliderMinimap)buttonObj;
                final EnumOptionsMinimap option = slider.returnEnumOptions();
                final float sValue = this.options.getOptionFloatValue(option);
                float fValue = 0.0f;
                switch (option) {
                    case MINZOOM: {
                        final float n = sValue;
                        this.options.getClass();
                        final float n2 = n + 3.0f;
                        this.options.getClass();
                        final int n3 = 5;
                        this.options.getClass();
                        fValue = n2 / (n3 + 3);
                        break;
                    }
                    case MAXZOOM: {
                        final float n4 = sValue;
                        this.options.getClass();
                        final float n5 = n4 + 3.0f;
                        this.options.getClass();
                        final int n6 = 5;
                        this.options.getClass();
                        fValue = n5 / (n6 + 3);
                        break;
                    }
                    case CACHESIZE: {
                        final float n7 = sValue;
                        this.options.getClass();
                        fValue = n7 / 5000.0f;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + option.getName() + ". (possibly not a float value applicable to persistent map)");
                    }
                }
                if (this.getFocused() == slider) {
                    continue;
                }
                slider.setValue(fValue);
            }
        }
        super.drawMap(matrixStack);
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.cacheSettings, this.getWidth() / 2, this.getHeight() / 6 + 24, 16777215);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.warning, this.getWidth() / 2, this.getHeight() / 6 + 34, 16777215);
        super.render(matrixStack, par1, par2, par3);
    }
}
