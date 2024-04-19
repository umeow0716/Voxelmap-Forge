// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.util.VoxelMapMutableBlockPos;
import com.mamiyaotaru.voxelmap.persistent.CachedRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public interface IPersistentMap extends IChangeObserver
{
    void newWorld(final ClientLevel p0);
    
    void onTick(final Minecraft p0);
    
    ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();
    
    void setLightMapArray(final int[] p0);
    
    void getAndStoreData(final AbstractMapData p0, final Level p1, final LevelChunk p2, final VoxelMapMutableBlockPos p3, final boolean p4, final int p5, final int p6, final int p7, final int p8);
    
    int getPixelColor(final AbstractMapData p0, final Level p1, final VoxelMapMutableBlockPos p2, final VoxelMapMutableBlockPos p3, final boolean p4, final int p5, final int p6, final int p7, final int p8, final int p9);
    
    CachedRegion[] getRegions(final int p0, final int p1, final int p2, final int p3);
    
    boolean isRegionLoaded(final int p0, final int p1);
    
    boolean isGroundAt(final int p0, final int p1);
    
    int getHeightAt(final int p0, final int p1);
    
    void purgeCachedRegions();
    
    void saveCachedRegions();
    
    void renameSubworld(final String p0, final String p1);
    
    PersistentMapSettingsManager getOptions();
    
    void compress();
}
