// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.Arrays;
import java.nio.ByteOrder;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import com.mamiyaotaru.voxelmap.interfaces.IGLBufferedImage;
import java.awt.image.BufferedImage;

public class GLBufferedImage extends BufferedImage implements IGLBufferedImage
{
    protected ByteBuffer buffer;
    protected byte[] bytes;
    protected int index;
    protected Object bufferLock;
    
    public GLBufferedImage(final int width, final int height, final int imageType) {
        super(width, height, imageType);
        this.index = 0;
        this.bufferLock = new Object();
        this.bytes = ((DataBufferByte)this.getRaster().getDataBuffer()).getData();
        this.buffer = ByteBuffer.allocateDirect(this.bytes.length).order(ByteOrder.nativeOrder());
    }
    
    @Override
    public int getIndex() {
        return this.index;
    }
    
    @Override
    public void baleet() {
        final int currentIndex = this.index;
        this.index = 0;
        if (currentIndex != 0) {
            GLShim.glDeleteTextures(currentIndex);
        }
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
        GLShim.glPixelStorei(3314, 0);
        GLShim.glPixelStorei(3316, 0);
        GLShim.glPixelStorei(3315, 0);
        GLShim.glTexImage2D(3553, 0, 6408, this.getWidth(), this.getHeight(), 0, 6408, 32821, this.buffer);
    }
    
    @Override
    public void blank() {
        Arrays.fill(this.bytes, (byte)0);
        this.write();
    }
    
    @Override
    public void setRGB(final int x, final int y, final int color24) {
        final int index = (x + y * this.getWidth()) * 4;
        synchronized (this.bufferLock) {
            this.bytes[index] = (byte)(color24 >> 24);
            this.bytes[index + 1] = (byte)(color24 >> 0);
            this.bytes[index + 2] = (byte)(color24 >> 8);
            this.bytes[index + 3] = (byte)(color24 >> 16);
        }
    }
    
    @Override
    public void moveX(final int offset) {
        synchronized (this.bufferLock) {
            if (offset > 0) {
                System.arraycopy(this.bytes, offset * 4, this.bytes, 0, this.bytes.length - offset * 4);
            }
            else if (offset < 0) {
                System.arraycopy(this.bytes, 0, this.bytes, -offset * 4, this.bytes.length + offset * 4);
            }
        }
    }
    
    @Override
    public void moveY(final int offset) {
        synchronized (this.bufferLock) {
            if (offset > 0) {
                System.arraycopy(this.bytes, offset * this.getWidth() * 4, this.bytes, 0, this.bytes.length - offset * this.getWidth() * 4);
            }
            else if (offset < 0) {
                System.arraycopy(this.bytes, 0, this.bytes, -offset * this.getWidth() * 4, this.bytes.length + offset * this.getWidth() * 4);
            }
        }
    }
}
