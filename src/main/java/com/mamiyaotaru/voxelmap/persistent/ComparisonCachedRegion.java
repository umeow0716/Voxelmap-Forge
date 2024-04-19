// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.util.MessageUtils;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import java.util.zip.ZipEntry;
import com.google.common.collect.BiMap;
import java.io.IOException;
import java.util.Properties;
import com.mamiyaotaru.voxelmap.util.BlockStateParser;
import com.google.common.collect.HashBiMap;
import java.util.Scanner;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mamiyaotaru.voxelmap.util.VoxelMapMutableBlockPos;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;

import net.minecraft.world.level.levelgen.Heightmap;

public class ComparisonCachedRegion
{
	private static final Minecraft game = Minecraft.getInstance();
    private IPersistentMap persistentMap;
    private String key;
    private Level world;
    private String subworldName;
    private String worldNamePathPart;
    private String subworldNamePathPart;
    private String dimensionNamePathPart;
    private boolean underground;
    private int x;
    private int z;
    private CompressibleMapData data;
    VoxelMapMutableBlockPos blockPos;
    private int loadedChunks;
    private boolean loaded;
    private boolean empty;
    
    public ComparisonCachedRegion(final IPersistentMap persistentMap, final String key, final ClientLevel world, final String worldName, final String subworldName, final int x, final int z) {
    	this.blockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.loadedChunks = 0;
        this.loaded = false;
        this.empty = true;
        this.data = new CompressibleMapData(256, 256);
        this.persistentMap = persistentMap;
        this.key = key;
        this.world = world;
        this.subworldName = subworldName;
        this.worldNamePathPart = TextUtils.scrubNameFile(worldName);
        if (subworldName != "") {
            this.subworldNamePathPart = TextUtils.scrubNameFile(subworldName) + "/";
        }
        final String dimensionName = AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(world).getStorageName();
        this.dimensionNamePathPart = TextUtils.scrubNameFile(dimensionName);
        final boolean knownUnderground = false;
        this.underground = ((!world.effects().forceBrightLightmap() && !world.dimensionType().hasSkyLight()) || world.dimensionType().hasCeiling() || knownUnderground);
        this.x = x;
        this.z = z;
    }
    
