// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mojang.blaze3d.platform.NativeImage;

public class ScaledMutableNativeImageBackedTexture extends MutableNativeImageBackedTexture
{
    private NativeImage image;
    private int scale;
    
    public ScaledMutableNativeImageBackedTexture(final int width, final int height, final boolean b) {
        super(512, 512, b);
        this.scale = 512 / width;
        this.image = this.getPixels();
        final String info = this.image.toString();
        final String pointerString = info.substring(info.indexOf("@") + 1, info.indexOf("]") - 1);
        Long.parseLong(pointerString);
    }
    
    @Override
    public void blank() {
    }
    
    @Override
    public void write() {
        this.upload();
    }
    
    @Override
    public int getWidth() {
        return this.image.getHeight();
    }
    
    @Override
    public int getHeight() {
        return this.image.getHeight();
    }
    
    @Override
    public int getIndex() {
        return this.getId();
    }
    
    @Override
    public void moveX(final int offset) {
        super.moveX(offset * this.scale);
    }
    
    @Override
    public void moveY(final int offset) {
        super.moveY(offset * this.scale);
    }
    
    @Override
    public void setRGB(final int x, final int y, final int color24) {
        final int alpha = color24 >> 24 & 0xFF;
        final byte a = -1;
        final byte r = (byte)((color24 >> 0 & 0xFF) * alpha / 255);
        final byte g = (byte)((color24 >> 8 & 0xFF) * alpha / 255);
        final byte b = (byte)((color24 >> 16 & 0xFF) * alpha / 255);
        final int color25 = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
        for (int t = 0; t < this.scale; ++t) {
            for (int s = 0; s < this.scale; ++s) {
                this.image.setPixelRGBA(x * this.scale + t, y * this.scale + s, color25);
            }
        }
    }
}
