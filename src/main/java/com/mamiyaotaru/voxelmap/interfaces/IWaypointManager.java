// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.BackgroundImageInfo;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import java.util.TreeSet;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public interface IWaypointManager
{
    ArrayList<Waypoint> getWaypoints();
    
    void deleteWaypoint(final Waypoint p0);
    
    void saveWaypoints();
    
    void addWaypoint(final Waypoint p0);
    
    void handleDeath();
    
    void newWorld(final Level p0);
    
    void setConnectedRealm(final String p0);
    
    String getCurrentWorldName();
    
    TreeSet<String> getKnownSubworldNames();
    
    boolean receivedAutoSubworldName();
    
    boolean isMultiworld();
    
    void setSubworldName(final String p0, final boolean p1);
    
    void setSubworldHash(final String p0);
    
    void changeSubworldName(final String p0, final String p1);
    
    void deleteSubworld(final String p0);
    
    void setOldNorth(final boolean p0);
    
    String getCurrentSubworldDescriptor(final boolean p0);
    
    void renderWaypoints(final float p0, final PoseStack p1, final boolean p2, final boolean p3, final boolean p4, final boolean p5);
    
    void onResourceManagerReload(final ResourceManager p0);
    
    TextureAtlas getTextureAtlas();
    
    TextureAtlas getTextureAtlasChooser();
    
    void setHighlightedWaypoint(final Waypoint p0, final boolean p1);
    
    Waypoint getHighlightedWaypoint();
    
    String getWorldSeed();
    
    void setWorldSeed(final String p0);
    
    BackgroundImageInfo getBackgroundImageInfo();
}
