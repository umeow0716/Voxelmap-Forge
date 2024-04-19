// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.gui.GuiWaypoints;
import com.mamiyaotaru.voxelmap.gui.GuiSubworldsSelect;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import java.util.TreeSet;
import com.mamiyaotaru.voxelmap.gui.overridden.Popup;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import java.util.ArrayList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.util.GLShim;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import com.mamiyaotaru.voxelmap.gui.GuiMinimapOptions;
import com.mamiyaotaru.voxelmap.util.I18nUtils;

import java.awt.image.BufferedImage;
import com.mamiyaotaru.voxelmap.util.ImageUtils;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;

import com.mamiyaotaru.voxelmap.util.Waypoint;
import com.mamiyaotaru.voxelmap.util.BiomeMapData;
import com.mamiyaotaru.voxelmap.util.BackgroundImageInfo;
import com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiButton;
import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import java.util.Random;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;

import com.mamiyaotaru.voxelmap.gui.IGuiWaypoints;
import com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiScreen;

public class GuiPersistentMap extends PopupGuiScreen implements IGuiWaypoints
{
    private Minecraft mc;
    private Random generator;
    private IVoxelMap master;
    private IPersistentMap persistentMap;
    private IWaypointManager waypointManager;
    private final Screen parent;
    private final MapSettingsManager mapOptions;
    private final PersistentMapSettingsManager options;
    protected String screenTitle;
    protected String worldNameDisplay;
    protected int worldNameDisplayLength;
    protected int maxWorldNameDisplayLength;
    private String subworldName;
    private PopupGuiButton buttonMultiworld;
    private int top;
    private int bottom;
    private boolean oldNorth;
    private boolean lastStill;
    private boolean editingCoordinates;
    private boolean lastEditingCoordinates;
    private EditBox coordinates;
    int centerX;
    int centerY;
    float mapCenterX;
    float mapCenterZ;
    float deltaX;
    float deltaY;
    float deltaXonRelease;
    float deltaYonRelease;
    long timeOfRelease;
    boolean mouseCursorShown;
    long timeAtLastTick;
    long timeOfLastKBInput;
    long timeOfLastMouseInput;
    final float TIME_CONSTANT = 350.0f;
    float lastMouseX;
    float lastMouseY;
    protected int mouseX;
    protected int mouseY;
    boolean leftMouseButtonDown;
    float zoom;
    float zoomStart;
    float zoomGoal;
    long timeOfZoom;
    float zoomDirectX;
    float zoomDirectY;
    private float scScale;
    private float guiToMap;
    private float mapToGui;
    private float mouseDirectToMap;
    private float guiToDirectMouse;
    private boolean closed;
    private CachedRegion[] regions;
    BackgroundImageInfo backGroundImageInfo;
    private BiomeMapData biomeMapData;
    private float mapPixelsX;
    private float mapPixelsY;
    private final Object closedLock;
    private KeyMapping keyBindForward;
    private KeyMapping keyBindLeft;
    private KeyMapping keyBindBack;
    private KeyMapping keyBindRight;
    private KeyMapping keyBindSprint;
    private Key forwardCode;
    private Key leftCode;
    private Key backCode;
    private Key rightCode;
    private Key sprintCode;
    Key nullInput;
    private TranslatableComponent multiworldButtonName;
    private MutableComponent multiworldButtonNameRed;
    int sideMargin;
    int buttonCount;
    int buttonSeparation;
    int buttonWidth;
    public boolean editClicked;
    public boolean deleteClicked;
    public boolean addClicked;
    Waypoint newWaypoint;
    Waypoint selectedWaypoint;
    
    public GuiPersistentMap(final Screen parent, final IVoxelMap master) {
        this.generator = new Random();
        this.screenTitle = "World Map";
        this.worldNameDisplay = "";
        this.worldNameDisplayLength = 0;
        this.maxWorldNameDisplayLength = 0;
        this.subworldName = "";
        this.oldNorth = false;
        this.lastStill = false;
        this.editingCoordinates = false;
        this.lastEditingCoordinates = false;
        this.centerX = 0;
        this.centerY = 0;
        this.mapCenterX = 0.0f;
        this.mapCenterZ = 0.0f;
        this.deltaX = 0.0f;
        this.deltaY = 0.0f;
        this.deltaXonRelease = 0.0f;
        this.deltaYonRelease = 0.0f;
        this.timeOfRelease = 0L;
        this.mouseCursorShown = true;
        this.timeAtLastTick = 0L;
        this.timeOfLastKBInput = 0L;
        this.timeOfLastMouseInput = 0L;
        this.lastMouseX = 0.0f;
        this.lastMouseY = 0.0f;
        this.leftMouseButtonDown = false;
        this.zoom = 4.0f;
        this.zoomStart = 4.0f;
        this.zoomGoal = 4.0f;
        this.timeOfZoom = 0L;
        this.zoomDirectX = 0.0f;
        this.zoomDirectY = 0.0f;
        this.scScale = 1.0f;
        this.guiToMap = 2.0f;
        this.mapToGui = 0.5f;
        this.mouseDirectToMap = 1.0f;
        this.guiToDirectMouse = 2.0f;
        this.closed = false;
        this.regions = new CachedRegion[0];
        this.backGroundImageInfo = null;
        this.biomeMapData = new BiomeMapData(760, 360);
        this.mapPixelsX = 0.0f;
        this.mapPixelsY = 0.0f;
        this.closedLock = new Object();
        this.keyBindForward = new KeyMapping("key.forward.fake", 17, "key.categories.movement");
        this.keyBindLeft = new KeyMapping("key.left.fake", 30, "key.categories.movement");
        this.keyBindBack = new KeyMapping("key.back.fake", 31, "key.categories.movement");
        this.keyBindRight = new KeyMapping("key.right.fake", 32, "key.categories.movement");
        this.keyBindSprint = new KeyMapping("key.sprint.fake", 29, "key.categories.movement");
        this.nullInput = InputConstants.getKey("key.keyboard.unknown");
        this.sideMargin = 10;
        this.buttonCount = 5;
        this.buttonSeparation = 4;
        this.buttonWidth = 66;
        this.editClicked = false;
        this.deleteClicked = false;
        this.addClicked = false;
        this.mc = Minecraft.getInstance();
        this.parent = parent;
        this.master = master;
        this.waypointManager = master.getWaypointManager();
        this.mapOptions = master.getMapOptions();
        this.persistentMap = master.getPersistentMap();
        this.options = master.getPersistentMapOptions();
        this.zoom = this.options.zoom;
        this.zoomStart = this.options.zoom;
        this.zoomGoal = this.options.zoom;
        this.persistentMap.setLightMapArray(master.getMap().getLightmapArray());
        this.forwardCode = InputConstants.getKey(this.mc.options.keyUp.saveString());
        this.leftCode = InputConstants.getKey(this.mc.options.keyLeft.saveString());
        this.backCode = InputConstants.getKey(this.mc.options.keyDown.saveString());
        this.rightCode = InputConstants.getKey(this.mc.options.keyRight.saveString());
        this.sprintCode = InputConstants.getKey(this.mc.options.keySprint.saveString());
    }
    
