// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import java.nio.file.Path;
import java.util.Enumeration;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Properties;
import com.mamiyaotaru.voxelmap.util.MessageUtils;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import com.mamiyaotaru.voxelmap.util.VoxelMapMutableBlockPos;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;

import com.google.common.collect.ImmutableList;
import com.ibm.icu.impl.UResource.Array;
import com.mamiyaotaru.voxelmap.forgemod.Share;
import java.awt.image.RasterFormatException;
import java.util.List;
import com.mamiyaotaru.voxelmap.util.BlockModel;
import java.util.Collection;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.util.ColorUtils;
import java.io.IOException;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Material;

import java.awt.Graphics;
import java.awt.Image;
import java.io.InputStream;
import javax.imageio.ImageIO;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;

import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.ImageUtils;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import java.util.Arrays;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import com.mamiyaotaru.voxelmap.util.BlockRepository;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.image.BufferedImage;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;

import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;

public class ColorManager implements IColorManager
{
    private IVoxelMap master;
    Minecraft game;
    private boolean resourcePacksChanged;
    private ClientLevel world;
    private BufferedImage terrainBuff;
    private BufferedImage colorPicker;
    private int sizeOfBiomeArray;
    private int[] blockColors;
    private int[] blockColorsWithDefaultTint;
    private HashSet<Integer> biomeTintsAvailable;
    private boolean optifineInstalled;
    private HashMap<Integer, int[][]> blockTintTables;
    private HashSet<Integer> biomeTextureAvailable;
    private HashMap<String, Integer> blockBiomeSpecificColors;
    private float failedToLoadX;
    private float failedToLoadY;
    private String renderPassThreeBlendMode;
    private Random random;
    private boolean loaded;
    private final VoxelMapMutableBlockPos dummyBlockPos;
    private final ColorResolver spruceColorResolver;
    private final ColorResolver birchColorResolver;
    private final ColorResolver grassColorResolver;
    private final ColorResolver foliageColorResolver;
    private final ColorResolver waterColorResolver;
    private final Vector3f fullbright;
    
    public ColorManager(final IVoxelMap master) {
        this.game = null;
        this.resourcePacksChanged = false;
        this.world = null;
        this.terrainBuff = null;
        this.sizeOfBiomeArray = 0;
        this.blockColors = new int[16384];
        this.blockColorsWithDefaultTint = new int[16384];
        this.biomeTintsAvailable = new HashSet<Integer>();
        this.optifineInstalled = false;
        this.blockTintTables = new HashMap<Integer, int[][]>();
        this.biomeTextureAvailable = new HashSet<Integer>();
        this.blockBiomeSpecificColors = new HashMap<String, Integer>();
        this.failedToLoadX = 0.0f;
        this.failedToLoadY = 0.0f;
        this.random = new Random();
        new Object();
        this.loaded = false;
        this.dummyBlockPos = new VoxelMapMutableBlockPos(BlockPos.ZERO.getX(), BlockPos.ZERO.getY(), BlockPos.ZERO.getZ());
        this.spruceColorResolver = new ColorResolver() {
            @Override
            public int getColorAtPos(final Biome biome, final BlockPos blockPos) {
                return FoliageColor.getEvergreenColor();
            }
        };
        this.birchColorResolver = new ColorResolver() {
            @Override
            public int getColorAtPos(final Biome biome, final BlockPos blockPos) {
                return FoliageColor.getBirchColor();
            }
        };
        this.grassColorResolver = new ColorResolver() {
            @Override
            public int getColorAtPos(final Biome biome, final BlockPos blockPos) {
                return biome.getGrassColor((double)blockPos.getX(), (double)blockPos.getZ());
            }
        };
        this.foliageColorResolver = new ColorResolver() {
            @Override
            public int getColorAtPos(final Biome biome, final BlockPos blockPos) {
                return biome.getFoliageColor();
            }
        };
        this.waterColorResolver = new ColorResolver() {
            @Override
            public int getColorAtPos(final Biome biome, final BlockPos blockPos) {
                return biome.getWaterColor();
            }
        };
        this.master = master;
        this.game = Minecraft.getInstance();
        this.optifineInstalled = false;
        Field ofProfiler = null;
        try {
            ofProfiler = Options.class.getDeclaredField("ofProfiler");
        }
        catch (final SecurityException ex) {}
        catch (final NoSuchFieldException ex2) {}
        finally {
            if (ofProfiler != null) {
                this.optifineInstalled = true;
            }
        }
        int largestBiomeID = 0;
        for (final Biome biome : BuiltinRegistries.BIOME) {
            final int biomeID = BuiltinRegistries.BIOME.getId(biome);
            if (biomeID > largestBiomeID) {
                largestBiomeID = biomeID;
            }
        }
        this.sizeOfBiomeArray = largestBiomeID + 1;
		this.fullbright = new Vector3f(1.0f, 1.0f, 1.0f);
    }
    
    @Override
    public int getAirColor() {
        return this.blockColors[BlockRepository.airID];
    }
    
    @Override
    public BufferedImage getColorPicker() {
        return this.colorPicker;
    }
    
    @Override
    public void onResourceManagerReload(final ResourceManager resourceManager) {
        this.resourcePacksChanged = true;
    }
    
