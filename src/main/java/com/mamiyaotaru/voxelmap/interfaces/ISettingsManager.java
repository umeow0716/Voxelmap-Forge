// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;

public interface ISettingsManager
{
    String getKeyText(final EnumOptionsMinimap p0);
    
    void setOptionFloatValue(final EnumOptionsMinimap p0, final float p1);
    
    float getOptionFloatValue(final EnumOptionsMinimap p0);
}
