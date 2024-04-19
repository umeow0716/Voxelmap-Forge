// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import java.util.function.Supplier;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import com.mamiyaotaru.voxelmap.util.MapUtils;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import com.mamiyaotaru.voxelmap.util.BlockRepository;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.nio.FloatBuffer;
import com.mamiyaotaru.voxelmap.util.ColorUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.mamiyaotaru.voxelmap.util.GLShim;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import com.mamiyaotaru.voxelmap.util.TickCounter;
import com.mamiyaotaru.voxelmap.util.VoxelMapMutableBlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Objects;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import java.util.TreeSet;
import com.mamiyaotaru.voxelmap.gui.GuiWaypoints;
import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.ImageUtils;

import java.lang.reflect.Field;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mamiyaotaru.voxelmap.util.ScaledMutableNativeImageBackedTexture;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import com.mamiyaotaru.voxelmap.util.LiveScaledGLBufferedImage;
import java.util.Random;
import com.mamiyaotaru.voxelmap.util.MutableNativeImageBackedTexture;
import com.mamiyaotaru.voxelmap.util.MapChunkCache;
import com.mamiyaotaru.voxelmap.util.FullMapData;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;
import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IMap;
import net.minecraft.network.chat.FormattedText;

public class Map implements Runnable, IMap
{
    private IVoxelMap master;
    private Minecraft game;
    private String zmodver;
    private ClientLevel world;
    private MapSettingsManager options;
    private LayoutVariables layoutVariables;
    private IColorManager colorManager;
    private IWaypointManager waypointManager;
    private int availableProcessors;
    private boolean multicore;
    private FullMapData[] mapData;
    private MapChunkCache[] chunkCache;
    private MutableNativeImageBackedTexture[] mapImages;
    private MutableNativeImageBackedTexture[] mapImagesFiltered;
    private MutableNativeImageBackedTexture[] mapImagesUnfiltered;
    private VoxelMapMutableBlockPos blockPos;
    private VoxelMapMutableBlockPos tempBlockPos;
    private BlockState transparentBlockState;
    private BlockState surfaceBlockState;
    private BlockState seafloorBlockState;
    private BlockState foliageBlockState;
    private boolean imageChanged;
    private DynamicTexture lightmapTexture;
    private boolean needLightmapRefresh;
    private int tickWithLightChange;
    private boolean lastPaused;
    private final float[] lastLightBrightnessTable;
    private double lastGamma;
    private float lastSunBrightness;
    private float lastLightning;
    private float lastPotion;
    private int[] lastLightmapValues;
    private boolean lastBeneathRendering;
    private boolean needSkyColor;
    private boolean lastAboveHorizon;
    private int lastBiome;
    private int lastSkyColor;
    private Random generator;
    private boolean showWelcomeScreen;
    private Screen lastGuiScreen;
    private boolean enabled;
    private boolean fullscreenMap;
    private boolean active;
    private int zoom;
    private int mapX;
    private int mapY;
    private int scWidth;
    private int scHeight;
    private String error;
    private Component[] welcomeText;
    private int ztimer;
    private int heightMapFudge;
    private int timer;
    private boolean doFullRender;
    private boolean zoomChanged;
    private int lastX;
    private int lastZ;
    private int lastY;
    private int lastImageX;
    private int lastImageZ;
    private boolean lastFullscreen;
    private float direction;
    private float percentX;
    private float percentY;
    private String subworldName;
    private int heightMapResetHeight;
    private int heightMapResetTime;
    private int northRotate;
    private Thread zCalc;
    private int zCalcTicker;
    private boolean threading;
    private Font fontRenderer;
    private int[] lightmapColors;
    private final Object coordinateLock;
    private double zoomScale;
    private double zoomScaleAdjusted;
    private final ResourceLocation arrowResourceLocation;
    private final ResourceLocation roundmapResourceLocation;
    private final ResourceLocation squareStencil;
    private final ResourceLocation circleStencil;
    LiveScaledGLBufferedImage roundImage;
    private int mapImageInt;
    
