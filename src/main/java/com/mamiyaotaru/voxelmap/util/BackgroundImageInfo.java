// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.awt.image.BufferedImage;

public class BackgroundImageInfo
{
    final BufferedImage image;
    public final int glid;
    public final int left;
    public final int top;
    private final int right;
    private final int bottom;
    public final int width;
    public final int height;
    public final float scale;
    
    public BackgroundImageInfo(final BufferedImage image, final int left, final int top, final float scale) {
        this(image, left, top, (int)(image.getWidth() * scale), (int)(image.getHeight() * scale));
    }
    
    public BackgroundImageInfo(final BufferedImage image, final int left, final int top, final int width, final int height) {
        this.image = image;
        this.glid = GLUtils.tex(image);
        this.left = left;
        this.top = top;
        this.right = left + width;
        this.bottom = top + height;
        this.width = width;
        this.height = height;
        this.scale = width / (float)image.getWidth();
    }
    
    public boolean isInRange(final int x, final int z) {
        return x >= this.left && x < this.right && z >= this.top && z < this.bottom;
    }
    
    public boolean isGroundAt(final int x, final int z) {
        final int imageX = (int)((x - this.left) / this.scale);
        final int imageY = (int)((z - this.top) / this.scale);
        if (imageX >= 0 && imageX < this.image.getWidth() && imageY >= 0 && imageY < this.image.getHeight()) {
            final int color = this.image.getRGB(imageX, imageY);
            return (color >> 24 & 0xFF) > 0;
        }
        return false;
    }
}
