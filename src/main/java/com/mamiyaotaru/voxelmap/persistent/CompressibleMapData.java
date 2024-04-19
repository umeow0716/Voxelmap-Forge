// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import java.util.Arrays;
import java.util.zip.DataFormatException;
import com.google.common.collect.HashBiMap;
import java.io.IOException;
import com.mamiyaotaru.voxelmap.util.CompressionUtils;

import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.BiMap;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;

public class CompressibleMapData extends AbstractMapData
{
    public static final int DATABITS = 18;
    public static final int BYTESPERDATUM = 1;
    private byte[] data;
    private boolean isCompressed;
    private BiMap<BlockState, Integer> stateToInt;
    int count;
    private static byte[] compressedEmptyData;
    
    public CompressibleMapData(final int width, final int height) {
        this.isCompressed = false;
        this.stateToInt = null;
        this.count = 1;
        this.width = width;
        this.height = height;
        this.data = CompressibleMapData.compressedEmptyData;
        this.isCompressed = true;
    }
    
    @Override
    public int getHeight(final int x, final int z) {
        return this.getData(x, z, 0) & 0xFF;
    }
    
    @Override
    public BlockState getBlockstate(final int x, final int z) {
        final int id = (this.getData(x, z, 1) & 0xFF) << 8 | (this.getData(x, z, 2) & 0xFF);
        return this.getStateFromID(id);
    }
    
    @Override
    public int getBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getLight(final int x, final int z) {
        return this.getData(x, z, 3) & 0xFF;
    }
    
    @Override
    public int getOceanFloorHeight(final int x, final int z) {
        return this.getData(x, z, 4) & 0xFF;
    }
    
    @Override
    public BlockState getOceanFloorBlockstate(final int x, final int z) {
        final int id = (this.getData(x, z, 5) & 0xFF) << 8 | (this.getData(x, z, 6) & 0xFF);
        return this.getStateFromID(id);
    }
    
    @Override
    public int getOceanFloorBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getOceanFloorLight(final int x, final int z) {
        return this.getData(x, z, 7) & 0xFF;
    }
    
    @Override
    public int getTransparentHeight(final int x, final int z) {
        return this.getData(x, z, 8) & 0xFF;
    }
    
    @Override
    public BlockState getTransparentBlockstate(final int x, final int z) {
        final int id = (this.getData(x, z, 9) & 0xFF) << 8 | (this.getData(x, z, 10) & 0xFF);
        return this.getStateFromID(id);
    }
    
    @Override
    public int getTransparentBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getTransparentLight(final int x, final int z) {
        return this.getData(x, z, 11) & 0xFF;
    }
    
    @Override
    public int getFoliageHeight(final int x, final int z) {
        return this.getData(x, z, 12) & 0xFF;
    }
    
    @Override
    public BlockState getFoliageBlockstate(final int x, final int z) {
        final int id = (this.getData(x, z, 13) & 0xFF) << 8 | (this.getData(x, z, 14) & 0xFF);
        return this.getStateFromID(id);
    }
    
    @Override
    public int getFoliageBiomeTint(final int x, final int z) {
        return 0;
    }
    
    @Override
    public int getFoliageLight(final int x, final int z) {
        return this.getData(x, z, 15) & 0xFF;
    }
    
    @Override
    public int getBiomeID(final int x, final int z) {
        final int id = (this.getData(x, z, 16) & 0xFF) << 8 | (this.getData(x, z, 17) & 0xFF);
        return id;
    }
    
    private synchronized byte getData(final int x, final int z, final int bit) {
        if (this.isCompressed) {
            this.decompress();
        }
        final int index = x + z * this.width + this.width * this.height * bit;
        return this.data[index];
    }
    
    @Override
    public void setHeight(final int x, final int z, final int value) {
        this.setData(x, z, 0, (byte)value);
    }
    
    @Override
    public void setBlockstate(final int x, final int z, final BlockState blockState) {
        final int id = this.getIDFromState(blockState);
        this.setData(x, z, 1, (byte)(id >> 8));
        this.setData(x, z, 2, (byte)id);
    }
    
    @Override
    public void setBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setLight(final int x, final int z, final int value) {
        this.setData(x, z, 3, (byte)value);
    }
    
    @Override
    public void setOceanFloorHeight(final int x, final int z, final int value) {
        this.setData(x, z, 4, (byte)value);
    }
    
