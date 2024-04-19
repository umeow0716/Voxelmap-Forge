// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.textures;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import com.mamiyaotaru.voxelmap.util.GLShim;
import java.nio.IntBuffer;

public class TextureUtilLegacy
{
    private static final IntBuffer DATA_BUFFER;
    
    public static void deleteTexture(final int textureId) {
        GLShim.glDeleteTextures(textureId);
    }
    
    public static synchronized ByteBuffer createDirectByteBuffer(final int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
    
    public static IntBuffer createDirectIntBuffer(final int capacity) {
        return createDirectByteBuffer(capacity << 2).asIntBuffer();
    }
    
    public static BufferedImage readBufferedImage(final InputStream imageStream) throws IOException {
        if (imageStream == null) {
            return null;
        }
        BufferedImage bufferedimage;
        try {
            bufferedimage = ImageIO.read(imageStream);
        }
        finally {
            IOUtils.closeQuietly(imageStream);
        }
        return bufferedimage;
    }
    
    public static int uploadTextureImage(final int textureId, final BufferedImage texture) {
        return uploadTextureImageAllocate(textureId, texture, false, false);
    }
    
    public static int uploadTextureImageAllocate(final int textureId, final BufferedImage texture, final boolean blur, final boolean clamp) {
        allocateTexture(textureId, texture.getWidth(), texture.getHeight());
        return uploadTextureImageSub(textureId, texture, 0, 0, blur, clamp);
    }
    
    public static void allocateTexture(final int textureId, final int width, final int height) {
        allocateTextureImpl(textureId, 0, width, height);
    }
    
    public static void allocateTextureImpl(final int glTextureId, final int mipmapLevels, final int width, final int height) {
        bindTexture(glTextureId);
        if (mipmapLevels >= 0) {
            GLShim.glTexParameteri(3553, 33085, mipmapLevels);
            GLShim.glTexParameteri(3553, 33082, 0);
            GLShim.glTexParameteri(3553, 33083, mipmapLevels);
            GLShim.glTexParameterf(3553, 34049, 0.0f);
        }
        for (int i = 0; i <= mipmapLevels; ++i) {
            GLShim.glPixelStorei(3314, 0);
            GLShim.glPixelStorei(3316, 0);
            GLShim.glPixelStorei(3315, 0);
            GLShim.glTexImage2D(3553, i, 6408, width >> i, height >> i, 0, 32993, 33639, (IntBuffer)null);
        }
    }
    
    public static int uploadTextureImageSub(final int textureId, final BufferedImage p_110995_1_, final int p_110995_2_, final int p_110995_3_, final boolean p_110995_4_, final boolean p_110995_5_) {
        bindTexture(textureId);
        uploadTextureImageSubImpl(p_110995_1_, p_110995_2_, p_110995_3_, p_110995_4_, p_110995_5_);
        return textureId;
    }
    
    private static void uploadTextureImageSubImpl(final BufferedImage p_110993_0_, final int p_110993_1_, final int p_110993_2_, final boolean p_110993_3_, final boolean p_110993_4_) {
        final int i = p_110993_0_.getWidth();
        final int j = p_110993_0_.getHeight();
        final int k = 4194304 / i;
        final int[] aint = new int[k * i];
        setTextureBlurred(p_110993_3_);
        setTextureClamped(p_110993_4_);
        GLShim.glPixelStorei(3314, 0);
        GLShim.glPixelStorei(3316, 0);
        GLShim.glPixelStorei(3315, 0);
        for (int l = 0; l < i * j; l += i * k) {
            final int i2 = l / i;
            final int j2 = Math.min(k, j - i2);
            final int k2 = i * j2;
            p_110993_0_.getRGB(0, i2, i, j2, aint, 0, i);
            copyToBuffer(aint, k2);
            GLShim.glTexSubImage2D(3553, 0, p_110993_1_, p_110993_2_ + i2, i, j2, 32993, 33639, TextureUtilLegacy.DATA_BUFFER);
        }
    }
    
    public static void uploadTexture(final int glTextureId, final int[] zeros, final int currentImageWidth, final int currentImageHeight) {
        bindTexture(glTextureId);
        uploadTextureSub(0, zeros, currentImageWidth, currentImageHeight, 0, 0, false, false, false);
    }
    
    private static void copyToBuffer(final int[] p_110990_0_, final int p_110990_1_) {
        copyToBufferPos(p_110990_0_, 0, p_110990_1_);
    }
    
    private static void copyToBufferPos(final int[] imageData, final int p_110994_1_, final int p_110994_2_) {
        final int[] aint = imageData;
        TextureUtilLegacy.DATA_BUFFER.clear();
        TextureUtilLegacy.DATA_BUFFER.put(aint, p_110994_1_, p_110994_2_);
        TextureUtilLegacy.DATA_BUFFER.position(0).limit(p_110994_2_);
    }
    
    static void bindTexture(final int id) {
        GLShim.glBindTexture(3553, id);
    }
    
    public static void uploadTextureMipmap(final int[][] textureData, final int width, final int height, final int originX, final int originY, final boolean blurred, final boolean clamped) {
        for (int i = 0; i < textureData.length; ++i) {
            final int[] aint = textureData[i];
            uploadTextureSub(i, aint, width >> i, height >> i, originX >> i, originY >> i, blurred, clamped, textureData.length > 1);
        }
    }
    
    public static void setTextureClamped(final boolean clamped) {
        if (clamped) {
            GLShim.glTexParameteri(3553, 10242, 33071);
            GLShim.glTexParameteri(3553, 10243, 33071);
        }
        else {
            GLShim.glTexParameteri(3553, 10242, 10497);
            GLShim.glTexParameteri(3553, 10243, 10497);
        }
    }
    
    private static void setTextureBlurred(final boolean p_147951_0_) {
        setTextureBlurMipmap(p_147951_0_, false);
    }
    
    public static void setTextureBlurMipmap(final boolean blurred, final boolean mipmapped) {
        if (blurred) {
            GLShim.glTexParameteri(3553, 10241, mipmapped ? 9987 : 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
        }
        else {
            GLShim.glTexParameteri(3553, 10241, mipmapped ? 9986 : 9728);
            GLShim.glTexParameteri(3553, 10240, 9728);
        }
    }
    
    private static void uploadTextureSub(final int mipmapLevel, final int[] imageData, final int width, final int height, final int originX, final int originY, final boolean blurred, final boolean clamped, final boolean mipmapped) {
        final int maxRows = 4194304 / width;
        setTextureBlurMipmap(blurred, mipmapped);
        setTextureClamped(clamped);
        GLShim.glPixelStorei(3314, width);
        GLShim.glPixelStorei(3316, 0);
        GLShim.glPixelStorei(3315, 0);
        int rowsToCopy;
        for (int pos = 0; pos < width * height; pos += width * rowsToCopy) {
            final int rowsCopied = pos / width;
            rowsToCopy = Math.min(maxRows, height - rowsCopied);
            final int sizeOfCopy = width * rowsToCopy;
            copyToBufferPos(imageData, pos, sizeOfCopy);
            GLShim.glTexSubImage2D(3553, mipmapLevel, originX, originY + rowsCopied, width, rowsToCopy, 32993, 33639, TextureUtilLegacy.DATA_BUFFER);
        }
    }
    
    static {
        DATA_BUFFER = createDirectIntBuffer(4194304);
    }
}
