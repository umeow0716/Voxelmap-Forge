// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL30;
import java.nio.IntBuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class GLShim
{
    public static final int GL_ALL_ATTRIB_BITS = 1048575;
    public static final int GL_BLEND = 3042;
    public static final int GL_CLAMP = 10496;
    public static final int GL_CLAMP_TO_EDGE = 33071;
    public static final int GL_COLOR_BUFFER_BIT = 16384;
    public static final int GL_COLOR_CLEAR_VALUE = 3106;
    public static final int GL_CULL_FACE = 2884;
    public static final int GL_DEPTH_BUFFER_BIT = 256;
    public static final int GL_DST_ALPHA = 772;
    public static final int GL_DST_COLOR = 774;
    public static final int GL_DEPTH_TEST = 2929;
    public static final int GL_FLAT = 7424;
    public static final int GL_GREATER = 516;
    public static final int GL_LIGHTING = 2896;
    public static final int GL_LINEAR = 9729;
    public static final int GL_LINES = 1;
    public static final int GL_LINEAR_MIPMAP_LINEAR = 9987;
    public static final int GL_LINEAR_MIPMAP_NEAREST = 9985;
    public static final int GL_MODELVIEW = 5888;
    public static final int GL_MODULATE = 8448;
    public static final int GL_NEAREST = 9728;
    public static final int GL_NEAREST_MIPMAP_LINEAR = 9986;
    public static final int GL_NEAREST_MIPMAP_NEAREST = 9984;
    public static final int GL_ONE = 1;
    public static final int GL_ONE_MINUS_DST_ALPHA = 773;
    public static final int GL_ONE_MINUS_DST_COLOR = 775;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
    public static final int GL_ONE_MINUS_SRC_COLOR = 769;
    public static final int GL_PACK_ALIGNMENT = 3333;
    public static final int GL_POLYGON_OFFSET_FILL = 32823;
    public static final int GL_PROJECTION = 5889;
    public static final int GL_PROJECTION_MATRIX = 2983;
    public static final int GL_QUADS = 7;
    public static final int GL_RGBA = 6408;
    public static final int GL_SMOOTH = 7425;
    public static final int GL_SCISSOR_TEST = 3089;
    public static final int GL_SRC_ALPHA = 770;
    public static final int GL_TEXTURE_2D = 3553;
    public static final int GL_TEXTURE_BINDING_2D = 32873;
    public static final int GL_TEXTURE_ENV = 8960;
    public static final int GL_TEXTURE_ENV_MODE = 8704;
    public static final int GL_TEXTURE_HEIGHT = 4097;
    public static final int GL_TEXTURE_MAG_FILTER = 10240;
    public static final int GL_TEXTURE_MIN_FILTER = 10241;
    public static final int GL_TEXTURE_WIDTH = 4096;
    public static final int GL_TEXTURE_WRAP_S = 10242;
    public static final int GL_TEXTURE_WRAP_T = 10243;
    public static final int GL_TRIANGLE_STRIP = 5;
    public static final int GL_TRUE = 1;
    public static final int GL_TRANSFORM_BIT = 4096;
    public static final int GL_UNPACK_ALIGNMENT = 3317;
    public static final int GL_UNPACK_ROW_LENGTH = 3314;
    public static final int GL_UNPACK_SKIP_PIXELS = 3316;
    public static final int GL_UNPACK_SKIP_ROWS = 3315;
    public static final int GL_UNSIGNED_BYTE = 5121;
    public static final int GL_VIEWPORT_BIT = 2048;
    public static final int GL_ZERO = 0;
    public static final int GL_BGRA = 32993;
    public static final int GL_RESCALE_NORMAL = 32826;
    public static final int GL_UNSIGNED_INT_8_8_8_8 = 32821;
    public static final int GL_UNSIGNED_INT_8_8_8_8_REV = 33639;
    
    public static void glEnable(final int attrib) {
        switch (attrib) {
            case 3042: {
                RenderSystem.enableBlend();
                break;
            }
            case 2884: {
                RenderSystem.enableCull();
                break;
            }
            case 2929: {
                RenderSystem.enableDepthTest();
                break;
            }
            case 32823: {
                RenderSystem.enablePolygonOffset();
                break;
            }
            case 3553: {
                RenderSystem.enableTexture();
                break;
            }
            case 3089: {
                GL11.glEnable(3089);
                break;
            }
        }
    }
    
    public static void glDisable(final int attrib) {
        switch (attrib) {
            case 3042: {
                RenderSystem.disableBlend();
                break;
            }
            case 2884: {
                RenderSystem.disableCull();
                break;
            }
            case 2929: {
                RenderSystem.disableDepthTest();
                break;
            }
            case 32823: {
                RenderSystem.disablePolygonOffset();
                break;
            }
            case 3553: {
                RenderSystem.disableTexture();
                break;
            }
            case 3089: {
                GL11.glDisable(3089);
                break;
            }
        }
    }
    
    public static void glBlendFunc(final int sfactor, final int dfactor) {
        RenderSystem.blendFunc(sfactor, dfactor);
    }
    
    public static void glBlendFuncSeparate(final int sfactorRGB, final int dfactorRGB, final int sfactorAlpha, final int dfactorAlpha) {
        RenderSystem.blendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }
    
    public static void glClear(final int mask) {
        RenderSystem.clear(mask, Minecraft.ON_OSX);
    }
    
    public static void glClearColor(final float red, final float green, final float blue, final float alpha) {
        RenderSystem.clearColor(red, green, blue, alpha);
    }
    
    public static void glClearDepth(final double depth) {
        RenderSystem.clearDepth(depth);
    }
    
    public static void glColor3f(final float red, final float green, final float blue) {
        RenderSystem.setShaderColor(red, green, blue, 1.0f);
    }
    
    public static void glColor4f(final float red, final float green, final float blue, final float alpha) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }
    
    public static void glColor3ub(final int red, final int green, final int blue) {
        RenderSystem.setShaderColor(red / 255.0f, green / 255.0f, blue / 255.0f, 1.0f);
    }
    
    public static void glColorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
        RenderSystem.colorMask(red, green, blue, alpha);
    }
    
    public static void glDeleteTextures(final int id) {
        RenderSystem.deleteTexture(id);
    }
    
    public static void glDepthFunc(final int func) {
        RenderSystem.depthFunc(func);
    }
    
    public static void glDepthMask(final boolean flag) {
        RenderSystem.depthMask(flag);
    }
    
    public static int glGenTextures() {
        return GlStateManager._genTexture();
    }
    
    public static void glGetTexImage(final int tex, final int level, final int format, final int type, final long pixels) {
        GlStateManager._getTexImage(tex, level, format, type, pixels);
    }
    
    public static int glGetTexLevelParameteri(final int target, final int level, final int pname) {
        return GlStateManager._getTexLevelParameter(target, level, pname);
    }
    
    public static void glLogicOp(final int opcode) {
        GlStateManager._logicOp(opcode);
    }
    
    public static void glPixelStorei(final int parameterName, final int parameter) {
        RenderSystem.pixelStore(parameterName, parameter);
    }
    
    public static void glPolygonOffset(final float factor, final float units) {
        RenderSystem.polygonOffset(factor, units);
    }
    
    public static void glSetActiveTextureUnit(final int texture) {
        RenderSystem.activeTexture(texture);
    }
    
    public static void glTexImage2D(final int target, final int level, final int internalformat, final int width, final int height, final int border, final int format, final int type, final IntBuffer pixels) {
        GlStateManager._texImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }
    
    public static void glTexParameterf(final int target, final int pname, final float param) {
        GlStateManager._texParameter(target, pname, param);
    }
    
    public static void glTexParameteri(final int target, final int pname, final int param) {
        RenderSystem.texParameter(target, pname, param);
    }
    
    public static void glTexSubImage2D(final int target, final int level, final int xOffset, final int yOffset, final int width, final int height, final int format, final int type, final long memAddress) {
        GlStateManager._texSubImage2D(target, level, xOffset, yOffset, width, height, format, type, memAddress);
    }
    
    public static void glViewport(final int x, final int y, final int width, final int height) {
        RenderSystem.viewport(x, y, width, height);
    }
    
    public static void glBindTexture(final int target, final int texture) {
        switch (target) {
            case 3553: {
                RenderSystem.bindTexture(texture);
                break;
            }
            default: {
                GL11.glBindTexture(target, texture);
                break;
            }
        }
    }
    
    public static void glGenerateMipmap(final int glTexture2d) {
        GL30.glGenerateMipmap(glTexture2d);
    }
    
    public static boolean glGetBoolean(final int pname) {
        return GL11.glGetBoolean(pname);
    }
    
    public static void glGetFloatv(final int pname, final FloatBuffer params) {
        GL11.glGetFloatv(pname, params);
    }
    
    public static int glGetInteger(final int pname) {
        return GL11.glGetInteger(pname);
    }
    
    public static void glGetTexImage(final int tex, final int level, final int format, final int type, final ByteBuffer pixels) {
        GL11.glGetTexImage(tex, level, format, type, pixels);
    }
    
    public static void glGetTexImage(final int tex, final int level, final int format, final int type, final IntBuffer pixels) {
        GL11.glGetTexImage(tex, level, format, type, pixels);
    }
    
    public static void glPopAttrib() {
        GL11.glPopAttrib();
    }
    
    public static void glPushAttrib(final int mask) {
        GL11.glPushAttrib(mask);
    }
    
    public static void glScissor(final int x, final int y, final int width, final int height) {
        GL11.glScissor(x, y, width, height);
    }
    
    public static void glTexImage2D(final int glTexture2d, final int level, final int glRgba, final int width, final int height, final int border, final int format, final int type, final ByteBuffer pixels) {
        GL11.glTexImage2D(glTexture2d, level, glRgba, width, height, border, format, type, pixels);
    }
    
    public static void glTexSubImage2D(final int target, final int level, final int xoffset, final int yoffset, final int width, final int height, final int format, final int type, final IntBuffer pixels) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }
    
    public static void glVertex2f(final float x, final float y) {
        GL11.glVertex2f(x, y);
    }
    
    public static void glVertex3f(final float x, final float y, final float z) {
        GL11.glVertex3f(x, y, z);
    }
}