    @Override
    public void setOceanFloorBlockstate(final int x, final int z, final BlockState blockState) {
        final int id = this.getIDFromState(blockState);
        this.setData(x, z, 5, (byte)(id >> 8));
        this.setData(x, z, 6, (byte)id);
    }
    
    @Override
    public void setOceanFloorBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setOceanFloorLight(final int x, final int z, final int value) {
        this.setData(x, z, 7, (byte)value);
    }
    
    @Override
    public void setTransparentHeight(final int x, final int z, final int value) {
        this.setData(x, z, 8, (byte)value);
    }
    
    @Override
    public void setTransparentBlockstate(final int x, final int z, final BlockState blockState) {
        final int id = this.getIDFromState(blockState);
        this.setData(x, z, 9, (byte)(id >> 8));
        this.setData(x, z, 10, (byte)id);
    }
    
    @Override
    public void setTransparentBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setTransparentLight(final int x, final int z, final int value) {
        this.setData(x, z, 11, (byte)value);
    }
    
    @Override
    public void setFoliageHeight(final int x, final int z, final int value) {
        this.setData(x, z, 12, (byte)value);
    }
    
    @Override
    public void setFoliageBlockstate(final int x, final int z, final BlockState blockState) {
        final int id = this.getIDFromState(blockState);
        this.setData(x, z, 13, (byte)(id >> 8));
        this.setData(x, z, 14, (byte)id);
    }
    
    @Override
    public void setFoliageBiomeTint(final int x, final int z, final int value) {
    }
    
    @Override
    public void setFoliageLight(final int x, final int z, final int value) {
        this.setData(x, z, 15, (byte)value);
    }
    
    @Override
    public void setBiomeID(final int x, final int z, final int value) {
        this.setData(x, z, 16, (byte)(value >> 8));
        this.setData(x, z, 17, (byte)value);
    }
    
    private synchronized void setData(final int x, final int z, final int bit, final byte value) {
        if (this.isCompressed) {
            this.decompress();
        }
        final int index = x + z * this.width + this.width * this.height * bit;
        this.data[index] = value;
    }
    
    @Override
    public void moveX(final int offset) {
        synchronized (this.dataLock) {
            if (offset > 0) {
                System.arraycopy(this.data, offset * 18, this.data, 0, this.data.length - offset * 18);
            }
            else if (offset < 0) {
                System.arraycopy(this.data, 0, this.data, -offset * 18, this.data.length + offset * 18);
            }
        }
    }
    
    @Override
    public void moveZ(final int offset) {
        synchronized (this.dataLock) {
            if (offset > 0) {
                System.arraycopy(this.data, offset * this.width * 18, this.data, 0, this.data.length - offset * this.width * 18);
            }
            else if (offset < 0) {
                System.arraycopy(this.data, 0, this.data, -offset * this.width * 18, this.data.length + offset * this.width * 18);
            }
        }
    }
    
    public synchronized void setData(final byte[] is, final BiMap<BlockState, Integer> newStateToInt, final int version) {
        this.data = is;
        this.isCompressed = false;
        if (version < 2) {
            this.convertData();
        }
        this.stateToInt = newStateToInt;
        this.count = this.stateToInt.size();
    }
    
    private synchronized void convertData() {
        if (this.isCompressed) {
            this.decompress();
        }
        final byte[] newData = new byte[this.data.length];
        for (int x = 0; x < this.width; ++x) {
            for (int z = 0; z < this.height; ++z) {
                for (int bit = 0; bit < 18; ++bit) {
                    final int oldIndex = (x + z * this.width) * 18 + bit;
                    final int newIndex = x + z * this.width + this.width * this.height * bit;
                    newData[newIndex] = this.data[oldIndex];
                }
            }
        }
        this.data = newData;
    }
    
    public synchronized byte[] getData() {
        if (this.isCompressed) {
            this.decompress();
        }
        return this.data;
    }
    
    public synchronized void compress() {
        if (this.isCompressed) {
            return;
        }
        try {
            this.isCompressed = true;
            this.data = CompressionUtils.compress(this.data);
        }
        catch (final IOException ex) {}
    }
    
