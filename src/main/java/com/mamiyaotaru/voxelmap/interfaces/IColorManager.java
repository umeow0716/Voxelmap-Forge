// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.VoxelMapMutableBlockPos;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.image.BufferedImage;

public interface IColorManager
{
    void onResourceManagerReload(final ResourceManager p0);
    
    BufferedImage getColorPicker();
    
    BufferedImage getBlockImage(final BlockState p0, final ItemStack p1, final Level p2, final float p3, final float p4);
    
    boolean checkForChanges();
    
    int getBlockColorWithDefaultTint(final VoxelMapMutableBlockPos p0, final int p1);
    
    int getBlockColor(final VoxelMapMutableBlockPos p0, final int p1, final int p2);
    
    void setSkyColor(final int p0);
    
    int getAirColor();
    
    int getBiomeTint(final AbstractMapData p0, final Level p1, final BlockState p2, final int p3, final VoxelMapMutableBlockPos p4, final VoxelMapMutableBlockPos p5, final int p6, final int p7);
}
