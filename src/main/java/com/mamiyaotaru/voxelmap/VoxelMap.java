// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import java.util.List;
import com.mamiyaotaru.voxelmap.util.TickCounter;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mamiyaotaru.voxelmap.forgemod.WorldIDPacket;
import com.mamiyaotaru.voxelmap.forgemod.ForgeModVoxelMap;
import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import com.mamiyaotaru.voxelmap.util.MapUtils;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.mamiyaotaru.voxelmap.util.DimensionManager;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import com.mamiyaotaru.voxelmap.util.GLUtils;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.UUID;
import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;
import com.mamiyaotaru.voxelmap.util.WorldUpdateListener;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeNotifier;
import com.mamiyaotaru.voxelmap.persistent.PersistentMap;
import com.mamiyaotaru.voxelmap.interfaces.IRadar;
import com.mamiyaotaru.voxelmap.interfaces.IMap;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;

public class VoxelMap extends AbstractVoxelMap implements PreparableReloadListener
{
    private MapSettingsManager mapOptions;
    private RadarSettingsManager radarOptions;
    private PersistentMapSettingsManager persistentMapOptions;
    private IMap map;
    private IRadar radar;
    private IRadar radarSimple;
    private PersistentMap persistentMap;
    private ISettingsAndLightingChangeNotifier settingsAndLightingChangeNotifier;
    private WorldUpdateListener worldUpdateListener;
    private IColorManager colorManager;
    private IWaypointManager waypointManager;
    private IDimensionManager dimensionManager;
    private ClientLevel world;
    private String worldName;
    private Long newServerTime;
    private boolean checkMOTD;
    private GuiMessage<TextComponent> mostRecentLine;
    private UUID devUUID;
    private String passMessage;
    
    public VoxelMap() {
    	this.mapOptions = null;
        this.radarOptions = null;
        this.persistentMapOptions = null;
        this.map = null;
        this.radar = null;
        this.radarSimple = null;
        this.persistentMap = null;
        this.settingsAndLightingChangeNotifier = null;
        this.worldUpdateListener = null;
        this.colorManager = null;
        this.waypointManager = null;
        this.dimensionManager = null;
        this.worldName = "";
        this.newServerTime = 0L;
        this.checkMOTD = false;
        this.mostRecentLine = null;
        this.devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
        this.passMessage = null;
        VoxelMap.instance = this;
    }
    
    public void lateInit(final boolean showUnderMenus, final boolean isFair) {
        GLUtils.textureManager = Minecraft.getInstance().getTextureManager();
        this.mapOptions = new MapSettingsManager();
        this.mapOptions.showUnderMenus = showUnderMenus;
        this.radarOptions = new RadarSettingsManager();
        this.mapOptions.addSecondaryOptionsManager(this.radarOptions);
        this.persistentMapOptions = new PersistentMapSettingsManager();
        this.mapOptions.addSecondaryOptionsManager(this.persistentMapOptions);
        BiomeRepository.loadBiomeColors();
        this.colorManager = new ColorManager(this);
        this.waypointManager = new WaypointManager(this);
        this.dimensionManager = new DimensionManager(this);
        this.persistentMap = new PersistentMap(this);
        this.mapOptions.loadAll();
        try {
            if (isFair) {
                this.radarOptions.radarAllowed = false;
                this.radarOptions.radarMobsAllowed = false;
                this.radarOptions.radarPlayersAllowed = false;
            }
            else {
            	this.radar = new Radar(this);
                this.radarSimple = new RadarSimple(this);
                this.radarOptions.radarAllowed = true;
                this.radarOptions.radarMobsAllowed = true;
                this.radarOptions.radarPlayersAllowed = true;
            }
        }
        catch (final Exception e) {
            this.radarOptions.radarAllowed = false;
            this.radarOptions.radarMobsAllowed = false;
            this.radarOptions.radarPlayersAllowed = false;
            this.radar = null;
            this.radarSimple = null;
        }
        this.map = new Map(this);
        this.settingsAndLightingChangeNotifier = new SettingsAndLightingChangeNotifier();
        (this.worldUpdateListener = new WorldUpdateListener()).addListener(this.map);
        this.worldUpdateListener.addListener(this.persistentMap);
        final ReloadableResourceManager resourceManager = (ReloadableResourceManager)Minecraft.getInstance().getResourceManager();
        resourceManager.registerReloadListener((PreparableReloadListener)this);
        this.apply((ResourceManager)resourceManager);
    }
    