    public void init() {
        this.passEvents = true;
        this.oldNorth = this.mapOptions.oldNorth;
        this.centerAt(this.options.mapX, this.options.mapZ);
        this.mc.keyboardHandler.setSendRepeatsToGui(true);
        if (this.mc.screen == this) {
            this.closed = false;
        }
        this.screenTitle = I18nUtils.getString("worldmap.title", new Object[0]);
        this.buildWorldName();
        this.leftMouseButtonDown = false;
        this.sideMargin = 10;
        this.buttonCount = 5;
        this.buttonSeparation = 4;
        this.buttonWidth = (this.width - this.sideMargin * 2 - this.buttonSeparation * (this.buttonCount - 1)) / this.buttonCount;
        this.addRenderableWidget(new PopupGuiButton(this.sideMargin + 0 * (this.buttonWidth + this.buttonSeparation), this.getHeight() - 28, this.buttonWidth, 20, new TranslatableComponent("options.minimap.waypoints"), buttonWidget_1 -> this.mc.setScreen(new GuiWaypoints(this, this.master)), this));
        this.multiworldButtonName = new TranslatableComponent(this.getMinecraft().isConnectedToRealms() ? "menu.online" : "options.worldmap.multiworld");
        this.multiworldButtonNameRed = new TranslatableComponent(this.getMinecraft().isConnectedToRealms() ? "menu.online" : "options.worldmap.multiworld").withStyle(ChatFormatting.RED);
        if (!this.getMinecraft().isLocalServer() && !this.master.getWaypointManager().receivedAutoSubworldName()) {
            this.addRenderableWidget((this.buttonMultiworld = new PopupGuiButton(this.sideMargin + 1 * (this.buttonWidth + this.buttonSeparation), this.getHeight() - 28, this.buttonWidth, 20, this.multiworldButtonName, buttonWidget_1 -> this.mc.setScreen(new GuiSubworldsSelect(this, this.master)), this)));
        }
        this.addRenderableWidget(new PopupGuiButton(this.sideMargin + 3 * (this.buttonWidth + this.buttonSeparation), this.getHeight() - 28, this.buttonWidth, 20, new TranslatableComponent("menu.options"), null, this) {
            public void onPress() {
                GuiPersistentMap.this.getMinecraft().setScreen(new GuiMinimapOptions(GuiPersistentMap.this, GuiPersistentMap.this.master));
            }
        });
        this.addRenderableWidget(new PopupGuiButton(this.sideMargin + 4 * (this.buttonWidth + this.buttonSeparation), this.getHeight() - 28, this.buttonWidth, 20, new TranslatableComponent("gui.done"), null, this) {
            public void onPress() {
                GuiPersistentMap.this.getMinecraft().setScreen(GuiPersistentMap.this.parent);
            }
        });
        this.coordinates = new EditBox(this.getFontRenderer(), this.sideMargin, 10, 140, 20, null);
        this.top = 32;
        this.bottom = this.getHeight() - 32;
        this.centerX = this.getWidth() / 2;
        this.centerY = (this.bottom - this.top) / 2;
        this.scScale = (float)this.mc.getWindow().getGuiScale();
        this.mapPixelsX = (float)this.mc.getWindow().getWidth();
        this.mapPixelsY = (float)(this.mc.getWindow().getHeight() - (int)(64.0f * this.scScale));
        this.lastStill = false;
        this.timeAtLastTick = System.currentTimeMillis();
        this.keyBindForward.setKey(this.forwardCode);
        this.keyBindLeft.setKey(this.leftCode);
        this.keyBindBack.setKey(this.backCode);
        this.keyBindRight.setKey(this.rightCode);
        this.keyBindSprint.setKey(this.sprintCode);
        this.mc.options.keyUp.setKey(this.nullInput);
        this.mc.options.keyLeft.setKey(this.nullInput);
        this.mc.options.keyDown.setKey(this.nullInput);
        this.mc.options.keyRight.setKey(this.nullInput);
        this.mc.options.keySprint.setKey(this.nullInput);
        KeyMapping.resetMapping();
    }
    
