// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import java.util.concurrent.locks.ReentrantLock;

public class Share
{
    public static final ReentrantLock updateCloudsLock;
    
    public static boolean isOldNorth() {
        return AbstractVoxelMap.getInstance().getMapOptions().oldNorth;
    }
    
    static {
        updateCloudsLock = new ReentrantLock();
    }
}
