// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.world.level.chunk.LevelChunk;

public interface IChangeObserver
{
    void handleChangeInWorld(final int p0, final int p1);
    
    void processChunk(final LevelChunk p0);
}