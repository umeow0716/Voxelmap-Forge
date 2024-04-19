// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.Arrays;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;

import net.minecraft.world.level.block.state.BlockState;

public class FullMapData extends AbstractMapData
{
    public static final int DATABITS = 17;
    public static final int BYTESPERDATUM = 4;
    private int[] data;
    
    public FullMapData(final int width, final int height) {
        this.width = width;
        this.height = height;
        Arrays.fill(this.data = new int[width * height * 17], 0);
    }
    
    public void blank() {
        Arrays.fill(this.data, 0);
    }
    
    @Override
    public int getHeight(final int x, final int z) {
        return this.getData(x, z, 0);
    }
    
    public int getBlockstateID(final int x, final int z) {
        return this.getData(x, z, 1);
    }
    
    @Override
    public BlockState getBlockstate(final int x, final int z) {
        return this.getStateFromID(this.getData(x, z, 1));
    }
    
    @Override
    public int getBiomeTint(final int x, final int z) {
        return this.getData(x, z, 2);
    }
    
    @Override
    public int getLight(final int x, final int z) {
        return this.getData(x, z, 3);
    }
    
    @Override
    public int getOceanFloorHeight(final int x, final int z) {
        return this.getData(x, z, 4);
    }
    
    public int getOceanFloorBlockstateID(final int x, final int z) {
        return this.getData(x, z, 5);
    }
    
    @Override
    public BlockState getOceanFloorBlockstate(final int x, final int z) {
        return this.getStateFromID(this.getData(x, z, 5));
    }
    
    @Override
    public int getOceanFloorBiomeTint(final int x, final int z) {
        return this.getData(x, z, 6);
    }
    
    @Override
    public int getOceanFloorLight(final int x, final int z) {
        return this.getData(x, z, 7);
    }
    
    @Override
    public int getTransparentHeight(final int x, final int z) {
        return this.getData(x, z, 8);
    }
    
    public int getTransparentBlockstateID(final int x, final int z) {
        return this.getData(x, z, 9);
    }
    
    @Override
    public BlockState getTransparentBlockstate(final int x, final int z) {
        return this.getStateFromID(this.getData(x, z, 9));
    }
    
    @Override
    public int getTransparentBiomeTint(final int x, final int z) {
        return this.getData(x, z, 10);
    }
    
    @Override
    public int getTransparentLight(final int x, final int z) {
        return this.getData(x, z, 11);
    }
    
    @Override
    public int getFoliageHeight(final int x, final int z) {
        return this.getData(x, z, 12);
    }
    
    public int getFoliageBlockstateID(final int x, final int z) {
        return this.getData(x, z, 13);
    }
    
    @Override
    public BlockState getFoliageBlockstate(final int x, final int z) {
        return this.getStateFromID(this.getData(x, z, 13));
    }
    
    @Override
    public int getFoliageBiomeTint(final int x, final int z) {
        return this.getData(x, z, 14);
    }
    
    @Override
    public int getFoliageLight(final int x, final int z) {
        return this.getData(x, z, 15);
    }
    
    @Override
    public int getBiomeID(final int x, final int z) {
        return this.getData(x, z, 16);
    }
    
    private int getData(final int x, final int z, final int bit) {
        final int index = (x + z * this.width) * 17 + bit;
        return this.data[index];
    }
    
    @Override
    public void setHeight(final int x, final int z, final int value) {
        this.setData(x, z, 0, value);
    }
    
    public void setBlockstateID(final int x, final int z, final int id) {
        this.setData(x, z, 1, id);
    }
    
    @Override
    public void setBlockstate(final int x, final int z, final BlockState blockState) {
        this.setData(x, z, 1, this.getIDFromState(blockState));
    }
    
    @Override
    public void setBiomeTint(final int x, final int z, final int value) {
        this.setData(x, z, 2, value);
    }
    
    @Override
    public void setLight(final int x, final int z, final int value) {
        this.setData(x, z, 3, value);
    }
    
    @Override
    public void setOceanFloorHeight(final int x, final int z, final int value) {
        this.setData(x, z, 4, value);
    }
    
    public void setOceanFloorBlockstateID(final int x, final int z, final int id) {
        this.setData(x, z, 5, id);
    }
    
    @Override
    public void setOceanFloorBlockstate(final int x, final int z, final BlockState blockState) {
        this.setData(x, z, 5, this.getIDFromState(blockState));
    }
    
    @Override
    public void setOceanFloorBiomeTint(final int x, final int z, final int value) {
        this.setData(x, z, 6, value);
    }
    
    @Override
    public void setOceanFloorLight(final int x, final int z, final int value) {
        this.setData(x, z, 7, value);
    }
    
    @Override
    public void setTransparentHeight(final int x, final int z, final int value) {
        this.setData(x, z, 8, value);
    }
    
    public void setTransparentBlockstateID(final int x, final int z, final int id) {
        this.setData(x, z, 9, id);
    }
    
    @Override
    public void setTransparentBlockstate(final int x, final int z, final BlockState blockState) {
        this.setData(x, z, 9, this.getIDFromState(blockState));
    }
    
    @Override
    public void setTransparentBiomeTint(final int x, final int z, final int value) {
        this.setData(x, z, 10, value);
    }
    
    @Override
    public void setTransparentLight(final int x, final int z, final int value) {
        this.setData(x, z, 11, value);
    }
    
    @Override
    public void setFoliageHeight(final int x, final int z, final int value) {
        this.setData(x, z, 12, value);
    }
    
    public void setFoliageBlockstateID(final int x, final int z, final int id) {
        this.setData(x, z, 13, id);
    }
    
    @Override
    public void setFoliageBlockstate(final int x, final int z, final BlockState blockState) {
        this.setData(x, z, 13, this.getIDFromState(blockState));
    }
    
    @Override
    public void setFoliageBiomeTint(final int x, final int z, final int value) {
        this.setData(x, z, 14, value);
    }
    
    @Override
    public void setFoliageLight(final int x, final int z, final int value) {
        this.setData(x, z, 15, value);
    }
    
    @Override
    public void setBiomeID(final int x, final int z, final int value) {
        this.setData(x, z, 16, value);
    }
    
    private void setData(final int x, final int z, final int bit, final int value) {
        final int index = (x + z * this.width) * 17 + bit;
        this.data[index] = value;
    }
    
    @Override
    public void moveX(final int offset) {
        synchronized (this.dataLock) {
            if (offset > 0) {
                System.arraycopy(this.data, offset * 17, this.data, 0, this.data.length - offset * 17);
            }
            else if (offset < 0) {
                System.arraycopy(this.data, 0, this.data, -offset * 17, this.data.length + offset * 17);
            }
        }
    }
    
    @Override
    public void moveZ(final int offset) {
        synchronized (this.dataLock) {
            if (offset > 0) {
                System.arraycopy(this.data, offset * this.width * 17, this.data, 0, this.data.length - offset * this.width * 17);
            }
            else if (offset < 0) {
                System.arraycopy(this.data, 0, this.data, -offset * this.width * 17, this.data.length + offset * this.width * 17);
            }
        }
    }
    
    public void setData(final int[] is) {
        this.data = is;
    }
    
    public int[] getData() {
        return this.data;
    }
    
    private int getIDFromState(final BlockState blockState) {
        return BlockRepository.getStateId(blockState);
    }
    
    private BlockState getStateFromID(final int id) {
        return BlockRepository.getStateById(id);
    }
}