    private int getSkin() {
        final ResourceLocation skinLocation = this.mc.player.getSkinTextureLocation();
        try {
        	BufferedImage skinImage = ImageUtils.createBufferedImageFromResourceLocation(skinLocation);
            final boolean showHat = this.mc.player.isModelPartShown(PlayerModelPart.HAT);
            if (showHat) {
                skinImage = ImageUtils.addImages(ImageUtils.loadImage(skinImage, 8, 8, 8, 8), ImageUtils.loadImage(skinImage, 40, 8, 8, 8), 0.0f, 0.0f, 8, 8);
            }
            else {
                skinImage = ImageUtils.loadImage(skinImage, 8, 8, 8, 8);
            }
            final float scale = skinImage.getWidth() / 8.0f;
            skinImage = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(skinImage, 2.0f / scale)), true, 1);
            return GLUtils.tex(skinImage);
        }
        catch (final Exception ex) {
        	ex.printStackTrace();
        }
        
        return -1;
    }
    
    private void centerAt(final int x, final int z) {
        if (this.oldNorth) {
            this.mapCenterX = (float)(-z);
            this.mapCenterZ = (float)x;
        }
        else {
            this.mapCenterX = (float)x;
            this.mapCenterZ = (float)z;
        }
    }
    
    private void buildWorldName() {
        String worldName = "";
        if (this.mc.isLocalServer()) {
            worldName = this.mc.getSingleplayerServer().getWorldData().getLevelName();
            if (worldName == null || worldName.equals("")) {
                worldName = "Singleplayer World";
            }
        }
        else {
            final ServerData serverData = this.mc.getCurrentServer();
            if (serverData != null) {
                worldName = serverData.name;
            }
            if (worldName == null || worldName.equals("")) {
                worldName = "Multiplayer Server";
            }
            if (this.minecraft.isConnectedToRealms()) {
                worldName = "Realms";
            }
        }
        StringBuilder worldNameBuilder = new StringBuilder("§r").append(worldName);
        String subworldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(true);
        this.subworldName = subworldName;
        if ((subworldName == null || subworldName.equals("")) && this.master.getWaypointManager().isMultiworld()) {
            subworldName = "???";
        }
        if (subworldName != null && !subworldName.equals("")) {
            worldNameBuilder.append(" - ").append(subworldName);
        }
        this.worldNameDisplay = worldNameBuilder.toString();
        this.worldNameDisplayLength = this.getFontRenderer().width(this.worldNameDisplay);
        this.maxWorldNameDisplayLength = this.getWidth() / 2 - this.getFontRenderer().width(this.screenTitle) / 2 - this.sideMargin * 2;
        while (this.worldNameDisplayLength > this.maxWorldNameDisplayLength && worldName.length() > 5) {
            worldName = worldName.substring(0, worldName.length() - 1);
            worldNameBuilder = new StringBuilder(worldName);
            worldNameBuilder.append("...");
            if (subworldName != null && !subworldName.equals("")) {
                worldNameBuilder.append(" - ").append(subworldName);
            }
            this.worldNameDisplay = worldNameBuilder.toString();
            this.worldNameDisplayLength = this.getFontRenderer().width(this.worldNameDisplay);
        }
        if (subworldName != null && !subworldName.equals("")) {
            while (this.worldNameDisplayLength > this.maxWorldNameDisplayLength && subworldName.length() > 5) {
                worldNameBuilder = new StringBuilder(worldName);
                worldNameBuilder.append("...");
                subworldName = subworldName.substring(0, subworldName.length() - 1);
                worldNameBuilder.append(" - ").append(subworldName);
                this.worldNameDisplay = worldNameBuilder.toString();
                this.worldNameDisplayLength = this.getFontRenderer().width(this.worldNameDisplay);
            }
        }
    }
    
    private float bindZoom(float zoom) {
        zoom = Math.max(this.options.minZoom, zoom);
        zoom = Math.min(this.options.maxZoom, zoom);
        return zoom;
    }
    
    private float easeOut(final float elapsedTime, final float startValue, final float finalDelta, final float totalTime) {
        float value;
        if (elapsedTime == totalTime) {
            value = startValue + finalDelta;
        }
        else {
            value = finalDelta * (-(float)Math.pow(2.0, -10.0f * elapsedTime / totalTime) + 1.0f) + startValue;
        }
        return value;
    }
    
    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double mouseRoll) {
        this.timeOfLastMouseInput = System.currentTimeMillis();
        this.switchToMouseInput();
        final float mouseDirectX = (float)this.mc.mouseHandler.xpos();
        final float mouseDirectY = (float)this.mc.mouseHandler.ypos();
        if (mouseRoll != 0.0) {
            if (mouseRoll > 0.0) {
                this.zoomGoal *= 1.26f;
            }
            else if (mouseRoll < 0.0) {
                this.zoomGoal /= 1.26f;
            }
            this.zoomStart = this.zoom;
            this.zoomGoal = this.bindZoom(this.zoomGoal);
            this.timeOfZoom = System.currentTimeMillis();
            this.zoomDirectX = mouseDirectX;
            this.zoomDirectY = mouseDirectY;
        }
        return true;
    }
    
    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseButton) {
        if (mouseY > this.top && mouseY < this.bottom && mouseButton == 1) {
            this.timeOfLastKBInput = 0L;
            final int mouseDirectX = (int)this.mc.mouseHandler.xpos();
            final int mouseDirectY = (int)this.mc.mouseHandler.ypos();
            this.createPopup((int)mouseX, (int)mouseY, mouseDirectX, mouseDirectY);
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        if (!this.popupOpen()) {
            this.coordinates.mouseClicked(mouseX, mouseY, mouseButton);
            this.editingCoordinates = this.coordinates.isFocused();
            if (this.editingCoordinates && !this.lastEditingCoordinates) {
                int x = 0;
                int z = 0;
                if (this.oldNorth) {
                    x = (int)Math.floor(this.mapCenterZ);
                    z = -(int)Math.floor(this.mapCenterX);
                }
                else {
                    x = (int)Math.floor(this.mapCenterX);
                    z = (int)Math.floor(this.mapCenterZ);
                }
                this.coordinates.setValue(x + ", " + z);
                this.coordinates.setTextColor(16777215);
            }
            this.lastEditingCoordinates = this.editingCoordinates;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton) || mouseButton == 1;
    }
    
    public boolean keyPressed(int keysm, int scancode, int b) {
        if (!this.editingCoordinates && (this.mc.options.keyJump.matches(keysm, scancode) || this.mc.options.keyShift.matches(keysm, scancode))) {
            if (this.mc.options.keyJump.matches(keysm, scancode)) {
                this.zoomGoal /= 1.26f;
            }
            if (this.mc.options.keyShift.matches(keysm, scancode)) {
                this.zoomGoal *= 1.26f;
            }
            this.zoomStart = this.zoom;
            this.zoomGoal = this.bindZoom(this.zoomGoal);
            this.timeOfZoom = System.currentTimeMillis();
            this.zoomDirectX = (float)(this.mc.getWindow().getWidth() / 2);
            this.zoomDirectY = (float)(this.mc.getWindow().getHeight() - this.mc.getWindow().getHeight() / 2);
            this.switchToKeyboardInput();
        }
//        this.clearPopups();
        if (this.editingCoordinates) {
            this.coordinates.keyPressed(keysm, scancode, b);
            final boolean isGood = this.isAcceptable();
            this.coordinates.setTextColor(isGood ? 16777215 : 16711680);
            if ((keysm == 257 || keysm == 335) && this.coordinates.isFocused() && isGood) {
                final String[] xz = this.coordinates.getValue().split(",");
                this.centerAt(Integer.valueOf(xz[0].trim()), Integer.valueOf(xz[1].trim()));
                this.editingCoordinates = false;
                this.lastEditingCoordinates = false;
                this.switchToKeyboardInput();
            }
            if (keysm == 258 && this.coordinates.isFocused()) {
                this.editingCoordinates = false;
                this.lastEditingCoordinates = false;
                this.switchToKeyboardInput();
            }
        }
        if (this.master.getMapOptions().keyBindMenu.matches(keysm, scancode)) {
            keysm = 256;
            scancode = -1;
            b = -1;
        }
        return super.keyPressed(keysm, scancode, b);
    }
    
    public boolean charTyped(final char typedChar, final int keyCode) {
        this.clearPopups();
        if (this.editingCoordinates) {
            this.coordinates.charTyped(typedChar, keyCode);
            final boolean isGood = this.isAcceptable();
            this.coordinates.setTextColor(isGood ? 16777215 : 16711680);
            if (typedChar == '\r' && this.coordinates.isFocused() && isGood) {
                final String[] xz = this.coordinates.getValue().split(",");
                this.centerAt(Integer.valueOf(xz[0].trim()), Integer.valueOf(xz[1].trim()));
                this.editingCoordinates = false;
                this.lastEditingCoordinates = false;
                this.switchToKeyboardInput();
            }
        }
        if (this.master.getMapOptions().keyBindMenu.matches(keyCode, -1)) {
            super.keyPressed(256, -1, -1);
        }
        return super.charTyped(typedChar, keyCode);
    }
    
    private boolean isAcceptable() {
        try {
            final String[] xz = this.coordinates.getValue().split(",");
            Integer.valueOf(xz[0].trim());
            Integer.valueOf(xz[1].trim());
            return true;
        }
        catch (final NumberFormatException e) {
            return false;
        }
        catch (final ArrayIndexOutOfBoundsException e2) {
            return false;
        }
    }
    
    private void switchToMouseInput() {
        this.timeOfLastKBInput = 0L;
        if (!this.mouseCursorShown) {
            GLFW.glfwSetInputMode(this.mc.getWindow().getWindow(), 208897, 212993);
        }
        this.mouseCursorShown = true;
    }
    
    private void switchToKeyboardInput() {
        this.timeOfLastKBInput = System.currentTimeMillis();
        this.mouseCursorShown = false;
        GLFW.glfwSetInputMode(this.mc.getWindow().getWindow(), 208897, 212995);
    }
    
    @Override
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
    	this.zoomGoal = this.bindZoom(this.zoomGoal);
        if (this.mouseX != mouseX || this.mouseY != mouseY) {
            this.timeOfLastMouseInput = System.currentTimeMillis();
            this.switchToMouseInput();
        }
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        final float mouseDirectX = (float)this.mc.mouseHandler.xpos();
        final float mouseDirectY = (float)this.mc.mouseHandler.ypos();
        if (this.zoom != this.zoomGoal) {
            final float previousZoom = this.zoom;
            final long timeSinceZoom = System.currentTimeMillis() - this.timeOfZoom;
            if (timeSinceZoom < 700.0f) {
                this.zoom = this.easeOut((float)timeSinceZoom, this.zoomStart, this.zoomGoal - this.zoomStart, 700.0f);
            }
            else {
                this.zoom = this.zoomGoal;
            }
            float scaledZoom = this.zoom;
            if (this.mc.getWindow().getWidth() > 1600) {
                scaledZoom = this.zoom * this.mc.getWindow().getWidth() / 1600.0f;
            }
            final float zoomDelta = this.zoom / previousZoom;
            final float zoomOffsetX = this.centerX * this.guiToDirectMouse - this.zoomDirectX;
            final float zoomOffsetY = (this.top + this.centerY) * this.guiToDirectMouse - this.zoomDirectY;
            final float zoomDeltaX = zoomOffsetX - zoomOffsetX * zoomDelta;
            final float zoomDeltaY = zoomOffsetY - zoomOffsetY * zoomDelta;
            this.mapCenterX += zoomDeltaX / scaledZoom;
            this.mapCenterZ += zoomDeltaY / scaledZoom;
        }
        this.options.zoom = this.zoomGoal;
        float scaledZoom2 = this.zoom;
        if (this.mc.getWindow().getScreenWidth() > 1600) {
            scaledZoom2 = this.zoom * this.mc.getWindow().getScreenWidth() / 1600.0f;
        }
        this.guiToMap = this.scScale / scaledZoom2;
        this.mapToGui = 1.0f / this.scScale * scaledZoom2;
        this.mouseDirectToMap = 1.0f / scaledZoom2;
        this.guiToDirectMouse = this.scScale;
        this.renderBackground(matrixStack);
        if (this.mc.mouseHandler.isLeftPressed()) {
            if (!this.leftMouseButtonDown && !this.overPopup(mouseX, mouseY)) {
                this.deltaX = 0.0f;
                this.deltaY = 0.0f;
                this.lastMouseX = mouseDirectX;
                this.lastMouseY = mouseDirectY;
                this.leftMouseButtonDown = true;
            }
            else if (this.leftMouseButtonDown) {
                this.deltaX = (this.lastMouseX - mouseDirectX) * this.mouseDirectToMap;
                this.deltaY = (this.lastMouseY - mouseDirectY) * this.mouseDirectToMap;
                this.lastMouseX = mouseDirectX;
                this.lastMouseY = mouseDirectY;
                this.deltaXonRelease = this.deltaX;
                this.deltaYonRelease = this.deltaY;
                this.timeOfRelease = System.currentTimeMillis();
            }
        }
        else {
            final long timeSinceRelease = System.currentTimeMillis() - this.timeOfRelease;
            if (timeSinceRelease < 700.0f) {
                this.deltaX = this.deltaXonRelease * (float)Math.exp(-timeSinceRelease / 350.0f);
                this.deltaY = this.deltaYonRelease * (float)Math.exp(-timeSinceRelease / 350.0f);
            }
            else {
                this.deltaX = 0.0f;
                this.deltaY = 0.0f;
                this.deltaXonRelease = 0.0f;
                this.deltaYonRelease = 0.0f;
            }
            this.leftMouseButtonDown = false;
        }
        final long timeSinceLastTick = System.currentTimeMillis() - this.timeAtLastTick;
        this.timeAtLastTick = System.currentTimeMillis();
        if (!this.editingCoordinates) {
            int kbDelta = 5;
            if (this.keyBindSprint.isDown()) {
                kbDelta = 10;
            }
            if (this.keyBindForward.isDown()) {
                this.deltaY -= kbDelta / scaledZoom2 * timeSinceLastTick / 12.0f;
                this.switchToKeyboardInput();
            }
            if (this.keyBindBack.isDown()) {
                this.deltaY += kbDelta / scaledZoom2 * timeSinceLastTick / 12.0f;
                this.switchToKeyboardInput();
            }
            if (this.keyBindLeft.isDown()) {
                this.deltaX -= kbDelta / scaledZoom2 * timeSinceLastTick / 12.0f;
                this.switchToKeyboardInput();
            }
            if (this.keyBindRight.isDown()) {
                this.deltaX += kbDelta / scaledZoom2 * timeSinceLastTick / 12.0f;
                this.switchToKeyboardInput();
            }
        }
        this.mapCenterX += this.deltaX;
        this.mapCenterZ += this.deltaY;
        if (this.oldNorth) {
            this.options.mapX = (int)this.mapCenterZ;
            this.options.mapZ = -(int)this.mapCenterX;
        }
        else {
            this.options.mapX = (int)this.mapCenterX;
            this.options.mapZ = (int)this.mapCenterZ;
        }
        this.centerX = this.getWidth() / 2;
        this.centerY = (this.bottom - this.top) / 2;
        int left = 0;
        int right = 0;
        int top = 0;
        int bottom = 0;
        if (this.oldNorth) {
            left = (int)Math.floor((this.mapCenterZ - this.centerY * this.guiToMap) / 256.0f);
            right = (int)Math.floor((this.mapCenterZ + this.centerY * this.guiToMap) / 256.0f);
            top = (int)Math.floor((-this.mapCenterX - this.centerX * this.guiToMap) / 256.0f);
            bottom = (int)Math.floor((-this.mapCenterX + this.centerX * this.guiToMap) / 256.0f);
        }
        else {
            left = (int)Math.floor((this.mapCenterX - this.centerX * this.guiToMap) / 256.0f);
            right = (int)Math.floor((this.mapCenterX + this.centerX * this.guiToMap) / 256.0f);
            top = (int)Math.floor((this.mapCenterZ - this.centerY * this.guiToMap) / 256.0f);
            bottom = (int)Math.floor((this.mapCenterZ + this.centerY * this.guiToMap) / 256.0f);
        }
        synchronized (this.closedLock) {
            if (this.closed) {
                return;
            }
            this.regions = this.persistentMap.getRegions(left - 1, right + 1, top - 1, bottom + 1);
        }
        final PoseStack modelViewMatrixStack = RenderSystem.getModelViewStack();
        modelViewMatrixStack.pushPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        modelViewMatrixStack.translate((double)(this.centerX - this.mapCenterX * this.mapToGui), (double)(this.top + this.centerY - this.mapCenterZ * this.mapToGui), 0.0);
        if (this.oldNorth) {
        	modelViewMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
        }
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        this.backGroundImageInfo = this.waypointManager.getBackgroundImageInfo();
        if (this.backGroundImageInfo != null) {
            GLUtils.disp2(this.backGroundImageInfo.glid);
            this.drawTexturedModalRect(this.backGroundImageInfo.left * this.mapToGui, this.backGroundImageInfo.top * this.mapToGui, this.backGroundImageInfo.width * this.mapToGui, this.backGroundImageInfo.height * this.mapToGui);
        }
        for (int t = 0; t < this.regions.length; ++t) {
            final CachedRegion region = this.regions[t];
            final int glid = region.getGLID();
            if (glid != 0) {
                GLUtils.disp2(glid);
                if (this.mapOptions.filtering) {
                    GLShim.glTexParameteri(3553, 10241, 9987);
                    GLShim.glTexParameteri(3553, 10240, 9729);
                }
                else {
                    GLShim.glTexParameteri(3553, 10241, 9987);
                    GLShim.glTexParameteri(3553, 10240, 9728);
                }
                this.drawTexturedModalRect(region.getX() * 256 * this.mapToGui, region.getZ() * 256 * this.mapToGui, region.getWidth() * this.mapToGui, region.getWidth() * this.mapToGui);
            }
        }
        float cursorX;
        float cursorY;
        if (this.mouseCursorShown) {
            cursorX = mouseDirectX;
            cursorY = mouseDirectY - this.top * this.guiToDirectMouse;
        }
        else {
            cursorX = (float)(this.mc.getWindow().getWidth() / 2);
            cursorY = this.mc.getWindow().getHeight() - this.mc.getWindow().getHeight() / 2 - this.top * this.guiToDirectMouse;
        }
        float cursorCoordX;
        float cursorCoordZ;
        if (this.oldNorth) {
            cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
            cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
        }
        else {
            cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
            cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if (this.options.showWaypoints) {
            for (final Waypoint pt : this.waypointManager.getWaypoints()) {
                this.drawWaypoint(matrixStack, pt, cursorCoordX, cursorCoordZ, null, null, null, null);
            }
            if (this.waypointManager.getHighlightedWaypoint() != null) {
                this.drawWaypoint(matrixStack, this.waypointManager.getHighlightedWaypoint(), cursorCoordX, cursorCoordZ, this.master.getWaypointManager().getTextureAtlas().getAtlasSprite("voxelmap:images/waypoints/target.png"), 1.0f, 0.0f, 0.0f);
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, getSkin());
        RenderSystem.texParameter(3553, 10241, 9729);
        RenderSystem.texParameter(3553, 10240, 9729);
        final float playerX = (float)GameVariableAccessShim.xCoordDouble();
        final float playerZ = (float)GameVariableAccessShim.zCoordDouble();
        if (this.oldNorth) {
        	modelViewMatrixStack.pushPose();
            modelViewMatrixStack.translate((double)(playerX * this.mapToGui), (double)(playerZ * this.mapToGui), 0.0);
            modelViewMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0f));
            modelViewMatrixStack.translate((double)(-(playerX * this.mapToGui)), (double)(-(playerZ * this.mapToGui)), 0.0);
            RenderSystem.applyModelViewMatrix();
        }
        this.drawTexturedModalRect(-10.0f / this.scScale + playerX * this.mapToGui, -10.0f / this.scScale + playerZ * this.mapToGui, 20.0f / this.scScale, 20.0f / this.scScale);
        if (this.oldNorth) {
        	modelViewMatrixStack.popPose();
        }
        if (this.oldNorth) {
        	modelViewMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0f));
        }
        modelViewMatrixStack.translate((double)(-(this.centerX - this.mapCenterX * this.mapToGui)), (double)(-(this.top + this.centerY - this.mapCenterZ * this.mapToGui)), 0.0);
        RenderSystem.applyModelViewMatrix();
        modelViewMatrixStack.popPose();
        final int biomeOverlay = this.mapOptions.biomeOverlay;

        if (biomeOverlay != 0) {
            final float biomeScaleX = this.mapPixelsX / 760.0f;
            final float biomeScaleY = this.mapPixelsY / 360.0f;
            boolean still = !this.leftMouseButtonDown;
            still = (still && this.zoom == this.zoomGoal);
            still = (still && this.deltaX == 0.0f && this.deltaY == 0.0f);
            still = (still && ThreadManager.executorService.getActiveCount() == 0);
            if (still && !this.lastStill) {
                int column = 0;
                if (this.oldNorth) {
                    column = (int)Math.floor(Math.floor(this.mapCenterZ - this.centerY * this.guiToMap) / 256.0) - (left - 1);
                }
                else {
                    column = (int)Math.floor(Math.floor(this.mapCenterX - this.centerX * this.guiToMap) / 256.0) - (left - 1);
                }
                for (int x = 0; x < this.biomeMapData.getWidth(); ++x) {
                    for (int z = 0; z < this.biomeMapData.getHeight(); ++z) {
                        float floatMapX;
                        float floatMapZ;
                        if (this.oldNorth) {
                            floatMapX = z * biomeScaleY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
                            floatMapZ = -(x * biomeScaleX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
                        }
                        else {
                            floatMapX = x * biomeScaleX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
                            floatMapZ = z * biomeScaleY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
                        }
                        final int mapX = (int)Math.floor(floatMapX);
                        final int mapZ = (int)Math.floor(floatMapZ);
                        final int regionX = (int)Math.floor(mapX / 256.0f) - (left - 1);
                        final int regionZ = (int)Math.floor(mapZ / 256.0f) - (top - 1);
                        if ((!this.oldNorth && regionX != column) || (this.oldNorth && regionZ != column)) {
                            this.persistentMap.compress();
                        }
                        column = (this.oldNorth ? regionZ : regionX);
                        final CachedRegion region2 = this.regions[regionZ * (right + 1 - (left - 1) + 1) + regionX];
                        int id = -1;
                        if (region2.getMapData() != null && region2.isLoaded() && !region2.isEmpty()) {
                            final int inRegionX = mapX - region2.getX() * region2.getWidth();
                            final int inRegionZ = mapZ - region2.getZ() * region2.getWidth();
                            final int height = region2.getMapData().getHeight(inRegionX, inRegionZ);
                            final int light = region2.getMapData().getLight(inRegionX, inRegionZ);
                            if (height != 0 || light != 0) {
                                id = region2.getMapData().getBiomeID(inRegionX, inRegionZ);
                            }
                        }
                        this.biomeMapData.setBiomeID(x, z, id);
                    }
                }
                this.persistentMap.compress();
                this.biomeMapData.segmentBiomes();
                this.biomeMapData.findCenterOfSegments(true);
            }
            this.lastStill = still;
            boolean displayStill = !this.leftMouseButtonDown;
            displayStill = (displayStill && this.zoom == this.zoomGoal);
            displayStill = (displayStill && this.deltaX == 0.0f && this.deltaY == 0.0f);
            if (displayStill) {
                int minimumSize = (int)(20.0f * this.scScale / biomeScaleX);
                minimumSize *= minimumSize;
                final ArrayList<AbstractMapData.BiomeLabel> labels = this.biomeMapData.getBiomeLabels();
                GLShim.glDisable(2929);
                for (int t2 = 0; t2 < labels.size(); ++t2) {
                    final AbstractMapData.BiomeLabel label = labels.get(t2);
                    if (label.segmentSize > minimumSize) {
                        final int nameWidth = this.chkLen(label.name);
                        final float x2 = label.x * biomeScaleX / this.scScale;
                        final float z2 = label.z * biomeScaleY / this.scScale;
                        this.write(matrixStack, label.name, x2 - nameWidth / 2, this.top + z2 - 3.0f, 16777215);
                    }
                }
                GLShim.glEnable(2929);
            }
        }
        if (System.currentTimeMillis() - this.timeOfLastKBInput < 2000L) {
            final int scWidth = this.mc.getWindow().getGuiScaledWidth();
            final int scHeight = this.mc.getWindow().getGuiScaledHeight();
            this.mc.getTextureManager().bindForSetup(GuiPersistentMap.GUI_ICONS_LOCATION);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GuiPersistentMap.GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(775, 769, 1, 0);
            this.blit(matrixStack, scWidth / 2 - 7, scHeight / 2 - 7, 0, 0, 16, 16);
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        }
        else {
            this.switchToMouseInput();
        }
        this.overlayBackground(0, this.top, 255, 255);
        this.overlayBackground(this.bottom, this.getHeight(), 255, 255);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 16, 16777215);
        final int x3 = (int)Math.floor(cursorCoordX);
        final int z3 = (int)Math.floor(cursorCoordZ);
        if (this.master.getMapOptions().coords) {
            if (!this.editingCoordinates) {
                drawString(matrixStack, this.getFontRenderer(), "X: " + x3, this.sideMargin, 16, 16777215);
                drawString(matrixStack, this.getFontRenderer(), "Z: " + z3, this.sideMargin + 64, 16, 16777215);
            }
            else {
                this.coordinates.render(matrixStack, mouseX, mouseY, partialTicks);
            }
        }
        if ((this.subworldName != null && !this.subworldName.equals(this.master.getWaypointManager().getCurrentSubworldDescriptor(true))) || (this.master.getWaypointManager().getCurrentSubworldDescriptor(true) != null && !this.master.getWaypointManager().getCurrentSubworldDescriptor(true).equals(this.subworldName))) {
            this.buildWorldName();
        }
        drawString(matrixStack, this.getFontRenderer(), this.worldNameDisplay, this.getWidth() - this.sideMargin - this.worldNameDisplayLength, 16, 16777215);
        if (this.buttonMultiworld != null) {
            if ((this.subworldName == null || this.subworldName.equals("")) && this.master.getWaypointManager().isMultiworld()) {
                if ((int)(System.currentTimeMillis() / 1000L % 2L) == 0) {
                    this.buttonMultiworld.setMessage(this.multiworldButtonNameRed);
                }
                else {
                    this.buttonMultiworld.setMessage(this.multiworldButtonName);
                }
            }
            else {
                this.buttonMultiworld.setMessage(this.multiworldButtonName);
            }
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    private void drawWaypoint(final PoseStack matrixStack, final Waypoint pt, final float cursorCoordX, final float cursorCoordZ, Sprite icon, Float r, Float g, Float b) {
    	if (pt.inWorld && pt.inDimension && this.isOnScreen(pt.getX(), pt.getZ())) {
            String name = pt.name;
            if (r == null) {
                r = pt.red;
            }
            if (g == null) {
                g = pt.green;
            }
            if (b == null) {
                b = pt.blue;
            }
            float ptX = (float)pt.getX();
            float ptZ = (float)pt.getZ();
            if ((this.backGroundImageInfo != null && this.backGroundImageInfo.isInRange((int)ptX, (int)ptZ)) || this.persistentMap.isRegionLoaded((int)ptX, (int)ptZ)) {
                ptX += 0.5f;
                ptZ += 0.5f;
                final boolean hover = cursorCoordX > ptX - 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordX < ptX + 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordZ > ptZ - 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordZ < ptZ + 18.0f * this.guiToMap / this.guiToDirectMouse;
                boolean target = false;
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                final TextureAtlas atlas = this.master.getWaypointManager().getTextureAtlas();
                
                ResourceLocation resourceLocation = null;
                if (icon == null) {
                	resourceLocation = new ResourceLocation("voxelmap", "images/waypoints/waypoint" + pt.imageSuffix + ".png");
                    icon = atlas.getAtlasSprite("voxelmap:images/waypoints/waypoint" + pt.imageSuffix + ".png");
                    if (icon == atlas.getMissingImage()) {
                        icon = atlas.getAtlasSprite("voxelmap:images/waypoints/waypoint.png");
                    }
                }
                else {
                	resourceLocation = new ResourceLocation("voxelmap", "images/waypoints/target.png");
                    name = "";
                    target = true;
                }
                RenderSystem.setShaderColor(r, g, b, (pt.enabled || target || hover) ? 1.0f : 0.3f);
                RenderSystem.texParameter(3553, 10241, 9729);
                RenderSystem.texParameter(3553, 10240, 9729);
                RenderSystem.setShaderTexture(0, resourceLocation);
                
                if (this.oldNorth) {
                    matrixStack.pushPose();
                    matrixStack.translate((double)(ptX * this.mapToGui), (double)(ptZ * this.mapToGui), 0.0);
                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0f));
                    matrixStack.translate((double)(-(ptX * this.mapToGui)), (double)(-(ptZ * this.mapToGui)), 0.0);
                    RenderSystem.applyModelViewMatrix();
                }
                
                // blit(matrixStack, (int) (-16.0f / this.scScale + ptX * this.mapToGui - 5), (int) (-16.0f / this.scScale + ptZ * this.mapToGui - 8), 0.0f, 0.0f, 26, 26, 26, 26);
                RenderSystem.setShaderTexture(0, atlas.getId());
                this.drawTexturedModalRect(-16.0f / this.scScale + ptX * this.mapToGui, -16.0f / this.scScale + ptZ * this.mapToGui, icon, 32.0f / this.scScale, 32.0f / this.scScale);
                if (this.oldNorth) {
                    matrixStack.popPose();
                    RenderSystem.applyModelViewMatrix();
                }
                final int biomeOverlay = this.mapOptions.biomeOverlay;
                if ((biomeOverlay == 0 && this.options.showWaypointNames) || target || hover) {
                    final float fontScale = 2.0f / this.scScale;
                    final int m = this.chkLen(name) / 2;
                    matrixStack.pushPose();
                    matrixStack.scale(fontScale, fontScale, 1.0f);
                    if (this.oldNorth) {
                        matrixStack.translate((double)(ptX * this.mapToGui / fontScale), (double)(ptZ * this.mapToGui / fontScale), 0.0);
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0f));
                        matrixStack.translate((double)(-(ptX * this.mapToGui / fontScale)), (double)(-(ptZ * this.mapToGui / fontScale)), 0.0);
                        RenderSystem.applyModelViewMatrix();
                    }
                    this.write(matrixStack, name, ptX * this.mapToGui / fontScale - m, ptZ * this.mapToGui / fontScale + 16.0f / this.scScale / fontScale, (pt.enabled || target || hover) ? 16777215 : 1442840575);
                    matrixStack.popPose();
                    RenderSystem.applyModelViewMatrix();
                    GLShim.glEnable(3042);
                }
            }
        }
    }
    
    private boolean isOnScreen(final int x, final int z) {
        int left;
        int right;
        int top;
        int bottom;
        if (this.oldNorth) {
            left = (int)Math.floor(this.mapCenterZ - this.centerY * this.guiToMap * 1.1);
            right = (int)Math.floor(this.mapCenterZ + this.centerY * this.guiToMap * 1.1);
            top = (int)Math.floor(-this.mapCenterX - this.centerX * this.guiToMap * 1.1);
            bottom = (int)Math.floor(-this.mapCenterX + this.centerX * this.guiToMap * 1.1);
        }
        else {
            left = (int)Math.floor(this.mapCenterX - this.centerX * this.guiToMap * 1.1);
            right = (int)Math.floor(this.mapCenterX + this.centerX * this.guiToMap * 1.1);
            top = (int)Math.floor(this.mapCenterZ - this.centerY * this.guiToMap * 1.1);
            bottom = (int)Math.floor(this.mapCenterZ + this.centerY * this.guiToMap * 1.1);
        }
        return x > left && x < right && z > top && z < bottom;
    }
    
    public void renderBackground(final PoseStack matrixStack) {
        fill(matrixStack, 0, 0, this.getWidth(), this.getHeight(), -16777216);
    }
    
    protected void overlayBackground(final int startY, final int endY, final int startAlpha, final int endAlpha) {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        final AbstractTexture background = this.mc.getTextureManager().getTexture(Screen.BACKGROUND_LOCATION);
        background.bind();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        vertexBuffer.vertex(0.0, (double)endY, 0.0).uv(0.0f, endY / 32.0f).color(64, 64, 64, endAlpha).endVertex();
        vertexBuffer.vertex((double)(0 + this.getWidth()), (double)endY, 0.0).uv(this.width / 32.0f, endY / 32.0f).color(64, 64, 64, endAlpha).endVertex();
        vertexBuffer.vertex((double)(0 + this.getWidth()), (double)startY, 0.0).uv(this.width / 32.0f, startY / 32.0f).color(64, 64, 64, startAlpha).endVertex();
        vertexBuffer.vertex(0.0, (double)startY, 0.0).uv(0.0f, startY / 32.0f).color(64, 64, 64, startAlpha).endVertex();
        tessellator.end();
    }
    
    public void tick() {
        this.coordinates.tick();
    }
    
    @Override
    public void removed() {
        this.mc.options.keyUp.setKey(this.forwardCode);
        this.mc.options.keyLeft.setKey(this.leftCode);
        this.mc.options.keyDown.setKey(this.backCode);
        this.mc.options.keyRight.setKey(this.rightCode);
        this.mc.options.keySprint.setKey(this.sprintCode);
        this.keyBindForward.setKey(this.nullInput);
        this.keyBindLeft.setKey(this.nullInput);
        this.keyBindBack.setKey(this.nullInput);
        this.keyBindRight.setKey(this.nullInput);
        this.keyBindSprint.setKey(this.nullInput);
        KeyMapping.resetMapping();
        KeyMapping.releaseAll();
        this.mc.keyboardHandler.setSendRepeatsToGui(false);
        synchronized (this.closedLock) {
            this.closed = true;
            this.persistentMap.getRegions(0, -1, 0, -1);
            this.regions = new CachedRegion[0];
        }
    }
    
    public void drawTexturedModalRect(final float x, final float y, final float width, final float height) {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        vertexBuffer.vertex((double)(x + 0.0f), (double)(y + height), (double)this.getBlitOffset()).uv(0.0f, 1.0f).endVertex();
        vertexBuffer.vertex((double)(x + width), (double)(y + height), (double)this.getBlitOffset()).uv(1.0f, 1.0f).endVertex();
        vertexBuffer.vertex((double)(x + width), (double)(y + 0.0f), (double)this.getBlitOffset()).uv(1.0f, 0.0f).endVertex();
        vertexBuffer.vertex((double)(x + 0.0f), (double)(y + 0.0f), (double)this.getBlitOffset()).uv(0.0f, 0.0f).endVertex();
        tessellator.end();
    }
    
    public void drawTexturedModalRect(final Sprite icon, final float x, final float y) {
        final float width = icon.getIconWidth() / this.scScale;
        final float height = icon.getIconHeight() / this.scScale;
        this.drawTexturedModalRect(x, y, icon, width, height);
    }
    
    public void drawTexturedModalRect(final float xCoord, final float yCoord, final Sprite icon, final float widthIn, final float heightIn) {
    	final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        vertexBuffer.vertex((double)(xCoord + 0.0f), (double)(yCoord + heightIn), (double)this.getBlitOffset()).uv(icon.getMinU(), icon.getMaxV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + widthIn), (double)(yCoord + heightIn), (double)this.getBlitOffset()).uv(icon.getMaxU(), icon.getMaxV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + widthIn), (double)(yCoord + 0.0f), (double)this.getBlitOffset()).uv(icon.getMaxU(), icon.getMinV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + 0.0f), (double)(yCoord + 0.0f), (double)this.getBlitOffset()).uv(icon.getMinU(), icon.getMinV()).endVertex();
        tessellator.end();
    }
    
    private void createPopup(final int mouseX, final int mouseY, final int mouseDirectX, final int mouseDirectY) {
        final ArrayList<Popup.PopupEntry> entries = new ArrayList<Popup.PopupEntry>();
        final float cursorX = (float)mouseDirectX;
        final float cursorY = mouseDirectY - this.top * this.guiToDirectMouse;
        float cursorCoordX;
        float cursorCoordZ;
        if (this.oldNorth) {
            cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
            cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
        }
        else {
            cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
            cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
        }
        
        final int x = (int)Math.floor(cursorCoordX);
        final int z = (int)Math.floor(cursorCoordZ);
        boolean canTeleport = this.canTeleport();
        canTeleport = (canTeleport && (this.persistentMap.isGroundAt(x, z) || (this.backGroundImageInfo != null && this.backGroundImageInfo.isGroundAt(x, z))));
        
        final Waypoint hovered = this.getHovered(cursorCoordX, cursorCoordZ);
        if (hovered == null || !this.waypointManager.getWaypoints().contains(hovered)) {
            final String newWaypointString = I18nUtils.getString("minimap.waypoints.newwaypoint");
            Popup.PopupEntry newWaypoint = new Popup.PopupEntry(newWaypointString, 0, true, true);
            
            final String highLightString = I18nUtils.getString((hovered == null) ? "minimap.waypoints.highlight" : "minimap.waypoints.removehighlight");
            Popup.PopupEntry highLight = new Popup.PopupEntry(highLightString, 1, true, true);
            
            final String teleportToString = I18nUtils.getString("minimap.waypoints.teleportto");
            Popup.PopupEntry teleporTo = new Popup.PopupEntry(teleportToString, 3, true, canTeleport);
            
            final String shareString = I18nUtils.getString("minimap.waypoints.share");
            Popup.PopupEntry share = new Popup.PopupEntry(shareString, 2, true, true);
            
            entries.add(newWaypoint);
            entries.add(highLight);
            entries.add(teleporTo);
            entries.add(share);
        } else {
        	final String editString = I18nUtils.getString("minimap.waypoints.edit");
        	Popup.PopupEntry edit = new Popup.PopupEntry(editString, 4, true, true);
        	
        	final String deleteString = I18nUtils.getString("minimap.waypoints.delete");
        	Popup.PopupEntry delete = new Popup.PopupEntry(deleteString, 5, true, true);
        	
        	final String highLightString = I18nUtils.getString((hovered != this.waypointManager.getHighlightedWaypoint()) ? "minimap.waypoints.highlight" : "minimap.waypoints.removehighlight");
        	Popup.PopupEntry highLight = new Popup.PopupEntry(highLightString, 1, true, true);
        	
        	final String teleportToString = I18nUtils.getString("minimap.waypoints.teleportto");
        	Popup.PopupEntry teleportTo = new Popup.PopupEntry(teleportToString, 3, true, canTeleport);
        	
        	final String shareString = I18nUtils.getString("minimap.waypoints.share");
        	Popup.PopupEntry share = new Popup.PopupEntry(shareString, 2, true, true);
        	
        	entries.add(edit);
        	entries.add(delete);
        	entries.add(highLight);
        	entries.add(teleportTo);
        	entries.add(share);
        }
        
        this.createPopup(mouseX, mouseY, mouseDirectX, mouseDirectY, entries);
    }
    
    private Waypoint getHovered(final float cursorCoordX, final float cursorCoordZ) {
        Waypoint waypoint = null;
        for (final Waypoint pt : this.waypointManager.getWaypoints()) {
            final float ptX = pt.getX() + 0.5f;
            final float ptZ = pt.getZ() + 0.5f;
            final boolean hover = pt.inDimension && pt.inWorld && cursorCoordX > ptX - 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordX < ptX + 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordZ > ptZ - 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordZ < ptZ + 18.0f * this.guiToMap / this.guiToDirectMouse;
            if (hover) {
                waypoint = pt;
            }
        }
        if (waypoint == null) {
            final Waypoint pt2 = this.waypointManager.getHighlightedWaypoint();
            if (pt2 != null) {
                final float ptX2 = pt2.getX() + 0.5f;
                final float ptZ2 = pt2.getZ() + 0.5f;
                final boolean hover2 = pt2.inDimension && pt2.inWorld && cursorCoordX > ptX2 - 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordX < ptX2 + 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordZ > ptZ2 - 18.0f * this.guiToMap / this.guiToDirectMouse && cursorCoordZ < ptZ2 + 18.0f * this.guiToMap / this.guiToDirectMouse;
                if (hover2) {
                    waypoint = pt2;
                }
            }
        }
        return waypoint;
    }
    
    @Override
    public void popupAction(final Popup popup, final int action) {
        final int mouseDirectX = popup.clickedDirectX;
        final int mouseDirectY = popup.clickedDirectY;
        final float cursorX = (float)mouseDirectX;
        final float cursorY = mouseDirectY - this.top * this.guiToDirectMouse;
        float cursorCoordX;
        float cursorCoordZ;
        if (this.oldNorth) {
            cursorCoordX = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
            cursorCoordZ = -(cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap));
        }
        else {
            cursorCoordX = cursorX * this.mouseDirectToMap + (this.mapCenterX - this.centerX * this.guiToMap);
            cursorCoordZ = cursorY * this.mouseDirectToMap + (this.mapCenterZ - this.centerY * this.guiToMap);
        }
        int x = (int)Math.floor(cursorCoordX);
        int z = (int)Math.floor(cursorCoordZ);
        int y = this.persistentMap.getHeightAt(x, z);
        final Waypoint hovered = this.getHovered(cursorCoordX, cursorCoordZ);
        this.editClicked = false;
        this.addClicked = false;
        this.deleteClicked = false;
        final double dimensionScale = this.mc.player.level.dimensionType().coordinateScale();
        switch (action) {
            case 0: {
                if (hovered != null) {
                    x = hovered.getX();
                    z = hovered.getZ();
                }
                this.addClicked = true;
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
                dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(this.mc.level));
                this.newWaypoint = new Waypoint("", (int)(x * dimensionScale), (int)(z * dimensionScale), y, true, r, g, b, "", this.master.getWaypointManager().getCurrentSubworldDescriptor(false), dimensions);
                this.mc.setScreen((Screen)new GuiAddWaypoint(this, this.master, this.newWaypoint, false));
                break;
            }
            case 4: {
                if (hovered != null) {
                    this.editClicked = true;
                    this.selectedWaypoint = hovered;
                    this.mc.setScreen((Screen)new GuiAddWaypoint(this, this.master, hovered, true));
                    break;
                }
                break;
            }
            case 5: {
                if (hovered != null) {
                    this.deleteClicked = true;
                    this.selectedWaypoint = hovered;
                    final TranslatableComponent title = new TranslatableComponent("minimap.waypoints.deleteconfirm");
                    final TranslatableComponent explanation = new TranslatableComponent("selectServer.deleteWarning", new Object[] { this.selectedWaypoint.name });
                    final TranslatableComponent affirm = new TranslatableComponent("selectServer.deleteButton");
                    final TranslatableComponent deny = new TranslatableComponent("gui.cancel");
                    final ConfirmScreen var8 = new ConfirmScreen((BooleanConsumer)this, title, explanation, affirm, deny);
                    this.getMinecraft().setScreen((Screen)var8);
                    break;
                }
                break;
            }
            case 1: {
                if (hovered != null) {
                    this.waypointManager.setHighlightedWaypoint(hovered, true);
                    break;
                }
                y = ((y > 0) ? y : 64);
                final TreeSet<DimensionContainer> dimensions2 = new TreeSet<DimensionContainer>();
                dimensions2.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(this.mc.level));
                final Waypoint fakePoint = new Waypoint("", (int)(x * dimensionScale), (int)(z * dimensionScale), y, true, 1.0f, 0.0f, 0.0f, "", this.master.getWaypointManager().getCurrentSubworldDescriptor(false), dimensions2);
                this.waypointManager.setHighlightedWaypoint(fakePoint, true);
                break;
            }
            case 2: {
                if (hovered != null) {
                    CommandUtils.sendWaypoint(hovered, this);
                    break;
                }
                y = ((y > 0) ? y : 64);
                CommandUtils.sendCoordinate(x, y, z);
                break;
            }
            case 3: {
                if (hovered != null) {
                    this.selectedWaypoint = hovered;
                    final boolean mp = !this.mc.isLocalServer();
                    y = ((this.selectedWaypoint.getY() > 0) ? this.selectedWaypoint.getY() : (this.mc.player.level.dimensionType().hasCeiling() ? 64 : 256));
                    this.mc.player.chat("/tp " + this.mc.player.getName().getString() + " " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
                    if (mp) {
                        this.mc.player.chat("/tppos " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
                    }
                    else {
                        this.getMinecraft().setScreen((Screen)null);
                    }
                    break;
                }
                if (y == 0) {
                    y = (this.mc.player.level.dimensionType().hasCeiling() ? 255 : 64);
                }
                this.mc.player.chat("/tp " + this.mc.player.getName().getString() + " " + x + " " + y + " " + z);
                if (!this.mc.isLocalServer()) {
                    this.mc.player.chat("/tppos " + x + " " + y + " " + z);
                    break;
                }
                break;
            }
            default: {
                break;
            }
        }
    }
    
    @Override
    public boolean isEditing() {
        return this.editClicked;
    }
    
    public void accept(final boolean confirm) {
        if (this.deleteClicked) {
            this.deleteClicked = false;
            if (confirm) {
                this.waypointManager.deleteWaypoint(this.selectedWaypoint);
                this.selectedWaypoint = null;
            }
        }
        if (this.editClicked) {
            this.editClicked = false;
            if (confirm) {
                this.waypointManager.saveWaypoints();
            }
        }
        if (this.addClicked) {
            this.addClicked = false;
            if (confirm) {
                this.waypointManager.addWaypoint(this.newWaypoint);
            }
        }
        this.getMinecraft().setScreen((Screen)this);
    }
    
    public boolean canTeleport() {
        boolean allowed = false;
        final boolean singlePlayer = this.mc.isLocalServer();
        if (singlePlayer) {
            try {
                allowed = this.mc.getSingleplayerServer().getPlayerList().isOp(this.mc.player.getGameProfile());
            }
            catch (final Exception e) {
                allowed = this.mc.getSingleplayerServer().getWorldData().getAllowCommands();
            }
        }
        else {
            allowed = true;
        }
        return allowed;
    }
    
    private int chkLen(final String string) {
        return this.getFontRenderer().width(string);
    }
    
    private void write(final PoseStack matrixStack, final String string, final float x, final float y, final int color) {
        this.getFontRenderer().drawShadow(matrixStack, string, x, y, color);
    }
}
