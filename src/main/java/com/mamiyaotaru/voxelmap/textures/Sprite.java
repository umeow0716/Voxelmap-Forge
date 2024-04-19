// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.textures;

import java.awt.image.BufferedImage;

import net.minecraft.resources.ResourceLocation;

public class Sprite
{
    private final String iconName;
    protected int[] imageData;
    protected int originX;
    protected int originY;
    protected int width;
    protected int height;
    private float minU;
    private float maxU;
    private float minV;
    private float maxV;
    
    public Sprite(final String iconName) {
        this.iconName = iconName;
    }
    
    public static Sprite spriteFromResourceLocation(final ResourceLocation resourceLocation) {
        final String name = resourceLocation.toString();
        return spriteFromString(name);
    }
    
    public static Sprite spriteFromString(final String name) {
        return new Sprite(name);
    }
    
    public void initSprite(final int sheetWidth, final int sheetHeight, final int originX, final int originY) {
        this.originX = originX;
        this.originY = originY;
        final float var6 = (float)(0.009999999776482582 / sheetWidth);
        final float var7 = (float)(0.009999999776482582 / sheetHeight);
        this.minU = originX / (float)sheetWidth + var6;
        this.maxU = (originX + this.width) / (float)sheetWidth - var6;
        this.minV = originY / (float)sheetHeight + var7;
        this.maxV = (originY + this.height) / (float)sheetHeight - var7;
    }
    
    public void copyFrom(final Sprite sourceSprite) {
        this.originX = sourceSprite.originX;
        this.originY = sourceSprite.originY;
        this.width = sourceSprite.width;
        this.height = sourceSprite.height;
        this.minU = sourceSprite.minU;
        this.maxU = sourceSprite.maxU;
        this.minV = sourceSprite.minV;
        this.maxV = sourceSprite.maxV;
    }
    
    public int getOriginX() {
        return this.originX;
    }
    
    public int getOriginY() {
        return this.originY;
    }
    
    public int getIconWidth() {
        return this.width;
    }
    
    public int getIconHeight() {
        return this.height;
    }
    
    public float getMinU() {
        return this.minU;
    }
    
    public float getMaxU() {
        return this.maxU;
    }
    
    public float getInterpolatedU(final double xPos0to16) {
        final float uWidth = this.maxU - this.minU;
        return this.minU + uWidth * (float)xPos0to16 / 16.0f;
    }
    
    public float getMinV() {
        return this.minV;
    }
    
    public float getMaxV() {
        return this.maxV;
    }
    
    public float getInterpolatedV(final double yPos0to16) {
        final float vHeight = this.maxV - this.minV;
        return this.minV + vHeight * ((float)yPos0to16 / 16.0f);
    }
    
    public String getIconName() {
        return this.iconName;
    }
    
    public int[] getTextureData() {
        return this.imageData;
    }
    
    public void setIconWidth(final int width) {
        this.width = width;
    }
    
    public void setIconHeight(final int height) {
        this.height = height;
    }
    
    public void bufferedImageToIntData(final BufferedImage bufferedImage) {
        this.resetSprite();
        final int var3 = bufferedImage.getWidth();
        final int var4 = bufferedImage.getHeight();
        this.width = var3;
        this.height = var4;
        int[] imageData = new int[0];
        if (bufferedImage != null) {
            imageData = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
            bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), imageData, 0, bufferedImage.getWidth());
        }
        if (var4 != var3) {
            throw new RuntimeException("broken aspect ratio");
        }
        this.imageData = imageData;
    }
    
    public void setTextureData(final int[] imageData) {
        this.imageData = imageData;
    }
    
    private void resetSprite() {
        this.imageData = new int[0];
    }
    
    @Override
    public String toString() {
        return "Sprite{name='" + this.iconName + "', x=" + this.originX + ", y=" + this.originY + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.minU + ", u1=" + this.maxU + ", v0=" + this.minV + ", v1=" + this.maxV;
    }
}
