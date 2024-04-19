// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.util.MessageUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.FileInputStream;
import com.mamiyaotaru.voxelmap.interfaces.ISubSettingsManager;
import java.util.ArrayList;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import java.io.File;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsManager;

public class MapSettingsManager implements ISettingsManager
{
    public final int SORT_DATE = 1;
    public final int SORT_NAME = 2;
    public final int SORT_DISTANCE = 3;
    public final int SORT_COLOR = 4;
    public final int TOP_LEFT = 0;
    public final int TOP_RIGHT = 1;
    public final int BOTTOM_RIGHT = 2;
    public final int BOTTOM_LEFT = 3;
    public final int SMALL = -1;
    public final int MEDIUM = 0;
    public final int LARGE = 1;
    public final int XL = 2;
    public final int XXL = 3;
    public final int XXXL = 4;
    public final int OFF = 0;
    public final int SOLID = 1;
    public final int TRANSPARENT = 2;
    public final int MOST_RECENT = 1;
    public final int ALL = 2;
    private File settingsFile;
    public boolean showUnderMenus;
    private int availableProcessors;
    public boolean multicore;
    public boolean hide;
    public boolean coords;
    protected boolean showCaves;
    public boolean lightmap;
    public boolean heightmap;
    public boolean slopemap;
    public boolean filtering;
    public boolean waterTransparency;
    public boolean blockTransparency;
    public boolean biomes;
    public int biomeOverlay;
    public boolean chunkGrid;
    public boolean slimeChunks;
    public boolean squareMap;
    public boolean rotates;
    public boolean oldNorth;
    public boolean showBeacons;
    public boolean showWaypoints;
    private boolean preToggleBeacons;
    private boolean preToggleSigns;
    public int deathpoints;
    public int maxWaypointDisplayDistance;
    protected boolean welcome;
    public int zoom;
    protected int regularZoom;
    public int sizeModifier;
    public int mapCorner;
    public Boolean cavesAllowed;
    public int sort;
    protected boolean realTimeTorches;
    public KeyMapping keyBindZoom;
    public KeyMapping keyBindFullscreen;
    public KeyMapping keyBindMenu;
    public KeyMapping keyBindWaypointMenu;
    public KeyMapping keyBindWaypoint;
    public KeyMapping keyBindMobToggle;
    public KeyMapping keyBindWaypointToggle;
    public KeyMapping[] keyBindings;
    public Minecraft game;
    private boolean somethingChanged;
    public static MapSettingsManager instance;
    private ArrayList<ISubSettingsManager> subSettingsManagers;
    
    public MapSettingsManager() {
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
        this.multicore = (this.availableProcessors > 1);
        this.hide = false;
        this.coords = true;
        this.showCaves = true;
        this.lightmap = true;
        this.heightmap = this.multicore;
        this.slopemap = true;
        this.filtering = false;
        this.waterTransparency = this.multicore;
        this.blockTransparency = this.multicore;
        this.biomes = this.multicore;
        this.biomeOverlay = 0;
        this.chunkGrid = false;
        this.slimeChunks = false;
        this.squareMap = false;
        this.rotates = true;
        this.oldNorth = false;
        this.showBeacons = false;
        this.showWaypoints = true;
        this.preToggleBeacons = false;
        this.preToggleSigns = true;
        this.deathpoints = 1;
        this.maxWaypointDisplayDistance = 1000;
        this.welcome = true;
        this.zoom = 2;
        this.regularZoom = 2;
        this.sizeModifier = 0;
        this.mapCorner = 1;
        this.cavesAllowed = true;
        this.sort = 1;
        this.realTimeTorches = false;
        this.keyBindZoom = new KeyMapping("key.minimap.zoom", InputConstants.getKey("key.keyboard.z").getValue(), "controls.minimap.title");
        this.keyBindFullscreen = new KeyMapping("key.minimap.togglefullscreen", InputConstants.getKey("key.keyboard.x").getValue(), "controls.minimap.title");
        this.keyBindMenu = new KeyMapping("key.minimap.voxelmapmenu", InputConstants.getKey("key.keyboard.m").getValue(), "controls.minimap.title");
        this.keyBindWaypointMenu = new KeyMapping("key.minimap.waypointmenu", -1, "controls.minimap.title");
        this.keyBindWaypoint = new KeyMapping("key.minimap.waypointhotkey", InputConstants.getKey("key.keyboard.n").getValue(), "controls.minimap.title");
        this.keyBindMobToggle = new KeyMapping("key.minimap.togglemobs", -1, "controls.minimap.title");
        this.keyBindWaypointToggle = new KeyMapping("key.minimap.toggleingamewaypoints", -1, "controls.minimap.title");
        this.game = null;
        this.subSettingsManagers = new ArrayList<ISubSettingsManager>();
        MapSettingsManager.instance = this;
        this.game = Minecraft.getInstance();
        this.keyBindings = new KeyMapping[] { this.keyBindMenu, this.keyBindWaypointMenu, this.keyBindZoom, this.keyBindFullscreen, this.keyBindWaypoint, this.keyBindMobToggle, this.keyBindWaypointToggle };
    }
    
