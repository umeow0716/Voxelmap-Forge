// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import net.minecraft.core.BlockPos;

public class VoxelMapMutableBlockPos extends BlockPos
{
    public int x;
    public int y;
    public int z;
    
    public VoxelMapMutableBlockPos(final int x, final int y, final int z) {
        super(0, 0, 0);
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public VoxelMapMutableBlockPos withXYZ(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }
    
    public void setXYZ(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getZ() {
        return this.z;
    }
}
