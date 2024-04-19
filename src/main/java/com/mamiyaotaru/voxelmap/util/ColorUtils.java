// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

public class ColorUtils
{
    public static int colorMultiplier(final int color1, final int color2) {
        final int alpha1 = color1 >> 24 & 0xFF;
        final int red1 = color1 >> 16 & 0xFF;
        final int green1 = color1 >> 8 & 0xFF;
        final int blue1 = color1 >> 0 & 0xFF;
        final int alpha2 = color2 >> 24 & 0xFF;
        final int red2 = color2 >> 16 & 0xFF;
        final int green2 = color2 >> 8 & 0xFF;
        final int blue2 = color2 >> 0 & 0xFF;
        final int alpha3 = alpha1 * alpha2 / 255;
        final int red3 = red1 * red2 / 255;
        final int green3 = green1 * green2 / 255;
        final int blue3 = blue1 * blue2 / 255;
        return (alpha3 & 0xFF) << 24 | (red3 & 0xFF) << 16 | (green3 & 0xFF) << 8 | (blue3 & 0xFF);
    }
    
    public static int colorAdder(final int color1, final int color2) {
        final float topAlpha = (color1 >> 24 & 0xFF) / 255.0f;
        final float red1 = (color1 >> 16 & 0xFF) * topAlpha;
        final float green1 = (color1 >> 8 & 0xFF) * topAlpha;
        final float blue1 = (color1 >> 0 & 0xFF) * topAlpha;
        final float bottomAlpha = (color2 >> 24 & 0xFF) / 255.0f;
        final float red2 = (color2 >> 16 & 0xFF) * bottomAlpha * (1.0f - topAlpha);
        final float green2 = (color2 >> 8 & 0xFF) * bottomAlpha * (1.0f - topAlpha);
        final float blue2 = (color2 >> 0 & 0xFF) * bottomAlpha * (1.0f - topAlpha);
        final float alpha = topAlpha + bottomAlpha * (1.0f - topAlpha);
        final float red3 = (red1 + red2) / alpha;
        final float green3 = (green1 + green2) / alpha;
        final float blue3 = (blue1 + blue2) / alpha;
        return ((int)(alpha * 255.0f) & 0xFF) << 24 | ((int)red3 & 0xFF) << 16 | ((int)green3 & 0xFF) << 8 | ((int)blue3 & 0xFF);
    }
}
