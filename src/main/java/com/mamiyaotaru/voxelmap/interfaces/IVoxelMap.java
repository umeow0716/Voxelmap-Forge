// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.WorldUpdateListener;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.MapSettingsManager;

public interface IVoxelMap
{
    MapSettingsManager getMapOptions();
    
    RadarSettingsManager getRadarOptions();
    
    PersistentMapSettingsManager getPersistentMapOptions();
    
    IMap getMap();
    
    IRadar getRadar();
    
    IColorManager getColorManager();
    
    IWaypointManager getWaypointManager();
    
    IDimensionManager getDimensionManager();
    
    IPersistentMap getPersistentMap();
    
    void setPermissions(final boolean p0, final boolean p1, final boolean p2, final boolean p3);
    
    void newSubWorldName(final String p0, final boolean p1);
    
    void newSubWorldHash(final String p0);
    
    ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();
    
    String getWorldSeed();
    
    void setWorldSeed(final String p0);
    
    void sendPlayerMessageOnMainThread(final String p0);
    
    WorldUpdateListener getWorldUpdateListener();
}
