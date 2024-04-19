// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

public class LiveGLBufferedImage extends GLBufferedImage
{
    public LiveGLBufferedImage(final int width, final int height, final int imageType) {
        super(width, height, imageType);
    }
    
    @Override
    public void write() {
        if (this.index == 0) {
            this.index = GLShim.glGenTextures();
        }
        this.buffer.clear();
        synchronized (this.bufferLock) {
            this.buffer.put(this.bytes);
        }
        this.buffer.position(0).limit(this.bytes.length);
        GLShim.glBindTexture(3553, this.index);
        GLShim.glTexParameteri(3553, 10241, 9728);
        GLShim.glTexParameteri(3553, 10240, 9728);
        GLShim.glTexParameteri(3553, 10242, 33071);
        GLShim.glTexParameteri(3553, 10243, 33071);
        GLShim.glTexParameteri(3553, 33169, 1);
        GLShim.glPixelStorei(3314, 0);
        GLShim.glPixelStorei(3316, 0);
        GLShim.glPixelStorei(3315, 0);
        GLShim.glTexImage2D(3553, 0, 6408, this.getWidth(), this.getHeight(), 0, 6408, 32821, this.buffer);
    }
    
    @Override
    public void setRGB(final int x, final int y, final int color24) {
        final int index = (x + y * this.getWidth()) * 4;
        synchronized (this.bufferLock) {
            final int alpha = color24 >> 24 & 0xFF;
            this.bytes[index] = -1;
            this.bytes[index + 1] = (byte)((color24 >> 0 & 0xFF) * alpha / 255);
            this.bytes[index + 2] = (byte)((color24 >> 8 & 0xFF) * alpha / 255);
            this.bytes[index + 3] = (byte)((color24 >> 16 & 0xFF) * alpha / 255);
        }
    }
}
