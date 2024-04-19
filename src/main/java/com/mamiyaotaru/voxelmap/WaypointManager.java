// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import java.awt.Graphics;
import java.awt.Image;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;
import com.mamiyaotaru.voxelmap.util.MessageUtils;
import java.util.Map;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

import java.net.InetSocketAddress;
import java.awt.image.BufferedImage;
import java.io.IOException;
import com.mamiyaotaru.voxelmap.util.ImageUtils;
import javax.imageio.ImageIO;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import java.util.Collection;
import java.util.List;
import com.mamiyaotaru.voxelmap.textures.IIconCreator;
import java.io.File;
import com.mamiyaotaru.voxelmap.util.WaypointContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mamiyaotaru.voxelmap.util.BackgroundImageInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import net.minecraft.client.Minecraft;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;

public class WaypointManager implements IWaypointManager
{
	private final Minecraft game = Minecraft.getInstance();
    IVoxelMap master;
    public MapSettingsManager options;
    TextureAtlas textureAtlas;
    TextureAtlas textureAtlasChooser;
    private boolean loaded;
    private boolean needSave;
    private ArrayList<Waypoint> wayPts;
    private Waypoint highlightedWaypoint;
    private String worldName;
    private String latestRealmsID;
    private String currentSubWorldName;
    private String currentSubWorldHash;
    private String currentSubworldDescriptor;
    private String currentSubworldDescriptorNoCodes;
    private boolean multiworld;
    private boolean gotAutoSubworldName;
    private DimensionContainer currentDimension;
    private TreeSet<String> knownSubworldNames;
    private HashSet<String> oldNorthWorldNames;
    private HashMap<String, String> worldSeeds;
    private BackgroundImageInfo backgroundImageInfo;
    private WaypointContainer waypointContainer;
    private File settingsFile;
    private Long lastNewWorldNameTime;
    private Object waypointLock;
    
    public WaypointManager(final IVoxelMap master) {
        this.options = null;
        this.loaded = false;
        this.needSave = false;
        this.wayPts = new ArrayList<Waypoint>();
        this.highlightedWaypoint = null;
        this.worldName = "";
        this.latestRealmsID = "";
        this.currentSubWorldName = "";
        this.currentSubWorldHash = "";
        this.currentSubworldDescriptor = "";
        this.currentSubworldDescriptorNoCodes = "";
        this.multiworld = false;
        this.gotAutoSubworldName = false;
        this.currentDimension = null;
        this.knownSubworldNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        this.oldNorthWorldNames = new HashSet<String>();
        this.worldSeeds = new HashMap<String, String>();
        this.backgroundImageInfo = null;
        this.waypointContainer = null;
        this.lastNewWorldNameTime = 0L;
        this.waypointLock = new Object();
        this.master = master;
        this.options = master.getMapOptions();
        (this.textureAtlas = new TextureAtlas("waypoints")).setFilter(false, false);
        (this.textureAtlasChooser = new TextureAtlas("chooser")).setFilter(false, false);
        this.waypointContainer = new WaypointContainer(this.options);
    }
    
