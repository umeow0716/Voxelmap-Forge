// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.DynamicTexture;

public class MutableNativeImageBackedTexture extends DynamicTexture
{
    private Object bufferLock;
    private NativeImage image;
    private long pointer;
    
    public MutableNativeImageBackedTexture(final NativeImage image) {
        super(image);
        this.bufferLock = new Object();
        this.image = image;
        final String info = image.toString();
        final String pointerString = info.substring(info.indexOf("(") + 1, info.indexOf("]") - 1);
        this.pointer = Long.parseLong(pointerString);
    }
    
    public MutableNativeImageBackedTexture(final int width, final int height, final boolean b) {
        super(width, height, b);
        this.bufferLock = new Object();
        this.image = this.getPixels();
        final String info = this.image.toString();
        final String pointerString = info.substring(info.indexOf("@") + 1, info.indexOf("]") - 1);
        this.pointer = Long.parseLong(pointerString);
    }
    
    public void blank() {
    }
    
    public void write() {
        this.upload();
    }
    
    public int getWidth() {
        return this.image.getHeight();
    }
    
    public int getHeight() {
        return this.image.getHeight();
    }
    
    public int getIndex() {
        return this.getId();
    }
    
    public void moveX(final int offset) {
        synchronized (this.bufferLock) {
            final int size = this.image.getHeight() * this.image.getWidth() * 4;
            if (offset > 0) {
                MemoryUtil.memCopy(this.pointer + offset * 4, this.pointer, (long)(size - offset * 4));
            }
            else if (offset < 0) {
                MemoryUtil.memCopy(this.pointer, this.pointer - offset * 4, (long)(size + offset * 4));
            }
        }
    }
    
    public void moveY(final int offset) {
        synchronized (this.bufferLock) {
            final int size = this.image.getHeight() * this.image.getWidth() * 4;
            final int width = this.image.getWidth();
            if (offset > 0) {
                MemoryUtil.memCopy(this.pointer + offset * width * 4, this.pointer, (long)(size - offset * width * 4));
            }
            else if (offset < 0) {
                MemoryUtil.memCopy(this.pointer, this.pointer - offset * width * 4, (long)(size + offset * width * 4));
            }
        }
    }
    
    public void setRGB(final int x, final int y, final int color24) {
        final int alpha = color24 >> 24 & 0xFF;
        final byte a = -1;
        final byte r = (byte)((color24 >> 0 & 0xFF) * alpha / 255);
        final byte g = (byte)((color24 >> 8 & 0xFF) * alpha / 255);
        final byte b = (byte)((color24 >> 16 & 0xFF) * alpha / 255);
        final int color25 = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
        this.image.setPixelRGBA(x, y, color25);
    }
}