    private synchronized void decompress() {
        if (this.stateToInt == null) {
            this.stateToInt = HashBiMap.create();
        }
        if (!this.isCompressed) {
            return;
        }
        try {
            this.data = CompressionUtils.decompress(this.data);
            this.isCompressed = false;
        }
        catch (final IOException ex) {}
        catch (final DataFormatException ex2) {}
    }
    
    public synchronized boolean isCompressed() {
        return this.isCompressed;
    }
    
    private synchronized int getIDFromState(final BlockState blockState) {
        Integer id = (Integer)this.stateToInt.get((Object)blockState);
        if (id == null && blockState != null) {
            while (this.stateToInt.inverse().containsKey((Object)this.count)) {
                ++this.count;
            }
            id = this.count;
            this.stateToInt.put(blockState, id);
        }
        return id;
    }
    
    private BlockState getStateFromID(final int id) {
        return (BlockState)this.stateToInt.inverse().get((Object)id);
    }
    
    public BiMap<BlockState, Integer> getStateToInt() {
        return this.stateToInt = this.createKeyFromCurrentBlocks(this.stateToInt);
    }
    
    private BiMap<BlockState, Integer> createKeyFromCurrentBlocks(final BiMap<BlockState, Integer> oldMap) {
        this.count = 1;
        final BiMap<BlockState, Integer> newMap = HashBiMap.create();
        for (int x = 0; x < this.width; ++x) {
            for (int z = 0; z < this.height; ++z) {
                int oldID = (this.getData(x, z, 1) & 0xFF) << 8 | (this.getData(x, z, 2) & 0xFF);
                if (oldID != 0) {
                    final BlockState blockState = (BlockState)oldMap.inverse().get((Object)oldID);
                    Integer id = (Integer)newMap.get((Object)blockState);
                    if (id == null && blockState != null) {
                        while (newMap.inverse().containsKey((Object)this.count)) {
                            ++this.count;
                        }
                        id = this.count;
                        newMap.put(blockState, id);
                    }
                    this.setData(x, z, 1, (byte)(id >> 8));
                    this.setData(x, z, 2, (byte)(int)id);
                }
                oldID = ((this.getData(x, z, 5) & 0xFF) << 8 | (this.getData(x, z, 6) & 0xFF));
                if (oldID != 0) {
                    final BlockState blockState = (BlockState)oldMap.inverse().get((Object)oldID);
                    Integer id = (Integer)newMap.get((Object)blockState);
                    if (id == null && blockState != null) {
                        while (newMap.inverse().containsKey((Object)this.count)) {
                            ++this.count;
                        }
                        id = this.count;
                        newMap.put(blockState, id);
                    }
                    this.setData(x, z, 5, (byte)(id >> 8));
                    this.setData(x, z, 6, (byte)(int)id);
                }
                oldID = ((this.getData(x, z, 9) & 0xFF) << 8 | (this.getData(x, z, 10) & 0xFF));
                if (oldID != 0) {
                    final BlockState blockState = (BlockState)oldMap.inverse().get((Object)oldID);
                    Integer id = (Integer)newMap.get((Object)blockState);
                    if (id == null && blockState != null) {
                        while (newMap.inverse().containsKey((Object)this.count)) {
                            ++this.count;
                        }
                        id = this.count;
                        newMap.put(blockState, id);
                    }
                    this.setData(x, z, 9, (byte)(id >> 8));
                    this.setData(x, z, 10, (byte)(int)id);
                }
                oldID = ((this.getData(x, z, 13) & 0xFF) << 8 | (this.getData(x, z, 14) & 0xFF));
                if (oldID != 0) {
                    final BlockState blockState = (BlockState)oldMap.inverse().get((Object)oldID);
                    Integer id = (Integer)newMap.get((Object)blockState);
                    if (id == null && blockState != null) {
                        while (newMap.inverse().containsKey((Object)this.count)) {
                            ++this.count;
                        }
                        id = this.count;
                        newMap.put(blockState, id);
                    }
                    this.setData(x, z, 13, (byte)(id >> 8));
                    this.setData(x, z, 14, (byte)(int)id);
                }
            }
        }
        return newMap;
    }
    
    static {
        Arrays.fill(CompressibleMapData.compressedEmptyData = new byte[1179648], (byte)0);
        try {
            CompressibleMapData.compressedEmptyData = CompressionUtils.compress(CompressibleMapData.compressedEmptyData);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
