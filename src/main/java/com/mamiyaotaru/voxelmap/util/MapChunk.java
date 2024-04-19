// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.chunk.LevelChunk;

public class MapChunk
{
	private static final Minecraft game = Minecraft.getInstance();;
    private int x;
    private int z;
    private LevelChunk chunk;
    private boolean isChanged;
    private boolean isLoaded;
    private boolean isSurroundedByLoaded;
    
    public MapChunk(final int x, final int z) {
        this.x = 0;
        this.z = 0;
        this.isChanged = false;
        this.isLoaded = false;
        this.isSurroundedByLoaded = false;
        this.x = x;
        this.z = z;
        this.chunk = game.level.getChunk(x, z);
        this.isLoaded = (this.chunk != null && !this.chunk.isEmpty() && game.level.hasChunk(x, z));
        this.isSurroundedByLoaded = false;
        this.isChanged = true;
    }
    
    public void checkIfChunkChanged(final IChangeObserver changeObserver) {
        if (this.hasChunkLoadedOrUnloaded() || this.isChanged) {
            this.isChanged = false;
            changeObserver.processChunk(this.chunk);
        }
    }
    
    private boolean hasChunkLoadedOrUnloaded() {
        boolean hasChanged = false;
        if (!this.isLoaded) {
            this.chunk = game.level.getChunk(this.x, this.z);
            if (this.chunk != null && !this.chunk.isEmpty() && game.level.hasChunk(this.x, this.z)) {
                this.isLoaded = true;
                hasChanged = true;
            }
        }
        else if (this.isLoaded && (this.chunk == null || this.chunk.isEmpty() || !game.level.hasChunk(this.x, this.z))) {
            this.isLoaded = false;
            hasChanged = true;
        }
        return hasChanged;
    }
    
    public void checkIfChunkBecameSurroundedByLoaded(final IChangeObserver changeObserver) {
        this.chunk = game.level.getChunk(this.x, this.z);
        this.isLoaded = (this.chunk != null && !this.chunk.isEmpty() && game.level.hasChunk(this.x, this.z));
        if (this.isLoaded) {
            final boolean formerSurroundedByLoaded = this.isSurroundedByLoaded;
            this.isSurroundedByLoaded = this.isSurroundedByLoaded();
            if (!formerSurroundedByLoaded && this.isSurroundedByLoaded) {
                changeObserver.processChunk(this.chunk);
            }
        }
        else {
            this.isSurroundedByLoaded = false;
        }
    }
    
    public boolean isSurroundedByLoaded() {
        this.chunk = game.level.getChunk(this.x, this.z);
        this.isLoaded = (this.chunk != null && !this.chunk.isEmpty() && game.level.hasChunk(this.x, this.z));
        boolean neighborsLoaded = this.isLoaded;
        for (int t = this.x - 1; t <= this.x + 1 && neighborsLoaded; ++t) {
            LevelChunk neighborChunk;
            for (int s = this.z - 1; s <= this.z + 1 && neighborsLoaded; neighborsLoaded = (neighborsLoaded && neighborChunk != null && !neighborChunk.isEmpty() && game.level.hasChunk(t, s)), ++s) {
                neighborChunk = game.level.getChunk(t, s);
            }
        }
        return neighborsLoaded;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public void setModified(final boolean isModified) {
        this.isChanged = isModified;
    }
}
