// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;

public class MessageUtils
{
    private static boolean debug;
    
    public static void chatInfo(final String s) {
        AbstractVoxelMap.getInstance().sendPlayerMessageOnMainThread(s);
    }
    
    public static void printDebug(final String line) {
        if (MessageUtils.debug) {
            System.out.println(line);
        }
    }
    
    static {
        MessageUtils.debug = false;
    }
}
