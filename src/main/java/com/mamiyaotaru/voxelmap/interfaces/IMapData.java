// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.world.level.block.state.BlockState;

public interface IMapData
{
    public static final int DATABITS = 17;
    public static final int BYTESPERDATUM = 4;
    
    int getWidth();
    
    int getHeight();
    
    int getHeight(final int p0, final int p1);
    
    BlockState getBlockstate(final int p0, final int p1);
    
    int getBiomeTint(final int p0, final int p1);
    
    int getLight(final int p0, final int p1);
    
    int getOceanFloorHeight(final int p0, final int p1);
    
    BlockState getOceanFloorBlockstate(final int p0, final int p1);
    
    int getOceanFloorBiomeTint(final int p0, final int p1);
    
    int getOceanFloorLight(final int p0, final int p1);
    
    int getTransparentHeight(final int p0, final int p1);
    
    BlockState getTransparentBlockstate(final int p0, final int p1);
    
    int getTransparentBiomeTint(final int p0, final int p1);
    
    int getTransparentLight(final int p0, final int p1);
    
    int getFoliageHeight(final int p0, final int p1);
    
    BlockState getFoliageBlockstate(final int p0, final int p1);
    
    int getFoliageBiomeTint(final int p0, final int p1);
    
    int getFoliageLight(final int p0, final int p1);
    
    int getBiomeID(final int p0, final int p1);
    
    void setHeight(final int p0, final int p1, final int p2);
    
    void setBlockstate(final int p0, final int p1, final BlockState p2);
    
    void setBiomeTint(final int p0, final int p1, final int p2);
    
    void setLight(final int p0, final int p1, final int p2);
    
    void setOceanFloorHeight(final int p0, final int p1, final int p2);
    
    void setOceanFloorBlockstate(final int p0, final int p1, final BlockState p2);
    
    void setOceanFloorBiomeTint(final int p0, final int p1, final int p2);
    
    void setOceanFloorLight(final int p0, final int p1, final int p2);
    
    void setTransparentHeight(final int p0, final int p1, final int p2);
    
    void setTransparentBlockstate(final int p0, final int p1, final BlockState p2);
    
    void setTransparentBiomeTint(final int p0, final int p1, final int p2);
    
    void setTransparentLight(final int p0, final int p1, final int p2);
    
    void setFoliageHeight(final int p0, final int p1, final int p2);
    
    void setFoliageBlockstate(final int p0, final int p1, final BlockState p2);
    
    void setFoliageBiomeTint(final int p0, final int p1, final int p2);
    
    void setFoliageLight(final int p0, final int p1, final int p2);
    
    void setBiomeID(final int p0, final int p1, final int p2);
    
    void moveX(final int p0);
    
    void moveZ(final int p0);
}