    @Override
    public void onResourceManagerReload(final ResourceManager resourceManager) {
        final List<ResourceLocation> images = new ArrayList<ResourceLocation>();
        final IIconCreator iconCreator = new IIconCreator() {
            @Override
            public void addIcons(final TextureAtlas textureAtlas) {
                final Minecraft mc = Minecraft.getInstance();
                final Collection<ResourceLocation> allImages = mc.getResourceManager().listResources("images", asset -> asset.endsWith(".png"));
                for (final ResourceLocation candidate : allImages) {
                    if (candidate.getNamespace().equals("voxelmap") && candidate.getPath().contains("images/waypoints")) {
                        images.add(candidate);
                    }
                }
                final Sprite markerIcon = textureAtlas.registerIconForResource(new ResourceLocation("voxelmap", "images/waypoints/marker.png"), Minecraft.getInstance().getResourceManager());
                final Sprite markerIconSmall = textureAtlas.registerIconForResource(new ResourceLocation("voxelmap", "images/waypoints/markersmall.png"), Minecraft.getInstance().getResourceManager());
                for (final ResourceLocation resourceLocation : images) {
                    final Sprite icon = textureAtlas.registerIconForResource(resourceLocation, Minecraft.getInstance().getResourceManager());
                    final String name = resourceLocation.toString();
                    if (name.toLowerCase().contains("waypoints/waypoint") && !name.toLowerCase().contains("small")) {
                        textureAtlas.registerMaskedIcon(name.replace(".png", "Small.png"), icon);
                        textureAtlas.registerMaskedIcon(name.replace("waypoints/waypoint", "waypoints/marker"), markerIcon);
                        textureAtlas.registerMaskedIcon(name.replace("waypoints/waypoint", "waypoints/marker").replace(".png", "Small.png"), markerIconSmall);
                    }
                    else {
                        if (!name.toLowerCase().contains("waypoints/marker") || name.toLowerCase().contains("small")) {
                            continue;
                        }
                        textureAtlas.registerMaskedIcon(name.replace(".png", "Small.png"), icon);
                    }
                }
            }
        };
        this.textureAtlas.loadTextureAtlas(iconCreator);
        this.textureAtlasChooser.reset();
        final int expectedSize = 32;
        for (final ResourceLocation resourceLocation : images) {
            final String name = resourceLocation.toString();
            if (name.toLowerCase().contains("waypoints/waypoint") && !name.toLowerCase().contains("small")) {
            	try {
                    final Resource imageResource = resourceManager.getResource(resourceLocation);
                    BufferedImage bufferedImage = ImageIO.read(imageResource.getInputStream());
                    imageResource.close();
                    final float scale = expectedSize / (float)bufferedImage.getWidth();
                    bufferedImage = ImageUtils.scaleImage(bufferedImage, scale);
                    this.textureAtlasChooser.registerIconForBufferedImage(name, bufferedImage);
                }
                catch (final IOException e) {
                    this.textureAtlasChooser.registerIconForResource(resourceLocation, Minecraft.getInstance().getResourceManager());
                }
            }
        }
        this.textureAtlasChooser.stitch();
    }
    
    @Override
    public TextureAtlas getTextureAtlas() {
        return this.textureAtlas;
    }
    
    @Override
    public TextureAtlas getTextureAtlasChooser() {
        return this.textureAtlasChooser;
    }
    
    @Override
    public ArrayList<Waypoint> getWaypoints() {
        return this.wayPts;
    }
    
    @Override
    public void newWorld(final Level world) {
        if (world == null) {
            this.currentDimension = null;
        }
        else {
            String mapName;
            if (this.game.isLocalServer()) {
                mapName = this.getMapName();
            }
            else {
                mapName = this.getServerName();
                if (mapName != null) {
                    mapName = mapName.toLowerCase();
                }
            }
            if (!this.worldName.equals(mapName) && mapName != null && !mapName.equals("")) {
                this.currentDimension = null;
                this.worldName = mapName;
                this.master.getDimensionManager().populateDimensions(world);
                this.loadWaypoints();
            }
            this.master.getDimensionManager().enteredWorld(world);
            final DimensionContainer dim = this.master.getDimensionManager().getDimensionContainerByWorld(world);
            this.enteredDimension(dim);
            this.setSubWorldDescriptor("");
        }
    }
    
    public String getMapName() {
        return this.game.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().toFile().getName();
    }
    
