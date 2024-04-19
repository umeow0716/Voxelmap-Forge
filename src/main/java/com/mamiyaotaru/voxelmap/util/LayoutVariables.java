// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

public class LayoutVariables
{
    public int scScale;
    public int mapX;
    public int mapY;
    public double zoomScale;
    public double zoomScaleAdjusted;
    
    public LayoutVariables() {
        this.scScale = 0;
        this.mapX = 0;
        this.mapY = 0;
        this.zoomScale = 0.0;
        this.zoomScaleAdjusted = 0.0;
    }
    
    public void updateVars(final int scScale, final int mapX, final int mapY, final double zoomScale, final double zoomScaleAdjusted) {
        this.scScale = scScale;
        this.mapX = mapX;
        this.mapY = mapY;
        this.zoomScale = zoomScale;
        this.zoomScaleAdjusted = zoomScaleAdjusted;
    }
}
