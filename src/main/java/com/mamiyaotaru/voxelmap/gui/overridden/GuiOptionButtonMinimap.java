// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiOptionButtonMinimap extends Button
{
    private final EnumOptionsMinimap enumOptions;
    
    public GuiOptionButtonMinimap(final int x, final int y, final Component buttonText, final Button.OnPress press) {
        this(x, y, null, buttonText, press);
    }
    
    public GuiOptionButtonMinimap(final int x, final int y, final EnumOptionsMinimap par4EnumOptions, final Component buttonText, final Button.OnPress press) {
        super(x, y, 150, 20, buttonText, press);
        this.enumOptions = par4EnumOptions;
    }
    
    public EnumOptionsMinimap returnEnumOptions() {
        return this.enumOptions;
    }
}