    public void addSecondaryOptionsManager(final ISubSettingsManager secondarySettingsManager) {
        this.subSettingsManagers.add(secondarySettingsManager);
    }
    
    public void loadAll() {
        this.settingsFile = new File(this.game.gameDirectory, "config/voxelmap.properties");
        try {
            if (this.settingsFile.exists()) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.settingsFile), Charset.forName("UTF-8").newDecoder()));
                String sCurrentLine;
                while ((sCurrentLine = in.readLine()) != null) {
                    final String[] curLine = sCurrentLine.split(":");
                    if (curLine[0].equals("Zoom Level")) {
                        this.zoom = Math.max(0, Math.min(4, Integer.parseInt(curLine[1])));
                    }
                    else if (curLine[0].equals("Hide Minimap")) {
                        this.hide = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Show Coordinates")) {
                        this.coords = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Enable Cave Mode")) {
                        this.showCaves = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Dynamic Lighting")) {
                        this.lightmap = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Height Map")) {
                        this.heightmap = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Slope Map")) {
                        this.slopemap = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Blur")) {
                        this.filtering = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Water Transparency")) {
                        this.waterTransparency = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Block Transparency")) {
                        this.blockTransparency = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Biomes")) {
                        this.biomes = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Biome Overlay")) {
                        this.biomeOverlay = Math.max(0, Math.min(2, Integer.parseInt(curLine[1])));
                    }
                    else if (curLine[0].equals("Chunk Grid")) {
                        this.chunkGrid = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Slime Chunks")) {
                        this.slimeChunks = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Square Map")) {
                        this.squareMap = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Rotation")) {
                        this.rotates = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Old North")) {
                        this.oldNorth = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Waypoint Beacons")) {
                        this.showBeacons = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Waypoint Signs")) {
                        this.showWaypoints = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Deathpoints")) {
                        this.deathpoints = Math.max(0, Math.min(2, Integer.parseInt(curLine[1])));
                    }
                    else if (curLine[0].equals("Waypoint Max Distance")) {
                        this.maxWaypointDisplayDistance = Math.max(-1, Math.min(10000, Integer.parseInt(curLine[1])));
                    }
                    else if (curLine[0].equals("Waypoint Sort By")) {
                        this.sort = Math.max(1, Math.min(4, Integer.parseInt(curLine[1])));
                    }
                    else if (curLine[0].equals("Welcome Message")) {
                        this.welcome = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Real Time Torch Flicker")) {
                        this.realTimeTorches = Boolean.parseBoolean(curLine[1]);
                    }
                    else if (curLine[0].equals("Map Corner")) {
                        this.mapCorner = Math.max(0, Math.min(3, Integer.parseInt(curLine[1])));
                    }
                    else if (curLine[0].equals("Map Size")) {
                        this.sizeModifier = Math.max(-1, Math.min(4, Integer.parseInt(curLine[1])));
                    }
                    else if (curLine[0].equals("Zoom Key")) {
                        this.bindKey(this.keyBindZoom, curLine[1]);
                    }
                    else if (curLine[0].equals("Fullscreen Key")) {
                        this.bindKey(this.keyBindFullscreen, curLine[1]);
                    }
                    else if (curLine[0].equals("Menu Key")) {
                        this.bindKey(this.keyBindMenu, curLine[1]);
                    }
                    else if (curLine[0].equals("Waypoint Menu Key")) {
                        this.bindKey(this.keyBindWaypointMenu, curLine[1]);
                    }
                    else if (curLine[0].equals("Waypoint Key")) {
                        this.bindKey(this.keyBindWaypoint, curLine[1]);
                    }
                    else if (curLine[0].equals("Mob Key")) {
                        this.bindKey(this.keyBindMobToggle, curLine[1]);
                    }
                    else if (curLine[0].equals("In-game Waypoint Key")) {
                        this.bindKey(this.keyBindWaypointToggle, curLine[1]);
                    }
                    KeyMapping.resetMapping();
                }
                for (final ISubSettingsManager subSettingsManager : this.subSettingsManagers) {
                    subSettingsManager.loadSettings(this.settingsFile);
                }
                in.close();
            }
            this.saveAll();
        }
        catch (final Exception ex) {}
    }
    
    private void bindKey(final KeyMapping keyBinding, final String id) {
        try {
            keyBinding.setKey(InputConstants.getKey(id));
        }
        catch (final Exception e) {
            System.err.println(id + " is not a valid keybinding");
        }
    }
    
    public void saveAll() {
        final File settingsFileDir = new File(this.game.gameDirectory, "/config/");
        if (!settingsFileDir.exists()) {
            settingsFileDir.mkdirs();
        }
        this.settingsFile = new File(settingsFileDir, "voxelmap.properties");
        try {
            final PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.settingsFile), Charset.forName("UTF-8").newEncoder())));
            out.println("Zoom Level:" + Integer.toString(this.zoom));
            out.println("Hide Minimap:" + Boolean.toString(this.hide));
            out.println("Show Coordinates:" + Boolean.toString(this.coords));
            out.println("Enable Cave Mode:" + Boolean.toString(this.showCaves));
            out.println("Dynamic Lighting:" + Boolean.toString(this.lightmap));
            out.println("Height Map:" + Boolean.toString(this.heightmap));
            out.println("Slope Map:" + Boolean.toString(this.slopemap));
            out.println("Blur:" + Boolean.toString(this.filtering));
            out.println("Water Transparency:" + Boolean.toString(this.waterTransparency));
            out.println("Block Transparency:" + Boolean.toString(this.blockTransparency));
            out.println("Biomes:" + Boolean.toString(this.biomes));
            out.println("Biome Overlay:" + Integer.toString(this.biomeOverlay));
            out.println("Chunk Grid:" + Boolean.toString(this.chunkGrid));
            out.println("Slime Chunks:" + Boolean.toString(this.slimeChunks));
            out.println("Square Map:" + Boolean.toString(this.squareMap));
            out.println("Rotation:" + Boolean.toString(this.rotates));
            out.println("Old North:" + Boolean.toString(this.oldNorth));
            out.println("Waypoint Beacons:" + Boolean.toString(this.showBeacons));
            out.println("Waypoint Signs:" + Boolean.toString(this.showWaypoints));
            out.println("Deathpoints:" + Integer.toString(this.deathpoints));
            out.println("Waypoint Max Distance:" + Integer.toString(this.maxWaypointDisplayDistance));
            out.println("Waypoint Sort By:" + Integer.toString(this.sort));
            out.println("Welcome Message:" + Boolean.toString(this.welcome));
            out.println("Map Corner:" + Integer.toString(this.mapCorner));
            out.println("Map Size:" + Integer.toString(this.sizeModifier));
            out.println("Zoom Key:" + this.keyBindZoom.saveString());
            out.println("Fullscreen Key:" + this.keyBindFullscreen.saveString());
            out.println("Menu Key:" + this.keyBindMenu.saveString());
            out.println("Waypoint Menu Key:" + this.keyBindWaypointMenu.saveString());
            out.println("Waypoint Key:" + this.keyBindWaypoint.saveString());
            out.println("Mob Key:" + this.keyBindMobToggle.saveString());
            out.println("In-game Waypoint Key:" + this.keyBindWaypointToggle.saveString());
            for (final ISubSettingsManager subSettingsManager : this.subSettingsManagers) {
                subSettingsManager.saveAll(out);
            }
            out.close();
        }
        catch (final Exception local) {
            MessageUtils.chatInfo("§EError Saving Settings " + local.getLocalizedMessage());
        }
    }
    
    @Override
    public String getKeyText(final EnumOptionsMinimap par1EnumOptions) {
        final String s = I18nUtils.getString(par1EnumOptions.getName(), new Object[0]) + ": ";
        if (par1EnumOptions.isFloat()) {
            final float f = this.getOptionFloatValue(par1EnumOptions);
            if (par1EnumOptions == EnumOptionsMinimap.ZOOM) {
                return s + (int)f;
            }
            if (par1EnumOptions == EnumOptionsMinimap.WAYPOINTDISTANCE) {
                if (f < 0.0f) {
                    return s + I18nUtils.getString("options.minimap.waypoints.infinite", new Object[0]);
                }
                return s + (int)f;
            }
            else {
                if (f == 0.0f) {
                    return s + I18nUtils.getString("options.off", new Object[0]);
                }
                return s + (int)f + "%";
            }
        }
        else if (par1EnumOptions.isBoolean()) {
            final boolean flag = this.getOptionBooleanValue(par1EnumOptions);
            if (flag) {
                return s + I18nUtils.getString("options.on", new Object[0]);
            }
            return s + I18nUtils.getString("options.off", new Object[0]);
        }
        else {
            if (par1EnumOptions.isList()) {
                final String state = this.getOptionListValue(par1EnumOptions);
                return s + state;
            }
            return s;
        }
    }
    
    @Override
    public float getOptionFloatValue(final EnumOptionsMinimap par1EnumOptions) {
        if (par1EnumOptions == EnumOptionsMinimap.ZOOM) {
            return (float)this.zoom;
        }
        if (par1EnumOptions == EnumOptionsMinimap.WAYPOINTDISTANCE) {
            return (float)this.maxWaypointDisplayDistance;
        }
        return 0.0f;
    }
    
    public boolean getOptionBooleanValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case COORDS: {
                return this.coords;
            }
            case HIDE: {
                return this.hide;
            }
            case CAVEMODE: {
                return this.cavesAllowed && this.showCaves;
            }
            case LIGHTING: {
                return this.lightmap;
            }
            case SQUARE: {
                return this.squareMap;
            }
            case ROTATES: {
                return this.rotates;
            }
            case OLDNORTH: {
                return this.oldNorth;
            }
            case WELCOME: {
                return this.welcome;
            }
            case FILTERING: {
                return this.filtering;
            }
            case WATERTRANSPARENCY: {
                return this.waterTransparency;
            }
            case BLOCKTRANSPARENCY: {
                return this.blockTransparency;
            }
            case BIOMES: {
                return this.biomes;
            }
            case CHUNKGRID: {
                return this.chunkGrid;
            }
            case SLIMECHUNKS: {
                return this.slimeChunks;
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a boolean applicable to minimap)");
            }
        }
    }
    
    public String getOptionListValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case TERRAIN: {
                if (this.slopemap && this.heightmap) {
                    return I18nUtils.getString("options.minimap.terrain.both", new Object[0]);
                }
                if (this.heightmap) {
                    return I18nUtils.getString("options.minimap.terrain.height", new Object[0]);
                }
                if (this.slopemap) {
                    return I18nUtils.getString("options.minimap.terrain.slope", new Object[0]);
                }
                return I18nUtils.getString("options.off", new Object[0]);
            }
            case BEACONS: {
                if (this.showBeacons && this.showWaypoints) {
                    return I18nUtils.getString("options.minimap.ingamewaypoints.both", new Object[0]);
                }
                if (this.showBeacons) {
                    return I18nUtils.getString("options.minimap.ingamewaypoints.beacons", new Object[0]);
                }
                if (this.showWaypoints) {
                    return I18nUtils.getString("options.minimap.ingamewaypoints.signs", new Object[0]);
                }
                return I18nUtils.getString("options.off", new Object[0]);
            }
            case LOCATION: {
                if (this.mapCorner == 0) {
                    return I18nUtils.getString("options.minimap.location.topleft", new Object[0]);
                }
                if (this.mapCorner == 1) {
                    return I18nUtils.getString("options.minimap.location.topright", new Object[0]);
                }
                if (this.mapCorner == 2) {
                    return I18nUtils.getString("options.minimap.location.bottomright", new Object[0]);
                }
                if (this.mapCorner == 3) {
                    return I18nUtils.getString("options.minimap.location.bottomleft", new Object[0]);
                }
                return "Error";
            }
            case SIZE: {
                if (this.sizeModifier == -1) {
                    return I18nUtils.getString("options.minimap.size.small", new Object[0]);
                }
                if (this.sizeModifier == 0) {
                    return I18nUtils.getString("options.minimap.size.medium", new Object[0]);
                }
                if (this.sizeModifier == 1) {
                    return I18nUtils.getString("options.minimap.size.large", new Object[0]);
                }
                if (this.sizeModifier == 2) {
                    return I18nUtils.getString("options.minimap.size.xl", new Object[0]);
                }
                if (this.sizeModifier == 3) {
                    return I18nUtils.getString("options.minimap.size.xxl", new Object[0]);
                }
                if (this.sizeModifier == 4) {
                    return I18nUtils.getString("options.minimap.size.xxxl", new Object[0]);
                }
                return "error";
            }
            case BIOMEOVERLAY: {
                if (this.biomeOverlay == 0) {
                    return I18nUtils.getString("options.off", new Object[0]);
                }
                if (this.biomeOverlay == 1) {
                    return I18nUtils.getString("options.minimap.biomeoverlay.solid", new Object[0]);
                }
                if (this.biomeOverlay == 2) {
                    return I18nUtils.getString("options.minimap.biomeoverlay.transparent", new Object[0]);
                }
                return "error";
            }
            case DEATHPOINTS: {
                if (this.deathpoints == 0) {
                    return I18nUtils.getString("options.off", new Object[0]);
                }
                if (this.deathpoints == 1) {
                    return I18nUtils.getString("options.minimap.waypoints.deathpoints.mostrecent", new Object[0]);
                }
                if (this.deathpoints == 2) {
                    return I18nUtils.getString("options.minimap.waypoints.deathpoints.all", new Object[0]);
                }
                return "error";
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a list value applicable to minimap)");
            }
        }
    }
    
    @Override
    public void setOptionFloatValue(final EnumOptionsMinimap par1EnumOptions, final float par2) {
        if (par1EnumOptions == EnumOptionsMinimap.WAYPOINTDISTANCE) {
            float distance = par2 * 9951.0f + 50.0f;
            if (distance > 10000.0f) {
                distance = -1.0f;
            }
            this.maxWaypointDisplayDistance = (int)distance;
        }
        this.somethingChanged = true;
    }
    
    public void setOptionValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case COORDS: {
                this.coords = !this.coords;
                break;
            }
            case HIDE: {
                this.hide = !this.hide;
                break;
            }
            case CAVEMODE: {
                this.showCaves = !this.showCaves;
                break;
            }
            case LIGHTING: {
                this.lightmap = !this.lightmap;
                break;
            }
            case TERRAIN: {
                if (this.slopemap && this.heightmap) {
                    this.slopemap = false;
                    this.heightmap = false;
                    break;
                }
                if (this.slopemap) {
                    this.slopemap = false;
                    this.heightmap = true;
                    break;
                }
                if (this.heightmap) {
                    this.slopemap = true;
                    this.heightmap = true;
                    break;
                }
                this.slopemap = true;
                this.heightmap = false;
                break;
            }
            case SQUARE: {
                this.squareMap = !this.squareMap;
                break;
            }
            case ROTATES: {
                this.rotates = !this.rotates;
                break;
            }
            case OLDNORTH: {
                this.oldNorth = !this.oldNorth;
                break;
            }
            case BEACONS: {
                if (this.showBeacons && this.showWaypoints) {
                    this.showBeacons = false;
                    this.showWaypoints = false;
                    break;
                }
                if (this.showBeacons) {
                    this.showBeacons = false;
                    this.showWaypoints = true;
                    break;
                }
                if (this.showWaypoints) {
                    this.showWaypoints = true;
                    this.showBeacons = true;
                    break;
                }
                this.showBeacons = true;
                this.showWaypoints = false;
                break;
            }
            case WELCOME: {
                this.welcome = !this.welcome;
                break;
            }
            case LOCATION: {
                this.mapCorner = ((this.mapCorner >= 3) ? 0 : (this.mapCorner + 1));
                break;
            }
            case SIZE: {
                this.sizeModifier = ((this.sizeModifier >= 4) ? -1 : (this.sizeModifier + 1));
                break;
            }
            case FILTERING: {
                this.filtering = !this.filtering;
                break;
            }
            case WATERTRANSPARENCY: {
                this.waterTransparency = !this.waterTransparency;
                break;
            }
            case BLOCKTRANSPARENCY: {
                this.blockTransparency = !this.blockTransparency;
                break;
            }
            case BIOMES: {
                this.biomes = !this.biomes;
                break;
            }
            case BIOMEOVERLAY: {
                ++this.biomeOverlay;
                if (this.biomeOverlay > 2) {
                    this.biomeOverlay = 0;
                    break;
                }
                break;
            }
            case CHUNKGRID: {
                this.chunkGrid = !this.chunkGrid;
                break;
            }
            case SLIMECHUNKS: {
                this.slimeChunks = !this.slimeChunks;
                break;
            }
            case DEATHPOINTS: {
                ++this.deathpoints;
                if (this.deathpoints > 2) {
                    this.deathpoints = 0;
                    break;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName());
            }
        }
        this.somethingChanged = true;
    }
    
    public void toggleIngameWaypoints() {
        if (this.showBeacons || this.showWaypoints) {
            this.preToggleBeacons = this.showBeacons;
            this.preToggleSigns = this.showWaypoints;
            this.showBeacons = false;
            this.showWaypoints = false;
        }
        else {
            this.showBeacons = this.preToggleBeacons;
            this.showWaypoints = this.preToggleSigns;
        }
    }
    
    public String getKeyMappingDescription(final int keybindIndex) {
        if (this.keyBindings[keybindIndex].getName().equals("key.minimap.voxelmapmenu")) {
            return I18nUtils.getString("key.minimap.menu", new Object[0]);
        }
        return I18nUtils.getString(this.keyBindings[keybindIndex].getName(), new Object[0]);
    }
    
    public TextComponent getKeybindDisplayString(final int keybindIndex) {
        final KeyMapping keyBinding = this.keyBindings[keybindIndex];
        return this.getKeybindDisplayString(keyBinding);
    }
    
    public TextComponent getKeybindDisplayString(final KeyMapping keyBinding) {
        return (TextComponent) keyBinding.getTranslatedKeyMessage();
    }
    
    public void setKeyMapping(final KeyMapping keyBinding, final Key input) {
        keyBinding.setKey(input);
        this.saveAll();
    }
    
    public void setSort(final int sort) {
        if (sort == this.sort || sort == -this.sort) {
            this.sort = -this.sort;
        }
        else {
            this.sort = sort;
        }
    }
    
    public boolean isChanged() {
        if (this.somethingChanged) {
            this.somethingChanged = false;
            return true;
        }
        return false;
    }
}