    public void loadCurrent() {
        this.loadedChunks = 0;
        for (int chunkX = 0; chunkX < 16; ++chunkX) {
            for (int chunkZ = 0; chunkZ < 16; ++chunkZ) {
                final LevelChunk chunk = this.world.getChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ);
                if (chunk != null && !chunk.isEmpty() && this.world.hasChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ) && !this.isChunkEmpty(this.world, chunk)) {
                    this.loadChunkData(chunk, chunkX, chunkZ);
                    ++this.loadedChunks;
                }
            }
        }
    }
    
    private boolean isChunkEmpty(final Level world, final LevelChunk chunk) {
        for (int t = 0; t < 16; ++t) {
            for (int s = 0; s < 16; ++s) {
                if (chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, t, s) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void loadChunkData(final LevelChunk chunk, final int chunkX, final int chunkZ) {
        for (int t = 0; t < 16; ++t) {
            for (int s = 0; s < 16; ++s) {
                this.persistentMap.getAndStoreData(this.data, this.world, chunk, this.blockPos, this.underground, this.x * 256, this.z * 256, chunkX * 16 + t, chunkZ * 16 + s);
            }
        }
    }
    
    public void loadStored() {
        try {
            final File cachedRegionFileDir = new File(game.gameDirectory, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
            cachedRegionFileDir.mkdirs();
            final File cachedRegionFile = new File(cachedRegionFileDir, "/" + this.key + ".zip");
            if (cachedRegionFile.exists()) {
                final FileInputStream fis = new FileInputStream(cachedRegionFile);
                final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
                final Scanner sc = new Scanner(zis);
                BiMap<BlockState, Integer> stateToInt = null;
                int version = 1;
                int total = 0;
                final byte[] decompressedByteData = new byte[this.data.getWidth() * this.data.getHeight() * 17 * 4];
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.getName().equals("data")) {
                        int count;
                        for (byte[] data = new byte[2048]; (count = zis.read(data, 0, 2048)) != -1 && count + total <= this.data.getWidth() * this.data.getHeight() * 17 * 4; total += count) {
                            System.arraycopy(data, 0, decompressedByteData, total, count);
                        }
                    }
                    if (ze.getName().equals("key")) {
                        stateToInt = HashBiMap.create();
                        while (sc.hasNextLine()) {
                            BlockStateParser.parseLine(sc.nextLine(), stateToInt);
                        }
                    }
                    if (ze.getName().equals("control")) {
                        final Properties properties = new Properties();
                        properties.load(zis);
                        final String versionString = properties.getProperty("version", "1");
                        try {
                            version = Integer.parseInt(versionString);
                        }
                        catch (final NumberFormatException e) {
                            version = 1;
                        }
                    }
                    zis.closeEntry();
                }
                if (total == this.data.getWidth() * this.data.getHeight() * 18 && stateToInt != null) {
                    final byte[] byteData = new byte[this.data.getWidth() * this.data.getHeight() * 18];
                    System.arraycopy(decompressedByteData, 0, byteData, 0, byteData.length);
                    this.data.setData(byteData, stateToInt, version);
                    this.empty = false;
                    this.loaded = true;
                }
                else {
                    System.out.println("failed to load data from " + cachedRegionFile.getPath());
                }
                sc.close();
                zis.close();
                fis.close();
            }
        }
        catch (final IOException e2) {
            System.err.println("Failed to load region file for " + this.x + "," + this.z + " in " + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
            e2.printStackTrace();
        }
    }
    
    public String getSubworldName() {
        return this.subworldName;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public CompressibleMapData getMapData() {
        return this.data;
    }
    
    public boolean isLoaded() {
        return this.loaded;
    }
    
    public boolean isEmpty() {
        return this.empty;
    }
    
    public int getLoadedChunks() {
        return this.loadedChunks;
    }
    
    public boolean isGroundAt(final int blockX, final int blockZ) {
        return this.isLoaded() && this.getHeightAt(blockX, blockZ) > 0;
    }
    
    public int getHeightAt(final int blockX, final int blockZ) {
        final int x = blockX - this.x * 256;
        final int z = blockZ - this.z * 256;
        int y = this.data.getHeight(x, z);
        if (this.underground && y == 255) {
            y = CommandUtils.getSafeHeight(blockX, 64, blockZ, this.world);
        }
        return y;
    }
    
    public int getSimilarityTo(final ComparisonCachedRegion candidate) {
        int compared = 0;
        int matched = 0;
        final CompressibleMapData candidateData = candidate.getMapData();
        for (int t = 0; t < 16; ++t) {
            for (int s = 0; s < 16; ++s) {
                int nonZeroHeights = 0;
                int nonZeroHeightsInCandidate = 0;
                int matchesInChunk = 0;
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        final int x = t * 16 + i;
                        final int z = s * 16 + j;
                        if (this.data.getHeight(x, z) == candidateData.getHeight(x, z) && this.data.getBlockstate(x, z) == candidateData.getBlockstate(x, z) && (this.data.getOceanFloorHeight(x, z) == 0 || (this.data.getOceanFloorHeight(x, z) == candidateData.getOceanFloorHeight(x, z) && this.data.getOceanFloorBlockstate(x, z) == candidateData.getOceanFloorBlockstate(x, z)))) {
                            ++matchesInChunk;
                        }
                        if (this.data.getHeight(x, z) != 0) {
                            ++nonZeroHeights;
                        }
                        if (candidateData.getHeight(x, z) != 0) {
                            ++nonZeroHeightsInCandidate;
                        }
                    }
                }
                if (nonZeroHeights != 0 && nonZeroHeightsInCandidate != 0) {
                    compared += 256;
                    matched += matchesInChunk;
                }
            }
        }
        MessageUtils.printDebug("compared: " + compared + ", matched: " + matched);
        if (compared >= 256) {
            return matched * 100 / compared;
        }
        return 0;
    }
}
