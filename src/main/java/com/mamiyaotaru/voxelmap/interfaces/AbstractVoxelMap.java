// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

public abstract class AbstractVoxelMap implements IVoxelMap
{
    public static AbstractVoxelMap instance;
    
    public static AbstractVoxelMap getInstance() {
        return AbstractVoxelMap.instance;
    }
    
    static {
        AbstractVoxelMap.instance = null;
    }
}
