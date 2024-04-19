// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import java.util.zip.DataFormatException;
import java.io.IOException;
import com.mamiyaotaru.voxelmap.util.CompressionUtils;
import java.util.Arrays;
import java.nio.ByteOrder;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import java.nio.ByteBuffer;
import java.util.HashMap;
import com.mamiyaotaru.voxelmap.interfaces.IGLBufferedImage;

public class CompressibleGLBufferedImage implements IGLBufferedImage
{
    private byte[] bytes;
    private int index;
    private int width;
    private int height;
    private Object bufferLock;
    private boolean isCompressed;
    private static HashMap<Integer, ByteBuffer> byteBuffers;
    private static ByteBuffer defaultSizeBuffer;
    private boolean compressNotDelete;
    
    public CompressibleGLBufferedImage(final int width, final int height, final int imageType) {
        this.index = 0;
        this.bufferLock = new Object();
        this.isCompressed = false;
        this.compressNotDelete = false;
        this.width = width;
        this.height = height;
        this.bytes = new byte[width * height * 4];
        this.compressNotDelete = AbstractVoxelMap.getInstance().getPersistentMapOptions().outputImages;
    }
    
    public byte[] getData() {
        if (this.isCompressed) {
            this.decompress();
        }
        return this.bytes;
    }
    
    @Override
    public int getIndex() {
        return this.index;
    }
    
    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    @Override
    public void baleet() {
        final int currentIndex = this.index;
        this.index = 0;
        if (currentIndex != 0 && RenderSystem.isOnRenderThreadOrInit()) {
            GLShim.glDeleteTextures(currentIndex);
        }
    }
    
    @Override
    public void write() {
        if (this.isCompressed) {
            this.decompress();
        }
        if (this.index == 0) {
            this.index = GLShim.glGenTextures();
        }
        ByteBuffer buffer = CompressibleGLBufferedImage.byteBuffers.get(this.width * this.height);
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(this.width * this.height * 4).order(ByteOrder.nativeOrder());
            CompressibleGLBufferedImage.byteBuffers.put(this.width * this.height, buffer);
        }
        buffer.clear();
        synchronized (this.bufferLock) {
            buffer.put(this.bytes);
        }
        buffer.position(0).limit(this.bytes.length);
        GLShim.glBindTexture(3553, this.index);
        GLShim.glTexParameteri(3553, 10241, 9728);
        GLShim.glTexParameteri(3553, 10240, 9728);
        GLShim.glTexParameteri(3553, 10242, 33071);
        GLShim.glTexParameteri(3553, 10243, 33071);
        GLShim.glPixelStorei(3314, 0);
        GLShim.glPixelStorei(3316, 0);
        GLShim.glPixelStorei(3315, 0);
        GLShim.glTexImage2D(3553, 0, 6408, this.getWidth(), this.getHeight(), 0, 6408, 32821, buffer);
        GLShim.glGenerateMipmap(3553);
        this.compress();
    }
    
    @Override
    public void blank() {
        if (this.isCompressed) {
            this.decompress();
        }
        Arrays.fill(this.bytes, (byte)0);
        this.write();
    }
    
    @Override
    public void setRGB(final int x, final int y, final int color24) {
        if (this.isCompressed) {
            this.decompress();
        }
        final int index = (x + y * this.getWidth()) * 4;
        synchronized (this.bufferLock) {
            final int alpha = color24 >> 24 & 0xFF;
            this.bytes[index] = -1;
            this.bytes[index + 1] = (byte)((color24 >> 0 & 0xFF) * alpha / 255);
            this.bytes[index + 2] = (byte)((color24 >> 8 & 0xFF) * alpha / 255);
            this.bytes[index + 3] = (byte)((color24 >> 16 & 0xFF) * alpha / 255);
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
    
    private synchronized void compress() {
        if (this.isCompressed) {
            return;
        }
        if (this.compressNotDelete) {
            try {
                this.bytes = CompressionUtils.compress(this.bytes);
            }
            catch (final IOException ex) {}
        }
        else {
            this.bytes = null;
        }
        this.isCompressed = true;
    }
    
    private synchronized void decompress() {
        if (!this.isCompressed) {
            return;
        }
        if (this.compressNotDelete) {
            try {
                this.bytes = CompressionUtils.decompress(this.bytes);
            }
            catch (final IOException ex) {}
            catch (final DataFormatException ex2) {}
        }
        else {
            this.bytes = new byte[this.width * this.height * 4];
            this.isCompressed = false;
        }
    }
    
    static {
        CompressibleGLBufferedImage.byteBuffers = new HashMap<Integer, ByteBuffer>(4);
        CompressibleGLBufferedImage.defaultSizeBuffer = ByteBuffer.allocateDirect(262144).order(ByteOrder.nativeOrder());
        CompressibleGLBufferedImage.byteBuffers.put(65536, CompressibleGLBufferedImage.defaultSizeBuffer);
    }
}
