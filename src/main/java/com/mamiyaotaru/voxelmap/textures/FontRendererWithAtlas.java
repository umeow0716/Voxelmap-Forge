// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.textures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import com.google.common.collect.Lists;
import java.util.Random;

public class FontRendererWithAtlas extends Font implements PreparableReloadListener
{
    private int[] charWidthArray;
    public int FONT_HEIGHT;
    public Random fontRandom;
    private int[] colorCode;
    private final ResourceLocation locationFontTexture;
    private Sprite fontIcon;
    private Sprite blankIcon;
    private float posX;
    private float posY;
    private float red;
    private float blue;
    private float green;
    private float alpha;
    private boolean randomStyle;
    private boolean boldStyle;
    private boolean italicStyle;
    private boolean underlineStyle;
    private boolean strikethroughStyle;
    private BufferBuilder vertexBuffer;
    
    public FontRendererWithAtlas(final TextureManager renderEngine, final ResourceLocation locationFontTexture) {
        super(identifierx -> Util.make(new FontSet(renderEngine, locationFontTexture), fontStorage -> fontStorage.reload(Lists.newArrayList(new GlyphProvider[] { new AllMissingGlyphProvider() }))));
        this.charWidthArray = new int[256];
        this.FONT_HEIGHT = 9;
        this.fontRandom = new Random();
        this.colorCode = new int[32];
        this.fontIcon = null;
        this.blankIcon = null;
        this.locationFontTexture = locationFontTexture;
        (renderEngine).bindForSetup(this.locationFontTexture);
        for (int colorCodeIndex = 0; colorCodeIndex < 32; ++colorCodeIndex) {
            final int var6 = (colorCodeIndex >> 3 & 0x1) * 85;
            int red = (colorCodeIndex >> 2 & 0x1) * 170 + var6;
            int green = (colorCodeIndex >> 1 & 0x1) * 170 + var6;
            int blue = (colorCodeIndex >> 0 & 0x1) * 170 + var6;
            if (colorCodeIndex == 6) {
                red += 85;
            }
            if (colorCodeIndex >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCode[colorCodeIndex] = ((red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        }
        this.vertexBuffer = Tesselator.getInstance().getBuilder();
    }
    
    public void onResourceManagerReload(final ResourceManager resourceManager) {
        this.readFontTexture();
    }
    
    private void readFontTexture() {
        BufferedImage fontImage;
        try {
            fontImage = TextureUtilLegacy.readBufferedImage(Minecraft.getInstance().getResourceManager().getResource(this.locationFontTexture).getInputStream());
        }
        catch (final IOException var17) {
            throw new RuntimeException(var17);
        }
        if (fontImage.getWidth() > 512 || fontImage.getHeight() > 512) {
            final int maxDim = Math.max(fontImage.getWidth(), fontImage.getHeight());
            final float scaleBy = 512.0f / maxDim;
            int type = fontImage.getType();
            if (type == 13) {
                type = 6;
            }
            final int newWidth = Math.max(1, (int)(fontImage.getWidth() * scaleBy));
            final int newHeight = Math.max(1, (int)(fontImage.getHeight() * scaleBy));
            final BufferedImage tmp = new BufferedImage(newWidth, newHeight, type);
            final Graphics2D g2 = tmp.createGraphics();
            g2.drawImage(fontImage, 0, 0, newWidth, newHeight, null);
            g2.dispose();
            fontImage = tmp;
        }
        final int sheetWidth = fontImage.getWidth();
        final int sheetHeight = fontImage.getHeight();
        final int[] sheetImageData = new int[sheetWidth * sheetHeight];
        fontImage.getRGB(0, 0, sheetWidth, sheetHeight, sheetImageData, 0, sheetWidth);
        final int characterHeight = sheetHeight / 16;
        final int characterWidth = sheetWidth / 16;
        final byte padding = 1;
        final float scale = 8.0f / characterWidth;
        for (int characterIndex = 0; characterIndex < 256; ++characterIndex) {
            final int characterX = characterIndex % 16;
            final int characterY = characterIndex / 16;
            if (characterIndex == 32) {
                this.charWidthArray[characterIndex] = 3 + padding;
            }
            int thisCharacterWidth = characterWidth - 1;
            for (boolean onlyBlankPixels = true; thisCharacterWidth >= 0 && onlyBlankPixels; --thisCharacterWidth) {
                final int pixelX = characterX * characterWidth + thisCharacterWidth;
                for (int characterPixelYPos = 0; characterPixelYPos < characterHeight && onlyBlankPixels; ++characterPixelYPos) {
                    final int pixelY = (characterY * characterWidth + characterPixelYPos) * sheetWidth;
                    if ((sheetImageData[pixelX + pixelY] >> 24 & 0xFF) != 0x0) {
                        onlyBlankPixels = false;
                    }
                }
                if (onlyBlankPixels) {}
            }
            ++thisCharacterWidth;
            this.charWidthArray[characterIndex] = (int)(0.5 + thisCharacterWidth * scale) + padding;
        }
    }
    
    public void setSprites(final Sprite text, final Sprite blank) {
        this.fontIcon = text;
        this.blankIcon = blank;
    }
    
    public void setFontRef(final int ref) {
    }
    
    private float renderCharAtPos(final int charIndex, final char character, final boolean shadow) {
        return (character == ' ') ? 4.0f : this.renderDefaultChar(charIndex, shadow);
    }
    
    private float renderDefaultChar(final int charIndex, final boolean shadow) {
        final float sheetWidth = (this.fontIcon.originX + this.fontIcon.width) / this.fontIcon.getMaxU();
        final float sheetHeight = (this.fontIcon.originY + this.fontIcon.height) / this.fontIcon.getMaxV();
        final float fontScaleX = (this.fontIcon.width - 2) / 128.0f;
        final float fontScaleY = (this.fontIcon.height - 2) / 128.0f;
        final float charXPosInSheet = charIndex % 16 * 8 * fontScaleX + this.fontIcon.originX + 1.0f;
        final float charYPosInSheet = charIndex / 16 * 8 * fontScaleY + this.fontIcon.originY + 1.0f;
        final float shadowOffset = shadow ? 1.0f : 0.0f;
        final float charWidth = this.charWidthArray[charIndex] - 0.01f;
        this.vertexBuffer.vertex((double)(this.posX + shadowOffset), (double)this.posY, 0.0).uv(charXPosInSheet / sheetWidth, charYPosInSheet / sheetHeight).color(this.red, this.blue, this.green, this.alpha).endVertex();
        this.vertexBuffer.vertex((double)(this.posX - shadowOffset), (double)(this.posY + 7.99f), 0.0).uv(charXPosInSheet / sheetWidth, (charYPosInSheet + 7.99f * fontScaleY) / sheetHeight).color(this.red, this.blue, this.green, this.alpha).endVertex();
        this.vertexBuffer.vertex((double)(this.posX + charWidth - 1.0f - shadowOffset), (double)(this.posY + 7.99f), 0.0).uv((charXPosInSheet + (charWidth - 1.0f) * fontScaleX) / sheetWidth, (charYPosInSheet + 7.99f * fontScaleY) / sheetHeight).color(this.red, this.blue, this.green, this.alpha).endVertex();
        this.vertexBuffer.vertex((double)(this.posX + charWidth - 1.0f + shadowOffset), (double)this.posY, 0.0).uv((charXPosInSheet + (charWidth - 1.0f) * fontScaleX) / sheetWidth, charYPosInSheet / sheetHeight).color(this.red, this.blue, this.green, this.alpha).endVertex();
        return (float)this.charWidthArray[charIndex];
    }
    
    public int drawStringWithShadow(final String text, final float x, final float y, final int color) {
        return this.drawString(text, x, y, color, true);
    }
    
    public int drawString(final String text, final int x, final int y, final int color) {
        return this.drawString(text, (float)x, (float)y, color, false);
    }
    
    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow) {
        this.resetStyles();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        this.vertexBuffer.discard();
        this.vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        int var6;
        if (shadow) {
            var6 = this.renderString(text, x + 1.0f, y + 1.0f, color, true);
            var6 = Math.max(var6, this.renderString(text, x, y, color, false));
        }
        else {
            var6 = this.renderString(text, x, y, color, false);
        }
        this.vertexBuffer.end();
        BufferUploader.end(this.vertexBuffer);
        return var6;
    }
    
    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }
    
    private void renderStringAtPos(final String text, final boolean shadow) {
        for (int textIndex = 0; textIndex < text.length(); ++textIndex) {
            final char character = text.charAt(textIndex);
            if (character == '§' && textIndex + 1 < text.length()) {
                int formatCode = "0123456789abcdefklmnor".indexOf(text.toLowerCase().charAt(textIndex + 1));
                if (formatCode < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    if (formatCode < 0 || formatCode > 15) {
                        formatCode = 15;
                    }
                    if (shadow) {
                        formatCode += 16;
                    }
                }
                else if (formatCode == 16) {
                    this.randomStyle = true;
                }
                else if (formatCode == 17) {
                    this.boldStyle = true;
                }
                else if (formatCode == 18) {
                    this.strikethroughStyle = true;
                }
                else if (formatCode == 19) {
                    this.underlineStyle = true;
                }
                else if (formatCode == 20) {
                    this.italicStyle = true;
                }
                else if (formatCode == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    GLShim.glColor4f(this.red, this.blue, this.green, this.alpha);
                }
                ++textIndex;
            }
            else {
            	int charIndex = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\000\000\000\000\000\000\000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\000".indexOf(character);
                if (charIndex != -1) {
                    final float sheetWidth = (this.blankIcon.originX + this.blankIcon.width) / this.blankIcon.getMaxU();
                    final float sheetHeight = (this.blankIcon.originY + this.blankIcon.height) / this.blankIcon.getMaxV();
                    final float u = (this.blankIcon.originX + 4) / sheetWidth;
                    final float v = (this.blankIcon.originY + 4) / sheetHeight;
                    if (this.randomStyle) {
                        int randomCharIndex;
                        do {
                            randomCharIndex = this.fontRandom.nextInt(this.charWidthArray.length);
                        } while (this.charWidthArray[charIndex] != this.charWidthArray[randomCharIndex]);
                        charIndex = randomCharIndex;
                    }
                    final float offset = 1.0f;
                    float widthOfRenderedChar = this.renderCharAtPos(charIndex, character, this.italicStyle);
                    if (this.boldStyle) {
                        this.posX += offset;
                        this.renderCharAtPos(charIndex, character, this.italicStyle);
                        this.posX -= offset;
                        ++widthOfRenderedChar;
                    }
                    if (this.strikethroughStyle) {
                        this.vertexBuffer.vertex((double)this.posX, (double)(this.posY + this.FONT_HEIGHT / 2), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                        this.vertexBuffer.vertex((double)(this.posX + widthOfRenderedChar), (double)(this.posY + this.FONT_HEIGHT / 2), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                        this.vertexBuffer.vertex((double)(this.posX + widthOfRenderedChar), (double)(this.posY + this.FONT_HEIGHT / 2 - 1.0f), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                        this.vertexBuffer.vertex((double)this.posX, (double)(this.posY + this.FONT_HEIGHT / 2 - 1.0f), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                    }
                    if (this.underlineStyle) {
                        final int l = this.underlineStyle ? -1 : 0;
                        this.vertexBuffer.vertex((double)(this.posX + l), (double)(this.posY + this.FONT_HEIGHT), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                        this.vertexBuffer.vertex((double)(this.posX + widthOfRenderedChar), (double)(this.posY + this.FONT_HEIGHT), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                        this.vertexBuffer.vertex((double)(this.posX + widthOfRenderedChar), (double)(this.posY + this.FONT_HEIGHT - 1.0f), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                        this.vertexBuffer.vertex((double)(this.posX + l), (double)(this.posY + this.FONT_HEIGHT - 1.0f), 0.0).uv(u, v).color(this.red, this.blue, this.green, this.alpha).endVertex();
                    }
                    this.posX += (int)widthOfRenderedChar;
                }
            }
        }
    }
    
    private int renderString(final String text, final float x, final float y, int color, final boolean shadow) {
        if (text == null) {
            return 0;
        }
        if ((color & 0xFC000000) == 0x0) {
            color |= 0xFF000000;
        }
        if (shadow) {
            color = ((color & 0xFCFCFC) >> 2 | (color & 0xFF000000));
        }
        this.red = (color >> 16 & 0xFF) / 255.0f;
        this.blue = (color >> 8 & 0xFF) / 255.0f;
        this.green = (color & 0xFF) / 255.0f;
        this.alpha = (color >> 24 & 0xFF) / 255.0f;
        this.posX = x;
        this.posY = y;
        this.renderStringAtPos(text, shadow);
        return (int)this.posX;
    }
    
    public int getStringWidth(final String string) {
        if (string == null) {
            return 0;
        }
        int totalWidth = 0;
        boolean includeSpace = false;
        for (int charIndex = 0; charIndex < string.length(); ++charIndex) {
            char character = string.charAt(charIndex);
            float characterWidth = this.getCharWidth(character);
            if (characterWidth < 0.0f && charIndex < string.length() - 1) {
                ++charIndex;
                character = string.charAt(charIndex);
                if (character != 'l' && character != 'L') {
                    if (character == 'r' || character == 'R') {
                        includeSpace = false;
                    }
                }
                else {
                    includeSpace = true;
                }
                characterWidth = 0.0f;
            }
            totalWidth += (int)characterWidth;
            if (includeSpace && characterWidth > 0.0f) {
                ++totalWidth;
            }
        }
        return totalWidth;
    }
    
    public float getCharWidth(final char character) {
        if (character == '§') {
            return -1.0f;
        }
        if (character == ' ') {
            return 4.0f;
        }
        int indexInDefaultSheet = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\000\000\000\000\000\000\000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\000".indexOf(character);
        if (character > '\0' && indexInDefaultSheet != -1) {
            return (float)this.charWidthArray[indexInDefaultSheet];
        }
        return 0.0f;
    }
    
    public CompletableFuture<Void> reload(final PreparableReloadListener.PreparationBarrier var1, final ResourceManager var2, final ProfilerFiller var3, final ProfilerFiller var4, final Executor var5, final Executor var6) {
        return null;
    }
}
