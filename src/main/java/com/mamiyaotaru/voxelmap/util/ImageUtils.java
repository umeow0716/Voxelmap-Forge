// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.util.Arrays;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Color;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.awt.Graphics2D;
import java.nio.IntBuffer;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.lwjgl.BufferUtils;
import java.io.File;
import org.apache.logging.log4j.LogManager;

public class ImageUtils
{
    public static void saveImage(final String name, final int glid, final int maxMipmapLevel, final int width, final int height) {
        final Logger logger = LogManager.getLogger();
        GLShim.glBindTexture(3553, glid);
        GLShim.glPixelStorei(3333, 1);
        GLShim.glPixelStorei(3317, 1);
        for (int mipmapLevel = 0; mipmapLevel <= maxMipmapLevel; ++mipmapLevel) {
            final File file = new File(name + "_" + mipmapLevel + ".png");
            final int destWidth = width >> mipmapLevel;
            final int destHeight = height >> mipmapLevel;
            final int numPixels = destWidth * destHeight;
            final IntBuffer pixelBuffer = BufferUtils.createIntBuffer(numPixels);
            final int[] pixelArray = new int[numPixels];
            GLShim.glGetTexImage(3553, mipmapLevel, 32993, 33639, pixelBuffer);
            pixelBuffer.get(pixelArray);
            final BufferedImage bufferedImage = new BufferedImage(destWidth, destHeight, 2);
            bufferedImage.setRGB(0, 0, destWidth, destHeight, pixelArray, 0, destWidth);
            try {
                ImageIO.write(bufferedImage, "png", file);
                logger.debug("Exported png to: {}", new Object[] { file.getAbsolutePath() });
            }
            catch (final IOException var14) {
                logger.debug("Unable to write: ", (Throwable)var14);
            }
        }
    }
    
