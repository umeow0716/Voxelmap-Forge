// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.interfaces.ISettingsManager;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TextComponent;

public class GuiOptionSliderMinimap extends AbstractSliderButton
{
    private ISettingsManager options;
    private EnumOptionsMinimap option;
    
    public GuiOptionSliderMinimap(final int x, final int y, final EnumOptionsMinimap optionIn, final float sliderValue, final ISettingsManager options) {
        super(x, y, 150, 20, new TextComponent(options.getKeyText(optionIn)), (double)sliderValue);
        this.option = null;
        this.options = options;
        this.option = optionIn;
    }
    
    protected void updateMessage() {
        this.setMessage(new TextComponent(this.options.getKeyText(this.option)));
    }
    
    protected void applyValue() {
        this.options.setOptionFloatValue(this.option, (float)this.value);
    }
    
    public EnumOptionsMinimap returnEnumOptions() {
        return this.option;
    }
    
    public void setValue(final float value) {
        if (!this.isHovered) {
            this.value = value;
            this.updateMessage();
        }
    }
}
