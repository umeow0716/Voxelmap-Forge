// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import java.util.ArrayList;

public class WorldUpdateListener
{
    private ArrayList<IChangeObserver> chunkProcessors;
    
    public WorldUpdateListener() {
        this.chunkProcessors = new ArrayList<IChangeObserver>();
    }
    
    public void addListener(final IChangeObserver chunkProcessor) {
        this.chunkProcessors.add(chunkProcessor);
    }
    
    public void notifyObservers(final int chunkX, final int chunkZ) {
        for (final IChangeObserver chunkProcessor : this.chunkProcessors) {
            chunkProcessor.handleChangeInWorld(chunkX, chunkZ);
        }
    }
}