    public static BufferedImage validateImage(BufferedImage image) {
        if (image.getType() != 6) {
            final BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), 6);
            final Graphics2D g2 = temp.createGraphics();
            g2.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            g2.dispose();
            image = temp;
        }
        return image;
    }
    
    public static BufferedImage createBufferedImageFromResourceLocation(final ResourceLocation resourceLocation) {
        try {
            final InputStream is = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).getInputStream();
            BufferedImage image = ImageIO.read(is);
            is.close();
            if (image.getType() != 6) {
                final BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), 6);
                final Graphics2D g2 = temp.createGraphics();
                g2.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                g2.dispose();
                image = temp;
            }
            return image;
        }
        catch (final Exception e) {
            return null;
        }
    }
    
    public static BufferedImage createBufferedImageFromGLID(final int id) {
        GLShim.glBindTexture(3553, id);
        return createBufferedImageFromCurrentGLImage();
    }
    
    public static BufferedImage createBufferedImageFromCurrentGLImage() {
    	int imageWidth = GLShim.glGetTexLevelParameteri(3553, 0, 4096);
        int imageHeight = GLShim.glGetTexLevelParameteri(3553, 0, 4097);
        long size = imageWidth * (long)imageHeight * 4L;
        BufferedImage image;
        if (size < 2147483647L) {
            image = new BufferedImage(imageWidth, imageHeight, 6);
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight * 4).order(ByteOrder.nativeOrder());
            GLShim.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);
            byteBuffer.position(0);
            final byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            for (int x = 0; x < imageWidth; ++x) {
                for (int y = 0; y < imageHeight; ++y) {
                    final int index = y * imageWidth * 4 + x * 4;
                    final byte var8 = 0;
                    int color24 = var8 | (bytes[index + 2] & 0xFF) << 0;
                    color24 |= (bytes[index + 1] & 0xFF) << 8;
                    color24 |= (bytes[index + 0] & 0xFF) << 16;
                    color24 |= (bytes[index + 3] & 0xFF) << 24;
                    image.setRGB(x, y, color24);
                }
            }
        }
        else {
            while (size > 2147483647L) {
                imageWidth /= 2;
                imageHeight /= 2;
                size = imageWidth * (long)imageHeight * 4L;
            }
            final int glid = GLShim.glGetInteger(32873);
            image = new BufferedImage(imageWidth, imageHeight, 6);
            final int fboWidth = 512;
            final int fboHeight = 512;
            final ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(fboWidth * fboHeight * 4).order(ByteOrder.nativeOrder());
            final byte[] bytes2 = new byte[byteBuffer2.remaining()];
            GLShim.glPushAttrib(4096);
            RenderSystem.backupProjectionMatrix();
            GLShim.glViewport(0, 0, fboWidth, fboHeight);
            final Matrix4f matrix4f = Matrix4f.orthographic((float)fboWidth, (float)(-fboHeight), 1000.0f, 3000.0f);
            RenderSystem.setProjectionMatrix(matrix4f);
            final PoseStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.setIdentity();
            matrixStack.translate(0.0, 0.0, -2000.0);
            GLUtils.bindFrameBuffer();
            for (int startX = 0; startX + fboWidth < imageWidth; startX += fboWidth) {
                for (int startY = 0; startY + fboWidth < imageHeight; startY += fboHeight) {
                    GLUtils.disp(glid);
                    GLShim.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                    GLShim.glClear(16640);
                    GLUtils.drawPre();
                    GLUtils.ldrawthree(0.0, fboHeight, 1.0, startX / (float)imageWidth, startY / (float)imageHeight);
                    GLUtils.ldrawthree(fboWidth, fboHeight, 1.0, (startX + (float)fboWidth) / imageWidth, startY / (float)imageHeight);
                    GLUtils.ldrawthree(fboWidth, 0.0, 1.0, (startX + (float)fboWidth) / imageWidth, (startY + (float)fboHeight) / imageHeight);
                    GLUtils.ldrawthree(0.0, 0.0, 1.0, startX / (float)imageWidth, (startY + (float)fboHeight) / imageHeight);
                    GLUtils.drawPost();
                    GLUtils.disp(GLUtils.fboTextureID);
                    byteBuffer2.position(0);
                    GLShim.glGetTexImage(3553, 0, 6408, 5121, byteBuffer2);
                    byteBuffer2.position(0);
                    byteBuffer2.get(bytes2);
                    for (int x2 = 0; x2 < fboWidth && startX + x2 < imageWidth; ++x2) {
                        for (int y2 = 0; y2 < fboHeight && startY + y2 < imageHeight; ++y2) {
                            final int index2 = y2 * fboWidth * 4 + x2 * 4;
                            final byte var9 = 0;
                            int color25 = var9 | (bytes2[index2 + 2] & 0xFF) << 0;
                            color25 |= (bytes2[index2 + 1] & 0xFF) << 8;
                            color25 |= (bytes2[index2 + 0] & 0xFF) << 16;
                            color25 |= (bytes2[index2 + 3] & 0xFF) << 24;
                            image.setRGB(startX + x2, startY + y2, color25);
                        }
                    }
                }
            }
            GLUtils.unbindFrameBuffer();
            RenderSystem.restoreProjectionMatrix();
            GLShim.glPopAttrib();
            GLShim.glViewport(0, 0, Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
        }
        return image;
    }
    
    public static BufferedImage blankImage(final ResourceLocation resourceLocation, final int w, final int h) {
        return blankImage(resourceLocation, w, h, 64, 32);
    }
    
    public static BufferedImage blankImage(final ResourceLocation resourceLocation, final int w, final int h, final int imageWidth, final int imageHeight) {
        return blankImage(resourceLocation, w, h, imageWidth, imageHeight, 0, 0, 0, 0);
    }
    
    public static BufferedImage blankImage(final ResourceLocation resourceLocation, final int w, final int h, final int r, final int g, final int b, final int a) {
        return blankImage(resourceLocation, w, h, 64, 32, r, g, b, a);
    }
    
    public static BufferedImage blankImage(final ResourceLocation resourceLocation, final int w, final int h, final int imageWidth, final int imageHeight, final int r, final int g, final int b, final int a) {
        try {
            final InputStream is = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).getInputStream();
            final BufferedImage mobSkin = ImageIO.read(is);
            is.close();
            final BufferedImage temp = new BufferedImage(w * mobSkin.getWidth() / imageWidth, h * mobSkin.getWidth() / imageWidth, 6);
            final Graphics2D g2 = temp.createGraphics();
            g2.setColor(new Color(r, g, b, a));
            g2.fillRect(0, 0, temp.getWidth(), temp.getHeight());
            g2.dispose();
            return temp;
        }
        catch (final Exception e) {
            System.err.println("Failed getting mob: " + resourceLocation.toString() + " - " + e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static BufferedImage blankImage(final BufferedImage mobSkin, final int w, final int h) {
        return blankImage(mobSkin, w, h, 64, 32);
    }
    
    public static BufferedImage blankImage(final BufferedImage mobSkin, final int w, final int h, final int imageWidth, final int imageHeight) {
        return blankImage(mobSkin, w, h, imageWidth, imageHeight, 0, 0, 0, 0);
    }
    
    public static BufferedImage blankImage(final BufferedImage mobSkin, final int w, final int h, final int r, final int g, final int b, final int a) {
        return blankImage(mobSkin, w, h, 64, 32, r, g, b, a);
    }
    
    public static BufferedImage blankImage(final BufferedImage mobSkin, final int w, final int h, final int imageWidth, final int imageHeight, final int r, final int g, final int b, final int a) {
        final BufferedImage temp = new BufferedImage(w * mobSkin.getWidth() / imageWidth, h * mobSkin.getWidth() / imageWidth, 6);
        final Graphics2D g2 = temp.createGraphics();
        g2.setColor(new Color(r, g, b, a));
        g2.fillRect(0, 0, temp.getWidth(), temp.getHeight());
        g2.dispose();
        return temp;
    }
    
    public static BufferedImage addCharacter(final BufferedImage image, final String character) {
        final Graphics2D g2 = image.createGraphics();
        g2.setColor(new Color(0, 0, 0, 255));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Arial", 0, image.getHeight()));
        final FontMetrics fm = g2.getFontMetrics();
        final int x = (image.getWidth() - fm.stringWidth("?")) / 2;
        final int y = fm.getAscent() + (image.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
        g2.drawString("?", x, y);
        g2.dispose();
        return image;
    }
    
    public static BufferedImage eraseArea(final BufferedImage image, int x, int y, int w, int h, final int imageWidth, final int imageHeight) {
        final float scaleX = (float)(image.getWidth(null) / imageWidth);
        final float scaleY = (float)(image.getHeight(null) / imageHeight);
        x *= (int)scaleX;
        y *= (int)scaleY;
        w *= (int)scaleX;
        h *= (int)scaleY;
        final int[] blankPixels = new int[w * h];
        Arrays.fill(blankPixels, 0);
        image.setRGB(x, y, w, h, blankPixels, 0, w);
        return image;
    }
    
    public static BufferedImage loadImage(final ResourceLocation resourceLocation, final int x, final int y, final int w, final int h) {
        return loadImage(resourceLocation, x, y, w, h, 64, 32);
    }
    
    public static BufferedImage loadImage(final ResourceLocation resourceLocation, final int x, final int y, final int w, final int h, final int imageWidth, final int imageHeight) {
        final BufferedImage mobSkin = createBufferedImageFromResourceLocation(resourceLocation);
        if (mobSkin != null) {
            return loadImage(mobSkin, x, y, w, h, imageWidth, imageHeight);
        }
        System.err.println("Failed getting image: " + resourceLocation.toString());
        return null;
    }
    
    public static BufferedImage loadImage(final BufferedImage mobSkin, final int x, final int y, final int w, final int h) {
        return loadImage(mobSkin, x, y, w, h, 64, 32);
    }
    
    public static BufferedImage loadImage(final BufferedImage mobSkin, int x, int y, int w, int h, final int imageWidth, final int imageHeight) {
        final float scale = (float)(mobSkin.getWidth(null) / imageWidth);
        x *= (int)scale;
        y *= (int)scale;
        w *= (int)scale;
        h *= (int)scale;
        w = Math.max(1, w);
        h = Math.max(1, h);
        x = Math.min(mobSkin.getWidth(null) - w, x);
        y = Math.min(mobSkin.getHeight(null) - h, y);
        final BufferedImage base = mobSkin.getSubimage(x, y, w, h);
        return base;
    }
    
    public static BufferedImage addImages(final BufferedImage base, final BufferedImage overlay, final float x, final float y, final int baseWidth, final int baseHeight) {
        final int scale = base.getWidth() / baseWidth;
        final Graphics gfx = base.getGraphics();
        gfx.drawImage(overlay, (int)(x * scale), (int)(y * scale), null);
        gfx.dispose();
        return base;
    }
    
    public static BufferedImage scaleImage(BufferedImage image, final float scaleBy) {
        if (scaleBy == 1.0f) {
            return image;
        }
        int type = image.getType();
        if (type == 13) {
            type = 6;
        }
        final int newWidth = Math.max(1, (int)(image.getWidth() * scaleBy));
        final int newHeight = Math.max(1, (int)(image.getHeight() * scaleBy));
        final BufferedImage tmp = new BufferedImage(newWidth, newHeight, type);
        final Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2.dispose();
        image = tmp;
        return image;
    }
    
    public static BufferedImage scaleImage(BufferedImage image, final float xScaleBy, final float yScaleBy) {
        if (xScaleBy == 1.0f && yScaleBy == 1.0f) {
            return image;
        }
        int type = image.getType();
        if (type == 13) {
            type = 6;
        }
        final int newWidth = Math.max(1, (int)(image.getWidth() * xScaleBy));
        final int newHeight = Math.max(1, (int)(image.getHeight() * yScaleBy));
        final BufferedImage tmp = new BufferedImage(newWidth, newHeight, type);
        final Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2.dispose();
        image = tmp;
        return image;
    }
    
    public static BufferedImage flipHorizontal(final BufferedImage image) {
        final AffineTransform tx = AffineTransform.getScaleInstance(-1.0, 1.0);
        tx.translate(-image.getWidth(null), 0.0);
        final AffineTransformOp op = new AffineTransformOp(tx, 1);
        return op.filter(image, null);
    }
    
    public static BufferedImage into128(final BufferedImage base) {
        final BufferedImage frame = new BufferedImage(128, 128, base.getType());
        final Graphics gfx = frame.getGraphics();
        gfx.drawImage(base, 64 - base.getWidth() / 2, 64 - base.getHeight() / 2, base.getWidth(), base.getHeight(), null);
        gfx.dispose();
        return frame;
    }
    
    public static BufferedImage intoSquare(final BufferedImage base) {
        int dim;
        int t;
        for (dim = Math.max(base.getWidth(), base.getHeight()), t = 1; Math.pow(2.0, t - 1) < dim; ++t) {}
        final int size = (int)Math.pow(2.0, t);
        final BufferedImage frame = new BufferedImage(size, size, base.getType());
        final Graphics gfx = frame.getGraphics();
        gfx.drawImage(base, (size - base.getWidth()) / 2, (size - base.getHeight()) / 2, base.getWidth(), base.getHeight(), null);
        gfx.dispose();
        return frame;
    }
    
    public static BufferedImage pad(final BufferedImage base) {
        final int dim = Math.max(base.getWidth(), base.getHeight());
        final int outlineWidth = 3;
        final int size = dim + outlineWidth * 2;
        final BufferedImage frame = new BufferedImage(size, size, base.getType());
        final Graphics gfx = frame.getGraphics();
        gfx.drawImage(base, (size - base.getWidth()) / 2, (size - base.getHeight()) / 2, base.getWidth(), base.getHeight(), null);
        gfx.dispose();
        return frame;
    }
    
    public static BufferedImage fillOutline(final BufferedImage image, final boolean outline, final int passes) {
        return fillOutline(image, outline, false, 0.0f, 0.0f, passes);
    }
    
    public static BufferedImage fillOutline(BufferedImage image, final boolean outline, final boolean armor, final float intendedWidth, final float intendedHeight, final int passes) {
        if (outline) {
            for (int t = 0; t < passes; ++t) {
                image = fillOutline(image, true, armor, intendedWidth, intendedHeight);
            }
        }
        image = fillOutline(image, false, armor, intendedWidth, intendedHeight);
        return image;
    }
    
    private static BufferedImage fillOutline(final BufferedImage image, final boolean solid, final boolean armor, final float intendedWidth, final float intendedHeight) {
        final float armorOutlineFractionHorizontal = intendedWidth / 2.0f - 1.0f;
        final float armorOutlineFractionVertical = intendedHeight / 2.0f - 1.0f;
        final BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        final Graphics gfx = temp.getGraphics();
        gfx.drawImage(image, 0, 0, null);
        gfx.dispose();
        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        for (int t = 0; t < image.getWidth(); ++t) {
            for (int s = 0; s < image.getHeight(); ++s) {
                final int color = image.getRGB(t, s);
                if ((color >> 24 & 0xFF) == 0x0) {
                    int newColor = sampleNonTransparentNeighborPixel(t, s, image);
                    if (newColor != -420) {
                        if (solid) {
                            if (!armor || t <= imageWidth / 2 - armorOutlineFractionHorizontal || t >= imageWidth / 2 + armorOutlineFractionHorizontal - 1.0f || s <= imageHeight / 2 - armorOutlineFractionVertical || s >= imageHeight / 2 + armorOutlineFractionVertical - 1.0f) {
                                newColor = -16777216;
                            }
                            else {
                                newColor = 0;
                            }
                        }
                        else {
                            final int red = newColor >> 16 & 0xFF;
                            final int green = newColor >> 8 & 0xFF;
                            final int blue = newColor >> 0 & 0xFF;
                            newColor = (0x0 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
                        }
                        temp.setRGB(t, s, newColor);
                    }
                }
            }
        }
        return temp;
    }
    
    public static int sampleNonTransparentNeighborPixel(final int x, final int y, final BufferedImage image) {
        if (x > 0) {
            final int color = image.getRGB(x - 1, y);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        if (x < image.getWidth() - 1) {
            final int color = image.getRGB(x + 1, y);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        if (y > 0) {
            final int color = image.getRGB(x, y - 1);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        if (y < image.getHeight() - 1) {
            final int color = image.getRGB(x, y + 1);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        if (x > 0 && y > 0) {
            final int color = image.getRGB(x - 1, y - 1);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        if (x > 0 && y < image.getHeight() - 1) {
            final int color = image.getRGB(x - 1, y + 1);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        if (x < image.getWidth() - 1 && y > 0) {
            final int color = image.getRGB(x + 1, y - 1);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        if (x < image.getWidth() - 1 && y < image.getHeight() - 1) {
            final int color = image.getRGB(x + 1, y + 1);
            if ((color >> 24 & 0xFF) > 50) {
                return color;
            }
        }
        return -420;
    }
    
    public static BufferedImage trim(BufferedImage image) {
        int left = -1;
        int right = image.getWidth();
        int top = -1;
        int bottom = image.getHeight();
        boolean foundColor = false;
        int color = 0;
        while (!foundColor && left < right - 1) {
            ++left;
            for (int t = 0; t < image.getHeight(); ++t) {
                color = image.getRGB(left, t);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
        }
        foundColor = false;
        while (!foundColor && right > left + 1) {
            --right;
            for (int t = 0; t < image.getHeight(); ++t) {
                color = image.getRGB(right, t);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
        }
        foundColor = false;
        while (!foundColor && top < bottom - 1) {
            ++top;
            for (int t = 0; t < image.getWidth(); ++t) {
                color = image.getRGB(t, top);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
        }
        foundColor = false;
        while (!foundColor && bottom > top + 1) {
            --bottom;
            for (int t = 0; t < image.getWidth(); ++t) {
                color = image.getRGB(t, bottom);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
        }
        image = image.getSubimage(left, top, right - left + 1, bottom - top + 1);
        return image;
    }
    
    public static BufferedImage trimCentered(BufferedImage image) {
        final int height = image.getHeight();
        final int width = image.getWidth();
        int left = -1;
        int right = width;
        int top = -1;
        int bottom = height;
        boolean foundColor = false;
        int color = 0;
        while (!foundColor && left < width / 2 - 1 && top < height / 2 - 1) {
            ++left;
            --right;
            ++top;
            --bottom;
            for (int y = top; y < bottom; ++y) {
                color = image.getRGB(left, y);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
            for (int y = top; y < bottom; ++y) {
                color = image.getRGB(right, y);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
            for (int x = left; x < right; ++x) {
                color = image.getRGB(x, top);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
            for (int x = left; x < right; ++x) {
                color = image.getRGB(x, bottom);
                if (color >> 24 != 0) {
                    foundColor = true;
                }
            }
        }
        image = image.getSubimage(left, top, right - left + 1, bottom - top + 1);
        return image;
    }
    
    public static BufferedImage colorify(final BufferedImage image, final float r, final float g, final float b) {
        final BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), 3);
        final Graphics2D gfx = temp.createGraphics();
        gfx.drawImage(image, 0, 0, null);
        gfx.dispose();
        for (int x = 0; x < temp.getWidth(); ++x) {
            for (int y = 0; y < temp.getHeight(); ++y) {
                final int ax = temp.getColorModel().getAlpha(temp.getRaster().getDataElements(x, y, null));
                int rx = temp.getColorModel().getRed(temp.getRaster().getDataElements(x, y, null));
                int gx = temp.getColorModel().getGreen(temp.getRaster().getDataElements(x, y, null));
                int bx = temp.getColorModel().getBlue(temp.getRaster().getDataElements(x, y, null));
                rx *= (int)r;
                gx *= (int)g;
                bx *= (int)b;
                temp.setRGB(x, y, ax << 24 | rx << 16 | gx << 8 | bx << 0);
            }
        }
        return temp;
    }
    
    public static BufferedImage colorify(final BufferedImage image, final int r, final int g, final int b) {
        return colorify(image, r / 255.0f, g / 255.0f, b / 255.0f);
    }
    
    public static BufferedImage colorify(final BufferedImage image, final int rgb) {
        return colorify(image, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }
    
    public static float percentageOfEdgePixelsThatAreSolid(final BufferedImage image) {
        final float edgePixels = (float)(image.getWidth() * 2 + image.getHeight() * 2 - 2);
        float edgePixelsWithColor = 0.0f;
        int color = 0;
        for (int t = 0; t < image.getHeight(); ++t) {
            color = image.getRGB(0, t);
            if (color >> 24 != 0) {
                ++edgePixelsWithColor;
            }
            color = image.getRGB(image.getWidth() - 1, t);
            if (color >> 24 != 0) {
                ++edgePixelsWithColor;
            }
        }
        for (int t = 1; t < image.getWidth() - 1; ++t) {
            color = image.getRGB(t, 0);
            if (color >> 24 != 0) {
                ++edgePixelsWithColor;
            }
            color = image.getRGB(t, image.getHeight() - 1);
            if (color >> 24 != 0) {
                ++edgePixelsWithColor;
            }
        }
        return edgePixelsWithColor / edgePixels;
    }
}
