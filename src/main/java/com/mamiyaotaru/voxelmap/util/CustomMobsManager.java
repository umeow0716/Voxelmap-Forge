// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.ArrayList;

public class CustomMobsManager
{
    public static ArrayList<CustomMob> mobs;
    
    public static void add(final String type, final boolean enabled) {
        final CustomMob mob = getCustomMobByType(type);
        if (mob != null) {
            mob.enabled = enabled;
        }
        else {
            CustomMobsManager.mobs.add(new CustomMob(type, enabled));
        }
    }
    
    public static void add(final String type, final boolean isHostile, final boolean isNeutral) {
        final CustomMob mob = getCustomMobByType(type);
        if (mob != null) {
            mob.isHostile = isHostile;
            mob.isNeutral = isNeutral;
        }
        else {
            CustomMobsManager.mobs.add(new CustomMob(type, isHostile, isNeutral));
        }
    }
    
    public static CustomMob getCustomMobByType(final String type) {
        for (final CustomMob mob : CustomMobsManager.mobs) {
            if (mob.id.equals(type)) {
                return mob;
            }
        }
        return null;
    }
    
    static {
        CustomMobsManager.mobs = new ArrayList<CustomMob>();
    }
}
