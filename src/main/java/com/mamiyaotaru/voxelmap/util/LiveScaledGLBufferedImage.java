// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

public class LiveScaledGLBufferedImage extends LiveGLBufferedImage
{
    private int scale;
    
    public LiveScaledGLBufferedImage(final int width, final int height, final int imageType) {
        super(512, 512, imageType);
        this.scale = 1;
        this.scale = 512 / width;
    }
    
    @Override
    public void setRGB(final int x, final int y, final int color24) {
        final int alpha = color24 >> 24 & 0xFF;
        final byte r = (byte)((color24 >> 0 & 0xFF) * alpha / 255);
        final byte g = (byte)((color24 >> 8 & 0xFF) * alpha / 255);
        final byte b = (byte)((color24 >> 16 & 0xFF) * alpha / 255);
        synchronized (this.bufferLock) {
            for (int t = 0; t < this.scale; ++t) {
                for (int s = 0; s < this.scale; ++s) {
                    final int index = (x * this.scale + t + (y * this.scale + s) * this.getWidth()) * 4;
                    this.bytes[index] = -1;
                    this.bytes[index + 1] = r;
                    this.bytes[index + 2] = g;
                    this.bytes[index + 3] = b;
                }
            }
        }
    }
    
    @Override
    public void moveX(final int offset) {
        super.moveX(offset * this.scale);
    }
    
    @Override
    public void moveY(final int offset) {
        super.moveY(offset * this.scale);
    }
}