    @Override
    public boolean checkForChanges() {
        boolean biomesChanged = false;
        if (this.game.level != null && this.game.level != this.world) {
            this.world = this.game.level;
            int largestBiomeID = 0;
            for (final Biome biome : this.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)) {
                final int biomeID = this.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getId(biome);
                if (biomeID > largestBiomeID) {
                    largestBiomeID = biomeID;
                }
            }
            if (this.sizeOfBiomeArray != largestBiomeID + 1) {
                this.sizeOfBiomeArray = largestBiomeID + 1;
                biomesChanged = true;
            }
        }
        final boolean changed = this.resourcePacksChanged || biomesChanged;
        this.resourcePacksChanged = false;
        if (changed) {
            this.loadColors();
        }
        return changed;
    }
    
    public void loadColors() {
        this.game.player.getSkinTextureLocation();
        BlockRepository.getBlocks();
        BiomeRepository.getBiomes();
        this.loadColorPicker();
        this.loadTexturePackTerrainImage();
        final TextureAtlasSprite missing = this.game.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("missingno"));
        this.failedToLoadX = missing.getU0();
        this.failedToLoadY = missing.getV0();
        this.loaded = false;
        try {
            Arrays.fill(this.blockColors, -16842497);
            Arrays.fill(this.blockColorsWithDefaultTint, -16842497);
            this.loadSpecialColors();
            this.biomeTintsAvailable.clear();
            this.biomeTextureAvailable.clear();
            this.blockBiomeSpecificColors.clear();
            this.blockTintTables.clear();
            if (this.optifineInstalled) {
                try {
                    this.processCTM();
                }
                catch (final Exception e) {
                    System.err.println("error loading CTM " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
            try {
                this.loadWaterColor();
            }
            catch (final Exception e) {
                System.err.println("error getting water color " + e.getLocalizedMessage());
            }
            if (this.optifineInstalled) {
                try {
                    this.processColorProperties();
                }
                catch (final Exception e) {
                    System.err.println("error loading custom color properties " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
            this.master.getMap().forceFullRender(true);
        }
        catch (final Exception e) {
            System.err.println("error loading pack");
            e.printStackTrace();
        }
        this.loaded = true;
    }
    
    @Override
    public final BufferedImage getBlockImage(final BlockState blockState, final ItemStack stack, final Level world, final float iconScale, final float captureDepth) {
        try {
            this.game.getBlockRenderer();
            final BakedModel model = this.game.getItemRenderer().getModel(stack, world, null, 0);
            this.drawModel(Direction.EAST, model, stack, iconScale, captureDepth);
            final BufferedImage blockImage = ImageUtils.createBufferedImageFromGLID(GLUtils.fboTextureID);
            return blockImage;
        }
        catch (final Exception e) {
            System.out.println("error getting block armor image for " + blockState.toString() + ": " + e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void drawModel(final Direction facing, final BakedModel model, final ItemStack stack, final float scale, final float captureDepth) {
        final float size = 8.0f * scale;
        final ItemTransforms transforms = model.getTransforms();
        final ItemTransform headTransforms = transforms.head;
        final Vector3f translations = headTransforms.translation;
        final float transX = translations.x() * size + 0.5f * size;
        final float transY = translations.y() * size + 0.5f * size;
        final float transZ = -translations.z() * size + 0.5f * size;
        final Vector3f rotations = headTransforms.rotation;
        final float rotX = rotations.x();
        final float rotY = -rotations.y();
        final float rotZ = rotations.z();
        GLShim.glBindTexture(3553, GLUtils.fboTextureID);
        final int width = GLShim.glGetTexLevelParameteri(3553, 0, 4096);
        final int height = GLShim.glGetTexLevelParameteri(3553, 0, 4097);
        GLShim.glBindTexture(3553, 0);
        GLShim.glViewport(0, 0, width, height);
        final Matrix4f minimapProjectionMatrix = RenderSystem.getProjectionMatrix();
        final Matrix4f matrix4f = Matrix4f.orthographic(0.0f, (float)width, 0.0f, (float)height, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f);
        final PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.setIdentity();
        matrixStack.translate(0.0, 0.0, -3000.0 + captureDepth * scale);
        RenderSystem.applyModelViewMatrix();
        GLUtils.bindFrameBuffer();
        GLShim.glDepthMask(true);
        GLShim.glEnable(2929);
        GLShim.glEnable(3553);
        GLShim.glEnable(3042);
        GLShim.glDisable(2884);
        GLShim.glBlendFunc(770, 771);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GLShim.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        GLShim.glClearDepth(1.0);
        GLShim.glClear(16640);
        GLShim.glBlendFunc(770, 771);
        matrixStack.pushPose();
        matrixStack.translate((double)(width / 2 - size / 2.0f + transX), (double)(height / 2 - size / 2.0f + transY), (double)(0.0f + transZ));
        matrixStack.scale(size, size, size);
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        GLUtils.img(TextureAtlas.LOCATION_BLOCKS);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotY));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotX));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotZ));
        if (facing == Direction.UP) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
        }
        RenderSystem.applyModelViewMatrix();
        final Vector4f fullbright2 = new Vector4f(this.fullbright);
        fullbright2.transform(matrixStack.last().pose());
        final Vector3f fullbright3 = new Vector3f(fullbright2);
        RenderSystem.setShaderLights(fullbright3, fullbright3);
        final PoseStack newMatrixStack = new PoseStack();
        final MultiBufferSource.BufferSource immediate = this.game.renderBuffers().bufferSource();
        this.game.getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, newMatrixStack, (MultiBufferSource)immediate, 15728880, OverlayTexture.NO_OVERLAY, model);
        immediate.endBatch();
        matrixStack.popPose();
        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();
        GLShim.glEnable(2884);
        GLShim.glDisable(2929);
        GLShim.glDepthMask(false);
        GLUtils.unbindFrameBuffer();
        RenderSystem.setProjectionMatrix(minimapProjectionMatrix);
        GLShim.glViewport(0, 0, this.game.getWindow().getWidth(), this.game.getWindow().getHeight());
    }
    
    private void loadColorPicker() {
        try {
            final InputStream is = this.game.getResourceManager().getResource(new ResourceLocation("voxelmap", "images/colorpicker.png")).getInputStream();
            final Image picker = ImageIO.read(is);
            is.close();
            this.colorPicker = new BufferedImage(picker.getWidth(null), picker.getHeight(null), 2);
            final Graphics gfx = this.colorPicker.createGraphics();
            gfx.drawImage(picker, 0, 0, null);
            gfx.dispose();
        }
        catch (final Exception e) {
            System.err.println("Error loading color picker: " + e.getLocalizedMessage());
        }
    }
    
    @Override
    public void setSkyColor(final int skyColor) {
        this.blockColors[BlockRepository.airID] = skyColor;
        this.blockColors[BlockRepository.voidAirID] = skyColor;
    }
    
    private void loadTexturePackTerrainImage() {
        try {
            final TextureManager textureManager = this.game.getTextureManager();
            textureManager.bindForSetup(TextureAtlas.LOCATION_BLOCKS);
            final BufferedImage terrainStitched = ImageUtils.createBufferedImageFromCurrentGLImage();
            this.terrainBuff = new BufferedImage(terrainStitched.getWidth(null), terrainStitched.getHeight(null), 6);
            final Graphics gfx = this.terrainBuff.createGraphics();
            gfx.drawImage(terrainStitched, 0, 0, null);
            gfx.dispose();
        }
        catch (final Exception e) {
            System.err.println("Error processing new resource pack: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    
    private void loadSpecialColors() {
        for (final BlockState blockState : BlockRepository.pistonTechBlock.getStateDefinition().getPossibleStates()) {
            final int blockStateID = BlockRepository.getStateId(blockState);
            this.blockColors[blockStateID] = 0;
        }
        for (final BlockState blockState : BlockRepository.barrier.getStateDefinition().getPossibleStates()) {
            final int blockStateID = BlockRepository.getStateId(blockState);
            this.blockColors[blockStateID] = 0;
        }
    }
    
    private void loadWaterColor() {
        int waterRGB = -1;
        BlockState blockState = BlockRepository.water.defaultBlockState();
        int blockStateID = BlockRepository.getStateId(blockState);
        waterRGB = this.getBlockColor(blockStateID);
        int waterMult = -1;
        if (this.optifineInstalled) {
            InputStream is = null;
            try {
                is = this.game.getResourceManager().getResource(new ResourceLocation("optifine/colormap/water.png")).getInputStream();
            }
            catch (final IOException e) {
                is = null;
            }
            if (is != null) {
                try {
                    final Image waterColor = ImageIO.read(is);
                    is.close();
                    final BufferedImage waterColorBuff = new BufferedImage(waterColor.getWidth(null), waterColor.getHeight(null), 1);
                    final Graphics gfx = waterColorBuff.createGraphics();
                    gfx.drawImage(waterColor, 0, 0, null);
                    gfx.dispose();
                    final Biome biome = BiomeRepository.FOREST;
                    double var1 = Mth.clamp(ColorManager.getTemperature(biome, new BlockPos(0, 64, 0)), 0.0f, 1.0f);
                    double var2 = Mth.clamp(biome.getDownfall(), 0.0f, 1.0f);
                    var2 *= var1;
                    var1 = 1.0 - var1;
                    var2 = 1.0 - var2;
                    waterMult = (waterColorBuff.getRGB((int)((waterColorBuff.getWidth() - 1) * var1), (int)((waterColorBuff.getHeight() - 1) * var2)) & 0xFFFFFF);
                }
                catch (final Exception ex) {}
            }
        }
        if (waterMult != -1 && waterMult != 0) {
            waterRGB = ColorUtils.colorMultiplier(waterRGB, waterMult | 0xFF000000);
        }
        else {
            waterRGB = ColorUtils.colorMultiplier(waterRGB, BiomeRepository.FOREST.getWaterColor() | 0xFF000000);
        }
        for (int t = 0; t < 16; ++t) {
            blockState = BlockRepository.water.defaultBlockState().setValue(LiquidBlock.LEVEL, t);
            blockStateID = BlockRepository.getStateId(blockState);
            this.blockColorsWithDefaultTint[blockStateID] = waterRGB;
        }
    }
    
    @Override
    public final int getBlockColorWithDefaultTint(final VoxelMapMutableBlockPos blockPos, final int blockStateID) {
        if (!this.loaded) {
            return 0;
        }
        int col = 452984832;
        try {
            col = this.blockColorsWithDefaultTint[blockStateID];
        }
        catch (final ArrayIndexOutOfBoundsException ex) {}
        if (col != -16842497 && col != 452984832) {
            return col;
        }
        return this.getBlockColor(blockPos, blockStateID);
    }
    
    @Override
    public final int getBlockColor(final VoxelMapMutableBlockPos blockPos, final int blockStateID, final int biomeID) {
        if (this.loaded) {
            if (this.optifineInstalled && this.biomeTextureAvailable.contains(blockStateID)) {
                final Integer col = this.blockBiomeSpecificColors.get("" + blockStateID + " " + biomeID);
                if (col != null) {
                    return col;
                }
            }
            return this.getBlockColor(blockPos, blockStateID);
        }
        return 0;
    }
    
    private int getBlockColor(final int blockStateID) {
        return this.getBlockColor(this.dummyBlockPos, blockStateID);
    }
    
    private final int getBlockColor(final VoxelMapMutableBlockPos blockPos, final int blockStateID) {
        int col = 452984832;
        try {
            col = this.blockColors[blockStateID];
        }
        catch (final ArrayIndexOutOfBoundsException e) {
            this.resizeColorArrays(blockStateID);
        }
        if (col == -16842497) {
            final BlockState blockState = BlockRepository.getStateById(blockStateID);
            final int[] blockColors = this.blockColors;
            final int color = this.getColor(blockPos, blockState);
            blockColors[blockStateID] = color;
            col = color;
        }
        return col;
    }
    
    private synchronized void resizeColorArrays(final int queriedID) {
        if (queriedID >= this.blockColors.length) {
            final int[] newBlockColors = new int[this.blockColors.length * 2];
            final int[] newBlockColorsWithDefaultTint = new int[this.blockColors.length * 2];
            System.arraycopy(this.blockColors, 0, newBlockColors, 0, this.blockColors.length);
            System.arraycopy(this.blockColorsWithDefaultTint, 0, newBlockColorsWithDefaultTint, 0, this.blockColorsWithDefaultTint.length);
            Arrays.fill(newBlockColors, this.blockColors.length, newBlockColors.length, -16842497);
            Arrays.fill(newBlockColorsWithDefaultTint, this.blockColorsWithDefaultTint.length, newBlockColorsWithDefaultTint.length, -16842497);
            this.blockColors = newBlockColors;
            this.blockColorsWithDefaultTint = newBlockColorsWithDefaultTint;
        }
    }
    
    private int getColor(final VoxelMapMutableBlockPos blockPos, final BlockState blockState) {
        try {
            final int colorForBlockPosBlockStateAndFacing;
            int color = colorForBlockPosBlockStateAndFacing = this.getColorForBlockPosBlockStateAndFacing(blockState, Direction.UP);
            this.getClass();
            if (colorForBlockPosBlockStateAndFacing == 452984832) {
                final BlockRenderDispatcher blockRendererDispatcher = this.game.getBlockRenderer();
                color = this.getColorForTerrainSprite(blockState, blockRendererDispatcher);
            }
            final Block block = blockState.getBlock();
            if (block == BlockRepository.cobweb) {
                color |= 0xFF000000;
            }
            if (block == BlockRepository.redstone) {
                color = ColorUtils.colorMultiplier(color, this.game.getBlockColors().getColor(blockState, (BlockAndTintGetter)null, (BlockPos)null, 0) | 0xFF000000);
            }
            if (BlockRepository.biomeBlocks.contains(block)) {
                this.applyDefaultBuiltInShading(blockState, color);
            }
            else {
                this.checkForBiomeTinting(blockPos, blockState, color);
            }
            if (BlockRepository.shapedBlocks.contains(block)) {
                color = this.applyShape(block, color);
            }
            if ((color >> 24 & 0xFF) < 27) {
                color |= 0x1B000000;
            }
            return color;
        }
        catch (final Exception e) {
            System.err.println("failed getting color: " + blockState.getBlock().getName().getString() + " pos: " + blockPos.x + "," + blockPos.y + "," + blockPos.z);
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
            return 452984832;
        }
    }
    
    private int getColorForBlockPosBlockStateAndFacing(final BlockState blockState, final Direction facing) {
        this.getClass();
        int color = 452984832;
        try {
            final RenderShape blockRenderType = blockState.getRenderShape();
            final BlockRenderDispatcher blockRendererDispatcher = this.game.getBlockRenderer();
            if (blockRenderType == RenderShape.MODEL) {
                final BakedModel iBakedModel = blockRendererDispatcher.getBlockModel(blockState);
                final List<BakedQuad> quads = new ArrayList<BakedQuad>();
                quads.addAll(iBakedModel.getQuads(blockState, facing, this.random));
                quads.addAll(iBakedModel.getQuads(blockState, (Direction)null, this.random));
                final BlockModel model = new BlockModel(quads, this.failedToLoadX, this.failedToLoadY);
                if (model.numberOfFaces() > 0) {
                    final BufferedImage modelImage = model.getImage(this.terrainBuff);
                    if (modelImage != null) {
                        color = this.getColorForCoordinatesAndImage(new float[] { 0.0f, 1.0f, 0.0f, 1.0f }, modelImage);
                    }
                }
            }
        }
        catch (final Exception e) {
            this.getClass();
            color = 452984832;
        }
        return color;
    }
    
    private int getColorForTerrainSprite(final BlockState blockState, final BlockRenderDispatcher blockRendererDispatcher) {
        this.getClass();
        int color = 452984832;
        final BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
        TextureAtlasSprite icon = blockModelShapes.getParticleIcon(blockState);
        if (icon == blockModelShapes.getModelManager().getMissingModel().getParticleIcon()) {
            final Block block = blockState.getBlock();
            final Material material = blockState.getMaterial();
            if (block instanceof LiquidBlock) {
                if (material == Material.WATER) {
                    icon = this.game.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("minecraft:blocks/water_flow"));
                }
                else if (material == Material.LAVA) {
                    icon = this.game.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("minecraft:blocks/lava_flow"));
                }
            }
            else if (material == Material.WATER) {
                icon = this.game.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("minecraft:blocks/water_still"));
            }
            else if (material == Material.LAVA) {
                icon = this.game.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("minecraft:blocks/lava_still"));
            }
        }
        color = this.getColorForIcon(icon);
        return color;
    }
    
    private int getColorForIcon(final TextureAtlasSprite icon) {
        this.getClass();
        int color = 452984832;
        if (icon != null) {
            final float left = icon.getU0();
            final float right = icon.getU1();
            final float top = icon.getV0();
            final float bottom = icon.getV1();
            color = this.getColorForCoordinatesAndImage(new float[] { left, right, top, bottom }, this.terrainBuff);
        }
        return color;
    }
    
    private int getColorForCoordinatesAndImage(final float[] uv, final BufferedImage imageBuff) {
        int color = 452984832;
        if (uv[0] != this.failedToLoadX || uv[2] != this.failedToLoadY) {
            final int left = (int)(uv[0] * imageBuff.getWidth());
            final int right = (int)Math.ceil(uv[1] * imageBuff.getWidth());
            final int top = (int)(uv[2] * imageBuff.getHeight());
            final int bottom = (int)Math.ceil(uv[3] * imageBuff.getHeight());
            try {
                final BufferedImage blockTexture = imageBuff.getSubimage(left, top, right - left, bottom - top);
                final Image singlePixel = blockTexture.getScaledInstance(1, 1, 4);
                final BufferedImage singlePixelBuff = new BufferedImage(1, 1, imageBuff.getType());
                final Graphics gfx = singlePixelBuff.createGraphics();
                gfx.drawImage(singlePixel, 0, 0, null);
                gfx.dispose();
                color = singlePixelBuff.getRGB(0, 0);
            }
            catch (final RasterFormatException e) {
                System.out.println("error getting color");
                System.out.println(left + " " + right + " " + top + " " + bottom);
                color = 452984832;
            }
        }
        return color;
    }
    
    private void applyDefaultBuiltInShading(final BlockState blockState, final int color) {
        final Block block = blockState.getBlock();
        final int blockStateID = BlockRepository.getStateId(blockState);
        if (block == BlockRepository.largeFern || block == BlockRepository.tallGrass || block == BlockRepository.reeds) {
            this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, GrassColor.get(0.7, 0.8) | 0xFF000000);
        }
        else {
            this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, this.game.getBlockColors().getColor(blockState, (BlockAndTintGetter)null, (BlockPos)null, 0) | 0xFF000000);
        }
    }
    
    private void checkForBiomeTinting(final VoxelMapMutableBlockPos blockPos, final BlockState blockState, final int color) {
        try {
            final Block block = blockState.getBlock();
            Registry.BLOCK.getKey(block);
            int tint = -1;
            final VoxelMapMutableBlockPos tempBlockPos = new VoxelMapMutableBlockPos(0, 0, 0);
            if (blockPos == this.dummyBlockPos) {
                tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, (byte)4);
            }
            else {
                final LevelChunk chunk = (LevelChunk) this.game.player.level.getChunk((BlockPos) blockPos);
                if (chunk != null && !((LevelChunk)chunk).isEmpty() && this.game.level.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
                    tint = (this.game.getBlockColors().getColor(blockState, (BlockAndTintGetter)this.game.level, (BlockPos)blockPos, 1) | 0xFF000000);
                }
                else {
                    tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, (byte)4);
                }
            }
            if (tint != 16777215 && tint != -1) {
                final int blockStateID = BlockRepository.getStateId(blockState);
                this.biomeTintsAvailable.add(blockStateID);
                this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(color, tint);
                this.createTintTable(blockState, tempBlockPos);
            }
            else {
                this.blockColorsWithDefaultTint[BlockRepository.getStateId(blockState)] = 452984832;
            }
        }
        catch (final Exception e) {
            this.blockColorsWithDefaultTint[BlockRepository.getStateId(blockState)] = 452984832;
        }
    }
    
    private int tintFromFakePlacedBlock(final BlockState blockState, final VoxelMapMutableBlockPos loopBlockPos, final byte biomeID) {
        final Level world = this.game.level;
        if (world == null) {
            return -1;
        }
        if (blockState.getBlock() == null) {
            return -1;
        }
        Share.updateCloudsLock.lock();
        int tint = -1;
        try {
            final int fakeX = (int)this.game.player.getX() - 32;
            final int fakeZ = (int)this.game.player.getZ() - 32;
            final LevelChunk chunk = (LevelChunk) world.getChunk((BlockPos)loopBlockPos.withXYZ(fakeX, 0, fakeZ));
            final BlockState actualBlockState = world.getBlockState((BlockPos)loopBlockPos);
            chunk.setBlockState((BlockPos)loopBlockPos, blockState, false);
            List<Biome> currentBiomes = new ArrayList<Biome>();
            for(BlockPos blockPos : chunk.getBlockEntitiesPos()) {
            	Biome biome = world.getBiome(blockPos).value();
            	if(!currentBiomes.contains(biome)) continue;
            	
            	currentBiomes.add(biome);
            }
            final Biome[] originalBiomes = new Biome[currentBiomes.size()];
            System.arraycopy(currentBiomes, 0, originalBiomes, 0, currentBiomes.size());
            Arrays.fill(currentBiomes.toArray(), world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).byId((int)biomeID));
            tint = (this.game.getBlockColors().getColor(blockState, (BlockAndTintGetter)world, (BlockPos)loopBlockPos, 1) | 0xFF000000);
            System.arraycopy(originalBiomes, 0, currentBiomes, 0, currentBiomes.size());
            chunk.setBlockState((BlockPos)loopBlockPos, actualBlockState, false);
        }
        catch (final Exception ex) {}
        finally {
            Share.updateCloudsLock.unlock();
        }
        return tint;
    }
    
    private void createTintTable(final BlockState blockState, final VoxelMapMutableBlockPos loopBlockPos) {
        final ClientLevel world = this.game.level;
        if (world == null) {
            return;
        }
        final Block block = blockState.getBlock();
        if (block == null) {
            return;
        }
        Share.updateCloudsLock.lock();
        try {
            ReflectionUtils.getPrivateFieldValueByType(this.game.getBlockColors(), BlockColors.class, HashMap.class);
            final int[][] array;
            final int[][] tints = array = new int[this.sizeOfBiomeArray][32];
            for (final int[] row : array) {
                Arrays.fill(row, -1);
            }
            final int fakeX = (int)this.game.player.getX();
            final int fakeZ = (int)this.game.player.getZ();
            final LevelChunk chunk = (LevelChunk) world.getChunk((BlockPos) loopBlockPos.withXYZ(fakeX, 64, fakeZ));
            final BlockState actualBlockState = world.getBlockState((BlockPos)loopBlockPos);
            chunk.setBlockState((BlockPos)loopBlockPos, blockState, false);
            final Holder<Biome> biomeArray = (Holder<Biome>) ReflectionUtils.getPrivateFieldValueByType(chunk, LevelChunk.class, Holder.class);
            final Biome[] currentBiomes = (Biome[]) ReflectionUtils.getPrivateFieldValueByType(biomeArray, Holder.class, Biome[].class);
            final Biome[] originalBiomes = new Biome[currentBiomes.length];
            System.arraycopy(currentBiomes, 0, originalBiomes, 0, currentBiomes.length);
            for (int biomeID = 0; biomeID < this.sizeOfBiomeArray; ++biomeID) {
                final Biome biome = (Biome)world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).byId(biomeID);
                if (biome != null) {
                    final int[] row2 = new int[32];
                    Arrays.fill(currentBiomes, biome);
                    world.onChunkLoaded(chunk.getPos());
                    int tint = this.game.getBlockColors().getColor(blockState, (BlockAndTintGetter)world, (BlockPos)loopBlockPos.withXYZ(fakeX + 264, 64, fakeZ + 264), 0) | 0xFF000000;
                    tint = (this.game.getBlockColors().getColor(blockState, (BlockAndTintGetter)world, (BlockPos)loopBlockPos.withXYZ(fakeX, 64, fakeZ), 0) | 0xFF000000);
                    Arrays.fill(row2, tint);
                    tints[biomeID] = row2;
                }
            }
            System.arraycopy(originalBiomes, 0, currentBiomes, 0, currentBiomes.length);
            chunk.setBlockState((BlockPos)loopBlockPos, actualBlockState, false);
            world.onChunkLoaded(chunk.getPos());
            final int blockStateID = BlockRepository.getStateId(blockState);
            this.blockTintTables.put(blockStateID, tints);
        }
        catch (final Exception e) {
            System.out.println("error creatint tint table: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        finally {
            Share.updateCloudsLock.unlock();
        }
    }
    
    @Override
    public int getBiomeTint(final AbstractMapData mapData, final Level world, final BlockState blockState, final int blockStateID, final VoxelMapMutableBlockPos blockPos, final VoxelMapMutableBlockPos loopBlockPos, final int startX, final int startZ) {
        final LevelChunk chunk = (LevelChunk) world.getChunk((BlockPos)blockPos);
        final boolean live = chunk != null && !((LevelChunk)chunk).isEmpty() && this.game.level.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        int tint = -2;
        Label_0445: {
            if (!this.optifineInstalled) {
                if (live || !this.biomeTintsAvailable.contains(blockStateID)) {
                    break Label_0445;
                }
            }
            try {
                final int[][] tints = this.blockTintTables.get(blockStateID);
                if (tints != null) {
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    for (int t = blockPos.getX() - 1; t <= blockPos.getX() + 1; ++t) {
                        for (int s = blockPos.getZ() - 1; s <= blockPos.getZ() + 1; ++s) {
                            int biomeID = 0;
                            if (live) {
                                biomeID = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getId(world.getBiome(loopBlockPos.withXYZ(t, blockPos.getY(), s)).value());
                            }
                            else {
                                int dataX = t - startX;
                                int dataZ = s - startZ;
                                dataX = Math.max(dataX, 0);
                                dataX = Math.min(dataX, mapData.getWidth() - 1);
                                dataZ = Math.max(dataZ, 0);
                                dataZ = Math.min(dataZ, mapData.getHeight() - 1);
                                biomeID = mapData.getBiomeID(dataX, dataZ);
                            }
                            if (biomeID < 0) {
                                biomeID = 1;
                            }
                            final int biomeTint = tints[biomeID][loopBlockPos.y / 8];
                            r += (biomeTint & 0xFF0000) >> 16;
                            g += (biomeTint & 0xFF00) >> 8;
                            b += (biomeTint & 0xFF);
                        }
                    }
                    tint = (0xFF000000 | (r / 9 & 0xFF) << 16 | (g / 9 & 0xFF) << 8 | (b / 9 & 0xFF));
                }
            }
            catch (final Exception e) {
                System.out.println("error getting biome tint for block " + blockState.getBlock().getName().getString());
                e.printStackTrace();
                tint = -2;
            }
        }
        if (tint == -2) {
            tint = this.getBuiltInBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ, live);
        }
        return tint;
    }
    
    private int getBuiltInBiomeTint(final AbstractMapData mapData, final Level world, final BlockState blockState, final int blockStateID, final VoxelMapMutableBlockPos blockPos, final VoxelMapMutableBlockPos loopBlockPos, final int startX, final int startZ, final boolean live) {
        int tint = -1;
        final Block block = blockState.getBlock();
        if (block == BlockRepository.redstone || BlockRepository.biomeBlocks.contains(block) || this.biomeTintsAvailable.contains(blockStateID)) {
            if (live) {
                try {
                    tint = (this.game.getBlockColors().getColor(blockState, (BlockAndTintGetter)world, (BlockPos)blockPos, 0) | 0xFF000000);
                }
                catch (final Exception ex) {}
            }
            if (tint == -1) {
                try {
                    tint = (this.getBuiltInBiomeTintFromUnloadedChunk(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ) | 0xFF000000);
                }
                catch (final Exception ex2) {}
            }
        }
        return tint;
    }
    
    private int getBuiltInBiomeTintFromUnloadedChunk(final AbstractMapData mapData, final Level world, final BlockState blockState, final int blockStateID, final VoxelMapMutableBlockPos blockPos, final VoxelMapMutableBlockPos loopBlockPos, final int startX, final int startZ) {
        int tint = -1;
        final Block block = blockState.getBlock();
        ColorResolver colorResolver = null;
        if (block == BlockRepository.water) {
            colorResolver = this.waterColorResolver;
        }
        else if (block == BlockRepository.spruceLeaves) {
            colorResolver = this.spruceColorResolver;
        }
        else if (block == BlockRepository.birchLeaves) {
            colorResolver = this.birchColorResolver;
        }
        else if (block == BlockRepository.oakLeaves || block == BlockRepository.jungleLeaves || block == BlockRepository.acaciaLeaves || block == BlockRepository.darkOakLeaves || block == BlockRepository.vine) {
            colorResolver = this.foliageColorResolver;
        }
        else if (BlockRepository.biomeBlocks.contains(block)) {
            colorResolver = this.grassColorResolver;
        }
        if (colorResolver != null) {
            int r = 0;
            int g = 0;
            int b = 0;
            for (int t = blockPos.getX() - 1; t <= blockPos.getX() + 1; ++t) {
                for (int s = blockPos.getZ() - 1; s <= blockPos.getZ() + 1; ++s) {
                    int dataX = t - startX;
                    int dataZ = s - startZ;
                    dataX = Math.max(dataX, 0);
                    dataX = Math.min(dataX, 255);
                    dataZ = Math.max(dataZ, 0);
                    dataZ = Math.min(dataZ, 255);
                    final int biomeID = mapData.getBiomeID(dataX, dataZ);
                    Biome biome = (Biome)world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).byId(biomeID);
                    if (biome == null) {
                        MessageUtils.printDebug("Null biome ID! " + biomeID + " at " + t + "," + s);
                        MessageUtils.printDebug("block: " + mapData.getBlockstate(dataX, dataZ) + ", height: " + mapData.getHeight(dataX, dataZ));
                        MessageUtils.printDebug("Mapdata: " + mapData.toString());
                        biome = BiomeRepository.FOREST;
                    }
                    final int biomeTint = colorResolver.getColorAtPos(biome, loopBlockPos.withXYZ(t, blockPos.getY(), s));
                    r += (biomeTint & 0xFF0000) >> 16;
                    g += (biomeTint & 0xFF00) >> 8;
                    b += (biomeTint & 0xFF);
                }
            }
            tint = ((r / 9 & 0xFF) << 16 | (g / 9 & 0xFF) << 8 | (b / 9 & 0xFF));
        }
        else if (this.biomeTintsAvailable.contains(blockStateID)) {
            tint = this.getCustomBlockBiomeTintFromUnloadedChunk(mapData, blockState, blockPos, loopBlockPos, startX, startZ);
        }
        return tint;
    }
    
    private int getCustomBlockBiomeTintFromUnloadedChunk(final AbstractMapData mapData, final BlockState blockState, final VoxelMapMutableBlockPos blockPos, final VoxelMapMutableBlockPos loopBlockPos, final int startX, final int startZ) {
        int tint = -1;
        try {
            int dataX = blockPos.getX() - startX;
            int dataZ = blockPos.getZ() - startZ;
            dataX = Math.max(dataX, 0);
            dataX = Math.min(dataX, mapData.getWidth() - 1);
            dataZ = Math.max(dataZ, 0);
            dataZ = Math.min(dataZ, mapData.getHeight() - 1);
            final byte biomeID = (byte)mapData.getBiomeID(dataX, dataZ);
            tint = this.tintFromFakePlacedBlock(blockState, loopBlockPos, biomeID);
        }
        catch (final Exception e) {
            tint = -1;
        }
        return tint;
    }
    
    private int applyShape(final Block block, int color) {
        int alpha = color >> 24 & 0xFF;
        final int red = color >> 16 & 0xFF;
        final int green = color >> 8 & 0xFF;
        final int blue = color >> 0 & 0xFF;
        if (block instanceof SignBlock) {
            alpha = 31;
        }
        else if (block instanceof DoorBlock) {
            alpha = 47;
        }
        else if (block == BlockRepository.ladder || block == BlockRepository.vine) {
            alpha = 15;
        }
        color = ((alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        return color;
    }
    
    private void processCTM() {
        this.renderPassThreeBlendMode = "alpha";
        final Properties properties = new Properties();
        final ResourceLocation propertiesFile = new ResourceLocation("minecraft", "optifine/renderpass.properties");
        try {
            final InputStream input = this.game.getResourceManager().getResource(propertiesFile).getInputStream();
            if (input != null) {
                properties.load(input);
                input.close();
                this.renderPassThreeBlendMode = properties.getProperty("blend.3", "alpha");
            }
        }
        catch (final IOException e) {
            this.renderPassThreeBlendMode = "alpha";
        }
        final String namespace = "minecraft";
        for (final ResourceLocation s : this.findResources(namespace, "/optifine/ctm", ".properties", true)) {
            try {
                this.loadCTM(s);
            }
            catch (final NumberFormatException ex) {}
            catch (final IllegalArgumentException ex2) {}
        }
        for (int t = 0; t < this.blockColors.length; ++t) {
            final int n = this.blockColors[t];
            this.getClass();
            if (n != 452984832) {
                final int n2 = this.blockColors[t];
                this.getClass();
                if (n2 != -16842497) {
                    if ((this.blockColors[t] >> 24 & 0xFF) < 27) {
                        this.blockColors[t] |= 0x1B000000;
                    }
                    this.checkForBiomeTinting(this.dummyBlockPos, BlockRepository.getStateById(t), this.blockColors[t]);
                }
            }
        }
    }
    
    private void loadCTM(final ResourceLocation propertiesFile) {
        if (propertiesFile == null) {
            return;
        }
        final BlockRenderDispatcher blockRendererDispatcher = this.game.getBlockRenderer();
        final BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
        final Properties properties = new Properties();
        try {
            final InputStream input = this.game.getResourceManager().getResource(propertiesFile).getInputStream();
            if (input != null) {
                properties.load(input);
                input.close();
            }
        }
        catch (final IOException e) {
            return;
        }
        final String filePath = propertiesFile.getPath();
        final String method = properties.getProperty("method", "").trim().toLowerCase();
        final String faces = properties.getProperty("faces", "").trim().toLowerCase();
        final String matchBlocks = properties.getProperty("matchBlocks", "").trim().toLowerCase();
        String matchTiles = properties.getProperty("matchTiles", "").trim().toLowerCase();
        String metadata = properties.getProperty("metadata", "").trim().toLowerCase();
        final String tiles = properties.getProperty("tiles", "").trim();
        final String biomes = properties.getProperty("biomes", "").trim().toLowerCase();
        final String renderPass = properties.getProperty("renderPass", "").trim().toLowerCase();
        metadata = metadata.replaceAll("\\s+", ",");
        final Set<BlockState> blockStates = new HashSet<BlockState>();
        blockStates.addAll(this.parseBlocksList(matchBlocks, metadata));
        final String directory = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        final String[] tilesParsed = this.parseStringList(tiles);
        String tilePath = directory + "0";
        if (tilesParsed.length > 0) {
            tilePath = tilesParsed[0].trim();
        }
        if (tilePath.startsWith("~")) {
            tilePath = tilePath.replace("~", "optifine");
        }
        else if (!tilePath.contains("/")) {
            tilePath = directory + tilePath;
        }
        if (!tilePath.toLowerCase().endsWith(".png")) {
            tilePath += ".png";
        }
        final String[] biomesArray = biomes.split(" ");
        if (blockStates.size() == 0) {
            Block block = null;
            final Pattern pattern = Pattern.compile(".*/block_(.+).properties");
            final Matcher matcher = pattern.matcher(filePath);
            if (matcher.find()) {
                block = this.getBlockFromName(matcher.group(1));
                if (block != null) {
                    final Set<BlockState> matching = this.parseBlockMetadata(block, metadata);
                    if (matching.size() == 0) {
                        matching.addAll((Collection<? extends BlockState>)block.getStateDefinition().getPossibleStates());
                    }
                    blockStates.addAll(matching);
                }
            }
            else {
                if (matchTiles.equals("")) {
                    matchTiles = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".properties"));
                }
                if (!matchTiles.contains(":")) {
                    matchTiles = "minecraft:blocks/" + matchTiles;
                }
                final ResourceLocation matchID = new ResourceLocation(matchTiles);
                final TextureAtlasSprite compareIcon = this.game.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(matchID);
                if (compareIcon.getName() != MissingTextureAtlasSprite.getLocation()) {
                    final ArrayList<BlockState> tmpList = new ArrayList<BlockState>();
                    for (final Block testBlock : Registry.BLOCK) {
                        for (final BlockState blockState : testBlock.getStateDefinition().getPossibleStates()) {
                            try {
                                final BakedModel bakedModel = blockModelShapes.getBlockModel(blockState);
                                final List<BakedQuad> quads = new ArrayList<BakedQuad>();
                                quads.addAll(bakedModel.getQuads(blockState, Direction.UP, this.random));
                                quads.addAll(bakedModel.getQuads(blockState, (Direction)null, this.random));
                                final BlockModel model = new BlockModel(quads, this.failedToLoadX, this.failedToLoadY);
                                if (model.numberOfFaces() <= 0) {
                                    continue;
                                }
                                final ArrayList<BlockModel.BlockFace> blockFaces = model.getFaces();
                                for (int i = 0; i < blockFaces.size(); ++i) {
                                    final BlockModel.BlockFace face = model.getFaces().get(i);
                                    final float minU = face.getMinU();
                                    final float maxU = face.getMaxU();
                                    final float minV = face.getMinV();
                                    final float maxV = face.getMaxV();
                                    if (this.similarEnough(minU, maxU, minV, maxV, compareIcon.getU0(), compareIcon.getU1(), compareIcon.getV0(), compareIcon.getV1())) {
                                        tmpList.add(blockState);
                                    }
                                }
                            }
                            catch (final Exception ex) {}
                        }
                    }
                    blockStates.addAll(tmpList);
                }
            }
        }
        if (blockStates.size() == 0) {
            return;
        }
        if (!method.equals("horizontal") && !method.startsWith("overlay")) {
            if (!method.equals("sandstone") && !method.equals("top") && !faces.contains("top") && !faces.contains("all")) {
                if (faces.length() != 0) {
                    return;
                }
            }
            try {
                final ResourceLocation pngResource = new ResourceLocation(propertiesFile.getNamespace(), tilePath);
                final InputStream is = this.game.getResourceManager().getResource(pngResource).getInputStream();
                Image top = ImageIO.read(is);
                is.close();
                top = top.getScaledInstance(1, 1, 4);
                final BufferedImage topBuff = new BufferedImage(top.getWidth(null), top.getHeight(null), 6);
                final Graphics gfx = topBuff.createGraphics();
                gfx.drawImage(top, 0, 0, null);
                gfx.dispose();
                int topRGB = topBuff.getRGB(0, 0);
                if ((topRGB >> 24 & 0xFF) == 0x0) {
                    return;
                }
                for (final BlockState blockState2 : blockStates) {
                    topRGB = topBuff.getRGB(0, 0);
                    if (blockState2.getBlock() == BlockRepository.cobweb) {
                        topRGB |= 0xFF000000;
                    }
                    if (renderPass.equals("3")) {
                        topRGB = this.processRenderPassThree(topRGB);
                        final int blockStateID = BlockRepository.getStateId(blockState2);
                        final int n;
                        final int baseRGB = n = this.blockColors[blockStateID];
                        this.getClass();
                        if (n != 452984832) {
                            final int n2 = baseRGB;
                            this.getClass();
                            if (n2 != -16842497) {
                                topRGB = ColorUtils.colorMultiplier(baseRGB, topRGB);
                            }
                        }
                    }
                    if (BlockRepository.shapedBlocks.contains(blockState2.getBlock())) {
                        topRGB = this.applyShape(blockState2.getBlock(), topRGB);
                    }
                    final int blockStateID = BlockRepository.getStateId(blockState2);
                    if (!biomes.equals("")) {
                        this.biomeTextureAvailable.add(blockStateID);
                        for (int r = 0; r < biomesArray.length; ++r) {
                            final int biomeInt = this.parseBiomeName(biomesArray[r]);
                            if (biomeInt != -1) {
                                this.blockBiomeSpecificColors.put("" + blockStateID + " " + biomeInt, topRGB);
                            }
                        }
                    }
                    else {
                        this.blockColors[blockStateID] = topRGB;
                    }
                }
            }
            catch (final IOException e2) {
                System.err.println("error getting CTM block from " + propertiesFile.getPath() + ": " + filePath + " " + Registry.BLOCK.getKey(blockStates.iterator().next().getBlock()).toString() + " " + tilePath);
                e2.printStackTrace();
            }
        }
    }
    
    private boolean similarEnough(final float a, final float b, final float c, final float d, final float one, final float two, final float three, final float four) {
        boolean similar = Math.abs(a - one) < 1.0E-4;
        similar = (similar && Math.abs(b - two) < 1.0E-4);
        similar = (similar && Math.abs(c - three) < 1.0E-4);
        similar = (similar && Math.abs(d - four) < 1.0E-4);
        return similar;
    }
    
    private int processRenderPassThree(int rgb) {
        if (this.renderPassThreeBlendMode.equals("color") || this.renderPassThreeBlendMode.equals("overlay")) {
            int red = rgb >> 16 & 0xFF;
            int green = rgb >> 8 & 0xFF;
            int blue = rgb >> 0 & 0xFF;
            final float colorAverage = (red + blue + green) / 3.0f;
            final float lighteningFactor = (colorAverage - 127.5f) * 2.0f;
            red += (int)(red * (lighteningFactor / 255.0f));
            blue += (int)(red * (lighteningFactor / 255.0f));
            green += (int)(red * (lighteningFactor / 255.0f));
            final int newAlpha = (int)Math.abs(lighteningFactor);
            rgb = (newAlpha << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        }
        return rgb;
    }
    
    private String[] parseStringList(final String list) {
        final ArrayList<String> tmpList = new ArrayList<String>();
        for (String token : list.split("\\s+")) {
            token = token.trim();
            try {
                if (token.matches("^\\d+$")) {
                    tmpList.add("" + Integer.parseInt(token));
                }
                else if (token.matches("^\\d+-\\d+$")) {
                    final String[] t = token.split("-");
                    final int min = Integer.parseInt(t[0]);
                    for (int max = Integer.parseInt(t[1]), i = min; i <= max; ++i) {
                        tmpList.add("" + i);
                    }
                }
                else if (token != null && token != "") {
                    tmpList.add(token);
                }
            }
            catch (final NumberFormatException ex) {}
        }
        final String[] a = new String[tmpList.size()];
        for (int j = 0; j < a.length; ++j) {
            a[j] = tmpList.get(j);
        }
        return a;
    }
    
    private Set<BlockState> parseBlocksList(final String blocks, final String metadataLine) {
        final Set<BlockState> blockStates = new HashSet<BlockState>();
        for (String blockString : blocks.split("\\s+")) {
            String metadata = metadataLine;
            blockString = blockString.trim();
            final String[] blockComponents = blockString.split(":");
            int tokensUsed = 0;
            Block block = null;
            block = this.getBlockFromName(blockComponents[0]);
            if (block != null) {
                tokensUsed = 1;
            }
            else if (blockComponents.length > 1) {
                block = this.getBlockFromName(blockComponents[0] + ":" + blockComponents[1]);
                if (block != null) {
                    tokensUsed = 2;
                }
            }
            if (block != null) {
                if (blockComponents.length > tokensUsed) {
                    metadata = blockComponents[tokensUsed];
                    for (int t = tokensUsed + 1; t < blockComponents.length; ++t) {
                        metadata = metadata + ":" + blockComponents[t];
                    }
                }
                blockStates.addAll(this.parseBlockMetadata(block, metadata));
            }
        }
        return blockStates;
    }
    
    private <T extends Comparable<T>, V extends T> Set<BlockState> parseBlockMetadata(final Block block, final String metadataList) {
        final Set<BlockState> blockStates = new HashSet<BlockState>();
        if (metadataList.equals("")) {
            blockStates.addAll((Collection<? extends BlockState>)block.getStateDefinition().getPossibleStates());
        }
        else {
            final Set<String> valuePairs = new HashSet<String>();
            for (final String metadata : metadataList.split(":")) {
                metadata.trim();
                if (metadata.contains("=")) {
                    valuePairs.add(metadata);
                }
            }
            if (valuePairs.size() > 0) {
                for (final BlockState blockState : block.getStateDefinition().getPossibleStates()) {
                    boolean matches = true;
                    for (final String pair : valuePairs) {
                        final String[] propertyAndValues = pair.split("\\s*=\\s*", 5);
                        if (propertyAndValues.length == 2) {
                            final Property<?> property = (Property<?>)block.getStateDefinition().getProperty(propertyAndValues[0]);
                            if (property == null) {
                                continue;
                            }
                            boolean valueIncluded = false;
                            final String[] split2;
                            split2 = propertyAndValues[1].split(",");
                            for (final String value : split2) {
                                if (property.getValueClass() == Integer.class && value.matches("^\\d+-\\d+$")) {
                                    final String[] range = value.split("-");
                                    final int min = Integer.parseInt(range[0]);
                                    final int max = Integer.parseInt(range[1]);
                                    final int intValue = Integer.class.cast(blockState.getValue(property));
                                    if (intValue >= min && intValue <= max) {
                                        valueIncluded = true;
                                    }
                                }
                                else if (!blockState.getValue(property).equals(property.getValue(value))) {
                                    valueIncluded = true;
                                }
                            }
                            matches = (matches && valueIncluded);
                        }
                    }
                    if (matches) {
                        blockStates.add(blockState);
                    }
                }
            }
        }
        return blockStates;
    }
    
    private int parseBiomeName(final String name) {
        final Biome biome = (Biome)this.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(new ResourceLocation(name));
        if (biome != null) {
            return this.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getId(biome);
        }
        return -1;
    }
    
    private List<ResourceLocation> findResources(final String namespace, String directory, final String suffixMaybeNull, final boolean sortByFilename) {
        if (directory == null) {
            directory = "";
        }
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        final String suffix = (suffixMaybeNull == null) ? "" : suffixMaybeNull;
        final ArrayList<ResourceLocation> resources = new ArrayList<ResourceLocation>();
        final Collection<ResourceLocation> candidates = this.game.getResourceManager().listResources(directory, asset -> asset.endsWith(suffix));
        for (final ResourceLocation candidate : candidates) {
            if (candidate.getNamespace().equals(namespace)) {
                resources.add(candidate);
            }
        }
        if (sortByFilename) {
            Collections.sort(resources, new Comparator<ResourceLocation>() {
                @Override
                public int compare(final ResourceLocation o1, final ResourceLocation o2) {
                    final String f1 = o1.getPath().replaceAll(".*/", "").replaceFirst("\\.properties", "");
                    final String f2 = o2.getPath().replaceAll(".*/", "").replaceFirst("\\.properties", "");
                    final int result = f1.compareTo(f2);
                    if (result != 0) {
                        return result;
                    }
                    return o1.getPath().compareTo(o2.getPath());
                }
            });
        }
        else {
            Collections.sort(resources, new Comparator<ResourceLocation>() {
                @Override
                public int compare(final ResourceLocation o1, final ResourceLocation o2) {
                    return o1.getPath().compareTo(o2.getPath());
                }
            });
        }
        return resources;
    }
    
    private void processColorProperties() {
        final Properties properties = new Properties();
        try {
            final InputStream input = this.game.getResourceManager().getResource(new ResourceLocation("optifine/color.properties")).getInputStream();
            if (input != null) {
                properties.load(input);
                input.close();
            }
        }
        catch (final IOException ex) {}
        final BlockState blockState = BlockRepository.lilypad.defaultBlockState();
        int blockStateID = BlockRepository.getStateId(blockState);
        final int lilyRGB = this.getBlockColor(blockStateID);
        int lilypadMultiplier = 2129968;
        final String lilypadMultiplierString = properties.getProperty("lilypad");
        if (lilypadMultiplierString != null) {
            lilypadMultiplier = Integer.parseInt(lilypadMultiplierString, 16);
        }
        for (final BlockState padBlockState : BlockRepository.lilypad.getStateDefinition().getPossibleStates()) {
            blockStateID = BlockRepository.getStateId(padBlockState);
            this.blockColors[blockStateID] = ColorUtils.colorMultiplier(lilyRGB, lilypadMultiplier | 0xFF000000);
            this.blockColorsWithDefaultTint[blockStateID] = this.blockColors[blockStateID];
        }
        final String defaultFormat = properties.getProperty("palette.format");
        final boolean globalGrid = defaultFormat != null && defaultFormat.equalsIgnoreCase("grid");
        final Enumeration<?> e = properties.propertyNames();
        while (e.hasMoreElements()) {
            final String key = (String)e.nextElement();
            if (key.startsWith("palette.block")) {
                String filename = key.substring("palette.block.".length());
                filename = filename.replace("~", "optifine");
                this.processColorPropertyHelper(new ResourceLocation(filename), properties.getProperty(key), globalGrid);
            }
        }
        for (final ResourceLocation resource : this.findResources("minecraft", "/optifine/colormap/blocks", ".properties", true)) {
            final Properties colorProperties = new Properties();
            try {
                final InputStream input2 = this.game.getResourceManager().getResource(resource).getInputStream();
                if (input2 != null) {
                    colorProperties.load(input2);
                    input2.close();
                }
            }
            catch (final IOException e2) {
                break;
            }
            String names = colorProperties.getProperty("blocks");
            if (names == null) {
                String name = resource.getPath();
                name = (names = name.substring(name.lastIndexOf("/") + 1, name.lastIndexOf(".properties")));
            }
            final String source = colorProperties.getProperty("source");
            ResourceLocation resourcePNG;
            if (source != null) {
                resourcePNG = new ResourceLocation(resource.getNamespace(), source);
                try {
                    this.game.getResourceManager().getResource(resourcePNG);
                }
                catch (final IOException e3) {
                    Path path = Paths.get("optifine/colormap/blocks/", source);
                    path = path.normalize();
                    resourcePNG = new ResourceLocation(resource.getNamespace(), path.toString().replace(File.separatorChar, '/'));
                    System.out.println("trying " + resourcePNG.toString());
                }
            }
            else {
                resourcePNG = new ResourceLocation(resource.getNamespace(), resource.getPath().replace(".properties", ".png"));
            }
            final String format = colorProperties.getProperty("format");
            boolean grid;
            if (format != null) {
                grid = (format != null && format.equalsIgnoreCase("grid"));
            }
            else {
                grid = globalGrid;
            }
            final String yOffsetString = colorProperties.getProperty("yOffset");
            int yOffset = 0;
            if (yOffsetString != null) {
                yOffset = Integer.valueOf(yOffsetString);
            }
            this.processColorProperty(resourcePNG, names, grid, yOffset);
        }
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/water.png"), "water", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/watercolorx.png"), "water", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampgrass.png"), "grass_block grass fern tall_grass large_fern", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampgrasscolor.png"), "grass_block grass fern tall_grass large_fern", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampfoliage.png"), "oak_leaves vine", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/swampfoliagecolor.png"), "oak_leaves vine", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/pine.png"), "spruce_leaves", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/pinecolor.png"), "spruce_leaves", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/birch.png"), "birch_leaves", globalGrid);
        this.processColorPropertyHelper(new ResourceLocation("optifine/colormap/birchcolor.png"), "birch_leaves", globalGrid);
    }
    
    private void processColorPropertyHelper(final ResourceLocation resource, final String list, boolean grid) {
        final ResourceLocation resourceProperties = new ResourceLocation(resource.getNamespace(), resource.getPath().replace(".png", ".properties"));
        final Properties colorProperties = new Properties();
        int yOffset = 0;
        try {
            final InputStream input = this.game.getResourceManager().getResource(resourceProperties).getInputStream();
            if (input != null) {
                colorProperties.load(input);
                input.close();
            }
            final String format = colorProperties.getProperty("format");
            if (format != null) {
                grid = format.equalsIgnoreCase("grid");
            }
            final String yOffsetString = colorProperties.getProperty("yOffset");
            if (yOffsetString != null) {
                yOffset = Integer.valueOf(yOffsetString);
            }
        }
        catch (final IOException ex) {}
        this.processColorProperty(resource, list, grid, yOffset);
    }
    
    private void processColorProperty(final ResourceLocation resource, final String list, final boolean grid, final int yOffset) {
        final int[][] array;
        final int[][] tints = array = new int[this.sizeOfBiomeArray][32];
        for (final int[] row : array) {
            Arrays.fill(row, -1);
        }
        final boolean swamp = resource.getPath().contains("/swamp");
        Image tintColors = null;
        try {
            final InputStream is = this.game.getResourceManager().getResource(resource).getInputStream();
            tintColors = ImageIO.read(is);
            is.close();
        }
        catch (final IOException e) {
            return;
        }
        final BufferedImage tintColorsBuff = new BufferedImage(tintColors.getWidth(null), tintColors.getHeight(null), 1);
        final Graphics gfx = tintColorsBuff.createGraphics();
        gfx.drawImage(tintColors, 0, 0, null);
        gfx.dispose();
        for (int numBiomesToCheck = grid ? Math.min(tintColorsBuff.getWidth(), this.sizeOfBiomeArray) : this.sizeOfBiomeArray, t = 0; t < numBiomesToCheck; ++t) {
            final Biome biome = (Biome) this.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).byId(t);
            if (biome != null) {
                int tintMult = 0;
                final int height = tintColorsBuff.getHeight();
                this.getClass();
                final int heightMultiplier = height / 32;
                for (int s = 0; s < 32; ++s) {
                    if (grid) {
                        tintMult = (tintColorsBuff.getRGB(t, Math.min(Math.max(0, s * heightMultiplier - yOffset), tintColorsBuff.getHeight() - 1)) & 0xFFFFFF);
                    }
                    else {
                        double var1 = Mth.clamp(ColorManager.getTemperature(biome, new BlockPos(0, 64, 0)), 0.0f, 1.0f);
                        double var2 = Mth.clamp(biome.getDownfall(), 0.0f, 1.0f);
                        var2 *= var1;
                        var1 = 1.0 - var1;
                        var2 = 1.0 - var2;
                        tintMult = (tintColorsBuff.getRGB((int)((tintColorsBuff.getWidth() - 1) * var1), (int)((tintColorsBuff.getHeight() - 1) * var2)) & 0xFFFFFF);
                    }
                    if (tintMult != 0 && (!swamp || biome == BiomeRepository.SWAMP || biome == BiomeRepository.SWAMP_HILLS)) {
                        tints[t][s] = tintMult;
                    }
                }
            }
        }
        final Set<BlockState> blockStates = new HashSet<BlockState>();
        blockStates.addAll(this.parseBlocksList(list, ""));
        for (final BlockState blockState : blockStates) {
            final int blockStateID = BlockRepository.getStateId(blockState);
            int[][] previousTints = this.blockTintTables.get(blockStateID);
            if (swamp && previousTints == null) {
                ResourceLocation defaultResource;
                if (resource.getPath().contains("grass")) {
                    defaultResource = new ResourceLocation("textures/colormap/grass.png");
                }
                else {
                    defaultResource = new ResourceLocation("textures/colormap/foliage.png");
                }
                String stateString = blockState.toString().toLowerCase();
                stateString = stateString.replaceAll("^block", "");
                stateString = stateString.replace("{", "");
                stateString = stateString.replace("}", "");
                stateString = stateString.replace("[", ":");
                stateString = stateString.replace("]", "");
                stateString = stateString.replace(",", ":");
                this.processColorProperty(defaultResource, stateString, false, 0);
                previousTints = this.blockTintTables.get(blockStateID);
            }
            if (previousTints != null) {
                for (int t2 = 0; t2 < this.sizeOfBiomeArray; ++t2) {
                    for (int s2 = 0; s2 < 32; ++s2) {
                        if (tints[t2][s2] == -1) {
                            tints[t2][s2] = previousTints[t2][s2];
                        }
                    }
                }
            }
            this.blockColorsWithDefaultTint[blockStateID] = ColorUtils.colorMultiplier(this.getBlockColor(blockStateID), tints[4][8] | 0xFF000000);
            this.blockTintTables.put(blockStateID, tints);
            this.biomeTintsAvailable.add(blockStateID);
        }
    }
    
    private Block getBlockFromName(final String name) {
        try {
            final ResourceLocation resourceLocation = new ResourceLocation(name);
            if (Registry.BLOCK.containsKey(resourceLocation)) {
                return (Block)Registry.BLOCK.get(resourceLocation);
            }
            return null;
        }
        catch (final ResourceLocationException e) {
            return null;
        }
        catch (final NumberFormatException e2) {
            return null;
        }
    }
    
    public static float getHeightAdjustedTemperature(Biome biome, BlockPos blockPos) {
    	final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(1234L)), ImmutableList.of(0));
    	
    	Biome.ClimateSettings climateSettings = (Biome.ClimateSettings) ReflectionUtils.getPrivateFieldValueByType(biome, biome.getClass(), Biome.ClimateSettings.class);
        float f = climateSettings.temperatureModifier.modifyTemperature(blockPos, biome.getBaseTemperature());
        if (blockPos.getY() > 80) {
           float f1 = (float)(TEMPERATURE_NOISE.getValue((double)((float)blockPos.getX() / 8.0F), (double)((float)blockPos.getZ() / 8.0F), false) * 8.0D);
           return f - (f1 + (float)blockPos.getY() - 80.0F) * 0.05F / 40.0F;
        } else {
           return f;
        }
     }
    
    public static float getTemperature(Biome biome, BlockPos blockPos) {
    	final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = (ThreadLocal<Long2FloatLinkedOpenHashMap>) ReflectionUtils.getPrivateFieldValueByType(biome, biome.getClass(), ThreadLocal.class);
    	
    	long i = blockPos.asLong();
        Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = temperatureCache.get();
        float f = long2floatlinkedopenhashmap.get(i);
        if (!Float.isNaN(f)) {
           return f;
        } else {
           float f1 = ColorManager.getHeightAdjustedTemperature(biome, blockPos);
           if (long2floatlinkedopenhashmap.size() == 1024) {
              long2floatlinkedopenhashmap.removeFirstFloat();
           }

           long2floatlinkedopenhashmap.put(i, f1);
           return f1;
        }
    }
    
    interface ColorResolver
    {
        int getColorAtPos(final Biome p0, final BlockPos p1);
    }
}
