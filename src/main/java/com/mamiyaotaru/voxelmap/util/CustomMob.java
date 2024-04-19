// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

public class CustomMob
{
    public String id;
    public boolean enabled;
    public boolean isHostile;
    public boolean isNeutral;
    
    public CustomMob(final String id, final boolean enabled) {
        this.id = "notLoaded";
        this.enabled = true;
        this.isHostile = false;
        this.isNeutral = false;
        this.id = id;
        this.enabled = enabled;
    }
    
    public CustomMob(final String id, final boolean isHostile, final boolean isNeutral) {
        this.id = "notLoaded";
        this.enabled = true;
        this.isHostile = false;
        this.isNeutral = false;
        this.id = id;
        this.isHostile = isHostile;
        this.isNeutral = isNeutral;
    }
}