    public Map(final IVoxelMap master) {
        this.zmodver = "v1.10.15";
        this.world = null;
        this.options = null;
        this.layoutVariables = null;
        this.colorManager = null;
        this.waypointManager = null;
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.multicore = (this.availableProcessors > 1);
        this.mapData = new FullMapData[5];
        this.chunkCache = new MapChunkCache[5];
        this.mapImagesFiltered = new MutableNativeImageBackedTexture[5];
        this.mapImagesUnfiltered = new MutableNativeImageBackedTexture[5];
        this.blockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.tempBlockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.imageChanged = true;
        this.lightmapTexture = null;
        this.needLightmapRefresh = true;
        this.tickWithLightChange = 0;
        this.lastPaused = true;
        this.lastLightBrightnessTable = new float[16];
        this.lastGamma = 0.0;
        this.lastSunBrightness = 0.0f;
        this.lastLightning = 0.0f;
        this.lastPotion = 0.0f;
        this.lastLightmapValues = new int[] { -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216 };
        this.lastBeneathRendering = false;
        this.needSkyColor = false;
        this.lastAboveHorizon = true;
        this.lastBiome = 0;
        this.lastSkyColor = 0;
        this.generator = new Random();
        this.showWelcomeScreen = true;
        this.lastGuiScreen = null;
        this.enabled = true;
        this.fullscreenMap = false;
        this.active = false;
        this.zoom = 2;
        this.mapX = 37;
        this.mapY = 37;
        this.error = "";
        this.welcomeText = new Component[8];
        this.ztimer = 0;
        this.heightMapFudge = 0;
        this.timer = 0;
        this.doFullRender = true;
        this.lastX = 0;
        this.lastZ = 0;
        this.lastY = 0;
        this.lastImageX = 0;
        this.lastImageZ = 0;
        this.lastFullscreen = false;
        this.direction = 0.0f;
        this.subworldName = "";
        this.heightMapResetHeight = (this.multicore ? 2 : 5);
        this.heightMapResetTime = (this.multicore ? 300 : 3000);
        this.northRotate = 0;
        this.zCalc = new Thread(this, "Voxelmap LiveMap Calculation Thread");
        this.zCalcTicker = 0;
        this.threading = this.multicore;
        this.lightmapColors = new int[256];
        this.coordinateLock = new Object();
        this.zoomScale = 1.0;
        this.zoomScaleAdjusted = 1.0;
        this.arrowResourceLocation = new ResourceLocation("voxelmap", "images/mmarrow.png");
        this.roundmapResourceLocation = new ResourceLocation("voxelmap", "images/roundmap.png");
        this.squareStencil = new ResourceLocation("voxelmap", "images/square.png");
        this.circleStencil = new ResourceLocation("voxelmap", "images/circle.png");
        this.roundImage = new LiveScaledGLBufferedImage(128, 128, 6);
        this.mapImageInt = -1;
        this.master = master;
        this.game = GameVariableAccessShim.getMinecraft();
        this.options = master.getMapOptions();
        this.colorManager = master.getColorManager();
        this.waypointManager = master.getWaypointManager();
        this.layoutVariables = new LayoutVariables();
        final ArrayList<KeyMapping> tempBindings = new ArrayList<KeyMapping>();
        tempBindings.addAll(Arrays.asList(this.game.options.keyMappings));
        tempBindings.addAll(Arrays.asList(this.options.keyBindings));
        final Field f = ReflectionUtils.getFieldByType(this.game.options, Options.class, KeyMapping[].class, 1);
        try {
            f.set(this.game.options, tempBindings.toArray(new KeyMapping[tempBindings.size()]));
        }
        catch (final IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (final IllegalAccessException e2) {
            e2.printStackTrace();
        }
        // final java.util.Map<String, Integer> categoryOrder = (java.util.Map<String, Integer>)ReflectionUtils.getPrivateFieldValueByType(null, KeyMapping.class, java.util.Map.class, 2);
        // System.out.println("CATEGORY ORDER IS " + categoryOrder.size());
        // final Integer categoryPlace = categoryOrder.get("controls.minimap.title");
        // if (categoryPlace == null) {
            // final int currentSize = categoryOrder.size();
            // categoryOrder.put("controls.minimap.title", currentSize + 1);
        // }
        this.showWelcomeScreen = this.options.welcome;
        this.zCalc.start();
        this.zCalc.setPriority(5);
        this.mapData[0] = new FullMapData(32, 32);
        this.mapData[1] = new FullMapData(64, 64);
        this.mapData[2] = new FullMapData(128, 128);
        this.mapData[3] = new FullMapData(256, 256);
        this.mapData[4] = new FullMapData(512, 512);
        this.chunkCache[0] = new MapChunkCache(3, 3, this);
        this.chunkCache[1] = new MapChunkCache(5, 5, this);
        this.chunkCache[2] = new MapChunkCache(9, 9, this);
        this.chunkCache[3] = new MapChunkCache(17, 17, this);
        this.chunkCache[4] = new MapChunkCache(33, 33, this);
        this.mapImagesFiltered[0] = new MutableNativeImageBackedTexture(32, 32, true);
        this.mapImagesFiltered[1] = new MutableNativeImageBackedTexture(64, 64, true);
        this.mapImagesFiltered[2] = new MutableNativeImageBackedTexture(128, 128, true);
        this.mapImagesFiltered[3] = new MutableNativeImageBackedTexture(256, 256, true);
        this.mapImagesFiltered[4] = new MutableNativeImageBackedTexture(512, 512, true);
        this.mapImagesUnfiltered[0] = new ScaledMutableNativeImageBackedTexture(32, 32, true);
        this.mapImagesUnfiltered[1] = new ScaledMutableNativeImageBackedTexture(64, 64, true);
        this.mapImagesUnfiltered[2] = new ScaledMutableNativeImageBackedTexture(128, 128, true);
        this.mapImagesUnfiltered[3] = new ScaledMutableNativeImageBackedTexture(256, 256, true);
        this.mapImagesUnfiltered[4] = new ScaledMutableNativeImageBackedTexture(512, 512, true);
        if (this.options.filtering) {
            this.mapImages = this.mapImagesFiltered;
        }
        else {
            this.mapImages = this.mapImagesUnfiltered;
        }
        GLUtils.setupFrameBuffer();
        this.fontRenderer = this.game.font;
        this.zoom = this.options.zoom;
        this.setZoomScale();
    }
    
    @Override
    public void forceFullRender(final boolean forceFullRender) {
        this.doFullRender = forceFullRender;
        this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
    }
    
    @Override
    public float getPercentX() {
        return this.percentX;
    }
    
    @Override
    public float getPercentY() {
        return this.percentY;
    }
    
    @Override
    public void run() {
        if (this.game == null) {
            return;
        }
        while (true) {
            if (this.threading) {
                this.active = true;
                while (this.game.player != null && this.world != null && this.active) {
                    if (!this.options.hide) {
                        try {
                            this.mapCalc(this.doFullRender);
                            if (!this.doFullRender) {
                                this.chunkCache[this.zoom].centerChunks(this.blockPos.withXYZ(this.lastX, 0, this.lastZ));
                                this.chunkCache[this.zoom].checkIfChunksChanged();
                            }
                        }
                        catch (final Exception ex) {}
                    }
                    this.doFullRender = this.zoomChanged;
                    this.zoomChanged = false;
                    this.active = false;
                }
                this.zCalcTicker = 0;
                synchronized (this.zCalc) {
                    try {
                        this.zCalc.wait(0L);
                    }
                    catch (final InterruptedException ex2) {}
                }
            }
            else {
                synchronized (this.zCalc) {
                    try {
                        this.zCalc.wait(0L);
                    }
                    catch (final InterruptedException ex3) {}
                }
            }
        }
    }
    
    @Override
    public void newWorld(final ClientLevel world) {
        this.world = world;
        this.lightmapTexture = this.getLightmapTexture();
        this.mapData[this.zoom].blank();
        this.mapImages[this.zoom].blank();
        this.doFullRender = true;
        this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
    }
    
    @Override
    public void newWorldName() {
        this.subworldName = this.waypointManager.getCurrentSubworldDescriptor(true);
        final StringBuilder subworldNameBuilder = new StringBuilder("��r").append(I18nUtils.getString("worldmap.multiworld.newworld", new Object[0])).append(":").append(" ");
        if (this.subworldName.equals("") && this.waypointManager.isMultiworld()) {
            subworldNameBuilder.append("???");
        }
        else if (!this.subworldName.equals("")) {
            subworldNameBuilder.append(this.subworldName);
        }
        this.error = subworldNameBuilder.toString();
    }
    
    @Override
    public void onTickInGame(final PoseStack matrixStack, final Minecraft mc) {
        this.northRotate = (this.options.oldNorth ? 90 : 0);
        if (this.game == null) {
            this.game = mc;
        }
        if (this.lightmapTexture == null) {
            this.lightmapTexture = this.getLightmapTexture();
        }
        if (this.game.screen == null && this.options.keyBindMenu.consumeClick()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            }
            this.game.setScreen(new GuiPersistentMap(null, this.master));
        }
        if (this.game.screen == null && this.options.keyBindWaypointMenu.consumeClick()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            }
            this.game.setScreen(new GuiWaypoints(null, this.master));
        }
        if (this.game.screen == null && this.options.keyBindWaypoint.consumeClick()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            }
            float r;
            float g;
            float b;
            if (this.waypointManager.getWaypoints().size() == 0) {
                r = 0.0f;
                g = 1.0f;
                b = 0.0f;
            }
            else {
                r = this.generator.nextFloat();
                g = this.generator.nextFloat();
                b = this.generator.nextFloat();
            }
            final TreeSet<DimensionContainer> dimensions = new TreeSet<DimensionContainer>();
            dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld((Level)this.game.level));
            final double dimensionScale = this.game.player.level.dimensionType().coordinateScale();
            final Waypoint newWaypoint = new Waypoint("", (int)(GameVariableAccessShim.xCoord() * dimensionScale), (int)(GameVariableAccessShim.zCoord() * dimensionScale), GameVariableAccessShim.yCoord(), true, r, g, b, "", this.master.getWaypointManager().getCurrentSubworldDescriptor(false), dimensions);
            this.game.setScreen(new GuiAddWaypoint(null, this.master, newWaypoint, false));
        }
        if (this.game.screen == null && this.options.keyBindMobToggle.consumeClick()) {
            this.master.getRadarOptions().setOptionValue(EnumOptionsMinimap.SHOWRADAR);
            this.options.saveAll();
        }
        if (this.game.screen == null && this.options.keyBindWaypointToggle.consumeClick()) {
            this.options.toggleIngameWaypoints();
        }
        if (this.game.screen == null && this.options.keyBindZoom.consumeClick()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            }
            else {
                this.cycleZoomLevel();
            }
        }
        if (this.game.screen == null && this.options.keyBindFullscreen.consumeClick()) {
            this.fullscreenMap = !this.fullscreenMap;
            if (this.zoom == 4) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.25x)";
            }
            else if (this.zoom == 3) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.5x)";
            }
            else if (this.zoom == 2) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (1.0x)";
            }
            else if (this.zoom == 1) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (2.0x)";
            }
            else {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (4.0x)";
            }
        }
        this.checkForChanges();
        if (this.game.screen instanceof DeathScreen && !(this.lastGuiScreen instanceof DeathScreen)) {
            this.waypointManager.handleDeath();
        }
        this.lastGuiScreen = this.game.screen;
        this.calculateCurrentLightAndSkyColor();
        if (this.threading) {
            if (!this.zCalc.isAlive() && this.threading) {
                (this.zCalc = new Thread(this, "Voxelmap LiveMap Calculation Thread")).setPriority(5);
                this.zCalc.start();
            }
            if (!(this.game.screen instanceof DeathScreen) && !(this.game.screen instanceof OutOfMemoryScreen)) {
                ++this.zCalcTicker;
                if (this.zCalcTicker > 2000) {
                    this.zCalcTicker = 0;
                    this.zCalc.stop();
                }
                else {
                    synchronized (this.zCalc) {
                        this.zCalc.notify();
                    }
                }
            }
        }
        else if (!this.threading) {
            if (!this.options.hide && this.world != null) {
                this.mapCalc(this.doFullRender);
                if (!this.doFullRender) {
                    this.chunkCache[this.zoom].centerChunks(this.blockPos.withXYZ(this.lastX, 0, this.lastZ));
                    this.chunkCache[this.zoom].checkIfChunksChanged();
                }
            }
            this.doFullRender = false;
        }
        if (!mc.options.hideGui && (this.options.showUnderMenus || this.game.screen == null) && !this.game.options.renderDebug) {
            this.enabled = true;
        }
        else {
            this.enabled = false;
        }
        this.direction = GameVariableAccessShim.rotationYaw() + 180.0f;
        while (this.direction >= 360.0f) {
            this.direction -= 360.0f;
        }
        while (this.direction < 0.0f) {
            this.direction += 360.0f;
        }
        if (!this.error.equals("") && this.ztimer == 0) {
            this.ztimer = 500;
        }
        if (this.ztimer > 0) {
            --this.ztimer;
        }
        if (this.ztimer == 0 && !this.error.equals("")) {
            this.error = "";
        }
        if (this.enabled) {
            this.drawMinimap(matrixStack, mc);
        }
        this.timer = ((this.timer > 5000) ? 0 : (this.timer + 1));
    }
    
    private void cycleZoomLevel() {
        if (this.options.zoom == 4) {
            this.options.zoom = 3;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel", new Object[0]) + " (0.5x)";
        }
        else if (this.options.zoom == 3) {
            this.options.zoom = 2;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel", new Object[0]) + " (1.0x)";
        }
        else if (this.options.zoom == 2) {
            this.options.zoom = 1;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel", new Object[0]) + " (2.0x)";
        }
        else if (this.options.zoom == 1) {
            this.options.zoom = 0;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel", new Object[0]) + " (4.0x)";
        }
        else if (this.options.zoom == 0) {
            if (this.multicore && Option.RENDER_DISTANCE.get(this.game.options) > 8.0) {
                this.options.zoom = 4;
                this.error = I18nUtils.getString("minimap.ui.zoomlevel", new Object[0]) + " (0.25x)";
            }
            else {
                this.options.zoom = 3;
                this.error = I18nUtils.getString("minimap.ui.zoomlevel", new Object[0]) + " (0.5x)";
            }
        }
        this.options.saveAll();
        this.zoomChanged = true;
        this.zoom = this.options.zoom;
        this.setZoomScale();
        this.mapImages[this.zoom].blank();
        this.doFullRender = true;
    }
    
    private void setZoomScale() {
        this.zoomScale = Math.pow(2.0, this.zoom) / 2.0;
        if (this.options.squareMap && this.options.rotates) {
            final double zoomScale = this.zoomScale;
            Objects.requireNonNull(this);
            this.zoomScaleAdjusted = zoomScale / 1.414199948310852;
        }
        else {
            this.zoomScaleAdjusted = this.zoomScale;
        }
    }
    
    private DynamicTexture getLightmapTexture() {
        final LightTexture lightTextureManager = this.game.gameRenderer.lightTexture();
        final Object lightmapTextureObj = ReflectionUtils.getPrivateFieldValueByType(lightTextureManager, LightTexture.class, DynamicTexture.class);
        if (lightmapTextureObj == null) {
            return null;
        }
        return (DynamicTexture)lightmapTextureObj;
    }
    
    public void calculateCurrentLightAndSkyColor() {
        if (this.world == null) {
            return;
        }
        if ((this.needLightmapRefresh && TickCounter.tickCounter != this.tickWithLightChange && !this.game.isPaused()) || this.options.realTimeTorches) {
            GLUtils.disp(this.lightmapTexture.getId());
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder());
            GLShim.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);
            for (int i = 0; i < this.lightmapColors.length; ++i) {
                final int index = i * 4;
                this.lightmapColors[i] = (byteBuffer.get(index + 3) << 24) + (byteBuffer.get(index) << 16) + (byteBuffer.get(index + 1) << 8) + (byteBuffer.get(index + 2) << 0);
            }
            if (this.lightmapColors[255] != 0) {
                this.needLightmapRefresh = false;
            }
        }
        boolean lightChanged = false;
        if (this.game.options.gamma != this.lastGamma) {
            lightChanged = true;
            this.lastGamma = this.game.options.gamma;
        }
        final float[] providerLightBrightnessTable = new float[16];
        for (int t = 0; t < 16; ++t) {
            providerLightBrightnessTable[t] = this.world.dimensionType().brightness(t);
        }
        for (int t = 0; t < 16; ++t) {
            if (providerLightBrightnessTable[t] != this.lastLightBrightnessTable[t]) {
                lightChanged = true;
                this.lastLightBrightnessTable[t] = providerLightBrightnessTable[t];
            }
        }
        final float sunBrightness = this.world.getSkyDarken(1.0f);
        if (Math.abs(this.lastSunBrightness - sunBrightness) > 0.01 || (sunBrightness == 1.0 && sunBrightness != this.lastSunBrightness) || (sunBrightness == 0.0 && sunBrightness != this.lastSunBrightness)) {
            lightChanged = true;
            this.needSkyColor = true;
            this.lastSunBrightness = sunBrightness;
        }
        float potionEffect = 0.0f;
        if (this.game.player.hasEffect(MobEffects.NIGHT_VISION)) {
            final int duration = this.game.player.getEffect(MobEffects.NIGHT_VISION).getDuration();
            potionEffect = ((duration > 200) ? 1.0f : (0.7f + Mth.sin((duration - 1.0f) * 3.1415927f * 0.2f) * 0.3f));
        }
        if (this.lastPotion != potionEffect) {
            this.lastPotion = potionEffect;
            lightChanged = true;
        }
        final int lastLightningBolt = this.world.getSkyFlashTime();
        if (this.lastLightning != lastLightningBolt) {
            this.lastLightning = (float)lastLightningBolt;
            lightChanged = true;
        }
        if (this.lastPaused != this.game.isPaused()) {
            this.lastPaused = !this.lastPaused;
            lightChanged = true;
        }
        final boolean scheduledUpdate = (this.timer - 50) % ((this.lastLightBrightnessTable[0] == 0.0f) ? 250 : 2000) == 0;
        if (lightChanged || scheduledUpdate) {
            this.tickWithLightChange = TickCounter.tickCounter;
            lightChanged = false;
            this.needLightmapRefresh = true;
        }
        boolean aboveHorizon = this.game.player.getEyePosition(0.0f).y >= this.world.getLevelData().getHorizonHeight((LevelHeightAccessor)this.world);
        if (this.world.dimension().location().toString().toLowerCase().contains("ether")) {
            aboveHorizon = true;
        }
        if (aboveHorizon != this.lastAboveHorizon) {
            this.needSkyColor = true;
            this.lastAboveHorizon = aboveHorizon;
        }
        final int biomeID = this.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getId((Biome)this.world.getBiome((BlockPos)this.blockPos.withXYZ(GameVariableAccessShim.xCoord(), GameVariableAccessShim.yCoord(), GameVariableAccessShim.zCoord())));
        if (biomeID != this.lastBiome) {
            this.needSkyColor = true;
            this.lastBiome = biomeID;
        }
        if (this.needSkyColor || scheduledUpdate) {
            this.colorManager.setSkyColor(this.getSkyColor());
        }
    }
    
    private int getSkyColor() {
        this.needSkyColor = false;
        final boolean aboveHorizon = this.lastAboveHorizon;
        final float[] fogColors = new float[4];
        final FloatBuffer temp = BufferUtils.createFloatBuffer(4);
        FogRenderer.setupColor(this.game.gameRenderer.getMainCamera(), 0.0f, this.world, this.game.options.renderDistance, this.game.gameRenderer.getDarkenWorldAmount(0.0f));
        GLShim.glGetFloatv(3106, temp);
        temp.get(fogColors);
        final float r = fogColors[0];
        final float g = fogColors[1];
        final float b = fogColors[2];
        if (!aboveHorizon && this.game.options.renderDistance >= 4) {
            return 167772160 + (int)(r * 255.0f) * 65536 + (int)(g * 255.0f) * 256 + (int)(b * 255.0f);
        }
        final int backgroundColor = -16777216 + (int)(r * 255.0f) * 65536 + (int)(g * 255.0f) * 256 + (int)(b * 255.0f);
        final float[] sunsetColors = this.world.effects().getSunriseColor(this.world.getTimeOfDay(0.0f), 0.0f);
        if (sunsetColors != null && this.game.options.renderDistance >= 4) {
            final int sunsetColor = (int)(sunsetColors[3] * 128.0f) * 16777216 + (int)(sunsetColors[0] * 255.0f) * 65536 + (int)(sunsetColors[1] * 255.0f) * 256 + (int)(sunsetColors[2] * 255.0f);
            return ColorUtils.colorAdder(sunsetColor, backgroundColor);
        }
        return backgroundColor;
    }
    
    @Override
    public int[] getLightmapArray() {
        return this.lightmapColors;
    }
    
    @Override
    public void drawMinimap(final PoseStack matrixStack, final Minecraft mc) {
        int scScaleOrig;
        for (scScaleOrig = 1; this.game.getWindow().getWidth() / (scScaleOrig + 1) >= 320 && this.game.getWindow().getHeight() / (scScaleOrig + 1) >= 240; ++scScaleOrig) {}
        final int scScale = scScaleOrig + (this.fullscreenMap ? 0 : this.options.sizeModifier);
        final double scaledWidthD = this.game.getWindow().getWidth() / (double)scScale;
        final double scaledHeightD = this.game.getWindow().getHeight() / (double)scScale;
        this.scWidth = Mth.ceil(scaledWidthD);
        this.scHeight = Mth.ceil(scaledHeightD);
        RenderSystem.backupProjectionMatrix();
        final Matrix4f matrix4f = Matrix4f.orthographic(0.0f, (float)scaledWidthD, 0.0f, (float)scaledHeightD, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f);
        final PoseStack modelViewMatrixStack = RenderSystem.getModelViewStack();
        modelViewMatrixStack.setIdentity();
        modelViewMatrixStack.translate(0.0, 0.0, -2000.0);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        final int mapCorner = this.options.mapCorner;
        Objects.requireNonNull(this.options);
        Label_0233: {
            if (mapCorner != 0) {
                final int mapCorner2 = this.options.mapCorner;
                if (mapCorner2 != 3) {
                    this.mapX = this.scWidth - 37;
                    break Label_0233;
                }
            }
            this.mapX = 37;
        }
        final int mapCorner3 = this.options.mapCorner;
        Label_0291: {
            if (mapCorner3 != 0) {
                final int mapCorner4 = this.options.mapCorner;
                if (mapCorner4 != 1) {
                    this.mapY = this.scHeight - 37;
                    break Label_0291;
                }
            }
            this.mapY = 37;
        }
        final int mapCorner5 = this.options.mapCorner;
        if (mapCorner5 == 1 && this.game.player.getActiveEffects().size() > 0) {
            float statusIconOffset = 0.0f;
            final Collection<MobEffectInstance> statusEffectInstances = this.game.player.getActiveEffects();
            for (final MobEffectInstance statusEffectInstance : statusEffectInstances) {
                if (statusEffectInstance.showIcon()) {
                    if (statusEffectInstance.getEffect().isBeneficial()) {
                        statusIconOffset = Math.max(statusIconOffset, 24.0f);
                    }
                    else {
                        statusIconOffset = Math.max(statusIconOffset, 50.0f);
                    }
                }
            }
            final int scHeight = this.game.getWindow().getGuiScaledHeight();
            final float resFactor = this.scHeight / (float)scHeight;
            this.mapY += (int)(statusIconOffset * resFactor);
        }
        GLShim.glEnable(3042);
        GLShim.glEnable(3553);
        GLShim.glBlendFunc(770, 0);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (!this.options.hide) {
            if (this.fullscreenMap) {
                this.renderMapFull(modelViewMatrixStack, this.scWidth, this.scHeight);
            }
            else {
                this.renderMap(modelViewMatrixStack, this.mapX, this.mapY, scScale);
            }
            GLShim.glDisable(2929);
            if (this.master.getRadar() != null && !this.fullscreenMap) {
                this.layoutVariables.updateVars(scScale, this.mapX, this.mapY, this.zoomScale, this.zoomScaleAdjusted);
                this.master.getRadar().onTickInGame(modelViewMatrixStack, mc, this.layoutVariables);
            }
            if (!this.fullscreenMap) {
                this.drawDirections(matrixStack, this.mapX, this.mapY);
            }
            GLShim.glEnable(3042);
            if (this.fullscreenMap) {
                this.drawArrow(modelViewMatrixStack, this.scWidth / 2, this.scHeight / 2);
            }
            else {
                this.drawArrow(modelViewMatrixStack, this.mapX, this.mapY);
            }
        }
        if (this.options.coords) {
            this.showCoords(matrixStack, this.mapX, this.mapY);
        }
        GLShim.glDepthMask(true);
        GLShim.glEnable(2929);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.applyModelViewMatrix();
        GLShim.glDisable(2929);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.game.font.drawShadow(modelViewMatrixStack, (Component)new TextComponent("******sdkfjhsdkjfhsdkjfh"), 100.0f, 100.0f, -1);
        if (this.showWelcomeScreen) {
            GLShim.glEnable(3042);
            this.drawWelcomeScreen(matrixStack, this.game.getWindow().getGuiScaledWidth(), this.game.getWindow().getGuiScaledHeight());
        }
        GLShim.glDepthMask(true);
        GLShim.glEnable(2929);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GLShim.glTexParameteri(3553, 10241, 9728);
        GLShim.glTexParameteri(3553, 10240, 9728);
    }
    
    private void checkForChanges() {
        boolean changed = false;
        if (this.colorManager.checkForChanges()) {
            this.loadMapImage();
            changed = true;
        }
        if (this.options.isChanged()) {
            if (this.options.filtering) {
                this.mapImages = this.mapImagesFiltered;
            }
            else {
                this.mapImages = this.mapImagesUnfiltered;
            }
            changed = true;
            this.setZoomScale();
        }
        if (changed) {
            this.doFullRender = true;
            this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }
    }
    
    private void mapCalc(boolean full) {
        final int currentX = GameVariableAccessShim.xCoord();
        final int currentZ = GameVariableAccessShim.zCoord();
        final int currentY = GameVariableAccessShim.yCoord();
        final int offsetX = currentX - this.lastX;
        final int offsetZ = currentZ - this.lastZ;
        final int offsetY = currentY - this.lastY;
        final int multi = (int)Math.pow(2.0, this.zoom);
        boolean needHeightAndID = false;
        boolean needHeightMap = false;
        boolean needLight = false;
        boolean skyColorChanged = false;
        final int skyColor = this.colorManager.getAirColor();
        if (this.lastSkyColor != skyColor) {
            skyColorChanged = true;
            this.lastSkyColor = skyColor;
        }
        if (this.options.lightmap) {
            final int torchOffset = this.options.realTimeTorches ? 8 : 0;
            final int skylightMultiplier = 16;
            for (int t = 0; t < 16; ++t) {
                if (this.lastLightmapValues[t] != this.lightmapColors[t * skylightMultiplier + torchOffset]) {
                    needLight = true;
                    this.lastLightmapValues[t] = this.lightmapColors[t * skylightMultiplier + torchOffset];
                }
            }
        }
        if (offsetY != 0) {
            ++this.heightMapFudge;
        }
        else if (this.heightMapFudge != 0) {
            ++this.heightMapFudge;
        }
        if (full || Math.abs(offsetY) >= this.heightMapResetHeight || this.heightMapFudge > this.heightMapResetTime) {
            if (this.lastY != currentY) {
                needHeightMap = true;
            }
            this.lastY = currentY;
            this.heightMapFudge = 0;
        }
        if (Math.abs(offsetX) > 32 * multi || Math.abs(offsetZ) > 32 * multi) {
            full = true;
        }
        boolean nether = false;
        boolean caves = false;
        boolean netherPlayerInOpen = false;
        final VoxelMapMutableBlockPos blockPos = this.blockPos;
        final int lastX = this.lastX;
        final int yCoord = GameVariableAccessShim.yCoord();
        Objects.requireNonNull(this);
        blockPos.setXYZ(lastX, Math.max(Math.min(yCoord, 256 - 1), 0), this.lastZ);
        if (this.game.player.level.dimensionType().hasCeiling()) {
            netherPlayerInOpen = (this.world.getChunk((BlockPos)this.blockPos).getHeight(Heightmap.Types.MOTION_BLOCKING, this.blockPos.getX() & 0xF, this.blockPos.getZ() & 0xF) <= currentY);
            nether = (currentY < 126);
            if (this.options.cavesAllowed && this.options.showCaves && currentY >= 126 && !netherPlayerInOpen) {
                caves = true;
            }
        }
        else if (this.game.player.clientLevel.effects().forceBrightLightmap() && !this.game.player.clientLevel.dimensionType().hasSkyLight()) {
            final boolean endPlayerInOpen = this.world.getChunk((BlockPos)this.blockPos).getHeight(Heightmap.Types.MOTION_BLOCKING, this.blockPos.getX() & 0xF, this.blockPos.getZ() & 0xF) <= currentY;
            if (this.options.cavesAllowed && this.options.showCaves && !endPlayerInOpen) {
                caves = true;
            }
        }
        else if (this.options.cavesAllowed && this.options.showCaves && this.world.getBrightness(LightLayer.SKY, (BlockPos)this.blockPos) <= 0) {
            caves = true;
        }
        final boolean beneathRendering = caves || nether;
        if (this.lastBeneathRendering != beneathRendering) {
            full = true;
        }
        this.lastBeneathRendering = beneathRendering;
        needHeightAndID = (needHeightMap && (nether || caves));
        int color24 = -1;
        synchronized (this.coordinateLock) {
            if (!full) {
                this.mapImages[this.zoom].moveY(offsetZ);
                this.mapImages[this.zoom].moveX(offsetX);
            }
            this.lastX = currentX;
            this.lastZ = currentZ;
        }
        final int startX = currentX - 16 * multi;
        final int startZ = currentZ - 16 * multi;
        if (!full) {
            this.mapData[this.zoom].moveZ(offsetZ);
            this.mapData[this.zoom].moveX(offsetX);
            for (int imageY = (offsetZ > 0) ? (32 * multi - 1) : (-offsetZ - 1); imageY >= ((offsetZ > 0) ? (32 * multi - offsetZ) : 0); --imageY) {
                for (int imageX = 0; imageX < 32 * multi; ++imageX) {
                    color24 = this.getPixelColor(true, true, true, true, nether, caves, (Level)this.world, multi, startX, startZ, imageX, imageY);
                    this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
                }
            }
            for (int imageY = 32 * multi - 1; imageY >= 0; --imageY) {
                for (int imageX = (offsetX > 0) ? (32 * multi - offsetX) : 0; imageX < ((offsetX > 0) ? (32 * multi) : (-offsetX)); ++imageX) {
                    color24 = this.getPixelColor(true, true, true, true, nether, caves, (Level)this.world, multi, startX, startZ, imageX, imageY);
                    this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
                }
            }
        }
        if (full || (this.options.heightmap && needHeightMap) || needHeightAndID || (this.options.lightmap && needLight) || skyColorChanged) {
            for (int imageY = 32 * multi - 1; imageY >= 0; --imageY) {
                for (int imageX = 0; imageX < 32 * multi; ++imageX) {
                    color24 = this.getPixelColor(full, full || needHeightAndID, full, full || needLight || needHeightAndID, nether, caves, (Level)this.world, multi, startX, startZ, imageX, imageY);
                    this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
                }
            }
        }
        if ((full || offsetX != 0 || offsetZ != 0 || !this.lastFullscreen) && this.fullscreenMap) {
            final int biomeOverlay = this.options.biomeOverlay;
            Objects.requireNonNull(this.options);
            if (biomeOverlay != 0) {
                this.mapData[this.zoom].segmentBiomes();
                this.mapData[this.zoom].findCenterOfSegments(!this.options.oldNorth);
            }
        }
        this.lastFullscreen = this.fullscreenMap;
        if (full || offsetX != 0 || offsetZ != 0 || needHeightMap || needLight || skyColorChanged) {
            this.imageChanged = true;
        }
        if (needLight || skyColorChanged) {
            this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }
    }
    
    @Override
    public void handleChangeInWorld(final int chunkX, final int chunkZ) {
        this.chunkCache[this.zoom].registerChangeAt(chunkX, chunkZ);
    }
    
    @Override
    public void processChunk(final LevelChunk chunk) {
        this.rectangleCalc(chunk.getPos().x * 16, chunk.getPos().z * 16, chunk.getPos().x * 16 + 15, chunk.getPos().z * 16 + 15);
    }
    
    private void rectangleCalc(int left, int top, int right, int bottom) {
        boolean nether = false;
        boolean caves = false;
        boolean netherPlayerInOpen = false;
        final VoxelMapMutableBlockPos blockPos = this.blockPos;
        final int lastX = this.lastX;
        final int yCoord = GameVariableAccessShim.yCoord();
        Objects.requireNonNull(this);
        blockPos.setXYZ(lastX, Math.max(Math.min(yCoord, 256 - 1), 0), this.lastZ);
        final int currentY = GameVariableAccessShim.yCoord();
        if (this.game.player.level.dimensionType().hasCeiling()) {
            netherPlayerInOpen = (this.world.getChunk((BlockPos)this.blockPos).getHeight(Heightmap.Types.MOTION_BLOCKING, this.blockPos.getX() & 0xF, this.blockPos.getZ() & 0xF) <= currentY);
            nether = (currentY < 126);
            if (this.options.cavesAllowed && this.options.showCaves && currentY >= 126 && !netherPlayerInOpen) {
                caves = true;
            }
        }
        else if (this.game.player.clientLevel.effects().forceBrightLightmap() && !this.game.player.clientLevel.dimensionType().hasSkyLight()) {
            final boolean endPlayerInOpen = this.world.getChunk((BlockPos)this.blockPos).getHeight(Heightmap.Types.MOTION_BLOCKING, this.blockPos.getX() & 0xF, this.blockPos.getZ() & 0xF) <= currentY;
            if (this.options.cavesAllowed && this.options.showCaves && !endPlayerInOpen) {
                caves = true;
            }
        }
        else if (this.options.cavesAllowed && this.options.showCaves && this.world.getBrightness(LightLayer.SKY, (BlockPos)this.blockPos) <= 0) {
            caves = true;
        }
        int startX = this.lastX;
        int startZ = this.lastZ;
        final int multi = (int)Math.pow(2.0, this.zoom);
        startX -= 16 * multi;
        startZ -= 16 * multi;
        left = left - startX - 1;
        right = right - startX + 1;
        top = top - startZ - 1;
        bottom = bottom - startZ + 1;
        left = Math.max(0, left);
        right = Math.min(32 * multi - 1, right);
        top = Math.max(0, top);
        bottom = Math.min(32 * multi - 1, bottom);
        int color24 = 0;
        for (int imageY = bottom; imageY >= top; --imageY) {
            for (int imageX = left; imageX <= right; ++imageX) {
                color24 = this.getPixelColor(true, true, true, true, nether, caves, (Level)this.world, multi, startX, startZ, imageX, imageY);
                this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
            }
        }
        this.imageChanged = true;
    }
    
    private int getPixelColor(final boolean needBiome, final boolean needHeightAndID, final boolean needTint, final boolean needLight, final boolean nether, final boolean caves, final Level world, final int multi, final int startX, final int startZ, final int imageX, final int imageY) {
        int surfaceHeight = 0;
        int seafloorHeight = -1;
        int transparentHeight = -1;
        int foliageHeight = -1;
        int surfaceColor = 0;
        int seafloorColor = 0;
        int transparentColor = 0;
        int foliageColor = 0;
        this.surfaceBlockState = null;
        this.transparentBlockState = BlockRepository.air.defaultBlockState();
        this.foliageBlockState = BlockRepository.air.defaultBlockState();
        this.seafloorBlockState = BlockRepository.air.defaultBlockState();
        boolean surfaceBlockChangeForcedTint = false;
        boolean transparentBlockChangeForcedTint = false;
        boolean foliageBlockChangeForcedTint = false;
        boolean seafloorBlockChangeForcedTint = false;
        int surfaceBlockStateID = 0;
        int transparentBlockStateID = 0;
        int foliageBlockStateID = 0;
        int seafloorBlockStateID = 0;
        this.blockPos = this.blockPos.withXYZ(startX + imageX, 0, startZ + imageY);
        int color24 = 0;
        int biomeID = 0;
        if (needBiome) {
            if (world.hasChunkAt((BlockPos)this.blockPos)) {
                biomeID = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getId((Biome)world.getBiome((BlockPos)this.blockPos));
            }
            else {
                biomeID = -1;
            }
            this.mapData[this.zoom].setBiomeID(imageX, imageY, biomeID);
        }
        else {
            biomeID = this.mapData[this.zoom].getBiomeID(imageX, imageY);
        }
        final int biomeOverlay = this.options.biomeOverlay;
        Objects.requireNonNull(this.options);
        if (biomeOverlay == 1) {
            if (biomeID >= 0) {
                color24 = (BiomeRepository.getBiomeColor(biomeID) | 0xFF000000);
            }
            else {
                color24 = 0;
            }
            color24 = MapUtils.doSlimeAndGrid(color24, startX + imageX, startZ + imageY);
            return color24;
        }
        boolean solid = false;
        if (needHeightAndID) {
            if (nether || caves) {
                surfaceHeight = this.getNetherHeight(nether, startX + imageX, startZ + imageY);
                this.surfaceBlockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                surfaceBlockStateID = BlockRepository.getStateId(this.surfaceBlockState);
                foliageHeight = surfaceHeight + 1;
                this.blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
                this.foliageBlockState = world.getBlockState((BlockPos)this.blockPos);
                final Material material = this.foliageBlockState.getMaterial();
                if (material == Material.TOP_SNOW || material == Material.AIR || material == Material.LAVA || material == Material.WATER) {
                    foliageHeight = -1;
                }
                else {
                    foliageBlockStateID = BlockRepository.getStateId(this.foliageBlockState);
                }
            }
            else {
                final LevelChunk chunk = world.getChunkAt((BlockPos)this.blockPos);
                transparentHeight = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, this.blockPos.getX() & 0xF, this.blockPos.getZ() & 0xF) + 1;
                this.transparentBlockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(startX + imageX, transparentHeight - 1, startZ + imageY));
                FluidState fluidState = this.transparentBlockState.getFluidState();
                if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                    this.transparentBlockState = fluidState.createLegacyBlock();
                }
                surfaceHeight = transparentHeight;
                this.surfaceBlockState = this.transparentBlockState;
                VoxelShape voxelShape = null;
                boolean hasOpacity = this.surfaceBlockState.getLightBlock((BlockGetter)world, (BlockPos)this.blockPos) > 0;
                if (!hasOpacity && this.surfaceBlockState.canOcclude() && this.surfaceBlockState.useShapeForLightOcclusion()) {
                    voxelShape = this.surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)this.blockPos, Direction.DOWN);
                    hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                    voxelShape = this.surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)this.blockPos, Direction.UP);
                    hasOpacity = (hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape));
                }
                while (!hasOpacity && surfaceHeight > 0) {
                    this.foliageBlockState = this.surfaceBlockState;
                    --surfaceHeight;
                    this.surfaceBlockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                    fluidState = this.surfaceBlockState.getFluidState();
                    if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                        this.surfaceBlockState = fluidState.createLegacyBlock();
                    }
                    hasOpacity = (this.surfaceBlockState.getLightBlock((BlockGetter)world, (BlockPos)this.blockPos) > 0);
                    if (!hasOpacity && this.surfaceBlockState.canOcclude() && this.surfaceBlockState.useShapeForLightOcclusion()) {
                        voxelShape = this.surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)this.blockPos, Direction.DOWN);
                        hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                        voxelShape = this.surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)this.blockPos, Direction.UP);
                        hasOpacity = (hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape));
                    }
                }
                if (surfaceHeight == transparentHeight) {
                    transparentHeight = -1;
                    this.transparentBlockState = BlockRepository.air.defaultBlockState();
                    this.foliageBlockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(startX + imageX, surfaceHeight, startZ + imageY));
                }
                if (this.foliageBlockState.getMaterial() == Material.TOP_SNOW) {
                    this.surfaceBlockState = this.foliageBlockState;
                    this.foliageBlockState = BlockRepository.air.defaultBlockState();
                }
                if (this.foliageBlockState == this.transparentBlockState) {
                    this.foliageBlockState = BlockRepository.air.defaultBlockState();
                }
                if (this.foliageBlockState == null || this.foliageBlockState.getMaterial() == Material.AIR) {
                    foliageHeight = -1;
                }
                else {
                    foliageHeight = surfaceHeight + 1;
                }
                Material material2 = this.surfaceBlockState.getMaterial();
                if (material2 == Material.WATER || material2 == Material.ICE) {
                    seafloorHeight = surfaceHeight;
                    this.seafloorBlockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY));
                    while (this.seafloorBlockState.getLightBlock((BlockGetter)world, (BlockPos)this.blockPos) < 5 && this.seafloorBlockState.getMaterial() != Material.LEAVES && seafloorHeight > 1) {
                        material2 = this.seafloorBlockState.getMaterial();
                        if (transparentHeight == -1 && material2 != Material.ICE && material2 != Material.WATER && material2.blocksMotion()) {
                            transparentHeight = seafloorHeight;
                            this.transparentBlockState = this.seafloorBlockState;
                        }
                        if (foliageHeight == -1 && seafloorHeight != transparentHeight && this.transparentBlockState != this.seafloorBlockState && material2 != Material.ICE && material2 != Material.WATER && material2 != Material.AIR && material2 != Material.BUBBLE_COLUMN) {
                            foliageHeight = seafloorHeight;
                            this.foliageBlockState = this.seafloorBlockState;
                        }
                        --seafloorHeight;
                        this.seafloorBlockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY));
                    }
                    if (this.seafloorBlockState.getMaterial() == Material.WATER) {
                        this.seafloorBlockState = BlockRepository.air.defaultBlockState();
                    }
                }
            }
            surfaceBlockStateID = BlockRepository.getStateId(this.surfaceBlockState);
            if (this.options.biomes && this.surfaceBlockState != this.mapData[this.zoom].getBlockstate(imageX, imageY)) {
                surfaceBlockChangeForcedTint = true;
            }
            this.mapData[this.zoom].setHeight(imageX, imageY, surfaceHeight);
            this.mapData[this.zoom].setBlockstateID(imageX, imageY, surfaceBlockStateID);
            if (this.options.biomes && this.transparentBlockState != this.mapData[this.zoom].getTransparentBlockstate(imageX, imageY)) {
                transparentBlockChangeForcedTint = true;
            }
            this.mapData[this.zoom].setTransparentHeight(imageX, imageY, transparentHeight);
            transparentBlockStateID = BlockRepository.getStateId(this.transparentBlockState);
            this.mapData[this.zoom].setTransparentBlockstateID(imageX, imageY, transparentBlockStateID);
            if (this.options.biomes && this.foliageBlockState != this.mapData[this.zoom].getFoliageBlockstate(imageX, imageY)) {
                foliageBlockChangeForcedTint = true;
            }
            this.mapData[this.zoom].setFoliageHeight(imageX, imageY, foliageHeight);
            foliageBlockStateID = BlockRepository.getStateId(this.foliageBlockState);
            this.mapData[this.zoom].setFoliageBlockstateID(imageX, imageY, foliageBlockStateID);
            if (this.options.biomes && this.seafloorBlockState != this.mapData[this.zoom].getOceanFloorBlockstate(imageX, imageY)) {
                seafloorBlockChangeForcedTint = true;
            }
            this.mapData[this.zoom].setOceanFloorHeight(imageX, imageY, seafloorHeight);
            seafloorBlockStateID = BlockRepository.getStateId(this.seafloorBlockState);
            this.mapData[this.zoom].setOceanFloorBlockstateID(imageX, imageY, seafloorBlockStateID);
        }
        else {
            surfaceHeight = this.mapData[this.zoom].getHeight(imageX, imageY);
            surfaceBlockStateID = this.mapData[this.zoom].getBlockstateID(imageX, imageY);
            this.surfaceBlockState = BlockRepository.getStateById(surfaceBlockStateID);
            transparentHeight = this.mapData[this.zoom].getTransparentHeight(imageX, imageY);
            transparentBlockStateID = this.mapData[this.zoom].getTransparentBlockstateID(imageX, imageY);
            this.transparentBlockState = BlockRepository.getStateById(transparentBlockStateID);
            foliageHeight = this.mapData[this.zoom].getFoliageHeight(imageX, imageY);
            foliageBlockStateID = this.mapData[this.zoom].getFoliageBlockstateID(imageX, imageY);
            this.foliageBlockState = BlockRepository.getStateById(foliageBlockStateID);
            seafloorHeight = this.mapData[this.zoom].getOceanFloorHeight(imageX, imageY);
            seafloorBlockStateID = this.mapData[this.zoom].getOceanFloorBlockstateID(imageX, imageY);
            this.seafloorBlockState = BlockRepository.getStateById(seafloorBlockStateID);
        }
        if (surfaceHeight == -1) {
            surfaceHeight = this.lastY + 1;
            solid = true;
        }
        if (this.surfaceBlockState.getMaterial() == Material.LAVA) {
            solid = false;
        }
        if (this.options.biomes) {
            surfaceColor = this.colorManager.getBlockColor(this.blockPos, surfaceBlockStateID, biomeID);
            int tint = -1;
            if (needTint || surfaceBlockChangeForcedTint) {
                tint = this.colorManager.getBiomeTint(this.mapData[this.zoom], world, this.surfaceBlockState, surfaceBlockStateID, this.blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY), this.tempBlockPos, startX, startZ);
                this.mapData[this.zoom].setBiomeTint(imageX, imageY, tint);
            }
            else {
                tint = this.mapData[this.zoom].getBiomeTint(imageX, imageY);
            }
            if (tint != -1) {
                surfaceColor = ColorUtils.colorMultiplier(surfaceColor, tint);
            }
        }
        else {
            surfaceColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, surfaceBlockStateID);
        }
        surfaceColor = this.applyHeight(surfaceColor, nether, caves, world, multi, startX, startZ, imageX, imageY, surfaceHeight, solid, 1);
        int light = solid ? 0 : 255;
        if (needLight) {
            light = this.getLight(surfaceColor, this.surfaceBlockState, world, startX + imageX, startZ + imageY, surfaceHeight, solid);
            this.mapData[this.zoom].setLight(imageX, imageY, light);
        }
        else {
            light = this.mapData[this.zoom].getLight(imageX, imageY);
        }
        if (light == 0) {
            surfaceColor = 0;
        }
        else if (light != 255) {
            surfaceColor = ColorUtils.colorMultiplier(surfaceColor, light);
        }
        if (this.options.waterTransparency && seafloorHeight != -1) {
            if (this.options.biomes) {
                seafloorColor = this.colorManager.getBlockColor(this.blockPos, seafloorBlockStateID, biomeID);
                int tint2 = -1;
                if (needTint || seafloorBlockChangeForcedTint) {
                    tint2 = this.colorManager.getBiomeTint(this.mapData[this.zoom], world, this.seafloorBlockState, seafloorBlockStateID, this.blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY), this.tempBlockPos, startX, startZ);
                    this.mapData[this.zoom].setOceanFloorBiomeTint(imageX, imageY, tint2);
                }
                else {
                    tint2 = this.mapData[this.zoom].getOceanFloorBiomeTint(imageX, imageY);
                }
                if (tint2 != -1) {
                    seafloorColor = ColorUtils.colorMultiplier(seafloorColor, tint2);
                }
            }
            else {
                seafloorColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, seafloorBlockStateID);
            }
            seafloorColor = this.applyHeight(seafloorColor, nether, caves, world, multi, startX, startZ, imageX, imageY, seafloorHeight, solid, 0);
            int seafloorLight = 255;
            if (needLight) {
                seafloorLight = this.getLight(seafloorColor, this.seafloorBlockState, world, startX + imageX, startZ + imageY, seafloorHeight, solid);
                this.blockPos.setXYZ(startX + imageX, seafloorHeight, startZ + imageY);
                final BlockState blockStateAbove = world.getBlockState((BlockPos)this.blockPos);
                final Material materialAbove = blockStateAbove.getMaterial();
                if (this.options.lightmap && materialAbove == Material.ICE) {
                    int multiplier = 255;
                    if (this.game.options.ambientOcclusion == AmbientOcclusionStatus.MIN) {
                        multiplier = 200;
                    }
                    else if (this.game.options.ambientOcclusion == AmbientOcclusionStatus.MAX) {
                        multiplier = 120;
                    }
                    seafloorLight = ColorUtils.colorMultiplier(seafloorLight, 0xFF000000 | multiplier << 16 | multiplier << 8 | multiplier);
                }
                this.mapData[this.zoom].setOceanFloorLight(imageX, imageY, seafloorLight);
            }
            else {
                seafloorLight = this.mapData[this.zoom].getOceanFloorLight(imageX, imageY);
            }
            if (seafloorLight == 0) {
                seafloorColor = 0;
            }
            else if (seafloorLight != 255) {
                seafloorColor = ColorUtils.colorMultiplier(seafloorColor, seafloorLight);
            }
        }
        if (this.options.blockTransparency) {
            if (transparentHeight != -1 && this.transparentBlockState != null && this.transparentBlockState != BlockRepository.air.defaultBlockState()) {
                if (this.options.biomes) {
                    transparentColor = this.colorManager.getBlockColor(this.blockPos, transparentBlockStateID, biomeID);
                    int tint2 = -1;
                    if (needTint || transparentBlockChangeForcedTint) {
                        tint2 = this.colorManager.getBiomeTint(this.mapData[this.zoom], world, this.transparentBlockState, transparentBlockStateID, this.blockPos.withXYZ(startX + imageX, transparentHeight - 1, startZ + imageY), this.tempBlockPos, startX, startZ);
                        this.mapData[this.zoom].setTransparentBiomeTint(imageX, imageY, tint2);
                    }
                    else {
                        tint2 = this.mapData[this.zoom].getTransparentBiomeTint(imageX, imageY);
                    }
                    if (tint2 != -1) {
                        transparentColor = ColorUtils.colorMultiplier(transparentColor, tint2);
                    }
                }
                else {
                    transparentColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, transparentBlockStateID);
                }
                transparentColor = this.applyHeight(transparentColor, nether, caves, world, multi, startX, startZ, imageX, imageY, transparentHeight, solid, 3);
                int transparentLight = 255;
                if (needLight) {
                    transparentLight = this.getLight(transparentColor, this.transparentBlockState, world, startX + imageX, startZ + imageY, transparentHeight, solid);
                    this.mapData[this.zoom].setTransparentLight(imageX, imageY, transparentLight);
                }
                else {
                    transparentLight = this.mapData[this.zoom].getTransparentLight(imageX, imageY);
                }
                if (transparentLight == 0) {
                    transparentColor = 0;
                }
                else if (transparentLight != 255) {
                    transparentColor = ColorUtils.colorMultiplier(transparentColor, transparentLight);
                }
            }
            if (foliageHeight != -1 && this.foliageBlockState != null && this.foliageBlockState != BlockRepository.air.defaultBlockState()) {
                if (this.options.biomes) {
                    foliageColor = this.colorManager.getBlockColor(this.blockPos, foliageBlockStateID, biomeID);
                    int tint2 = -1;
                    if (needTint || foliageBlockChangeForcedTint) {
                        tint2 = this.colorManager.getBiomeTint(this.mapData[this.zoom], world, this.foliageBlockState, foliageBlockStateID, this.blockPos.withXYZ(startX + imageX, foliageHeight - 1, startZ + imageY), this.tempBlockPos, startX, startZ);
                        this.mapData[this.zoom].setFoliageBiomeTint(imageX, imageY, tint2);
                    }
                    else {
                        tint2 = this.mapData[this.zoom].getFoliageBiomeTint(imageX, imageY);
                    }
                    if (tint2 != -1) {
                        foliageColor = ColorUtils.colorMultiplier(foliageColor, tint2);
                    }
                }
                else {
                    foliageColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, foliageBlockStateID);
                }
                foliageColor = this.applyHeight(foliageColor, nether, caves, world, multi, startX, startZ, imageX, imageY, foliageHeight, solid, 2);
                int foliageLight = 255;
                if (needLight) {
                    foliageLight = this.getLight(foliageColor, this.foliageBlockState, world, startX + imageX, startZ + imageY, foliageHeight, solid);
                    this.mapData[this.zoom].setFoliageLight(imageX, imageY, foliageLight);
                }
                else {
                    foliageLight = this.mapData[this.zoom].getFoliageLight(imageX, imageY);
                }
                if (foliageLight == 0) {
                    foliageColor = 0;
                }
                else if (foliageLight != 255) {
                    foliageColor = ColorUtils.colorMultiplier(foliageColor, foliageLight);
                }
            }
        }
        if (seafloorColor != 0 && seafloorHeight > 0) {
            color24 = seafloorColor;
            if (foliageColor != 0 && foliageHeight <= surfaceHeight) {
                color24 = ColorUtils.colorAdder(foliageColor, color24);
            }
            if (transparentColor != 0 && transparentHeight <= surfaceHeight) {
                color24 = ColorUtils.colorAdder(transparentColor, color24);
            }
            color24 = ColorUtils.colorAdder(surfaceColor, color24);
        }
        else {
            color24 = surfaceColor;
        }
        if (foliageColor != 0 && foliageHeight > surfaceHeight) {
            color24 = ColorUtils.colorAdder(foliageColor, color24);
        }
        if (transparentColor != 0 && transparentHeight > surfaceHeight) {
            color24 = ColorUtils.colorAdder(transparentColor, color24);
        }
        final int biomeOverlay2 = this.options.biomeOverlay;
        Objects.requireNonNull(this.options);
        if (biomeOverlay2 == 2) {
            int bc = 0;
            if (biomeID >= 0) {
                bc = BiomeRepository.getBiomeColor(biomeID);
            }
            bc |= 0x7F000000;
            color24 = ColorUtils.colorAdder(bc, color24);
        }
        color24 = MapUtils.doSlimeAndGrid(color24, startX + imageX, startZ + imageY);
        return color24;
    }
    
    private final int getBlockHeight(final boolean nether, final boolean caves, final Level world, final int x, final int z) {
        final int playerHeight = GameVariableAccessShim.yCoord();
        this.blockPos.setXYZ(x, playerHeight, z);
        final LevelChunk chunk = (LevelChunk)world.getChunk((BlockPos)this.blockPos);
        int height = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, this.blockPos.getX() & 0xF, this.blockPos.getZ() & 0xF) + 1;
        BlockState blockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(x, height - 1, z));
        FluidState fluidState = this.transparentBlockState.getFluidState();
        if (fluidState != Fluids.EMPTY.defaultFluidState()) {
            blockState = fluidState.createLegacyBlock();
        }
        while (blockState.getLightBlock((BlockGetter)world, (BlockPos)this.blockPos) == 0 && height > 0) {
            --height;
            blockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(x, height - 1, z));
            fluidState = this.surfaceBlockState.getFluidState();
            if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                blockState = fluidState.createLegacyBlock();
            }
        }
        if ((!nether && !caves) || height <= playerHeight) {
            return height;
        }
        return this.getNetherHeight(nether, x, z);
    }
    
    private int getNetherHeight(final boolean nether, final int x, final int z) {
        int y = this.lastY;
        this.blockPos.setXYZ(x, y, z);
        BlockState blockState = this.world.getBlockState((BlockPos)this.blockPos);
        if (blockState.getLightBlock((BlockGetter)this.world, (BlockPos)this.blockPos) == 0 && blockState.getMaterial() != Material.LAVA) {
            while (y > 0) {
                --y;
                this.blockPos.setXYZ(x, y, z);
                blockState = this.world.getBlockState((BlockPos)this.blockPos);
                if (blockState.getLightBlock((BlockGetter)this.world, (BlockPos)this.blockPos) > 0 || blockState.getMaterial() == Material.LAVA) {
                    return y + 1;
                }
            }
            return y;
        }
        while (y <= this.lastY + 10 && y < (nether ? 127 : 256)) {
            ++y;
            this.blockPos.setXYZ(x, y, z);
            blockState = this.world.getBlockState((BlockPos)this.blockPos);
            if (blockState.getLightBlock((BlockGetter)this.world, (BlockPos)this.blockPos) == 0 && blockState.getMaterial() != Material.LAVA) {
                return y;
            }
        }
        return -1;
    }
    
    private final int getSeafloorHeight(final Level world, final int x, final int z, int height) {
        for (BlockState blockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(x, height - 1, z)); blockState.getLightBlock((BlockGetter)world, (BlockPos)this.blockPos) < 5 && blockState.getMaterial() != Material.LEAVES && height > 1; --height, blockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(x, height - 1, z))) {}
        return height;
    }
    
    private final int getTransparentHeight(final boolean nether, final boolean caves, final Level world, final int x, final int z, final int height) {
        int transHeight = -1;
        if (caves || nether) {
            transHeight = -1;
        }
        else {
            transHeight = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, (BlockPos)this.blockPos.withXYZ(x, height, z)).getY();
            if (transHeight <= height) {
                transHeight = -1;
            }
        }
        BlockState blockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(x, transHeight - 1, z));
        Material material = blockState.getMaterial();
        if (transHeight == height + 1 && material == Material.TOP_SNOW) {
            transHeight = -1;
        }
        if (material == Material.BARRIER) {
            ++transHeight;
            blockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(x, transHeight - 1, z));
            material = blockState.getMaterial();
            if (material == Material.AIR) {
                transHeight = -1;
            }
        }
        return transHeight;
    }
    
    private int applyHeight(int color24, final boolean nether, final boolean caves, final Level world, final int multi, final int startX, final int startZ, final int imageX, final int imageY, final int height, final boolean solid, final int layer) {
        if (color24 != this.colorManager.getAirColor() && color24 != 0 && (this.options.heightmap || this.options.slopemap) && !solid) {
            int heightComp = -1;
            int diff = 0;
            double sc = 0.0;
            if (this.options.slopemap) {
                if (imageX > 0 && imageY < 32 * multi - 1) {
                    if (layer == 0) {
                        heightComp = this.mapData[this.zoom].getOceanFloorHeight(imageX - 1, imageY + 1);
                    }
                    if (layer == 1) {
                        heightComp = this.mapData[this.zoom].getHeight(imageX - 1, imageY + 1);
                    }
                    if (layer == 2) {
                        heightComp = height;
                    }
                    if (layer == 3) {
                        heightComp = this.mapData[this.zoom].getTransparentHeight(imageX - 1, imageY + 1);
                        if (heightComp == -1) {
                            final Block block = BlockRepository.getStateById(this.mapData[this.zoom].getTransparentBlockstateID(imageX, imageY)).getBlock();
                            if (block instanceof GlassBlock || block instanceof StainedGlassBlock) {
                                heightComp = this.mapData[this.zoom].getHeight(imageX - 1, imageY + 1);
                            }
                        }
                    }
                }
                else {
                    if (layer == 0) {
                        final int baseHeight = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                        heightComp = this.getSeafloorHeight(world, startX + imageX - 1, startZ + imageY + 1, baseHeight);
                    }
                    if (layer == 1) {
                        heightComp = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                    }
                    if (layer == 2) {
                        heightComp = height;
                    }
                    if (layer == 3) {
                        final int baseHeight = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                        heightComp = this.getTransparentHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1, baseHeight);
                        if (heightComp == -1) {
                            final BlockState blockState = world.getBlockState((BlockPos)this.blockPos.withXYZ(startX + imageX, height - 1, startZ + imageY));
                            final Block block2 = blockState.getBlock();
                            if (block2 instanceof GlassBlock || block2 instanceof StainedGlassBlock) {
                                heightComp = baseHeight;
                            }
                        }
                    }
                }
                if (heightComp == -1) {
                    heightComp = height;
                }
                diff = heightComp - height;
                if (diff != 0) {
                    sc = ((diff > 0) ? 1.0 : ((diff < 0) ? -1.0 : 0.0));
                    sc /= 8.0;
                }
                if (this.options.heightmap) {
                    diff = height - this.lastY;
                    final double heightsc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 3.0;
                    sc = ((diff > 0) ? (sc + heightsc) : (sc - heightsc));
                }
            }
            else if (this.options.heightmap) {
                diff = height - this.lastY;
                sc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 1.8;
                if (diff < 0) {
                    sc = 0.0 - sc;
                }
            }
            final int alpha = color24 >> 24 & 0xFF;
            int r = color24 >> 16 & 0xFF;
            int g = color24 >> 8 & 0xFF;
            int b = color24 >> 0 & 0xFF;
            if (sc > 0.0) {
                r += (int)(sc * (255 - r));
                g += (int)(sc * (255 - g));
                b += (int)(sc * (255 - b));
            }
            else if (sc < 0.0) {
                sc = Math.abs(sc);
                r -= (int)(sc * r);
                g -= (int)(sc * g);
                b -= (int)(sc * b);
            }
            color24 = alpha * 16777216 + r * 65536 + g * 256 + b;
        }
        return color24;
    }
    
    private int getLight(final int color24, final BlockState blockState, final Level world, final int x, final int z, final int height, final boolean solid) {
        int i3 = 255;
        if (solid) {
            i3 = 0;
        }
        else if (color24 != this.colorManager.getAirColor() && color24 != 0 && this.options.lightmap) {
            final VoxelMapMutableBlockPos blockPos = this.blockPos;
            Objects.requireNonNull(this);
            blockPos.setXYZ(x, Math.max(Math.min(height, 256 - 1), 0), z);
            int blockLight = world.getBrightness(LightLayer.BLOCK, (BlockPos)this.blockPos);
            final int skyLight = world.getBrightness(LightLayer.SKY, (BlockPos)this.blockPos);
            if (blockState.getMaterial() == Material.LAVA || blockState.getBlock() == Blocks.MAGMA_BLOCK) {
                blockLight = 14;
            }
            i3 = this.lightmapColors[blockLight + skyLight * 16];
        }
        return i3;
    }
    
    private void renderMap(final PoseStack matrixStack, final int x, final int y, final int scScale) {
        float scale = 1.0f;
        if (this.options.squareMap && this.options.rotates) {
            scale = 1.4142f;
        }
        if (GLUtils.hasAlphaBits) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            GLShim.glColorMask(false, false, false, true);
            GLShim.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLShim.glClear(16384);
            GLShim.glBlendFunc(770, 771);
            GLShim.glColorMask(true, true, true, true);
            GLUtils.img2(this.options.squareMap ? this.squareStencil : this.circleStencil);
            GLUtils.drawPre();
            GLUtils.setMap((float)x, (float)y, 128);
            GLUtils.drawPost();
            GLShim.glBlendFunc(772, 773);
            synchronized (this.coordinateLock) {
                if (this.imageChanged) {
                    this.imageChanged = false;
                    this.mapImages[this.zoom].write();
                    this.lastImageX = this.lastX;
                    this.lastImageZ = this.lastZ;
                }
            }
            final float multi = (float)(1.0 / this.zoomScaleAdjusted);
            this.percentX = (float)(GameVariableAccessShim.xCoordDouble() - this.lastImageX);
            this.percentY = (float)(GameVariableAccessShim.zCoordDouble() - this.lastImageZ);
            this.percentX *= multi;
            this.percentY *= multi;
            GLUtils.disp2(this.mapImages[this.zoom].getIndex());
            matrixStack.pushPose();
            matrixStack.translate((double)x, (double)y, 0.0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(this.options.rotates ? (-this.direction) : ((float)this.northRotate)));
            matrixStack.translate((double)(-x), (double)(-y), 0.0);
            matrixStack.translate((double)(-this.percentX), (double)(-this.percentY), 0.0);
            RenderSystem.applyModelViewMatrix();
            GLShim.glTexParameteri(3553, 10241, 9987);
            GLShim.glTexParameteri(3553, 10240, 9729);
        }
        else {
            GLShim.glBindTexture(3553, 0);
            final Matrix4f minimapProjectionMatrix = RenderSystem.getProjectionMatrix();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            final Matrix4f matrix4f = Matrix4f.orthographic(0.0f, 512.0f, 0.0f, 512.0f, 1000.0f, 3000.0f);
            RenderSystem.setProjectionMatrix(matrix4f);
            GLUtils.bindFrameBuffer();
            GLShim.glViewport(0, 0, 512, 512);
            matrixStack.pushPose();
            matrixStack.setIdentity();
            matrixStack.translate(0.0, 0.0, -2000.0);
            RenderSystem.applyModelViewMatrix();
            GLShim.glDepthMask(false);
            GLShim.glDisable(2929);
            GLShim.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLShim.glClear(16384);
            GLShim.glBlendFunc(770, 0);
            GLUtils.img2(this.options.squareMap ? this.squareStencil : this.circleStencil);
            GLUtils.drawPre();
            GLUtils.ldrawthree(256.0f - 256.0f / scale, 256.0f + 256.0f / scale, 1.0, 0.0f, 0.0f);
            GLUtils.ldrawthree(256.0f + 256.0f / scale, 256.0f + 256.0f / scale, 1.0, 1.0f, 0.0f);
            GLUtils.ldrawthree(256.0f + 256.0f / scale, 256.0f - 256.0f / scale, 1.0, 1.0f, 1.0f);
            GLUtils.ldrawthree(256.0f - 256.0f / scale, 256.0f - 256.0f / scale, 1.0, 0.0f, 1.0f);
            final BufferBuilder bb = Tesselator.getInstance().getBuilder();
            bb.end();
            BufferUploader.end(bb);
            GLShim.glBlendFuncSeparate(1, 0, 774, 0);
            synchronized (this.coordinateLock) {
                if (this.imageChanged) {
                    this.imageChanged = false;
                    this.mapImages[this.zoom].write();
                    this.lastImageX = this.lastX;
                    this.lastImageZ = this.lastZ;
                }
            }
            final float multi2 = (float)(1.0 / this.zoomScale);
            this.percentX = (float)(GameVariableAccessShim.xCoordDouble() - this.lastImageX);
            this.percentY = (float)(GameVariableAccessShim.zCoordDouble() - this.lastImageZ);
            this.percentX *= multi2;
            this.percentY *= multi2;
            GLUtils.disp2(this.mapImages[this.zoom].getIndex());
            GLShim.glTexParameteri(3553, 10241, 9987);
            GLShim.glTexParameteri(3553, 10240, 9729);
            matrixStack.pushPose();
            matrixStack.translate(256.0, 256.0, 0.0);
            if (!this.options.rotates) {
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)(-this.northRotate)));
            }
            else {
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(this.direction));
            }
            matrixStack.translate(-256.0, -256.0, 0.0);
            matrixStack.translate((double)(-this.percentX * 512.0f / 64.0f), (double)(this.percentY * 512.0f / 64.0f), 0.0);
            RenderSystem.applyModelViewMatrix();
            GLUtils.drawPre();
            GLUtils.ldrawthree(0.0, 512.0, 1.0, 0.0f, 0.0f);
            GLUtils.ldrawthree(512.0, 512.0, 1.0, 1.0f, 0.0f);
            GLUtils.ldrawthree(512.0, 0.0, 1.0, 1.0f, 1.0f);
            GLUtils.ldrawthree(0.0, 0.0, 1.0, 0.0f, 1.0f);
            GLUtils.drawPost();
            matrixStack.popPose();
            RenderSystem.applyModelViewMatrix();
            GLShim.glDepthMask(true);
            GLShim.glEnable(2929);
            GLUtils.unbindFrameBuffer();
            GLShim.glViewport(0, 0, this.game.getWindow().getWidth(), this.game.getWindow().getHeight());
            matrixStack.popPose();
            RenderSystem.setProjectionMatrix(minimapProjectionMatrix);
            matrixStack.pushPose();
            GLShim.glBlendFunc(770, 0);
            GLUtils.disp2(GLUtils.fboTextureID);
        }
        final double guiScale = this.game.getWindow().getWidth() / (double)this.scWidth;
        GLShim.glEnable(3089);
        GLShim.glScissor((int)(guiScale * (x - 32)), (int)(guiScale * (this.scHeight - y - 32.0)), (int)(guiScale * 64.0), (int)(guiScale * 63.0));
        GLUtils.drawPre();
        GLUtils.setMapWithScale(x, y, scale);
        GLUtils.drawPost();
        GLShim.glDisable(3089);
        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();
        GLShim.glBlendFunc(770, 771);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.options.squareMap) {
            this.drawSquareMapFrame(x, y);
        }
        else {
            this.drawRoundMapFrame(x, y);
        }
        final double lastXDouble = GameVariableAccessShim.xCoordDouble();
        final double lastZDouble = GameVariableAccessShim.zCoordDouble();
        final TextureAtlas textureAtlas = this.master.getWaypointManager().getTextureAtlas();
        GLUtils.disp2(textureAtlas.getId());
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);
        GLShim.glDisable(2929);
        final Waypoint highlightedPoint = this.waypointManager.getHighlightedWaypoint();
        for (final Waypoint pt : this.waypointManager.getWaypoints()) {
            if (pt.isActive() || pt == highlightedPoint) {
                final double distanceSq = pt.getDistanceSqToEntity(this.game.getCameraEntity());
                if (distanceSq >= this.options.maxWaypointDisplayDistance * this.options.maxWaypointDisplayDistance && this.options.maxWaypointDisplayDistance >= 0 && pt != highlightedPoint) {
                    continue;
                }
                this.drawWaypoint(matrixStack, pt, x, y, scScale, lastXDouble, lastZDouble, null, null, null, null);
            }
        }
        if (highlightedPoint != null) {
            this.drawWaypoint(matrixStack, highlightedPoint, x, y, scScale, lastXDouble, lastZDouble, new ResourceLocation("voxelmap", "images/waypoints/target.png"), 1.0f, 0.0f, 0.0f);
        }
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    private void drawWaypoint(final PoseStack matrixStack, final Waypoint pt, final int x, final int y, final int scScale, final double lastXDouble, final double lastZDouble, ResourceLocation resourceLocation, Float r, Float g, Float b) {
    	final boolean uprightIcon = resourceLocation != null;
        if (r == null) {
            r = pt.red;
        }
        if (g == null) {
            g = pt.green;
        }
        if (b == null) {
            b = pt.blue;
        }
        final double wayX = lastXDouble - pt.getX() - 0.5;
        final double wayY = lastZDouble - pt.getZ() - 0.5;
        float locate = (float) Math.toDegrees(Math.atan2(wayX, wayY));
        double hypot = Math.sqrt(wayX * wayX + wayY * wayY);
        boolean far = false;
        if (this.options.rotates) {
            locate += this.direction;
        }
        else {
            locate -= this.northRotate;
        }
        hypot /= this.zoomScaleAdjusted;
        if (this.options.squareMap) {
            final double radLocate = Math.toRadians(locate);
            final double dispX = hypot * Math.cos(radLocate);
            final double dispY = hypot * Math.sin(radLocate);
            far = (Math.abs(dispX) > 28.5 || Math.abs(dispY) > 28.5);
            if (far) {
                hypot = hypot / Math.max(Math.abs(dispX), Math.abs(dispY)) * 30.0;
            }
        }
        else {
            far = (hypot >= 31.0);
            if (far) {
                hypot = 34.0;
            }
        }
        boolean target = false;
        
        final String fileExt = scScale >= 3 ? ".png" : "small.png";
        
        if(far && !uprightIcon) {
        	resourceLocation = new ResourceLocation("voxelmap", "images/waypoints/marker" + fileExt);
        } else if(!uprightIcon) {
        	resourceLocation = new ResourceLocation("voxelmap", "images/waypoints/waypoint" + pt.imageSuffix + ".png");
        }
        
        try {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            matrixStack.pushPose();
            RenderSystem.setShaderColor(r, g, b, (pt.enabled || target) ? 1.0f : 0.3f);
            RenderSystem.blendFunc(770, 771);
            RenderSystem.setShaderTexture(0, resourceLocation);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            if(far) {
            	matrixStack.translate((double)x, (double)y, 0.0);
            	matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-locate));
            	if (uprightIcon) {
            		matrixStack.translate(0.0, -hypot, 0.0);
            		matrixStack.mulPose(Vector3f.ZP.rotationDegrees(locate));
            		matrixStack.translate((double)(-x), (double)(-y), 0.0);
            	} else {
            	  	matrixStack.translate((double)(-x), (double)(-y), 0.0);
            	  	matrixStack.translate(0.0, -hypot, 0.0);
              	}
            } else {
            	matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-locate));
            	matrixStack.translate(0.0, -hypot, 0.0);
            	matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-(-locate)));
            }
            RenderSystem.applyModelViewMatrix();
            GLUtils.drawPre();
            GLUtils.setMap((float)x, (float)y, 16);
            GLUtils.drawPost();
        }
        catch (final Exception localException) {
            this.error = "Error: minimap arrow not found!";
        }
        finally {
            matrixStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        
//        if (far) {
//            try {
//            	RenderSystem.setShaderColor(r, g, b, (pt.enabled || target) ? 1.0f : 0.3f);
//                matrixStack.translate((double)x, (double)y, 0.0);
//                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-locate));
//                if (uprightIcon) {
//                    matrixStack.translate(0.0, -hypot, 0.0);
//                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(locate));
//                    matrixStack.translate((double)(-x), (double)(-y), 0.0);
//                }
//                else {
//                    matrixStack.translate((double)(-x), (double)(-y), 0.0);
//                    matrixStack.translate(0.0, -hypot, 0.0);
//                }
//                RenderSystem.applyModelViewMatrix();
//                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
//                this.drawTexturedModalRect(x, y, icon, 4.0f, 4.0f);
//            }
//            catch (final Exception localException) {
//                this.error = "Error: marker overlay not found!";
//            }
//            finally {
//                matrixStack.popPose();
//                RenderSystem.applyModelViewMatrix();
//            }
//        }
//        else {
//            try {
//            	RenderSystem.enableTexture();
//            	RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//            	// RenderSystem.setShaderColor(r, g, b, (pt.enabled || target) ? 1.0f : 0.3f);
//            	RenderSystem.setShaderTexture(0, resourceLocation);
//            	matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-locate));
//                matrixStack.translate(0.0, -hypot, 0.0);
//                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-(-locate)));
//                RenderSystem.applyModelViewMatrix();
//                this.drawTexturedModalRect(x, y, icon, 4.0f, 4.0f);
//            }
//            catch (final Exception localException) {
//                this.error = "Error: waypoint overlay not found!";
//            }
//            finally {
//            	matrixStack.popPose();
//                RenderSystem.applyModelViewMatrix();
//            }
//        }
    }
    
    private void drawArrow(final PoseStack matrixStack, final int x, final int y) {
        try {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            matrixStack.pushPose();
            GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GLShim.glBlendFunc(770, 771);
            GLUtils.img2(this.arrowResourceLocation);
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            matrixStack.translate((double)x, (double)y, 0.0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees((!this.options.rotates || this.fullscreenMap) ? (this.direction + this.northRotate) : 0.0f));
            matrixStack.translate((double)(-x), (double)(-y), 0.0);
            RenderSystem.applyModelViewMatrix();
            GLUtils.drawPre();
            GLUtils.setMap((float)x, (float)y, 16);
            GLUtils.drawPost();
        }
        catch (final Exception localException) {
            this.error = "Error: minimap arrow not found!";
        }
        finally {
            matrixStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }
    
    private void renderMapFull(final PoseStack matrixStack, final int scWidth, final int scHeight) {
        synchronized (this.coordinateLock) {
            if (this.imageChanged) {
                this.imageChanged = false;
                this.mapImages[this.zoom].write();
                this.lastImageX = this.lastX;
                this.lastImageZ = this.lastZ;
            }
        }
        RenderSystem.setShader((Supplier)GameRenderer::getPositionTexShader);
        GLUtils.disp2(this.mapImages[this.zoom].getIndex());
        GLShim.glTexParameteri(3553, 10241, 9987);
        GLShim.glTexParameteri(3553, 10240, 9729);
        matrixStack.pushPose();
        matrixStack.translate((double)(scWidth / 2.0f), (double)(scHeight / 2.0f), -0.0);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)this.northRotate));
        matrixStack.translate((double)(-(scWidth / 2.0f)), (double)(-(scHeight / 2.0f)), -0.0);
        RenderSystem.applyModelViewMatrix();
        GLShim.glDisable(2929);
        GLUtils.drawPre();
        final int left = scWidth / 2 - 128;
        final int top = scHeight / 2 - 128;
        GLUtils.ldrawone(left, top + 256, 160.0, 0.0f, 1.0f);
        GLUtils.ldrawone(left + 256, top + 256, 160.0, 1.0f, 1.0f);
        GLUtils.ldrawone(left + 256, top, 160.0, 1.0f, 0.0f);
        GLUtils.ldrawone(left, top, 160.0, 0.0f, 0.0f);
        GLUtils.drawPost();
        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();
        final int biomeOverlay = this.options.biomeOverlay;
        Objects.requireNonNull(this.options);
        if (biomeOverlay != 0) {
            final double factor = Math.pow(2.0, 3 - this.zoom);
            int minimumSize = (int)Math.pow(2.0, this.zoom);
            minimumSize *= minimumSize;
            final ArrayList<AbstractMapData.BiomeLabel> labels = this.mapData[this.zoom].getBiomeLabels();
            GLShim.glDisable(2929);
            matrixStack.pushPose();
            matrixStack.translate(0.0, 0.0, 1160.0);
            RenderSystem.applyModelViewMatrix();
            for (int t = 0; t < labels.size(); ++t) {
                final AbstractMapData.BiomeLabel label = labels.get(t);
                if (label.segmentSize > minimumSize) {
                    final String name = label.name;
                    final int nameWidth = this.chkLen(name);
                    final float x = (float)(label.x * factor);
                    final float z = (float)(label.z * factor);
                    if (this.options.oldNorth) {
                        this.write(matrixStack, name, left + 256 - z - nameWidth / 2, top + x - 3.0f, 16777215);
                    }
                    else {
                        this.write(matrixStack, name, left + x - nameWidth / 2, top + z - 3.0f, 16777215);
                    }
                }
            }
            matrixStack.popPose();
            RenderSystem.applyModelViewMatrix();
            GLShim.glEnable(2929);
        }
    }
    
    private void drawSquareMapFrame(final int x, final int y) {
        try {
            GLUtils.disp2(this.mapImageInt);
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glTexParameteri(3553, 10242, 10496);
            GLShim.glTexParameteri(3553, 10243, 10496);
            GLUtils.drawPre();
            GLUtils.setMap((float)x, (float)y, 128);
            GLUtils.drawPost();
        }
        catch (final Exception localException) {
            this.error = "error: minimap overlay not found!";
        }
    }
    
    private void loadMapImage() {
        if (this.mapImageInt != -1) {
            GLUtils.glah(this.mapImageInt);
        }
        try {
        	BufferedImage image = ImageUtils.createBufferedImageFromResourceLocation(new ResourceLocation("voxelmap", "images/squaremap.png"));
            this.mapImageInt = GLUtils.tex(image);
        }
        catch (final Exception e) {
            try {
                final Image tpMap = ImageUtils.createBufferedImageFromResourceLocation(new ResourceLocation("textures/map/map_background.png"));
                final BufferedImage mapImage2 = new BufferedImage(tpMap.getWidth(null), tpMap.getHeight(null), 2);
                final Graphics2D gfx = mapImage2.createGraphics();
                gfx.drawImage(tpMap, 0, 0, null);
                final int border = mapImage2.getWidth() * 8 / 128;
                gfx.setComposite(AlphaComposite.Clear);
                gfx.fillRect(border, border, mapImage2.getWidth() - border * 2, mapImage2.getHeight() - border * 2);
                gfx.dispose();
                this.mapImageInt = GLUtils.tex(mapImage2);
            }
            catch (final Exception f) {
                System.err.println("Error loading texture pack's map image: " + f.getLocalizedMessage());
            }
        }
    }
    
    private void drawRoundMapFrame(final int x, final int y) {
        try {
            GLUtils.img2(this.roundmapResourceLocation);
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLUtils.drawPre();
            GLUtils.setMap((float)x, (float)y, 128);
            GLUtils.drawPost();
        }
        catch (final Exception localException) {
            this.error = "Error: minimap overlay not found!";
        }
    }
    
    private void drawDirections(final PoseStack matrixStack, final int x, final int y) {
        final boolean unicode = this.game.options.forceUnicodeFont;
        final float scale = unicode ? 0.65f : 0.5f;
        float rotate;
        if (this.options.rotates) {
            rotate = -this.direction - 90.0f - this.northRotate;
        }
        else {
            rotate = -90.0f;
        }
        float distance;
        if (this.options.squareMap) {
            if (this.options.rotates) {
                float tempdir = this.direction % 90.0f;
                tempdir = 45.0f - Math.abs(45.0f - tempdir);
                distance = (float)(33.5 / scale / Math.cos(Math.toRadians(tempdir)));
            }
            else {
                distance = 33.5f / scale;
            }
        }
        else {
            distance = 32.0f / scale;
        }
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, 1.0f);
        matrixStack.translate(distance * Math.sin(Math.toRadians(-(rotate - 90.0))), distance * Math.cos(Math.toRadians(-(rotate - 90.0))), 100.0);
        this.write(matrixStack, "N", x / scale - 2.0f, y / scale - 4.0f, 16777215);
        matrixStack.popPose();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, 1.0f);
        matrixStack.translate(distance * Math.sin(Math.toRadians(-rotate)), distance * Math.cos(Math.toRadians(-rotate)), 10.0);
        this.write(matrixStack, "E", x / scale - 2.0f, y / scale - 4.0f, 16777215);
        matrixStack.popPose();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, 1.0f);
        matrixStack.translate(distance * Math.sin(Math.toRadians(-(rotate + 90.0))), distance * Math.cos(Math.toRadians(-(rotate + 90.0))), 10.0);
        this.write(matrixStack, "S", x / scale - 2.0f, y / scale - 4.0f, 16777215);
        matrixStack.popPose();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, 1.0f);
        matrixStack.translate(distance * Math.sin(Math.toRadians(-(rotate + 180.0))), distance * Math.cos(Math.toRadians(-(rotate + 180.0))), 10.0);
        this.write(matrixStack, "W", x / scale - 2.0f, y / scale - 4.0f, 16777215);
        matrixStack.popPose();
    }
    
    private void showCoords(final PoseStack matrixStack, final int x, final int y) {
        int textStart;
        if (y > this.scHeight - 37 - 32 - 4 - 15) {
            textStart = y - 32 - 4 - 9;
        }
        else {
            textStart = y + 32 + 4;
        }
        if (!this.options.hide && !this.fullscreenMap) {
            final boolean unicode = this.game.options.forceUnicodeFont;
            final float scale = unicode ? 0.65f : 0.5f;
            matrixStack.pushPose();
            matrixStack.scale(scale, scale, 1.0f);
            String xy = this.dCoord(GameVariableAccessShim.xCoord()) + ", " + this.dCoord(GameVariableAccessShim.zCoord());
            int m = this.chkLen(xy) / 2;
            this.write(matrixStack, xy, x / scale - m, textStart / scale, 16777215);
            xy = Integer.toString(GameVariableAccessShim.yCoord());
            m = this.chkLen(xy) / 2;
            this.write(matrixStack, xy, x / scale - m, textStart / scale + 10.0f, 16777215);
            if (this.ztimer > 0) {
                m = this.chkLen(this.error) / 2;
                this.write(matrixStack, this.error, x / scale - m, textStart / scale + 19.0f, 16777215);
            }
            matrixStack.popPose();
        }
        else {
            int heading = (int)(this.direction + this.northRotate);
            if (heading > 360) {
                heading -= 360;
            }
            final String stats = "(" + this.dCoord(GameVariableAccessShim.xCoord()) + ", " + GameVariableAccessShim.yCoord() + ", " + this.dCoord(GameVariableAccessShim.zCoord()) + ") " + heading;
            int i = this.chkLen(stats) / 2;
            this.write(matrixStack, stats, (float)(this.scWidth / 2 - i), 5.0f, 16777215);
            if (this.ztimer > 0) {
                i = this.chkLen(this.error) / 2;
                this.write(matrixStack, this.error, (float)(this.scWidth / 2 - i), 15.0f, 16777215);
            }
        }
    }
    
    private String dCoord(final int paramInt1) {
        if (paramInt1 < 0) {
            return "-" + Math.abs(paramInt1);
        }
        if (paramInt1 > 0) {
            return "+" + paramInt1;
        }
        return " " + paramInt1;
    }
    
    private int chkLen(final String string) {
        return this.fontRenderer.width(string);
    }
    
    private void write(final PoseStack matrixStack, final String text, final float x, final float y, final int color) {
        this.fontRenderer.drawShadow(matrixStack, text, x, y, color);
    }
    
    private int chkLen(final Component text) {
        return this.fontRenderer.width((FormattedText)text);
    }
    
    private void write(final PoseStack matrixStack, final Component text, final float x, final float y, final int color) {
        this.fontRenderer.drawShadow(matrixStack, text, x, y, color);
    }
    
    private void drawWelcomeScreen(final PoseStack matrixStack, final int scWidth, final int scHeight) {
        if (this.welcomeText[1] == null || this.welcomeText[1].getString().equals("minimap.ui.welcome2")) {
            this.welcomeText[0] = (Component)new TextComponent("").append((Component)new TextComponent("VoxelMap! ").withStyle(ChatFormatting.RED)).append(this.zmodver).append((Component)new TranslatableComponent("minimap.ui.welcome1"));
            this.welcomeText[1] = (Component)new TranslatableComponent("minimap.ui.welcome2");
            this.welcomeText[2] = (Component)new TranslatableComponent("minimap.ui.welcome3");
            this.welcomeText[3] = (Component)new TranslatableComponent("minimap.ui.welcome4");
            this.welcomeText[4] = (Component)new TextComponent("").append((Component)new KeybindComponent(this.options.keyBindZoom.getName()).withStyle(ChatFormatting.AQUA)).append(": ").append((Component)new TranslatableComponent("minimap.ui.welcome5a")).append(", ").append((Component)new KeybindComponent(this.options.keyBindMenu.getName()).withStyle(ChatFormatting.AQUA)).append(": ").append((Component)new TranslatableComponent("minimap.ui.welcome5b"));
            this.welcomeText[5] = (Component)new TextComponent("").append((Component)new KeybindComponent(this.options.keyBindFullscreen.getName()).withStyle(ChatFormatting.AQUA)).append(": ").append((Component)new TranslatableComponent("minimap.ui.welcome6"));
            this.welcomeText[6] = (Component)new TextComponent("").append((Component)new KeybindComponent(this.options.keyBindWaypoint.getName()).withStyle(ChatFormatting.AQUA)).append(": ").append((Component)new TranslatableComponent("minimap.ui.welcome7"));
            this.welcomeText[7] = (Component)this.options.keyBindZoom.getTranslatedKeyMessage().copy().append(": ").append((Component)new TranslatableComponent("minimap.ui.welcome8").withStyle(ChatFormatting.GRAY));
        }
        GLShim.glBlendFunc(770, 771);
        int maxSize = 0;
        final int border = 2;
        final Component head = this.welcomeText[0];
        int height;
        for (height = 1; height < this.welcomeText.length - 1; ++height) {
            if (this.chkLen(this.welcomeText[height]) > maxSize) {
                maxSize = this.chkLen(this.welcomeText[height]);
            }
        }
        final int title = this.chkLen(head);
        final int centerX = (int)((scWidth + 5) / 2.0);
        final int centerY = (int)((scHeight + 5) / 2.0);
        final Component hide = this.welcomeText[this.welcomeText.length - 1];
        final int footer = this.chkLen(hide);
        GLShim.glDisable(3553);
        GLShim.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        double leftX = centerX - title / 2.0 - border;
        double rightX = centerX + title / 2.0 + border;
        double topY = centerY - (height - 1) / 2.0 * 10.0 - border - 20.0;
        double botY = centerY - (height - 1) / 2.0 * 10.0 + border - 10.0;
        this.drawBox(leftX, rightX, topY, botY);
        leftX = centerX - maxSize / 2.0 - border;
        rightX = centerX + maxSize / 2.0 + border;
        topY = centerY - (height - 1) / 2.0 * 10.0 - border;
        botY = centerY + (height - 1) / 2.0 * 10.0 + border;
        this.drawBox(leftX, rightX, topY, botY);
        leftX = centerX - footer / 2.0 - border;
        rightX = centerX + footer / 2.0 + border;
        topY = centerY + (height - 1) / 2.0 * 10.0 - border + 10.0;
        botY = centerY + (height - 1) / 2.0 * 10.0 + border + 20.0;
        this.drawBox(leftX, rightX, topY, botY);
        GLShim.glEnable(3553);
        this.write(matrixStack, head, (float)(centerX - title / 2), (float)(centerY - (height - 1) * 10 / 2 - 19), 16777215);
        for (int n = 1; n < height; ++n) {
            this.write(matrixStack, this.welcomeText[n], (float)(centerX - maxSize / 2), (float)(centerY - (height - 1) * 10 / 2 + n * 10 - 9), 16777215);
        }
        this.write(matrixStack, hide, (float)(centerX - footer / 2), (float)((scHeight + 5) / 2 + (height - 1) * 10 / 2 + 11), 16777215);
    }
    
    private void drawBox(final double leftX, final double rightX, final double topY, final double botY) {
        GLUtils.drawPre(DefaultVertexFormat.POSITION);
        GLUtils.ldrawtwo(leftX, botY, 0.0);
        GLUtils.ldrawtwo(rightX, botY, 0.0);
        GLUtils.ldrawtwo(rightX, topY, 0.0);
        GLUtils.ldrawtwo(leftX, topY, 0.0);
        GLUtils.drawPost();
    }
    
    public void drawTexturedModalRect(final float xCoord, final float yCoord, final Sprite icon, final float widthIn, final float heightIn) {
    	final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        vertexBuffer.vertex((double)(xCoord - widthIn), (double)(yCoord + heightIn), 1.0d).uv(icon.getMinU(), icon.getMaxV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + widthIn), (double)(yCoord + heightIn), 1.0d).uv(icon.getMaxU(), icon.getMaxV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + widthIn), (double)(yCoord - heightIn), 1.0d).uv(icon.getMaxU(), icon.getMinV()).endVertex();
        vertexBuffer.vertex((double)(xCoord - widthIn), (double)(yCoord - heightIn), 1.0d).uv(icon.getMinU(), icon.getMinV()).endVertex();
        tessellator.end();
    }
}
