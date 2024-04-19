// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mojang.blaze3d.platform.TextureUtil;
import java.awt.image.BufferedImage;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.NativeImage;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL11;
import java.nio.IntBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;

public class GLUtils
{
    private static Tesselator tessellator;
    private static BufferBuilder vertexBuffer;
    public static TextureManager textureManager;
    public static RenderTarget frameBuffer;
    public static int fboID;
    public static int rboID;
    public static int fboTextureID;
    public static int depthTextureID;
    private static int previousFBOID;
    private static int previousFBOIDREAD;
    private static int previousFBOIDDRAW;
    public static boolean hasAlphaBits;
    public static final int fboSize = 512;
    public static final int fboRad = 256;
    private static final IntBuffer dataBuffer;
    
    public static void setupFrameBuffer() {
        GLUtils.previousFBOID = GL11.glGetInteger(36006);
        GLUtils.fboID = GL30.glGenFramebuffers();
        GLUtils.fboTextureID = GL11.glGenTextures();
        final int width = 512;
        final int height = 512;
        GL30.glBindFramebuffer(36160, GLUtils.fboID);
        final ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);
        GL11.glBindTexture(3553, GLUtils.fboTextureID);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5120, byteBuffer);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, GLUtils.fboTextureID, 0);
        GL30.glBindRenderbuffer(36161, GLUtils.rboID = GL30.glGenRenderbuffers());
        GL30.glRenderbufferStorage(36161, 33190, width, height);
        GL30.glFramebufferRenderbuffer(36160, 36096, 36161, GLUtils.rboID);
        GL30.glBindRenderbuffer(36161, 0);
        checkFramebufferStatus();
        GL30.glBindFramebuffer(36160, GLUtils.previousFBOID);
        GlStateManager._bindTexture(0);
    }
    
    public static void setupFrameBufferUsingMinecraft() {
        GLUtils.frameBuffer = (RenderTarget) new TextureTarget(512, 512, true, Minecraft.ON_OSX);
        GLUtils.fboID = GLUtils.frameBuffer.frameBufferId;
        GLUtils.fboTextureID = GLUtils.frameBuffer.getColorTextureId();
    }
    
    public static void setupFrameBufferUsingMinecraftUnrolled() {
        // RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GLUtils.fboID = GL30.glGenFramebuffers();
        GLUtils.fboTextureID = GL11.glGenTextures();
        GL11.glBindTexture(3553, GLUtils.depthTextureID = GL11.glGenTextures());
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexParameteri(3553, 34892, 0);
        GL11.glTexImage2D(3553, 0, 6402, 512, 512, 0, 6402, 5126, (IntBuffer)null);
        GL11.glBindTexture(3553, GLUtils.fboTextureID);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexImage2D(3553, 0, 32856, 512, 512, 0, 6408, 5121, (IntBuffer)null);
        GL30.glBindFramebuffer(36160, GLUtils.fboID);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, GLUtils.fboTextureID, 0);
        GL30.glFramebufferTexture2D(36160, 36096, 3553, GLUtils.depthTextureID, 0);
        checkFramebufferStatus();
        GlStateManager._clearColor(1.0f, 1.0f, 1.0f, 0.0f);
        int i = 16384;
        GlStateManager._clearDepth(1.0);
        i |= 0x100;
        GlStateManager._clear(i, Minecraft.ON_OSX);
        GlStateManager._glBindFramebuffer(36160, 0);
        GlStateManager._bindTexture(0);
    }
    
    public static void checkFramebufferStatus() {
        final int i = GL30.glCheckFramebufferStatus(36160);
        if (i == 36053) {
            return;
        }
        if (i == 36054) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
        }
        if (i == 36055) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
        }
        if (i == 36059) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
        }
        if (i == 36060) {
            throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
        }
        throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
    }
    
    public static void bindFrameBuffer() {
        GLUtils.previousFBOID = GL11.glGetInteger(36006);
        GLUtils.previousFBOIDREAD = GL11.glGetInteger(36010);
        GLUtils.previousFBOIDDRAW = GL11.glGetInteger(36006);
        GL30.glBindFramebuffer(36160, GLUtils.fboID);
        GL30.glBindFramebuffer(36008, GLUtils.fboID);
        GL30.glBindFramebuffer(36009, GLUtils.fboID);
    }
    
    public static void unbindFrameBuffer() {
        GL30.glBindFramebuffer(36160, GLUtils.previousFBOID);
        GL30.glBindFramebuffer(36008, GLUtils.previousFBOIDREAD);
        GL30.glBindFramebuffer(36009, GLUtils.previousFBOIDDRAW);
    }
    
    public static void setMap(final int x, final int y) {
        setMap((float)x, (float)y, 128);
    }
    
    public static void setMapWithScale(final int x, final int y, final float scale) {
        setMap((float)x, (float)y, (int)(128.0f * scale));
    }
    
    public static void setMap(final float x, final float y, final int imageSize) {
        final float scale = imageSize / 4.0f;
        ldrawthree(x - scale, y + scale, 1.0, 0.0f, 1.0f);
        ldrawthree(x + scale, y + scale, 1.0, 1.0f, 1.0f);
        ldrawthree(x + scale, y - scale, 1.0, 1.0f, 0.0f);
        ldrawthree(x - scale, y - scale, 1.0, 0.0f, 0.0f);
    }
    
    public static void setMap(final Sprite icon, final float x, final float y, final float imageSize) {
        final float halfWidth = imageSize / 4.0f;
        ldrawthree(x - halfWidth, y + halfWidth, 1.0, icon.getMinU(), icon.getMaxV());
        ldrawthree(x + halfWidth, y + halfWidth, 1.0, icon.getMaxU(), icon.getMaxV());
        ldrawthree(x + halfWidth, y - halfWidth, 1.0, icon.getMaxU(), icon.getMinV());
        ldrawthree(x - halfWidth, y - halfWidth, 1.0, icon.getMinU(), icon.getMinV());
    }
    
    public static int tex(final BufferedImage paramImg) {
        final int glid = TextureUtil.generateTextureId();
        final int width = paramImg.getWidth();
        final int height = paramImg.getHeight();
        final int[] imageData = new int[width * height];
        paramImg.getRGB(0, 0, width, height, imageData, 0, width);
        GLShim.glBindTexture(3553, glid);
        GLUtils.dataBuffer.clear();
        GLUtils.dataBuffer.put(imageData, 0, width * height);
        GLUtils.dataBuffer.position(0).limit(width * height);
        GLShim.glTexParameteri(3553, 10241, 9729);
        GLShim.glTexParameteri(3553, 10240, 9729);
        GLShim.glPixelStorei(3314, 0);
        GLShim.glPixelStorei(3316, 0);
        GLShim.glPixelStorei(3315, 0);
        GLShim.glTexImage2D(3553, 0, 6408, width, height, 0, 32993, 33639, GLUtils.dataBuffer);
        return glid;
    }
    
    public static void img(final String paramStr) {
        GLUtils.textureManager.bindForSetup(new ResourceLocation(paramStr));
    }
    
    public static void img2(final String paramStr) {
        RenderSystem.setShaderTexture(0, new ResourceLocation(paramStr));
    }
    
    public static void img(final ResourceLocation paramResourceLocation) {
        GLUtils.textureManager.bindForSetup(paramResourceLocation);
    }
    
    public static void img2(final ResourceLocation paramResourceLocation) {
        RenderSystem.setShaderTexture(0, paramResourceLocation);
    }
    
    public static void disp(final int paramInt) {
        GLShim.glBindTexture(3553, paramInt);
    }
    
    public static void disp2(final int paramInt) {
        RenderSystem.setShaderTexture(0, paramInt);
    }
    
    public static void register(final ResourceLocation resourceLocation, final AbstractTexture image) {
        GLUtils.textureManager.register(resourceLocation, image);
    }
    
    public static NativeImage nativeImageFromBufferedImage(final BufferedImage base) {
        final int glid = tex(base);
        final NativeImage nativeImage = new NativeImage(base.getWidth(), base.getHeight(), false);
        RenderSystem.bindTexture(glid);
        nativeImage.downloadTexture(0, false);
        return nativeImage;
    }
    
    public static void drawPre() {
        GLUtils.vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }
    
    public static void drawPre(final VertexFormat vertexFormat) {
        GLUtils.vertexBuffer.begin(VertexFormat.Mode.QUADS, vertexFormat);
    }
    
    public static void drawPost() {
        GLUtils.tessellator.end();
    }
    
    public static void glah(final int g) {
        GLShim.glDeleteTextures(g);
    }
    
    public static void ldrawone(final int x, final int y, final double z, final float u, final float v) {
        GLUtils.vertexBuffer.vertex((double)x, (double)y, z).uv(u, v).endVertex();
    }
    
    public static void ldrawtwo(final double x, final double y, final double z) {
        GLUtils.vertexBuffer.vertex(x, y, z).endVertex();
    }
    
    public static void ldrawthree(final double x, final double y, final double z, final float u, final float v) {
        GLUtils.vertexBuffer.vertex(x, y, z).uv(u, v).endVertex();
    }
    
    public static void ldrawthree(final Matrix4f matrix4f, final double x, final double y, final double z, final float u, final float v) {
        GLUtils.vertexBuffer.vertex(matrix4f, (float)x, (float)y, (float)z).uv(u, v).endVertex();
    }
    
    static {
        GLUtils.tessellator = Tesselator.getInstance();
        GLUtils.vertexBuffer = GLUtils.tessellator.getBuilder();
        GLUtils.fboID = 0;
        GLUtils.rboID = 0;
        GLUtils.fboTextureID = 0;
        GLUtils.depthTextureID = 0;
        GLUtils.previousFBOID = 0;
        GLUtils.previousFBOIDREAD = 0;
        GLUtils.previousFBOIDDRAW = 0;
        GLUtils.hasAlphaBits = (GL30.glGetFramebufferAttachmentParameteri(36008, 1026, 33301) > 0);
        dataBuffer = MemoryTracker.create(16777216).asIntBuffer();
    }
}
