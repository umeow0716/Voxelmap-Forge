// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

public interface IGLBufferedImage
{
    int getIndex();
    
    int getWidth();
    
    int getHeight();
    
    void baleet();
    
    void write();
    
    void blank();
    
    void setRGB(final int p0, final int p1, final int p2);
    
    void moveX(final int p0);
    
    void moveY(final int p0);
}
