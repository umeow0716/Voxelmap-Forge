// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;

public class MapChunkCache
{
	private static final Minecraft game = Minecraft.getInstance();
    private int width;
    private int height;
    private LevelChunk lastCenterChunk;
    private MapChunk[] mapChunks;
    private int left;
    private int right;
    private int top;
    private int bottom;
    private boolean loaded;
    private IChangeObserver changeObserver;
    
    public MapChunkCache(final int width, final int height, final IChangeObserver changeObserver) {
        this.lastCenterChunk = null;
        this.left = 0;
        this.right = 0;
        this.top = 0;
        this.bottom = 0;
        this.loaded = false;
        this.width = width;
        this.height = height;
        this.mapChunks = new MapChunk[width * height];
        this.changeObserver = changeObserver;
    }
    
    public void centerChunks(final BlockPos blockPos) {
        final LevelChunk currentChunk = game.level.getChunkAt(blockPos);
        if (currentChunk != this.lastCenterChunk) {
            if (this.lastCenterChunk == null) {
                this.fillAllChunks(blockPos);
                this.lastCenterChunk = currentChunk;
                return;
            }
            final int middleX = this.width / 2;
            final int middleZ = this.height / 2;
            final int movedX = currentChunk.getPos().x - this.lastCenterChunk.getPos().x;
            final int movedZ = currentChunk.getPos().z - this.lastCenterChunk.getPos().z;
            if (Math.abs(movedX) < this.width && Math.abs(movedZ) < this.height && currentChunk.getLevel().equals(this.lastCenterChunk.getLevel())) {
                this.moveX(movedX);
                this.moveZ(movedZ);
                for (int z = (movedZ > 0) ? (this.height - movedZ) : 0; z < ((movedZ > 0) ? this.height : (-movedZ)); ++z) {
                    for (int x = 0; x < this.width; ++x) {
                        this.mapChunks[x + z * this.width] = new MapChunk(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                    }
                }
                for (int z = 0; z < this.height; ++z) {
                    for (int x = (movedX > 0) ? (this.width - movedX) : 0; x < ((movedX > 0) ? this.width : (-movedX)); ++x) {
                        this.mapChunks[x + z * this.width] = new MapChunk(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
                    }
                }
            }
            else {
                this.fillAllChunks(blockPos);
            }
            this.left = this.mapChunks[0].getX();
            this.top = this.mapChunks[0].getZ();
            this.right = this.mapChunks[this.mapChunks.length - 1].getX();
            this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
            this.lastCenterChunk = currentChunk;
        }
    }
    
    private void fillAllChunks(final BlockPos blockPos) {
        final ChunkAccess currentChunk = game.level.getChunk(blockPos);
        final int middleX = this.width / 2;
        final int middleZ = this.height / 2;
        for (int z = 0; z < this.height; ++z) {
            for (int x = 0; x < this.width; ++x) {
                this.mapChunks[x + z * this.width] = new MapChunk(currentChunk.getPos().x - (middleX - x), currentChunk.getPos().z - (middleZ - z));
            }
        }
        this.left = this.mapChunks[0].getX();
        this.top = this.mapChunks[0].getZ();
        this.right = this.mapChunks[this.mapChunks.length - 1].getX();
        this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
        this.loaded = true;
    }
    
    private void moveX(final int offset) {
        if (offset > 0) {
            System.arraycopy(this.mapChunks, offset, this.mapChunks, 0, this.mapChunks.length - offset);
        }
        else if (offset < 0) {
            System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset, this.mapChunks.length + offset);
        }
    }
    
    private void moveZ(final int offset) {
        if (offset > 0) {
            System.arraycopy(this.mapChunks, offset * this.width, this.mapChunks, 0, this.mapChunks.length - offset * this.width);
        }
        else if (offset < 0) {
            System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset * this.width, this.mapChunks.length + offset * this.width);
        }
    }
    
    public void checkIfChunksChanged() {
        if (!this.loaded) {
            return;
        }
        for (int z = this.height - 1; z >= 0; --z) {
            for (int x = 0; x < this.width; ++x) {
                this.mapChunks[x + z * this.width].checkIfChunkChanged(this.changeObserver);
            }
        }
    }
    
    public void checkIfChunksBecameSurroundedByLoaded() {
        if (!this.loaded) {
            return;
        }
        for (int z = this.height - 1; z >= 0; --z) {
            for (int x = 0; x < this.width; ++x) {
                this.mapChunks[x + z * this.width].checkIfChunkBecameSurroundedByLoaded(this.changeObserver);
            }
        }
    }
    
    public void registerChangeAt(final int chunkX, final int chunkZ) {
        if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
            final int arrayX = chunkX - this.left;
            final int arrayZ = chunkZ - this.top;
            final MapChunk mapChunk = this.mapChunks[arrayX + arrayZ * this.width];
            mapChunk.setModified(true);
        }
    }
    
    public boolean isChunkSurroundedByLoaded(final int chunkX, final int chunkZ) {
        if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
            final int arrayX = chunkX - this.left;
            final int arrayZ = chunkZ - this.top;
            final MapChunk mapChunk = this.mapChunks[arrayX + arrayZ * this.width];
            return mapChunk.isSurroundedByLoaded();
        }
        return false;
    }
}
