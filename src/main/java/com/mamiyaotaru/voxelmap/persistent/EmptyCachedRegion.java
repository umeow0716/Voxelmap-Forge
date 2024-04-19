// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeNotifier;

import net.minecraft.world.level.chunk.LevelChunk;

public class EmptyCachedRegion extends CachedRegion
{
    @Override
    public void notifyOfActionableChange(final ISettingsAndLightingChangeNotifier notifier) {
    }
    
    @Override
    public void refresh(final boolean forceCompress) {
    }
    
    @Override
    public void handleChangedChunk(final LevelChunk chunk) {
    }
    
    @Override
    public void notifyOfThreadComplete(final AbstractNotifyingRunnable runnable) {
    }
    
    @Override
    public long getMostRecentView() {
        return 0L;
    }
    
    @Override
    public String getKey() {
        return "";
    }
    
    @Override
    public int getX() {
        return 0;
    }
    
    @Override
    public int getZ() {
        return 0;
    }
    
    @Override
    public int getWidth() {
        return 256;
    }
    
    @Override
    public int getGLID() {
        return 0;
    }
    
    @Override
    public CompressibleMapData getMapData() {
        return null;
    }
    
    @Override
    public boolean isLoaded() {
        return true;
    }
    
    @Override
    public boolean isEmpty() {
        return true;
    }
    
    @Override
    public boolean isGroundAt(final int blockX, final int blockZ) {
        return false;
    }
    
    @Override
    public int getHeightAt(final int blockX, final int blockZ) {
        return 0;
    }
    
    @Override
    public void cleanup() {
    }
    
    @Override
    public void compress() {
    }
}
