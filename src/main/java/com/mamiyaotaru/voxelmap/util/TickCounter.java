// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

public class TickCounter
{
    public static int tickCounter;
    
    public static void onTick(final boolean clock) {
        if (clock) {
            TickCounter.tickCounter = ((TickCounter.tickCounter == Integer.MAX_VALUE) ? 0 : (TickCounter.tickCounter + 1));
        }
    }
    
    static {
        TickCounter.tickCounter = 0;
    }
}
