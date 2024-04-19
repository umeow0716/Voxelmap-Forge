// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import java.util.ArrayList;

public interface IDimensionManager
{
    ArrayList<DimensionContainer> getDimensions();
    
    DimensionContainer getDimensionContainerByWorld(final Level p0);
    
    DimensionContainer getDimensionContainerByIdentifier(final String p0);
    
    void enteredWorld(final Level p0);
    
    void populateDimensions(final Level p0);
    
    DimensionContainer getDimensionContainerByResourceLocation(final ResourceLocation p0);
}
