// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.util.ColorUtils;
import com.mamiyaotaru.voxelmap.util.MapUtils;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import com.mamiyaotaru.voxelmap.util.BlockRepository;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeNotifier;
import java.util.Iterator;
import com.mamiyaotaru.voxelmap.util.TickCounter;
import com.mamiyaotaru.voxelmap.util.VoxelMapMutableBlockPos;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
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

import com.mamiyaotaru.voxelmap.util.TextUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.mamiyaotaru.voxelmap.util.MapChunkCache;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import net.minecraft.world.level.levelgen.Heightmap;

public class PersistentMap implements IPersistentMap, IChangeObserver
{
    IVoxelMap master;
    VoxelMapMutableBlockPos blockPos;
    IColorManager colorManager;
    MapSettingsManager mapOptions;
    PersistentMapSettingsManager options;
    WorldMatcher worldMatcher;
    int[] lightmapColors;
    ClientLevel world;
    String subworldName;
    protected List<CachedRegion> cachedRegionsPool;
    protected ConcurrentHashMap<String, CachedRegion> cachedRegions;
    int lastLeft;
    int lastRight;
    int lastTop;
    int lastBottom;
    CachedRegion[] lastRegionsArray;
    Comparator<CachedRegion> ageThenDistanceSorter;
    Comparator<RegionCoordinates> distanceSorter;
    private boolean queuedChangedChunks;
    private MapChunkCache chunkCache = new MapChunkCache(33, 33, this);
    private ConcurrentLinkedQueue<ChunkWithAge> chunkUpdateQueue;
    
    public PersistentMap(final IVoxelMap master) {
        this.blockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.subworldName = "";
        this.cachedRegionsPool = Collections.synchronizedList(new ArrayList<CachedRegion>());
        this.cachedRegions = new ConcurrentHashMap<String, CachedRegion>(150, 0.9f, 2);
        this.lastLeft = 0;
        this.lastRight = 0;
        this.lastTop = 0;
        this.lastBottom = 0;
        this.lastRegionsArray = new CachedRegion[0];
        this.ageThenDistanceSorter = new Comparator<CachedRegion>() {
            @Override
            public int compare(final CachedRegion region1, final CachedRegion region2) {
                final long mostRecentAccess1 = region1.getMostRecentView();
                final long mostRecentAccess2 = region2.getMostRecentView();
                if (mostRecentAccess1 < mostRecentAccess2) {
                    return 1;
                }
                if (mostRecentAccess1 > mostRecentAccess2) {
                    return -1;
                }
                final double distance1sq = (region1.getX() * 256 + region1.getWidth() / 2 - PersistentMap.this.options.mapX) * (region1.getX() * 256 + region1.getWidth() / 2 - PersistentMap.this.options.mapX) + (region1.getZ() * 256 + region1.getWidth() / 2 - PersistentMap.this.options.mapZ) * (region1.getZ() * 256 + region1.getWidth() / 2 - PersistentMap.this.options.mapZ);
                final double distance2sq = (region2.getX() * 256 + region2.getWidth() / 2 - PersistentMap.this.options.mapX) * (region2.getX() * 256 + region2.getWidth() / 2 - PersistentMap.this.options.mapX) + (region2.getZ() * 256 + region2.getWidth() / 2 - PersistentMap.this.options.mapZ) * (region2.getZ() * 256 + region2.getWidth() / 2 - PersistentMap.this.options.mapZ);
                return Double.compare(distance1sq, distance2sq);
            }
        };
        this.distanceSorter = new Comparator<RegionCoordinates>() {
            @Override
            public int compare(final RegionCoordinates coordinates1, final RegionCoordinates coordinates2) {
                final double distance1sq = (coordinates1.x * 256 + 128 - PersistentMap.this.options.mapX) * (coordinates1.x * 256 + 128 - PersistentMap.this.options.mapX) + (coordinates1.z * 256 + 128 - PersistentMap.this.options.mapZ) * (coordinates1.z * 256 + 128 - PersistentMap.this.options.mapZ);
                final double distance2sq = (coordinates2.x * 256 + 128 - PersistentMap.this.options.mapX) * (coordinates2.x * 256 + 128 - PersistentMap.this.options.mapX) + (coordinates2.z * 256 + 128 - PersistentMap.this.options.mapZ) * (coordinates2.z * 256 + 128 - PersistentMap.this.options.mapZ);
                return Double.compare(distance1sq, distance2sq);
            }
        };
        this.queuedChangedChunks = false;
        this.chunkUpdateQueue = new ConcurrentLinkedQueue<ChunkWithAge>();
        this.master = master;
        this.colorManager = master.getColorManager();
        this.mapOptions = master.getMapOptions();
        this.options = master.getPersistentMapOptions();
        Arrays.fill(this.lightmapColors = new int[256], -16777216);
    }
    
