// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import java.util.Random;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.MapSettingsManager;

public class MapUtils
{
    private static MapSettingsManager options;
    private static IVoxelMap master;
    private static Random slimeRandom;
    private static int lastSlimeX;
    private static int lastSlimeZ;
    private static boolean isSlimeChunk;
    
    public static void reset() {
        MapUtils.master = AbstractVoxelMap.getInstance();
        MapUtils.options = MapUtils.master.getMapOptions();
        MapUtils.slimeRandom = null;
        MapUtils.lastSlimeX = -120000;
        MapUtils.lastSlimeZ = 10000;
        MapUtils.isSlimeChunk = false;
    }
    
    public static int doSlimeAndGrid(int color24, final int mcX, final int mcZ) {
        if (MapUtils.options.slimeChunks && isSlimeChunk(mcX, mcZ)) {
            color24 = ColorUtils.colorAdder(2097217280, color24);
        }
        if (MapUtils.options.chunkGrid) {
            if (mcX % 512 == 0 || mcZ % 512 == 0) {
                color24 = ColorUtils.colorAdder(2113863680, color24);
            }
            else if (mcX % 16 == 0 || mcZ % 16 == 0) {
                color24 = ColorUtils.colorAdder(2097152000, color24);
            }
        }
        return color24;
    }
    
    public static boolean isSlimeChunk(final int mcX, final int mcZ) {
        final int xPosition = mcX >> 4;
        final int zPosition = mcZ >> 4;
        final String seedString = AbstractVoxelMap.getInstance().getWorldSeed();
        if (!seedString.equals("")) {
            long seed = 0L;
            try {
                seed = Long.parseLong(seedString);
            }
            catch (final NumberFormatException e) {
                seed = seedString.hashCode();
            }
            if (xPosition != MapUtils.lastSlimeX || zPosition != MapUtils.lastSlimeZ || MapUtils.slimeRandom == null) {
                MapUtils.lastSlimeX = xPosition;
                MapUtils.lastSlimeZ = zPosition;
                MapUtils.slimeRandom = new Random(seed + xPosition * xPosition * 4987142 + xPosition * 5947611 + zPosition * zPosition * 4392871L + zPosition * 389711 ^ 0x3AD8025FL);
                MapUtils.isSlimeChunk = (MapUtils.slimeRandom.nextInt(10) == 0);
            }
        }
        return MapUtils.isSlimeChunk;
    }
    
    static {
        MapUtils.options = null;
        MapUtils.master = null;
        MapUtils.slimeRandom = null;
        MapUtils.lastSlimeX = 0;
        MapUtils.lastSlimeZ = 0;
        MapUtils.isSlimeChunk = false;
    }
}