    public String getServerName() {
        String serverName = "";
        try {
            final ServerData serverData = this.game.getCurrentServer();
            if (serverData != null) {
                boolean isOnLAN = false;
                isOnLAN = serverData.isLan();
                if (isOnLAN) {
                    System.out.println("LAN server detected!");
                    serverName = serverData.name;
                }
                else {
                    serverName = serverData.ip;
                }
            }
            else if (!this.latestRealmsID.equals("")) {
                System.out.println("REALMS server detected!");
                serverName = this.latestRealmsID;
            }
            else {
                final ClientPacketListener netHandler = this.game.getConnection();
                final Connection networkManager = netHandler.getConnection();
                final InetSocketAddress socketAddress = (InetSocketAddress)networkManager.getRemoteAddress();
                serverName = socketAddress.getHostString() + ":" + socketAddress.getPort();
            }
        }
        catch (final Exception e) {
            System.err.println("error getting ServerData");
            e.printStackTrace();
        }
        return serverName;
    }
    
    @Override
    public void setConnectedRealm(final String id) {
        this.latestRealmsID = id;
    }
    
    @Override
    public String getCurrentWorldName() {
        return this.worldName;
    }
    
    @Override
    public void handleDeath() {
        final HashSet<Waypoint> toDel = new HashSet<Waypoint>();
        for (final Waypoint pt : this.wayPts) {
            if (pt.name.equals("Latest Death")) {
                pt.name = "Previous Death";
            }
            if (pt.name.startsWith("Previous Death")) {
                final int deathpoints = this.options.deathpoints;
                this.options.getClass();
                if (deathpoints == 2) {
                    int num = 0;
                    try {
                        if (pt.name.length() > 15) {
                            num = Integer.parseInt(pt.name.substring(15));
                        }
                    }
                    catch (final Exception e) {
                        num = 0;
                    }
                    pt.red -= (pt.red - 0.5f) / 8.0f;
                    pt.green -= (pt.green - 0.5f) / 8.0f;
                    pt.blue -= (pt.blue - 0.5f) / 8.0f;
                    pt.name = "Previous Death " + (num + 1);
                }
                else {
                    toDel.add(pt);
                }
            }
        }
        final int deathpoints2 = this.options.deathpoints;
        this.options.getClass();
        if (deathpoints2 != 2 && toDel.size() > 0) {
            for (final Waypoint pt : toDel) {
                this.deleteWaypoint(pt);
            }
        }
        final int deathpoints3 = this.options.deathpoints;
        this.options.getClass();
        if (deathpoints3 != 0) {
            final LocalPlayer thePlayer = game.player;
            final TreeSet<DimensionContainer> dimensions = new TreeSet<DimensionContainer>();
            dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(game.level));
            final double dimensionScale = thePlayer.level.dimensionType().coordinateScale();
            this.addWaypoint(new Waypoint("Latest Death", (int)(GameVariableAccessShim.xCoord() * dimensionScale), (int)(GameVariableAccessShim.zCoord() * dimensionScale), GameVariableAccessShim.yCoord() - 1, true, 1.0f, 1.0f, 1.0f, "Skull", this.getCurrentSubworldDescriptor(false), dimensions));
        }
    }
    
    private void enteredDimension(final DimensionContainer dimension) {
        this.highlightedWaypoint = null;
        if (dimension == this.currentDimension) {
            this.multiworld = true;
        }
        this.currentDimension = dimension;
        synchronized (this.waypointLock) {
            this.waypointContainer = new WaypointContainer(this.options);
            for (final Waypoint pt : this.wayPts) {
                if (pt.dimensions.size() == 0 || pt.dimensions.contains(dimension)) {
                    pt.inDimension = true;
                }
                else {
                    pt.inDimension = false;
                }
                this.waypointContainer.addWaypoint(pt);
            }
            this.waypointContainer.setHighlightedWaypoint(this.highlightedWaypoint);
        }
        this.loadBackgroundMapImage();
    }
    
    @Override
    public void setOldNorth(final boolean oldNorth) {
        String oldNorthWorldName = "";
        if (this.knownSubworldNames.size() == 0) {
            oldNorthWorldName = "all";
        }
        else {
            oldNorthWorldName = this.getCurrentSubworldDescriptor(false);
        }
        if (oldNorth) {
            this.oldNorthWorldNames.add(oldNorthWorldName);
        }
        else {
            this.oldNorthWorldNames.remove(oldNorthWorldName);
        }
        this.saveWaypoints();
    }
    
    @Override
    public TreeSet<String> getKnownSubworldNames() {
        return this.knownSubworldNames;
    }
    
    @Override
    public boolean receivedAutoSubworldName() {
        return this.gotAutoSubworldName;
    }
    
    @Override
    public boolean isMultiworld() {
        return this.multiworld;
    }
    
    @Override
    public synchronized void setSubworldName(final String name, final boolean fromServer) {
        final boolean notNull = !name.equals("");
        if (notNull || System.currentTimeMillis() - this.lastNewWorldNameTime > 2000L) {
            if (notNull) {
                if (fromServer) {
                    this.gotAutoSubworldName = true;
                }
                if (!name.equals(this.currentSubWorldName)) {
                    System.out.println("New world name: " + TextUtils.scrubCodes(name));
                }
                this.lastNewWorldNameTime = System.currentTimeMillis();
            }
            this.setSubWorldDescriptor(this.currentSubWorldName = name);
        }
    }
    
    @Override
    public synchronized void setSubworldHash(final String hash) {
        this.currentSubWorldHash = hash;
        if (this.currentSubWorldName.equals("")) {
            this.setSubWorldDescriptor(this.currentSubWorldHash);
        }
    }
    
    private void setSubWorldDescriptor(String descriptor) {
        boolean serverSaysOldNorth = false;
        if (descriptor.endsWith("§o§n")) {
            descriptor = descriptor.substring(0, descriptor.length() - 4);
            serverSaysOldNorth = true;
        }
        this.currentSubworldDescriptor = descriptor;
        this.newSubworldName(this.currentSubworldDescriptorNoCodes = TextUtils.scrubCodes(this.currentSubworldDescriptor));
        final String currentSubWorldDescriptorScrubbed = TextUtils.scrubName(this.currentSubworldDescriptorNoCodes);
        synchronized (this.waypointLock) {
            for (final Waypoint pt : this.wayPts) {
                if (currentSubWorldDescriptorScrubbed == "" || pt.world == "" || currentSubWorldDescriptorScrubbed.equals(pt.world)) {
                    pt.inWorld = true;
                }
                else {
                    pt.inWorld = false;
                }
            }
        }
        if (serverSaysOldNorth) {
            if (this.currentSubworldDescriptorNoCodes.equals("")) {
                this.oldNorthWorldNames.add("all");
            }
            else {
                this.oldNorthWorldNames.add(this.currentSubworldDescriptorNoCodes);
            }
        }
        this.master.getMapOptions().oldNorth = this.oldNorthWorldNames.contains(this.currentSubworldDescriptorNoCodes);
    }
    
    private void newSubworldName(final String name) {
        if (name != null && !name.equals("")) {
            this.multiworld = true;
            if (this.knownSubworldNames.add(name)) {
                if (this.loaded) {
                    this.saveWaypoints();
                }
                else {
                    this.needSave = true;
                }
            }
        }
        this.loadBackgroundMapImage();
    }
    
    @Override
    public void changeSubworldName(final String oldName, final String newName) {
        if (!newName.equals(oldName) && this.knownSubworldNames.remove(oldName)) {
            this.knownSubworldNames.add(newName);
            synchronized (this.waypointLock) {
                for (final Waypoint pt : this.wayPts) {
                    if (pt.world.equals(oldName)) {
                        pt.world = newName;
                    }
                }
            }
            this.master.getPersistentMap().renameSubworld(oldName, newName);
            final String worldName = this.getCurrentWorldName();
            final String worldNamePathPart = TextUtils.scrubNameFile(worldName);
            String subWorldNamePathPart = TextUtils.scrubNameFile(oldName) + "/";
            final File oldCachedRegionFileDir = new File(game.gameDirectory, "/mods/mamiyaotaru/voxelmap/cache/" + worldNamePathPart + "/" + subWorldNamePathPart);
            if (oldCachedRegionFileDir.exists() && oldCachedRegionFileDir.isDirectory()) {
                subWorldNamePathPart = TextUtils.scrubNameFile(newName) + "/";
                final File newCachedRegionFileDir = new File(game.gameDirectory, "/mods/mamiyaotaru/voxelmap/cache/" + worldNamePathPart + "/" + subWorldNamePathPart);
                final boolean success = oldCachedRegionFileDir.renameTo(newCachedRegionFileDir);
                if (!success) {
                    System.out.println("Failed renaming " + oldCachedRegionFileDir.getPath() + " to " + newCachedRegionFileDir.getPath());
                }
            }
            if (oldName.equals(this.getCurrentSubworldDescriptor(false))) {
                this.setSubworldName(newName, false);
            }
            this.saveWaypoints();
        }
    }
    
    @Override
    public void deleteSubworld(final String name) {
        if (this.knownSubworldNames.remove(name)) {
            synchronized (this.waypointLock) {
                for (final Waypoint pt : this.wayPts) {
                    if (pt.world.equals(name)) {
                        pt.world = "";
                        pt.inWorld = true;
                    }
                }
            }
            this.saveWaypoints();
            this.lastNewWorldNameTime = 0L;
            this.setSubworldName("", false);
        }
    }
    
    @Override
    public String getCurrentSubworldDescriptor(final boolean withCodes) {
        return withCodes ? this.currentSubworldDescriptor : this.currentSubworldDescriptorNoCodes;
    }
    
    @Override
    public String getWorldSeed() {
        String key = "all";
        if (this.knownSubworldNames.size() > 0) {
            key = this.getCurrentSubworldDescriptor(false);
        }
        String seed = this.worldSeeds.get(key);
        if (seed == null) {
            seed = "";
        }
        return seed;
    }
    
    @Override
    public void setWorldSeed(final String newSeed) {
        System.out.println("waypoint manager gets new world seed: " + newSeed);
        String worldName = "all";
        if (this.knownSubworldNames.size() > 0) {
            worldName = this.getCurrentSubworldDescriptor(false);
        }
        this.worldSeeds.put(worldName, newSeed);
        this.saveWaypoints();
    }
    
    @Override
    public void saveWaypoints() {
        String worldNameSave = this.getCurrentWorldName();
        if (worldNameSave.endsWith(":25565")) {
            final int portSepLoc = worldNameSave.lastIndexOf(":");
            if (portSepLoc != -1) {
                worldNameSave = worldNameSave.substring(0, portSepLoc);
            }
        }
        worldNameSave = TextUtils.scrubNameFile(worldNameSave);
        final File saveDir = new File(game.gameDirectory, "/voxelmap/");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        this.settingsFile = new File(saveDir, worldNameSave + ".points");
        try {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.settingsFile), StandardCharsets.UTF_8));
            String knownSubworldsString = "";
            for (final String subworldName : this.knownSubworldNames) {
                knownSubworldsString = knownSubworldsString + TextUtils.scrubName(subworldName) + ",";
            }
            out.println("subworlds:" + knownSubworldsString);
            String oldNorthWorldsString = "";
            for (final String oldNorthWorldName : this.oldNorthWorldNames) {
                oldNorthWorldsString = oldNorthWorldsString + TextUtils.scrubName(oldNorthWorldName) + ",";
            }
            out.println("oldNorthWorlds:" + oldNorthWorldsString);
            String seedsString = "";
            for (final Map.Entry<String, String> entry : this.worldSeeds.entrySet()) {
                seedsString = seedsString + TextUtils.scrubName(entry.getKey()) + "#" + entry.getValue() + ",";
            }
            out.println("seeds:" + seedsString);
            for (final Waypoint pt : this.wayPts) {
                if (!pt.name.startsWith("^")) {
                    String dimensionsString = "";
                    for (final DimensionContainer dimension : pt.dimensions) {
                        dimensionsString = dimensionsString + "" + dimension.getStorageName() + "#";
                    }
                    if (dimensionsString.equals("")) {
                        dimensionsString += AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByResourceLocation(DimensionType.OVERWORLD_LOCATION.location()).getStorageName();
                    }
                    out.println("name:" + TextUtils.scrubName(pt.name) + ",x:" + pt.x + ",z:" + pt.z + ",y:" + pt.y + ",enabled:" + Boolean.toString(pt.enabled) + ",red:" + pt.red + ",green:" + pt.green + ",blue:" + pt.blue + ",suffix:" + pt.imageSuffix + ",world:" + TextUtils.scrubName(pt.world) + ",dimensions:" + dimensionsString);
                }
            }
            out.close();
        }
        catch (final Exception local) {
            MessageUtils.chatInfo("§EError Saving Waypoints");
            local.printStackTrace();
        }
    }
    
    private void loadWaypoints() {
        this.loaded = false;
        this.multiworld = false;
        this.gotAutoSubworldName = false;
        this.currentDimension = null;
        this.setSubWorldDescriptor("");
        this.knownSubworldNames.clear();
        this.oldNorthWorldNames.clear();
        this.worldSeeds.clear();
        synchronized (this.waypointLock) {
            boolean loaded = false;
            this.wayPts = new ArrayList<Waypoint>();
            String worldNameStandard = this.getCurrentWorldName();
            if (worldNameStandard.endsWith(":25565")) {
                final int portSepLoc = worldNameStandard.lastIndexOf(":");
                if (portSepLoc != -1) {
                    worldNameStandard = worldNameStandard.substring(0, portSepLoc);
                }
            }
            worldNameStandard = TextUtils.scrubNameFile(worldNameStandard);
            loaded = this.loadWaypointsExtensible(worldNameStandard);
            if (!loaded) {
                MessageUtils.chatInfo("§ENo waypoints exist for this world/server.");
            }
        }
        this.loaded = true;
        if (this.needSave) {
            this.needSave = false;
            this.saveWaypoints();
        }
        this.multiworld = (this.multiworld || this.knownSubworldNames.size() > 0);
    }
    
    private boolean loadWaypointsExtensible(final String worldNameStandard) {
        final File settingsFileNew = new File(game.gameDirectory, "/voxelmap/" + worldNameStandard + ".points");
        final File settingsFileOld = new File(game.gameDirectory, "/mods/mamiyaotaru/voxelmap/" + worldNameStandard + ".points");
        if (!settingsFileOld.exists() && !settingsFileNew.exists()) {
            return false;
        }
        if (!settingsFileOld.exists()) {
            this.settingsFile = settingsFileNew;
        }
        else if (!settingsFileNew.exists()) {
            this.settingsFile = settingsFileOld;
        }
        else {
            this.settingsFile = settingsFileNew;
        }
        if (this.settingsFile.exists()) {
            try {
                final Properties properties = new Properties();
                final FileReader fr = new FileReader(this.settingsFile);
                properties.load(fr);
                final String subWorldsS = properties.getProperty("subworlds", "");
                final String[] subWorlds = subWorldsS.split(",");
                for (int t = 0; t < subWorlds.length; ++t) {
                    if (!subWorlds[t].equals("")) {
                        this.knownSubworldNames.add(TextUtils.descrubName(subWorlds[t]));
                    }
                }
                final String oldNorthWorldsS = properties.getProperty("oldNorthWorlds", "");
                final String[] oldNorthWorlds = oldNorthWorldsS.split(",");
                for (int t2 = 0; t2 < oldNorthWorlds.length; ++t2) {
                    if (!oldNorthWorlds[t2].equals("")) {
                        this.oldNorthWorldNames.add(TextUtils.descrubName(oldNorthWorlds[t2]));
                    }
                }
                final String worldSeedsS = properties.getProperty("seeds", "");
                final String[] worldSeedPairs = worldSeedsS.split(",");
                for (int t3 = 0; t3 < worldSeedPairs.length; ++t3) {
                    final String pair = worldSeedPairs[t3];
                    final String[] worldSeedPair = pair.split("#");
                    if (worldSeedPair.length == 2) {
                        this.worldSeeds.put(worldSeedPair[0], worldSeedPair[1]);
                    }
                }
                fr.close();
            }
            catch (final IOException ex) {}
            try {
                final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.settingsFile), StandardCharsets.UTF_8));
                String sCurrentLine;
                while ((sCurrentLine = in.readLine()) != null) {
                    try {
                        final String[] pairs = sCurrentLine.split(",");
                        if (pairs.length <= 1) {
                            continue;
                        }
                        String name = "";
                        int x = 0;
                        int z = 0;
                        int y = -1;
                        boolean enabled = false;
                        float red = 0.5f;
                        float green = 0.0f;
                        float blue = 0.0f;
                        String suffix = "";
                        String world = "";
                        final TreeSet<DimensionContainer> dimensions = new TreeSet<DimensionContainer>();
                        for (int t4 = 0; t4 < pairs.length; ++t4) {
                            final int splitIndex = pairs[t4].indexOf(":");
                            if (splitIndex != -1) {
                                final String key = pairs[t4].substring(0, splitIndex).toLowerCase().trim();
                                final String value = pairs[t4].substring(splitIndex + 1).trim();
                                if (key.equals("name")) {
                                    name = TextUtils.descrubName(value);
                                }
                                else if (key.equals("x")) {
                                    x = Integer.parseInt(value);
                                }
                                else if (key.equals("z")) {
                                    z = Integer.parseInt(value);
                                }
                                else if (key.equals("y")) {
                                    y = Integer.parseInt(value);
                                }
                                else if (key.equals("enabled")) {
                                    enabled = Boolean.parseBoolean(value);
                                }
                                else if (key.equals("red")) {
                                    red = Float.parseFloat(value);
                                }
                                else if (key.equals("green")) {
                                    green = Float.parseFloat(value);
                                }
                                else if (key.equals("blue")) {
                                    blue = Float.parseFloat(value);
                                }
                                else if (key.equals("suffix")) {
                                    suffix = value;
                                }
                                else if (key.equals("world")) {
                                    world = TextUtils.descrubName(value);
                                }
                                else if (key.equals("dimensions")) {
                                    final String[] dimensionStrings = value.split("#");
                                    for (int s = 0; s < dimensionStrings.length; ++s) {
                                        dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByIdentifier(dimensionStrings[s]));
                                    }
                                    if (dimensions.size() == 0) {
                                        dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByResourceLocation(DimensionType.OVERWORLD_LOCATION.location()));
                                    }
                                }
                            }
                        }
                        if (name.equals("")) {
                            continue;
                        }
                        this.loadWaypoint(name, x, z, y, enabled, red, green, blue, suffix, world, dimensions);
                        if (world.equals("")) {
                            continue;
                        }
                        this.knownSubworldNames.add(TextUtils.descrubName(world));
                    }
                    catch (final Exception ex2) {}
                }
                in.close();
            }
            catch (final Exception local) {
                MessageUtils.chatInfo("§EError Loading Waypoints");
                System.err.println("waypoint load error: " + local.getLocalizedMessage());
                local.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }
    
    private void loadWaypoint(final String name, final int x, final int z, final int y, final boolean enabled, final float red, final float green, final float blue, final String suffix, final String world, final TreeSet<DimensionContainer> dimensions) {
        final Waypoint newWaypoint = new Waypoint(name, x, z, y, enabled, red, green, blue, suffix, world, dimensions);
        if (!this.wayPts.contains(newWaypoint)) {
            this.wayPts.add(newWaypoint);
        }
    }
    
    @Override
    public void deleteWaypoint(final Waypoint point) {
        this.waypointContainer.removeWaypoint(point);
        this.wayPts.remove(point);
        this.saveWaypoints();
        if (point == this.highlightedWaypoint) {
            this.setHighlightedWaypoint(null, false);
        }
    }
    
    @Override
    public void addWaypoint(final Waypoint newWaypoint) {
        this.wayPts.add(newWaypoint);
        this.waypointContainer.addWaypoint(newWaypoint);
        this.saveWaypoints();
        if (this.highlightedWaypoint != null && this.highlightedWaypoint.getX() == newWaypoint.getX() && this.highlightedWaypoint.getZ() == newWaypoint.getZ()) {
            this.setHighlightedWaypoint(newWaypoint, false);
        }
    }
    
    @Override
    public void setHighlightedWaypoint(final Waypoint waypoint, final boolean toggle) {
        if (toggle && waypoint == this.highlightedWaypoint) {
            this.highlightedWaypoint = null;
        }
        else {
            if (waypoint != null && !this.wayPts.contains(waypoint)) {
                waypoint.red = 2.0f;
                waypoint.blue = 0.0f;
                waypoint.green = 0.0f;
            }
            this.highlightedWaypoint = waypoint;
        }
        this.waypointContainer.setHighlightedWaypoint(this.highlightedWaypoint);
    }
    
    @Override
    public Waypoint getHighlightedWaypoint() {
        return this.highlightedWaypoint;
    }
    
    @Override
    public void renderWaypoints(final float partialTicks, final PoseStack matrixStack, final boolean beacons, final boolean signs, final boolean withDepth, final boolean withoutDepth) {
        if (this.waypointContainer != null) {
            this.waypointContainer.renderWaypoints(partialTicks, matrixStack, beacons, signs, withDepth, withoutDepth);
        }
    }
    
    private void loadBackgroundMapImage() {
        if (this.backgroundImageInfo != null) {
            GLUtils.glah(this.backgroundImageInfo.glid);
            this.backgroundImageInfo = null;
        }
        try {
            String path = this.getCurrentWorldName();
            final String subworldDescriptor = this.getCurrentSubworldDescriptor(false);
            if (subworldDescriptor != null && !subworldDescriptor.equals("")) {
                path = path + "/" + subworldDescriptor;
            }
            path = path + "/" + this.currentDimension.getStorageName();
            InputStream is = this.game.getResourceManager().getResource(new ResourceLocation("voxelmap", "images/backgroundmaps/" + path + "/map.png")).getInputStream();
            final Image image = ImageIO.read(is);
            is.close();
            final BufferedImage mapImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);
            final Graphics gfx = mapImage.createGraphics();
            gfx.drawImage(image, 0, 0, null);
            gfx.dispose();
            is = this.game.getResourceManager().getResource(new ResourceLocation("voxelmap", "images/backgroundmaps/" + path + "/map.txt")).getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            final Properties mapProperties = new Properties();
            mapProperties.load(isr);
            final String left = mapProperties.getProperty("left");
            final String right = mapProperties.getProperty("right");
            final String top = mapProperties.getProperty("top");
            final String bottom = mapProperties.getProperty("bottom");
            final String width = mapProperties.getProperty("width");
            final String height = mapProperties.getProperty("height");
            final String scale = mapProperties.getProperty("scale");
            if (left != null && top != null && width != null && height != null) {
                this.backgroundImageInfo = new BackgroundImageInfo(mapImage, Integer.parseInt(left), Integer.parseInt(top), Integer.parseInt(width), Integer.parseInt(height));
            }
            else if (left != null && top != null && scale != null) {
                this.backgroundImageInfo = new BackgroundImageInfo(mapImage, Integer.parseInt(left), Integer.parseInt(top), Float.parseFloat(scale));
            }
            else if (left != null && top != null && right != null && bottom != null) {
                final int widthInt = Integer.parseInt(right) - Integer.parseInt(left);
                final int heightInt = Integer.parseInt(right) - Integer.parseInt(left);
                this.backgroundImageInfo = new BackgroundImageInfo(mapImage, Integer.parseInt(left), Integer.parseInt(top), widthInt, heightInt);
            }
            isr.close();
        }
        catch (final Exception ex) {}
    }
    
    @Override
    public BackgroundImageInfo getBackgroundImageInfo() {
        return this.backgroundImageInfo;
    }
}