    @Override
    public void newWorld(final ClientLevel world) {
        this.subworldName = "";
        this.purgeCachedRegions();
        this.queuedChangedChunks = false;
        this.chunkUpdateQueue.clear();
        this.world = world;
        if (this.worldMatcher != null) {
            this.worldMatcher.cancel();
        }
        if (world != null) {
            this.newWorldStuff();
        }
        else if (world == null) {
            final Thread pauseForSubworldNamesThread = new Thread(null, null, "VoxelMap Pause for Subworld Name Thread") {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (PersistentMap.this.world != null) {
                        PersistentMap.this.newWorldStuff();
                    }
                }
            };
            pauseForSubworldNamesThread.start();
        }
    }
    
    private void newWorldStuff() {
        final String worldName = TextUtils.scrubNameFile(this.master.getWaypointManager().getCurrentWorldName());
        final File oldCacheDir = new File(Minecraft.getInstance().gameDirectory, "/mods/mamiyaotaru/voxelmap/cache/" + worldName + "/");
        if (oldCacheDir.exists() && oldCacheDir.isDirectory()) {
            final File newCacheDir = new File(Minecraft.getInstance().gameDirectory, "/voxelmap/cache/" + worldName + "/");
            newCacheDir.getParentFile().mkdirs();
            final boolean success = oldCacheDir.renameTo(newCacheDir);
            if (!success) {
                System.out.println("Failed moving Voxelmap cache files.  Please move " + oldCacheDir.getPath() + " to " + newCacheDir.getPath());
            }
            else {
                System.out.println("Moved Voxelmap cache files from " + oldCacheDir.getPath() + " to " + newCacheDir.getPath());
            }
        }
        if (this.master.getWaypointManager().isMultiworld() && !Minecraft.getInstance().isLocalServer() && !this.master.getWaypointManager().receivedAutoSubworldName()) {
            (this.worldMatcher = new WorldMatcher(this.master, this, this.world)).findMatch();
        }
        this.chunkCache = new MapChunkCache(33, 33, this);
    }
    
    @Override
    public void onTick(final Minecraft mc) {
        if (mc.screen == null) {
            this.options.mapX = GameVariableAccessShim.xCoord();
            this.options.mapZ = GameVariableAccessShim.zCoord();
        }
        if (!this.master.getWaypointManager().getCurrentSubworldDescriptor(false).equals(this.subworldName)) {
            this.subworldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(false);
            if (this.worldMatcher != null && !this.subworldName.equals("")) {
                this.worldMatcher.cancel();
            }
            this.purgeCachedRegions();
        }
        if (this.queuedChangedChunks) {
            this.queuedChangedChunks = false;
            this.prunePool();
        }
        if (this.world != null) {
            this.chunkCache.centerChunks(this.blockPos.withXYZ(GameVariableAccessShim.xCoord(), 0, GameVariableAccessShim.zCoord()));
            this.chunkCache.checkIfChunksBecameSurroundedByLoaded();
            while (!this.chunkUpdateQueue.isEmpty() && Math.abs(TickCounter.tickCounter - this.chunkUpdateQueue.peek().tick) >= 20) {
                this.doProcessChunk(this.chunkUpdateQueue.remove().chunk);
            }
        }
    }
    
    @Override
    public PersistentMapSettingsManager getOptions() {
        return this.options;
    }
    
    @Override
    public void purgeCachedRegions() {
        synchronized (this.cachedRegionsPool) {
            for (final CachedRegion cachedRegion : this.cachedRegionsPool) {
                cachedRegion.cleanup();
            }
            this.cachedRegions.clear();
            this.cachedRegionsPool.clear();
            this.getRegions(0, -1, 0, -1);
        }
    }
    
    @Override
    public void saveCachedRegions() {
        synchronized (this.cachedRegionsPool) {
            for (final CachedRegion cachedRegion : this.cachedRegionsPool) {
                cachedRegion.save();
            }
        }
    }
    
    @Override
    public void renameSubworld(final String oldName, final String newName) {
        synchronized (this.cachedRegionsPool) {
            for (final CachedRegion cachedRegion : this.cachedRegionsPool) {
                cachedRegion.renameSubworld(oldName, newName);
            }
        }
    }
    
    @Override
    public ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier() {
        return this.master.getSettingsAndLightingChangeNotifier();
    }
    
    @Override
    public void setLightMapArray(final int[] lightmapColors) {
        boolean changed = false;
        final int torchOffset = 0;
        final int skylightMultiplier = 16;
        for (int t = 0; t < 16; ++t) {
            if (lightmapColors[t * skylightMultiplier + torchOffset] != this.lightmapColors[t * skylightMultiplier + torchOffset]) {
                changed = true;
            }
        }
        System.arraycopy(lightmapColors, 0, this.lightmapColors, 0, 256);
        if (changed) {
            this.getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }
    }
    
    @Override
    public void getAndStoreData(final AbstractMapData mapData, final Level world, final LevelChunk chunk, VoxelMapMutableBlockPos blockPos, final boolean underground, final int startX, final int startZ, final int imageX, final int imageY) {
        int surfaceHeight = 0;
        int seafloorHeight = 0;
        int transparentHeight = 0;
        int foliageHeight = 0;
        BlockState surfaceBlockState = BlockRepository.air.defaultBlockState();
        BlockState transparentBlockState = BlockRepository.air.defaultBlockState();
        BlockState foliageBlockState = BlockRepository.air.defaultBlockState();
        BlockState seafloorBlockState = BlockRepository.air.defaultBlockState();
        blockPos = blockPos.withXYZ(startX + imageX, 0, startZ + imageY);
        int biomeID = 0;
        if (!chunk.isEmpty()) {
            biomeID = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getId(world.getBiome((BlockPos)blockPos));
        }
        else {
            biomeID = -1;
        }
        mapData.setBiomeID(imageX, imageY, biomeID);
        if (biomeID == -1) {
            return;
        }
        boolean solid = false;
        if (underground) {
            surfaceHeight = this.getNetherHeight(chunk, startX + imageX, startZ + imageY);
            surfaceBlockState = chunk.getBlockState((BlockPos)blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
            if (surfaceHeight != -1) {
                foliageHeight = surfaceHeight + 1;
                blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
                foliageBlockState = chunk.getBlockState((BlockPos)blockPos);
                final Material material = foliageBlockState.getMaterial();
                if (material == Material.TOP_SNOW || material == Material.AIR || material == Material.LAVA || material == Material.WATER) {
                    foliageHeight = 0;
                }
            }
        }
        else {
            transparentHeight = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX() & 0xF, blockPos.getZ() & 0xF) + 1;
            transparentBlockState = chunk.getBlockState((BlockPos)blockPos.withXYZ(startX + imageX, transparentHeight - 1, startZ + imageY));
            FluidState fluidState = transparentBlockState.getFluidState();
            if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                transparentBlockState = fluidState.createLegacyBlock();
            }
            surfaceHeight = transparentHeight;
            surfaceBlockState = transparentBlockState;
            VoxelShape voxelShape = null;
            boolean hasOpacity = surfaceBlockState.getLightBlock((BlockGetter)world, (BlockPos)blockPos) > 0;
            if (!hasOpacity && surfaceBlockState.canOcclude() && surfaceBlockState.useShapeForLightOcclusion()) {
                voxelShape = surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)blockPos, Direction.DOWN);
                hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                voxelShape = surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)blockPos, Direction.UP);
                hasOpacity = (hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape));
            }
            while (!hasOpacity && surfaceHeight > 0) {
                foliageBlockState = surfaceBlockState;
                --surfaceHeight;
                surfaceBlockState = chunk.getBlockState((BlockPos)blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                fluidState = surfaceBlockState.getFluidState();
                if (fluidState != Fluids.EMPTY.defaultFluidState()) {
                    surfaceBlockState = fluidState.createLegacyBlock();
                }
                hasOpacity = (surfaceBlockState.getLightBlock((BlockGetter)world, (BlockPos)blockPos) > 0);
                if (!hasOpacity && surfaceBlockState.canOcclude() && surfaceBlockState.useShapeForLightOcclusion()) {
                    voxelShape = surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)blockPos, Direction.DOWN);
                    hasOpacity = Shapes.faceShapeOccludes(voxelShape, Shapes.empty());
                    voxelShape = surfaceBlockState.getFaceOcclusionShape((BlockGetter)world, (BlockPos)blockPos, Direction.UP);
                    hasOpacity = (hasOpacity || Shapes.faceShapeOccludes(Shapes.empty(), voxelShape));
                }
            }
            if (surfaceHeight == transparentHeight) {
                transparentHeight = 0;
                transparentBlockState = BlockRepository.air.defaultBlockState();
                foliageBlockState = chunk.getBlockState((BlockPos)blockPos.withXYZ(startX + imageX, surfaceHeight, startZ + imageY));
            }
            if (foliageBlockState.getMaterial() == Material.TOP_SNOW) {
                surfaceBlockState = foliageBlockState;
                foliageBlockState = BlockRepository.air.defaultBlockState();
            }
            if (foliageBlockState == transparentBlockState) {
                foliageBlockState = BlockRepository.air.defaultBlockState();
            }
            if (foliageBlockState == null || foliageBlockState.getMaterial() == Material.AIR) {
                foliageHeight = 0;
                foliageBlockState = BlockRepository.air.defaultBlockState();
            }
            else {
                foliageHeight = surfaceHeight + 1;
            }
            Material material2 = surfaceBlockState.getMaterial();
            if (material2 == Material.WATER || material2 == Material.ICE) {
                for (seafloorHeight = surfaceHeight, seafloorBlockState = chunk.getBlockState((BlockPos)blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY)); seafloorBlockState.getLightBlock((BlockGetter)world, (BlockPos)blockPos) < 5 && seafloorBlockState.getMaterial() != Material.LEAVES && seafloorHeight > 1; --seafloorHeight, seafloorBlockState = chunk.getBlockState((BlockPos)blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY))) {
                    material2 = seafloorBlockState.getMaterial();
                    if (transparentHeight == 0 && material2 != Material.ICE && material2 != Material.WATER && material2.blocksMotion()) {
                        transparentHeight = seafloorHeight;
                        transparentBlockState = seafloorBlockState;
                    }
                    if (foliageHeight == 0 && seafloorHeight != transparentHeight && transparentBlockState != seafloorBlockState && material2 != Material.ICE && material2 != Material.WATER && material2 != Material.AIR && material2 != Material.BUBBLE_COLUMN) {
                        foliageHeight = seafloorHeight;
                        foliageBlockState = seafloorBlockState;
                    }
                }
                if (seafloorBlockState.getMaterial() == Material.WATER) {
                    seafloorBlockState = BlockRepository.air.defaultBlockState();
                }
            }
        }
        mapData.setHeight(imageX, imageY, surfaceHeight);
        mapData.setBlockstate(imageX, imageY, surfaceBlockState);
        mapData.setTransparentHeight(imageX, imageY, transparentHeight);
        mapData.setTransparentBlockstate(imageX, imageY, transparentBlockState);
        mapData.setFoliageHeight(imageX, imageY, foliageHeight);
        mapData.setFoliageBlockstate(imageX, imageY, foliageBlockState);
        mapData.setOceanFloorHeight(imageX, imageY, seafloorHeight);
        mapData.setOceanFloorBlockstate(imageX, imageY, seafloorBlockState);
        if (surfaceHeight == -1) {
            surfaceHeight = 80;
            solid = true;
        }
        if (surfaceBlockState.getMaterial() == Material.LAVA) {
            solid = false;
        }
        int light = solid ? 0 : 255;
        if (!solid) {
            light = this.getLight(surfaceBlockState, world, blockPos, startX + imageX, startZ + imageY, surfaceHeight, solid);
        }
        mapData.setLight(imageX, imageY, light);
        int seafloorLight = 0;
        if (seafloorBlockState != null && seafloorBlockState != BlockRepository.air.defaultBlockState()) {
            seafloorLight = 255;
            seafloorLight = this.getLight(seafloorBlockState, world, blockPos, startX + imageX, startZ + imageY, seafloorHeight, solid);
        }
        mapData.setOceanFloorLight(imageX, imageY, seafloorLight);
        int transparentLight = 0;
        if (transparentBlockState != null && transparentBlockState != BlockRepository.air.defaultBlockState()) {
            transparentLight = 255;
            transparentLight = this.getLight(transparentBlockState, world, blockPos, startX + imageX, startZ + imageY, transparentHeight, solid);
        }
        mapData.setTransparentLight(imageX, imageY, transparentLight);
        int foliageLight = 0;
        if (foliageBlockState != null && foliageBlockState != BlockRepository.air.defaultBlockState()) {
            foliageLight = 255;
            foliageLight = this.getLight(foliageBlockState, world, blockPos, startX + imageX, startZ + imageY, foliageHeight, solid);
        }
        mapData.setFoliageLight(imageX, imageY, foliageLight);
    }
    
    private int getNetherHeight(final LevelChunk chunk, final int x, final int z) {
        int y = 80;
        this.blockPos.setXYZ(x, y, z);
        BlockState blockState = chunk.getBlockState((BlockPos)this.blockPos);
        if (blockState.getLightBlock((BlockGetter)this.world, (BlockPos)this.blockPos) == 0 && blockState.getMaterial() != Material.LAVA) {
            while (y > 0) {
                --y;
                this.blockPos.setXYZ(x, y, z);
                blockState = chunk.getBlockState((BlockPos)this.blockPos);
                if (blockState.getLightBlock((BlockGetter)this.world, (BlockPos)this.blockPos) > 0 || blockState.getMaterial() == Material.LAVA) {
                    return y + 1;
                }
            }
            return y;
        }
        while (y <= 90) {
            ++y;
            this.blockPos.setXYZ(x, y, z);
            blockState = chunk.getBlockState((BlockPos)this.blockPos);
            if (blockState.getLightBlock((BlockGetter)this.world, (BlockPos)this.blockPos) == 0 && blockState.getMaterial() != Material.LAVA) {
                return y;
            }
        }
        return -1;
    }
    
    private int getLight(final BlockState blockState, final Level world, final VoxelMapMutableBlockPos blockPos, final int x, final int z, final int height, final boolean solid) {
        int i3 = 255;
        if (solid) {
            i3 = 0;
        }
        else if (blockState.getBlock() != BlockRepository.air) {
            blockPos.setXYZ(x, Math.max(Math.min(height, 255), 0), z);
            int blockLight = world.getBrightness(LightLayer.BLOCK, (BlockPos)blockPos) & 0xF;
            final int skyLight = world.getBrightness(LightLayer.SKY, (BlockPos)blockPos);
            if (blockState.getMaterial() == Material.LAVA || blockState.getBlock() == Blocks.MAGMA_BLOCK) {
                blockLight = 14;
            }
            i3 = blockLight + skyLight * 16;
        }
        return i3;
    }
    
    @Override
    public int getPixelColor(final AbstractMapData mapData, final Level world, VoxelMapMutableBlockPos blockPos, final VoxelMapMutableBlockPos loopBlockPos, final boolean underground, final int multi, final int startX, final int startZ, final int imageX, final int imageY) {
        final int mcX = startX + imageX;
        final int mcZ = startZ + imageY;
        int surfaceHeight = 0;
        int seafloorHeight = -1;
        int transparentHeight = -1;
        int foliageHeight = -1;
        int surfaceColor = 0;
        int seafloorColor = 0;
        int transparentColor = 0;
        int foliageColor = 0;
        blockPos = blockPos.withXYZ(mcX, 0, mcZ);
        BlockState blockState = null;
        int color24 = 0;
        final int biomeID = mapData.getBiomeID(imageX, imageY);
        blockState = mapData.getBlockstate(imageX, imageY);
        if (blockState == null || (blockState.getBlock() == BlockRepository.air && mapData.getLight(imageX, imageY) == 0 && mapData.getHeight(imageX, imageY) == 0) || biomeID == -1 || biomeID == 255) {
            return 0;
        }
        final int biomeOverlay = this.mapOptions.biomeOverlay;
        this.mapOptions.getClass();
        if (biomeOverlay == 1) {
            if (biomeID >= 0) {
                color24 = (BiomeRepository.getBiomeColor(biomeID) | 0xFF000000);
            }
            else {
                color24 = 0;
            }
            color24 = MapUtils.doSlimeAndGrid(color24, mcX, mcZ);
            return color24;
        }
        boolean solid = false;
        int blockStateID = 0;
        surfaceHeight = mapData.getHeight(imageX, imageY);
        blockStateID = BlockRepository.getStateId(blockState);
        if (surfaceHeight == -1 || surfaceHeight == 255) {
            surfaceHeight = 80;
            solid = true;
        }
        blockPos.setXYZ(mcX, surfaceHeight - 1, mcZ);
        if (blockState.getMaterial() == Material.LAVA) {
            solid = false;
        }
        if (this.mapOptions.biomes) {
            surfaceColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
            int tint = -1;
            tint = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
            if (tint != -1) {
                surfaceColor = ColorUtils.colorMultiplier(surfaceColor, tint);
            }
        }
        else {
            surfaceColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
        }
        surfaceColor = this.applyHeight(mapData, surfaceColor, multi, imageX, imageY, surfaceHeight, solid, 1);
        final int light = mapData.getLight(imageX, imageY);
        if (solid) {
            surfaceColor = 0;
        }
        else if (this.mapOptions.lightmap) {
            final int lightValue = this.getLight(light);
            surfaceColor = ColorUtils.colorMultiplier(surfaceColor, lightValue);
        }
        if (this.mapOptions.waterTransparency && !solid) {
            final Material material = blockState.getMaterial();
            if (material == Material.WATER || material == Material.ICE) {
                seafloorHeight = mapData.getOceanFloorHeight(imageX, imageY);
                blockPos.setXYZ(mcX, seafloorHeight - 1, mcZ);
                blockState = mapData.getOceanFloorBlockstate(imageX, imageY);
                if (blockState != null && blockState != BlockRepository.air.defaultBlockState()) {
                    blockStateID = BlockRepository.getStateId(blockState);
                    if (this.mapOptions.biomes) {
                        seafloorColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
                        int tint2 = -1;
                        tint2 = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
                        if (tint2 != -1) {
                            seafloorColor = ColorUtils.colorMultiplier(seafloorColor, tint2);
                        }
                    }
                    else {
                        seafloorColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
                    }
                    seafloorColor = this.applyHeight(mapData, seafloorColor, multi, imageX, imageY, seafloorHeight, solid, 0);
                    int seafloorLight = 255;
                    seafloorLight = mapData.getOceanFloorLight(imageX, imageY);
                    if (this.mapOptions.lightmap) {
                        final int lightValue2 = this.getLight(seafloorLight);
                        seafloorColor = ColorUtils.colorMultiplier(seafloorColor, lightValue2);
                    }
                }
            }
        }
        if (this.mapOptions.blockTransparency && !solid) {
            transparentHeight = mapData.getTransparentHeight(imageX, imageY);
            blockPos.setXYZ(mcX, transparentHeight - 1, mcZ);
            blockState = mapData.getTransparentBlockstate(imageX, imageY);
            if (blockState != null && blockState != BlockRepository.air.defaultBlockState()) {
                blockStateID = BlockRepository.getStateId(blockState);
                if (this.mapOptions.biomes) {
                    transparentColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
                    int tint3 = -1;
                    tint3 = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
                    if (tint3 != -1) {
                        transparentColor = ColorUtils.colorMultiplier(transparentColor, tint3);
                    }
                }
                else {
                    transparentColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
                }
                transparentColor = this.applyHeight(mapData, transparentColor, multi, imageX, imageY, transparentHeight, solid, 3);
                int transparentLight = 255;
                transparentLight = mapData.getTransparentLight(imageX, imageY);
                if (this.mapOptions.lightmap) {
                    final int lightValue3 = this.getLight(transparentLight);
                    transparentColor = ColorUtils.colorMultiplier(transparentColor, lightValue3);
                }
            }
            foliageHeight = mapData.getFoliageHeight(imageX, imageY);
            blockPos.setXYZ(mcX, foliageHeight - 1, mcZ);
            blockState = mapData.getFoliageBlockstate(imageX, imageY);
            if (blockState != null && blockState != BlockRepository.air.defaultBlockState()) {
                blockStateID = BlockRepository.getStateId(blockState);
                if (this.mapOptions.biomes) {
                    foliageColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
                    int tint3 = -1;
                    tint3 = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
                    if (tint3 != -1) {
                        foliageColor = ColorUtils.colorMultiplier(foliageColor, tint3);
                    }
                }
                else {
                    foliageColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
                }
                foliageColor = this.applyHeight(mapData, foliageColor, multi, imageX, imageY, foliageHeight, solid, 2);
                int foliageLight = 255;
                foliageLight = mapData.getFoliageLight(imageX, imageY);
                if (this.mapOptions.lightmap) {
                    final int lightValue3 = this.getLight(foliageLight);
                    foliageColor = ColorUtils.colorMultiplier(foliageColor, lightValue3);
                }
            }
        }
        if (this.mapOptions.waterTransparency && seafloorHeight > 0) {
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
        final int biomeOverlay2 = this.mapOptions.biomeOverlay;
        this.mapOptions.getClass();
        if (biomeOverlay2 == 2) {
            int bc = 0;
            if (biomeID >= 0) {
                bc = BiomeRepository.getBiomeColor(biomeID);
            }
            bc |= 0x7F000000;
            color24 = ColorUtils.colorAdder(bc, color24);
        }
        color24 = MapUtils.doSlimeAndGrid(color24, mcX, mcZ);
        return color24;
    }
    
    private int applyHeight(final AbstractMapData mapData, int color24, final int multi, final int imageX, final int imageY, final int height, final boolean solid, final int layer) {
        if (color24 != this.colorManager.getAirColor() && color24 != 0) {
            int heightComp = -1;
            if ((this.mapOptions.heightmap || this.mapOptions.slopemap) && !solid) {
                int diff = 0;
                double sc = 0.0;
                boolean invert = false;
                if (this.mapOptions.slopemap) {
                    if (imageX > 0 && imageY < 32 * multi - 1) {
                        if (layer == 0) {
                            heightComp = mapData.getOceanFloorHeight(imageX - 1, imageY + 1);
                        }
                        if (layer == 1) {
                            heightComp = mapData.getHeight(imageX - 1, imageY + 1);
                        }
                        if (layer == 2) {
                            heightComp = height;
                        }
                        if (layer == 3) {
                            heightComp = mapData.getTransparentHeight(imageX - 1, imageY + 1);
                            if (heightComp == -1) {
                                final BlockState transparentBlockState = mapData.getTransparentBlockstate(imageX, imageY);
                                if (transparentBlockState != null && transparentBlockState != BlockRepository.air.defaultBlockState()) {
                                    final Block block = transparentBlockState.getBlock();
                                    if (block instanceof GlassBlock || block instanceof StainedGlassBlock) {
                                        heightComp = mapData.getHeight(imageX - 1, imageY + 1);
                                    }
                                }
                            }
                        }
                    }
                    else if (imageX < 32 * multi - 1 && imageY > 0) {
                        if (layer == 0) {
                            heightComp = mapData.getOceanFloorHeight(imageX + 1, imageY - 1);
                        }
                        if (layer == 1) {
                            heightComp = mapData.getHeight(imageX + 1, imageY - 1);
                        }
                        if (layer == 2) {
                            heightComp = height;
                        }
                        if (layer == 3) {
                            heightComp = mapData.getTransparentHeight(imageX + 1, imageY - 1);
                            if (heightComp == -1) {
                                final BlockState transparentBlockState = mapData.getTransparentBlockstate(imageX, imageY);
                                if (transparentBlockState != null && transparentBlockState != BlockRepository.air.defaultBlockState()) {
                                    final Block block = transparentBlockState.getBlock();
                                    if (block instanceof GlassBlock || block instanceof StainedGlassBlock) {
                                        heightComp = mapData.getHeight(imageX + 1, imageY - 1);
                                    }
                                }
                            }
                        }
                        invert = true;
                    }
                    else {
                        heightComp = height;
                    }
                    if (heightComp == -1) {
                        heightComp = height;
                    }
                    if (!invert) {
                        diff = heightComp - height;
                    }
                    else {
                        diff = height - heightComp;
                    }
                    if (diff != 0) {
                        sc = ((diff > 0) ? 1.0 : ((diff < 0) ? -1.0 : 0.0));
                        sc /= 8.0;
                    }
                    if (this.mapOptions.heightmap) {
                        diff = height - 80;
                        final double heightsc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 3.0;
                        sc = ((diff > 0) ? (sc + heightsc) : (sc - heightsc));
                    }
                }
                else if (this.mapOptions.heightmap) {
                    diff = height - 80;
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
        }
        return color24;
    }
    
    private int getLight(final int light) {
        return this.lightmapColors[light];
    }
    
    @Override
    public CachedRegion[] getRegions(final int left, final int right, final int top, final int bottom) {
        if (left == this.lastLeft && right == this.lastRight && top == this.lastTop && bottom == this.lastBottom) {
            return this.lastRegionsArray;
        }
        ThreadManager.emptyQueue();
        final CachedRegion[] visibleCachedRegionsArray = new CachedRegion[(right - left + 1) * (bottom - top + 1)];
        final String worldName = this.master.getWaypointManager().getCurrentWorldName();
        final String subWorldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(false);
        final ArrayList<RegionCoordinates> regionsToDisplay = new ArrayList<RegionCoordinates>();
        for (int t = left; t <= right; ++t) {
            for (int s = top; s <= bottom; ++s) {
                final RegionCoordinates regionCoordinates = new RegionCoordinates(t, s);
                regionsToDisplay.add(regionCoordinates);
            }
        }
        Collections.sort(regionsToDisplay, this.distanceSorter);
        for (final RegionCoordinates regionCoordinates2 : regionsToDisplay) {
            final int x = regionCoordinates2.x;
            final int z = regionCoordinates2.z;
            final StringBuilder keyBuilder = new StringBuilder("").append(x).append(",").append(z);
            final String key = keyBuilder.toString();
            CachedRegion cachedRegion;
            synchronized (this.cachedRegions) {
                cachedRegion = this.cachedRegions.get(key);
                if (cachedRegion == null) {
                    cachedRegion = new CachedRegion(this, key, this.world, worldName, subWorldName, x, z);
                    this.cachedRegions.put(key, cachedRegion);
                    synchronized (this.cachedRegionsPool) {
                        this.cachedRegionsPool.add(cachedRegion);
                    }
                }
            }
            cachedRegion.refresh(true);
            visibleCachedRegionsArray[(z - top) * (right - left + 1) + (x - left)] = cachedRegion;
        }
        this.prunePool();
        synchronized (this.lastRegionsArray) {
            this.lastLeft = left;
            this.lastRight = right;
            this.lastTop = top;
            this.lastBottom = bottom;
            this.lastRegionsArray = visibleCachedRegionsArray;
        }
        return visibleCachedRegionsArray;
    }
    
    private void prunePool() {
        synchronized (this.cachedRegionsPool) {
            final Iterator<CachedRegion> iterator = this.cachedRegionsPool.iterator();
            while (iterator.hasNext()) {
                final CachedRegion region = iterator.next();
                if (region.isLoaded() && region.isEmpty()) {
                    this.cachedRegions.put(region.getKey(), CachedRegion.emptyRegion);
                    region.cleanup();
                    iterator.remove();
                }
            }
            if (this.cachedRegionsPool.size() > this.options.cacheSize) {
                Collections.sort(this.cachedRegionsPool, this.ageThenDistanceSorter);
                final List<CachedRegion> toRemove = this.cachedRegionsPool.subList(this.options.cacheSize, this.cachedRegionsPool.size());
                for (final CachedRegion cachedRegion : toRemove) {
                    this.cachedRegions.remove(cachedRegion.getKey());
                    cachedRegion.cleanup();
                }
                toRemove.clear();
            }
            this.compress();
        }
    }
    
    @Override
    public void compress() {
        synchronized (this.cachedRegionsPool) {
            for (final CachedRegion cachedRegion : this.cachedRegionsPool) {
                if (System.currentTimeMillis() - cachedRegion.getMostRecentChange() > 5000L) {
                    cachedRegion.compress();
                }
            }
        }
    }
    
    @Override
    public void handleChangeInWorld(final int chunkX, final int chunkZ) {
        if (this.world == null) {
            return;
        }
        final LevelChunk chunk = this.world.getChunk(chunkX, chunkZ);
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        if (this.isChunkReady(this.world, chunk)) {
            this.processChunk(chunk);
        }
    }
    
    @Override
    public void processChunk(final LevelChunk chunk) {
        this.chunkUpdateQueue.add(new ChunkWithAge(chunk, TickCounter.tickCounter));
    }
    
    private void doProcessChunk(final LevelChunk chunk) {
        this.queuedChangedChunks = true;
        try {
            if (this.world == null) {
                return;
            }
            if (chunk == null || chunk.isEmpty()) {
                return;
            }
            final int chunkX = chunk.getPos().x;
            final int chunkZ = chunk.getPos().z;
            final int regionX = (int)Math.floor(chunkX / 16.0);
            final int regionZ = (int)Math.floor(chunkZ / 16.0);
            final StringBuilder keyBuilder = new StringBuilder("").append(regionX).append(",").append(regionZ);
            final String key = keyBuilder.toString();
            CachedRegion cachedRegion;
            synchronized (this.cachedRegions) {
                cachedRegion = this.cachedRegions.get(key);
                if (cachedRegion == null || cachedRegion == CachedRegion.emptyRegion) {
                    final String worldName = this.master.getWaypointManager().getCurrentWorldName();
                    final String subWorldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(false);
                    cachedRegion = new CachedRegion(this, key, this.world, worldName, subWorldName, regionX, regionZ);
                    this.cachedRegions.put(key, cachedRegion);
                    synchronized (this.cachedRegionsPool) {
                        this.cachedRegionsPool.add(cachedRegion);
                    }
                    synchronized (this.lastRegionsArray) {
                        if (regionX >= this.lastLeft && regionX <= this.lastRight && regionZ >= this.lastTop && regionZ <= this.lastBottom) {
                            this.lastRegionsArray[(regionZ - this.lastTop) * (this.lastRight - this.lastLeft + 1) + (regionX - this.lastLeft)] = cachedRegion;
                        }
                    }
                }
            }
            if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof GuiPersistentMap) {
                cachedRegion.registerChangeAt(chunkX, chunkZ);
                cachedRegion.refresh(false);
            }
            else {
                cachedRegion.handleChangedChunk(chunk);
            }
        }
        catch (final Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean isChunkReady(final ClientLevel world, final LevelChunk chunk) {
        return this.chunkCache.isChunkSurroundedByLoaded(chunk.getPos().x, chunk.getPos().z);
    }
    
    @Override
    public boolean isRegionLoaded(final int blockX, final int blockZ) {
        final int x = (int)Math.floor(blockX / 256.0f);
        final int z = (int)Math.floor(blockZ / 256.0f);
        final CachedRegion cachedRegion = this.cachedRegions.get("" + x + "," + z);
        return cachedRegion != null && cachedRegion.isLoaded();
    }
    
    @Override
    public boolean isGroundAt(final int blockX, final int blockZ) {
        final int x = (int)Math.floor(blockX / 256.0f);
        final int z = (int)Math.floor(blockZ / 256.0f);
        final CachedRegion cachedRegion = this.cachedRegions.get("" + x + "," + z);
        return cachedRegion != null && cachedRegion.isGroundAt(blockX, blockZ);
    }
    
    @Override
    public int getHeightAt(final int blockX, final int blockZ) {
        final int x = (int)Math.floor(blockX / 256.0f);
        final int z = (int)Math.floor(blockZ / 256.0f);
        final CachedRegion cachedRegion = this.cachedRegions.get("" + x + "," + z);
        if (cachedRegion == null) {
            return 64;
        }
        return cachedRegion.getHeightAt(blockX, blockZ);
    }
    
    private class RegionCoordinates
    {
        int x;
        int z;
        
        public RegionCoordinates(final int x, final int z) {
            this.x = x;
            this.z = z;
        }
    }
    
    private class ChunkWithAge
    {
        LevelChunk chunk;
        int tick;
        
        public ChunkWithAge(final LevelChunk chunk, final int tick) {
            this.chunk = chunk;
            this.tick = tick;
        }
    }
}