    public CompletableFuture<Void> reload(final PreparableReloadListener.PreparationBarrier synchronizer, final ResourceManager resourceManager, final ProfilerFiller loadProfiler, final ProfilerFiller applyProfiler, final Executor loadExecutor, final Executor applyExecutor) {
        return synchronizer.wait((Object)Unit.INSTANCE).thenRunAsync(() -> this.apply(resourceManager), applyExecutor);
    }
    
    private CompletableFuture<Void> apply(final ResourceManager resourceManager) {
        this.waypointManager.onResourceManagerReload(resourceManager);
        if (this.radar != null) {
            this.radar.onResourceManagerReload(resourceManager);
        }
        if (this.radarSimple != null) {
            this.radarSimple.onResourceManagerReload(resourceManager);
        }
        this.colorManager.onResourceManagerReload(resourceManager);
        return null;
    }
    
    public void onTickInGame(final PoseStack matrixStack, final Minecraft mc) {
        this.map.onTickInGame(matrixStack, mc);
        if (this.passMessage != null) {
            mc.gui.getChat().addMessage(new TextComponent(this.passMessage));
            this.passMessage = null;
        }
    }
    
    public void onTick(final Minecraft mc, final boolean clock) {
        if (this.checkMOTD) {
            this.checkPermissionMessages(mc);
        }
        if ((GameVariableAccessShim.getWorld() != null && !GameVariableAccessShim.getWorld().equals(this.world)) || (this.world != null && !this.world.equals(GameVariableAccessShim.getWorld()))) {
            this.world = GameVariableAccessShim.getWorld();
            this.waypointManager.newWorld(this.world);
            this.persistentMap.newWorld(this.world);
            if (this.world != null) {
                MapUtils.reset();
                final StringBuilder channelList = new StringBuilder();
                channelList.append("worldinfo:world_id");
                final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeBytes(channelList.toString().getBytes(Charsets.UTF_8));
                mc.getConnection().send(new ServerboundCustomPayloadPacket(new ResourceLocation("register"), buffer));
                ForgeModVoxelMap.WORLD_ID.sendToServer(new WorldIDPacket());
                mc.player.getSkinTextureLocation();
                final java.util.Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> skinMap = mc.getSkinManager().getInsecureSkinInformation(mc.player.getGameProfile());
                if (skinMap.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    mc.getSkinManager().registerTexture((MinecraftProfileTexture)skinMap.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                }
                if (!this.worldName.equals(this.waypointManager.getCurrentWorldName())) {
                    this.worldName = this.waypointManager.getCurrentWorldName();
                    this.radarOptions.radarAllowed = true;
                    this.radarOptions.radarPlayersAllowed = this.radarOptions.radarAllowed;
                    this.radarOptions.radarMobsAllowed = this.radarOptions.radarAllowed;
                    this.mapOptions.cavesAllowed = true;
                    if (!mc.isLocalServer()) {
                        this.newServerTime = System.currentTimeMillis();
                        this.checkMOTD = true;
                    }
                }
                this.map.newWorld(this.world);
            }
        }
        TickCounter.onTick(clock);
        this.persistentMap.onTick(mc);
    }
    
    private void checkPermissionMessages(Minecraft mc) {
    	if (GameVariableAccessShim.getWorld() != null && mc.player != null && mc.gui != null && System.currentTimeMillis() - this.newServerTime.longValue() < 5000L) {
    			UUID playerUUID = mc.player.getUUID();
    			ChatComponent guiNewChat = mc.gui.getChat();
    			if (guiNewChat == null) {
    					System.out.println("failed to get guiNewChat");
    			} else {
    				Object chatListObj = ReflectionUtils.getPrivateFieldValueByType(guiNewChat, ChatComponent.class, List.class, 1);
    				if (chatListObj == null) {
    					System.out.println("could not get chatlist");
    				} else {
    					List<GuiMessage<Component>> chatList = (List<GuiMessage<Component>>) chatListObj;
    					boolean killRadar = false;
    					boolean killCaves = false;
    					for (int t = 0; t < chatList.size(); t++) {
    						GuiMessage<Component> checkMe = chatList.get(t);
    						if (checkMe.equals(this.mostRecentLine))
    							break; 
    						Component rawText = (Component)checkMe.getMessage();
    						String msg = TextUtils.asFormattedString(rawText);
    						String error = "";
    						msg = msg.replaceAll("§r", "");
    						if (msg.contains("§3 §6 §3 §6 §3 §6 §d")) {
    							killCaves = true;
    							error = error + "Server disabled cavemapping.  ";
    						} 
    						if (msg.contains("§3 §6 §3 §6 §3 §6 §e")) {
    							killRadar = true;
    							error = error + "Server disabled radar.  ";
    						} 
    						if (!error.equals(""))
    							this.passMessage = error; 
    				} 
    				this.radarOptions.radarAllowed = Boolean.valueOf((this.radarOptions.radarAllowed.booleanValue() && (!killRadar || this.devUUID.equals(playerUUID))));
    				this.radarOptions.radarPlayersAllowed = this.radarOptions.radarAllowed;
    				this.radarOptions.radarMobsAllowed = this.radarOptions.radarAllowed;
    				this.mapOptions.cavesAllowed = Boolean.valueOf((this.mapOptions.cavesAllowed.booleanValue() && (!killCaves || this.devUUID.equals(playerUUID))));
    				this.mostRecentLine = chatList.isEmpty() ? null : (GuiMessage) chatList.get(0);
    			} 
    		} 
    	} else if (System.currentTimeMillis() - this.newServerTime.longValue() >= 5000L) {
    			this.checkMOTD = false;
    	} 
}
    
    public void setConnectedRealm(final String id) {
        this.waypointManager.setConnectedRealm(id);
    }
    
    public MapSettingsManager getMapOptions() {
        return this.mapOptions;
    }
    
    public RadarSettingsManager getRadarOptions() {
        return this.radarOptions;
    }
    
    public PersistentMapSettingsManager getPersistentMapOptions() {
        return this.persistentMapOptions;
    }
    
    public IMap getMap() {
        return this.map;
    }
    
    public ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier() {
        return this.settingsAndLightingChangeNotifier;
    }
    
    public IRadar getRadar() {
        if (this.radarOptions.showRadar) {
            final int radarMode = this.radarOptions.radarMode;
            this.radarOptions.getClass();
            if (radarMode == 1) {
                return this.radarSimple;
            }
            final int radarMode2 = this.radarOptions.radarMode;
            this.radarOptions.getClass();
            if (radarMode2 == 2) {
                return this.radar;
            }
        }
        return null;
    }
    
    public IColorManager getColorManager() {
        return this.colorManager;
    }
    
    public IWaypointManager getWaypointManager() {
        return this.waypointManager;
    }
    
    public IDimensionManager getDimensionManager() {
        return this.dimensionManager;
    }
    
    public IPersistentMap getPersistentMap() {
        return this.persistentMap;
    }
    
    public void setPermissions(final boolean hasFullRadarPermission, final boolean hasPlayersOnRadarPermission, final boolean hasMobsOnRadarPermission, final boolean hasCavemodePermission) {
        boolean override = false;
        try {
            final UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
            final UUID playerUUID = Minecraft.getInstance().player.getUUID();
            override = playerUUID.equals(devUUID);
        }
        catch (final Exception ex) {}
        this.radarOptions.radarAllowed = (hasFullRadarPermission || override);
        this.radarOptions.radarPlayersAllowed = (hasPlayersOnRadarPermission || override);
        this.radarOptions.radarMobsAllowed = (hasMobsOnRadarPermission || override);
        this.mapOptions.cavesAllowed = (hasCavemodePermission || override);
    }
    
    public synchronized void newSubWorldName(final String name, final boolean fromServer) {
        this.waypointManager.setSubworldName(name, fromServer);
        this.map.newWorldName();
    }
    
    public synchronized void newSubWorldHash(final String hash) {
        this.waypointManager.setSubworldHash(hash);
    }
    
    public String getWorldSeed() {
        if (Minecraft.getInstance().isLocalServer()) {
            String seed = "";
            try {
                seed = Long.toString(Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD).getSeed());
            }
            catch (final Exception ex) {}
            return seed;
        }
        return this.waypointManager.getWorldSeed();
    }
    
    public void setWorldSeed(final String newSeed) {
        if (!Minecraft.getInstance().isLocalServer()) {
            this.waypointManager.setWorldSeed(newSeed);
        }
    }
    
    public void sendPlayerMessageOnMainThread(final String s) {
        this.passMessage = s;
    }
    
    public WorldUpdateListener getWorldUpdateListener() {
        return this.worldUpdateListener;
    }
}
