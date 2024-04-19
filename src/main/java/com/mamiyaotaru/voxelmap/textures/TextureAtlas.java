// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.textures;

import org.apache.logging.log4j.LogManager;
import java.awt.image.BufferedImage;
import com.mamiyaotaru.voxelmap.util.GLShim;
import java.util.HashMap;
import java.util.Iterator;
import com.mamiyaotaru.voxelmap.util.ImageUtils;
import java.util.Objects;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Arrays;
import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public class TextureAtlas extends AbstractTexture
{
    private static final Logger logger;
    private final Map<String, Sprite> mapRegisteredSprites;
    private final Map<String, Sprite> mapUploadedSprites;
    private final String basePath;
    private final IIconCreator iconCreator;
    private final Sprite missingImage;
    private final Sprite failedImage;
    private Stitcher stitcher;
    
    public TextureAtlas(final String basePath) {
        this(basePath, null);
    }
    
    public TextureAtlas(final String basePath, final IIconCreator iconCreator) {
        this.mapRegisteredSprites = Maps.newHashMap();
        this.mapUploadedSprites = Maps.newHashMap();
        this.missingImage = new Sprite("missingno");
        this.failedImage = new Sprite("notfound");
        this.basePath = basePath;
        this.iconCreator = iconCreator;
    }
    
    private void initMissingImage() {
        final int[] missingTextureData = { 0 };
        Arrays.fill(missingTextureData, 0);
        this.missingImage.setIconWidth(1);
        this.missingImage.setIconHeight(1);
        this.missingImage.setTextureData(missingTextureData);
        this.failedImage.copyFrom(this.missingImage);
        this.failedImage.setTextureData(missingTextureData);
    }
    
    public void load(final ResourceManager resourceManager) throws IOException {
        if (this.iconCreator != null) {
            this.loadTextureAtlas(this.iconCreator);
        }
    }
    
    public void reset() {
        this.mapRegisteredSprites.clear();
        this.mapUploadedSprites.clear();
        this.initMissingImage();
        final int glMaxTextureSize = RenderSystem.maxSupportedTextureSize();
        this.stitcher = new Stitcher(glMaxTextureSize, glMaxTextureSize, 0);
    }
    
    public void loadTextureAtlas(final IIconCreator iconCreator) {
        this.reset();
        iconCreator.addIcons(this);
        this.stitch();
    }
    
    public void stitch() {
        for (final Map.Entry<String, Sprite> entry : this.mapRegisteredSprites.entrySet()) {
            final Sprite icon = entry.getValue();
            this.stitcher.addSprite(icon);
        }
        try {
            this.stitcher.doStitch();
        }
        catch (final StitcherException e) {
            throw e;
        }
        TextureAtlas.logger.info("Created: {}x{} {}-atlas", new Object[] { this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight(), this.basePath });
        final int id = this.getId();
        TextureUtilLegacy.allocateTextureImpl(id, 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
        final int[] zeros = new int[this.stitcher.getCurrentImageWidth() * this.stitcher.getCurrentImageHeight()];
        Arrays.fill(zeros, 0);
        TextureUtilLegacy.uploadTexture(this.getId(), zeros, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
        final HashMap<String, Sprite> tempMapRegisteredSprites = Maps.newHashMap(this.mapRegisteredSprites);
        for (final Sprite icon2 : this.stitcher.getStitchSlots()) {
            final String iconName = icon2.getIconName();
            tempMapRegisteredSprites.remove(iconName);
            this.mapUploadedSprites.put(iconName, icon2);
            try {
                TextureUtilLegacy.uploadTextureMipmap(new int[][] { icon2.getTextureData() }, icon2.getIconWidth(), icon2.getIconHeight(), icon2.getOriginX(), icon2.getOriginY(), false, false);
            }
            catch (final Throwable var19) {
                final CrashReport crashReport = CrashReport.forThrowable(var19, "Stitching texture atlas");
                final CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
                crashReportCategory.setDetail("Atlas path", (Object)this.basePath);
                crashReportCategory.setDetail("Sprite", (Object)icon2);
                throw new ReportedException(crashReport);
            }
        }
        for (final Sprite icon2 : tempMapRegisteredSprites.values()) {
            icon2.copyFrom(this.missingImage);
        }
        this.mapRegisteredSprites.clear();
        this.missingImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        this.failedImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        final String replaceAll = this.basePath.replaceAll("/", "_");
        ImageUtils.saveImage(replaceAll, id, 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
    }
    
    public void stitchNew() {
        for (final Map.Entry<String, Sprite> entry : this.mapRegisteredSprites.entrySet()) {
            final Sprite icon = entry.getValue();
            this.stitcher.addSprite(icon);
        }
        final int oldWidth = this.stitcher.getCurrentImageWidth();
        final int oldHeight = this.stitcher.getCurrentImageHeight();
        try {
            this.stitcher.doStitchNew();
        }
        catch (final StitcherException var20) {
            throw var20;
        }
        if (oldWidth != this.stitcher.getCurrentImageWidth() || oldHeight != this.stitcher.getCurrentImageHeight()) {
            TextureAtlas.logger.info("Resized to: {}x{} {}-atlas", new Object[] { this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight(), this.basePath });
            final int getId = this.getId();
            Objects.requireNonNull(this);
            TextureUtilLegacy.allocateTextureImpl(getId, 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
            final int[] zeros = new int[this.stitcher.getCurrentImageWidth() * this.stitcher.getCurrentImageHeight()];
            Arrays.fill(zeros, 0);
            TextureUtilLegacy.uploadTexture(this.getId(), zeros, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
        }
        else {
            GLShim.glBindTexture(3553, this.id);
        }
        final HashMap<String, Sprite> tempMapRegisteredSprites = Maps.newHashMap(this.mapRegisteredSprites);
        for (final Sprite icon2 : this.stitcher.getStitchSlots()) {
            final String iconName = icon2.getIconName();
            tempMapRegisteredSprites.remove(iconName);
            this.mapUploadedSprites.put(iconName, icon2);
            try {
                TextureUtilLegacy.uploadTextureMipmap(new int[][] { icon2.getTextureData() }, icon2.getIconWidth(), icon2.getIconHeight(), icon2.getOriginX(), icon2.getOriginY(), false, false);
            }
            catch (final Throwable var21) {
                final CrashReport crashReport = CrashReport.forThrowable(var21, "Stitching texture atlas");
                final CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
                crashReportCategory.setDetail("Atlas path", (Object)this.basePath);
                crashReportCategory.setDetail("Sprite", (Object)icon2);
                throw new ReportedException(crashReport);
            }
        }
        for (final Sprite icon2 : tempMapRegisteredSprites.values()) {
            icon2.copyFrom(this.missingImage);
        }
        this.mapRegisteredSprites.clear();
        this.missingImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        this.failedImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        if (oldWidth != this.stitcher.getCurrentImageWidth() || oldHeight != this.stitcher.getCurrentImageHeight()) {
            final String replaceAll = this.basePath.replaceAll("/", "_");
            final int id = this.getId();
            ImageUtils.saveImage(replaceAll, id, 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
        }
    }
    
    public Sprite getIconAt(final float x, final float y) {
        final Iterator<Map.Entry<String, Sprite>> uploadedSpritesEntriesIterator = this.mapUploadedSprites.entrySet().iterator();
        while (uploadedSpritesEntriesIterator.hasNext()) {
            final Sprite icon = (Sprite) uploadedSpritesEntriesIterator.next().getValue();
            if (x >= icon.originX && x < icon.originX + icon.width && y >= icon.originY && y < icon.originY + icon.height) {
                return icon;
            }
        }
        return this.missingImage;
    }
    
    public Sprite getAtlasSprite(final String name) {
        Sprite icon = this.mapUploadedSprites.get(name);
        if (icon == null) {
            icon = this.missingImage;
        }
        return icon;
    }
    
    public Sprite getAtlasSpriteIncludingYetToBeStitched(final String name) {
        Sprite icon = this.mapUploadedSprites.get(name);
        if (icon == null) {
            icon = this.mapRegisteredSprites.get(name);
        }
        if (icon == null) {
            icon = this.missingImage;
        }
        return icon;
    }
    
    public Sprite registerIconForResource(final ResourceLocation resourceLocation, final ResourceManager resourceManager) {
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Location cannot be null!");
        }
        Sprite icon = this.mapRegisteredSprites.get(resourceLocation.toString());
        if (icon == null) {
            icon = Sprite.spriteFromResourceLocation(resourceLocation);
            try {
                final Resource entryResource = resourceManager.getResource(resourceLocation);
                final BufferedImage entryBufferedImage = TextureUtilLegacy.readBufferedImage(entryResource.getInputStream());
                icon.bufferedImageToIntData(entryBufferedImage);
                entryBufferedImage.flush();
            }
            catch (final RuntimeException var23) {
                TextureAtlas.logger.error("Unable to parse metadata from " + resourceLocation, (Throwable)var23);
            }
            catch (final IOException var24) {
                TextureAtlas.logger.error("Using missing texture, unable to load " + resourceLocation, (Throwable)var24);
            }
            this.mapRegisteredSprites.put(resourceLocation.toString(), icon);
        }
        return icon;
    }
    
    public Sprite registerIconForBufferedImage(final String name, final BufferedImage bufferedImage) {
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Name cannot be null!");
        }
        Sprite icon = this.mapRegisteredSprites.get(name);
        if (icon == null) {
            icon = Sprite.spriteFromString(name);
            icon.bufferedImageToIntData(bufferedImage);
            bufferedImage.flush();
            for (final Sprite existing : this.mapUploadedSprites.values()) {
                if (Arrays.equals(existing.imageData, icon.imageData)) {
                    this.registerMaskedIcon(name, existing);
                    return existing;
                }
            }
            for (final Sprite existing : this.mapRegisteredSprites.values()) {
                if (Arrays.equals(existing.imageData, icon.imageData)) {
                    this.registerMaskedIcon(name, existing);
                    return existing;
                }
            }
            this.mapRegisteredSprites.put(name, icon);
        }
        return icon;
    }
    
    public void registerOrOverwriteSprite(final String name, final BufferedImage bufferedImage) {
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Name cannot be null!");
        }
        Sprite icon = this.mapRegisteredSprites.get(name);
        if (icon != null) {
            icon.bufferedImageToIntData(bufferedImage);
        }
        else {
            icon = this.getAtlasSprite(name);
            if (icon != null) {
                icon.bufferedImageToIntData(bufferedImage);
                try {
                    GLShim.glBindTexture(3553, this.id);
                    TextureUtilLegacy.uploadTextureMipmap(new int[][] { icon.getTextureData() }, icon.getIconWidth(), icon.getIconHeight(), icon.getOriginX(), icon.getOriginY(), false, false);
                }
                catch (final Throwable var19) {
                    final CrashReport crashReport = CrashReport.forThrowable(var19, "Stitching texture atlas");
                    final CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
                    crashReportCategory.setDetail("Atlas path", this.basePath);
                    crashReportCategory.setDetail("Sprite", icon);
                    throw new ReportedException(crashReport);
                }
            }
        }
        bufferedImage.flush();
    }
    
    public Sprite getMissingImage() {
        return this.missingImage;
    }
    
    public Sprite getFailedImage() {
        return this.failedImage;
    }
    
    public void registerFailedIcon(final String name) {
        this.mapUploadedSprites.put(name, this.failedImage);
    }
    
    public void registerMaskedIcon(final String name, final Sprite originalIcon) {
        Sprite existingIcon = this.mapUploadedSprites.get(name);
        if (existingIcon == null) {
            existingIcon = this.mapRegisteredSprites.get(name);
        }
        if (existingIcon == null) {
            this.mapUploadedSprites.put(name, originalIcon);
        }
    }
    
    public int getWidth() {
        return this.stitcher.getCurrentWidth();
    }
    
    public int getHeight() {
        return this.stitcher.getCurrentHeight();
    }
    
    public int getImageWidth() {
        return this.stitcher.getCurrentImageWidth();
    }
    
    public int getImageHeight() {
        return this.stitcher.getCurrentImageHeight();
    }
    
    static {
        logger = LogManager.getLogger();
    }
}
