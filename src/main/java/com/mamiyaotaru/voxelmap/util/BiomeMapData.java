// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.Arrays;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;

import net.minecraft.world.level.block.state.BlockState;

public class BiomeMapData extends AbstractMapData
{
    public static final int DATABITS = 1;
    public static final int BYTESPERDATUM = 4;
    private int[] data;
    
    public BiomeMapData(final int width, final int height) {
        this.width = width;
        this.height = height;
        Arrays.fill(this.data = new int[width * height * 1], 0);
    }
    
    @Override
    public int getHeight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public BlockState getBlockstate(final int x, final int z) {
        return null;
    }
    
    @Override
    public int getBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getLight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getOceanFloorHeight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public BlockState getOceanFloorBlockstate(final int x, final int z) {
        return null;
    }
    
    @Override
    public int getOceanFloorBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getOceanFloorLight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getTransparentHeight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public BlockState getTransparentBlockstate(final int x, final int z) {
        return null;
    }
    
    @Override
    public int getTransparentBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getTransparentLight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getFoliageHeight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public BlockState getFoliageBlockstate(final int x, final int z) {
        return null;
    }
    
    @Override
    public int getFoliageBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getFoliageLight(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getBiomeID(final int x, final int z) {
        return this.getData(x, z, 0);
    }
    
    private int getData(final int x, final int z, final int bit) {
        final int index = (x + z * this.width) * 1 + bit;
        return this.data[index];
    }
    
    @Override
    public void setHeight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setBlockstate(final int x, final int z, final BlockState blockState) {
    }
    
    @Override
    public void setBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setLight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setOceanFloorHeight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setOceanFloorBlockstate(final int x, final int z, final BlockState blockState) {
    }
    
    @Override
    public void setOceanFloorBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setOceanFloorLight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setTransparentHeight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setTransparentBlockstate(final int x, final int z, final BlockState blockState) {
    }
    
    @Override
    public void setTransparentBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setTransparentLight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setFoliageHeight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setFoliageBlockstate(final int x, final int z, final BlockState blockState) {
    }
    
    @Override
    public void setFoliageBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setFoliageLight(final int x, final int z, final int value) {
    }
    
    @Override
    public void setBiomeID(final int x, final int z, final int value) {
        this.setData(x, z, 0, value);
    }
    
    private void setData(final int x, final int z, final int bit, final int value) {
        final int index = (x + z * this.width) * 1 + bit;
        this.data[index] = value;
    }
    
    @Override
    public void moveX(final int offset) {
        synchronized (this.dataLock) {
            if (offset > 0) {
                System.arraycopy(this.data, offset * 1, this.data, 0, this.data.length - offset * 1);
            }
            else if (offset < 0) {
                System.arraycopy(this.data, 0, this.data, -offset * 1, this.data.length + offset * 1);
            }
        }
    }
    
    @Override
    public void moveZ(final int offset) {
        synchronized (this.dataLock) {
            if (offset > 0) {
                System.arraycopy(this.data, offset * this.width * 1, this.data, 0, this.data.length - offset * this.width * 1);
            }
            else if (offset < 0) {
                System.arraycopy(this.data, 0, this.data, -offset * this.width * 1, this.data.length + offset * this.width * 1);
            }
        }
    }
    
    public void setData(final int[] is) {
        this.data = is;
    }
    
    public int[] getData() {
        return this.data;
    }
}
