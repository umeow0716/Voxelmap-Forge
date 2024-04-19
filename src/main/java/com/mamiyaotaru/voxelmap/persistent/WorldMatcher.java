// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import java.util.Iterator;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import java.io.File;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;

public class WorldMatcher
{
	private static final Minecraft game = Minecraft.getInstance();
    private IVoxelMap master;
    private IPersistentMap map;
    private ClientLevel world;
    private boolean cancelled;
    
    public WorldMatcher(final IVoxelMap master, final IPersistentMap map, final ClientLevel world) {
        this.cancelled = false;
        this.master = master;
        this.map = map;
        this.world = world;
    }
    
    public void findMatch() {
        final Runnable runnable = new Runnable() {
            int x;
            int z;
            ArrayList<ComparisonCachedRegion> candidateRegions = new ArrayList<ComparisonCachedRegion>();
            ComparisonCachedRegion region;
            String worldName = WorldMatcher.this.master.getWaypointManager().getCurrentWorldName();
            String worldNamePathPart = TextUtils.scrubNameFile(this.worldName);
            String dimensionName = AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(WorldMatcher.this.world).getStorageName();
            String dimensionNamePathPart = TextUtils.scrubNameFile(this.dimensionName);
            File cachedRegionFileDir = new File(Minecraft.getInstance().gameDirectory, "/voxelmap/cache/" + this.worldNamePathPart + "/");
            
            @Override
            public void run() {
                try {
                    Thread.sleep(500L);
                }
                catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                this.cachedRegionFileDir.mkdirs();
                final ArrayList<String> knownSubworldNames = new ArrayList<String>(WorldMatcher.this.master.getWaypointManager().getKnownSubworldNames());
                final String[] subworldNamesArray = new String[knownSubworldNames.size()];
                knownSubworldNames.toArray(subworldNamesArray);
                final LocalPlayer player = game.player;
                MessageUtils.printDebug("player coords " + player.getX() + " " + player.getZ() + " in world " + WorldMatcher.this.master.getWaypointManager().getCurrentWorldName());
                this.x = (int)Math.floor(player.getX() / 256.0);
                this.z = (int)Math.floor(player.getZ() / 256.0);
                this.loadRegions(subworldNamesArray);
                int attempts = 0;
                while (!WorldMatcher.this.cancelled && (this.candidateRegions.size() == 0 || this.region.getLoadedChunks() < 5) && attempts < 5) {
                    ++attempts;
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (final InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    if (this.x != (int)Math.floor(player.getX() / 256.0) || this.z != (int)Math.floor(player.getZ() / 256.0)) {
                        this.x = (int)Math.floor(player.getX() / 256.0);
                        this.z = (int)Math.floor(player.getZ() / 256.0);
                        MessageUtils.printDebug("player coords changed to " + player.getX() + " " + player.getZ() + " in world " + WorldMatcher.this.master.getWaypointManager().getCurrentWorldName());
                        this.loadRegions(subworldNamesArray);
                    }
                    else if (this.candidateRegions.size() > 0) {
                        MessageUtils.printDebug("going to load current region");
                        this.region.loadCurrent();
                        MessageUtils.printDebug("loaded chunks in local region: " + this.region.getLoadedChunks());
                    }
                    if (attempts >= 5) {
                        if (this.candidateRegions.size() == 0) {
                            MessageUtils.printDebug("no candidate regions at current coordinates, bailing");
                        }
                        else {
                            MessageUtils.printDebug("took too long to load local region, bailing");
                        }
                    }
                }
                final Iterator<ComparisonCachedRegion> iterator = this.candidateRegions.iterator();
                while (!WorldMatcher.this.cancelled && iterator.hasNext()) {
                    final ComparisonCachedRegion candidateRegion = iterator.next();
                    MessageUtils.printDebug("testing region " + candidateRegion.getSubworldName() + ": " + candidateRegion.getKey());
                    if (this.region.getSimilarityTo(candidateRegion) < 95) {
                        MessageUtils.printDebug("region failed");
                        iterator.remove();
                    }
                    else {
                        MessageUtils.printDebug("region succeeded");
                    }
                }
                MessageUtils.printDebug("remaining regions: " + this.candidateRegions.size());
                if (!WorldMatcher.this.cancelled && this.candidateRegions.size() == 1 && !WorldMatcher.this.master.getWaypointManager().receivedAutoSubworldName()) {
                    WorldMatcher.this.master.newSubWorldName(this.candidateRegions.get(0).getSubworldName(), false);
                    final StringBuilder successBuilder = new StringBuilder(I18nUtils.getString("worldmap.multiworld.foundworld1", new Object[0])).append(":").append(" ").append(this.candidateRegions.get(0).getSubworldName()).append(".").append(" ").append(I18nUtils.getString("worldmap.multiworld.foundworld2", new Object[0]));
                    MessageUtils.chatInfo(successBuilder.toString());
                }
                else if (!WorldMatcher.this.cancelled && !WorldMatcher.this.master.getWaypointManager().receivedAutoSubworldName()) {
                    MessageUtils.printDebug("remaining regions: " + this.candidateRegions.size());
                    final StringBuilder failureBuilder = new StringBuilder("§4VoxelMap§r").append(":").append(" ").append(I18nUtils.getString("worldmap.multiworld.unknownsubworld", new Object[0]));
                    MessageUtils.chatInfo(failureBuilder.toString());
                }
            }
            
            private void loadRegions(final String[] subworldNamesArray) {
                for (final String subworldName : subworldNamesArray) {
                    if (!WorldMatcher.this.cancelled) {
                        final File subworldDir = new File(this.cachedRegionFileDir, subworldName + "/" + this.dimensionNamePathPart);
                        if (subworldDir != null && subworldDir.isDirectory()) {
                            final ComparisonCachedRegion candidateRegion = new ComparisonCachedRegion(WorldMatcher.this.map, this.x + "," + this.z, WorldMatcher.this.world, this.worldName, subworldName, this.x, this.z);
                            candidateRegion.loadStored();
                            this.candidateRegions.add(candidateRegion);
                            MessageUtils.printDebug("added candidate region " + candidateRegion.getSubworldName() + ": " + candidateRegion.getKey());
                        }
                    }
                }
                this.region = new ComparisonCachedRegion(WorldMatcher.this.map, this.x + "," + this.z, game.level, this.worldName, "", this.x, this.z);
                MessageUtils.printDebug("going to load current region");
                this.region.loadCurrent();
                MessageUtils.printDebug("loaded chunks in local region: " + this.region.getLoadedChunks());
            }
        };
        ThreadManager.executorService.execute(runnable);
    }
    
    public void cancel() {
        this.cancelled = true;
    }
}
