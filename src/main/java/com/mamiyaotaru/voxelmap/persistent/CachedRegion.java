// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import com.google.common.collect.BiMap;
import java.util.Properties;
import com.mamiyaotaru.voxelmap.util.BlockStateParser;
import java.util.Scanner;
import com.google.common.collect.HashBiMap;
import java.util.zip.ZipFile;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import java.io.File;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeNotifier;
import java.util.Arrays;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mamiyaotaru.voxelmap.util.VoxelMapMutableBlockPos;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Future;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeListener;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;

public class CachedRegion implements IThreadCompleteListener, ISettingsAndLightingChangeListener
{
	private final Minecraft game = Minecraft.getInstance();
    public static EmptyCachedRegion emptyRegion;
    private long mostRecentView;
    private long mostRecentChange;
    private IPersistentMap persistentMap;
    private String key;
    private Level world;
    private ServerLevel worldServer;
    private ServerChunkCache chunkProvider;
    Class<?> executorClass;
    private BlockableEventLoop<Runnable> executor;
    private ChunkMap chunkLoader;
    private String subworldName;
    private String worldNamePathPart;
    private String subworldNamePathPart;
    private String dimensionNamePathPart;
    private boolean underground;
    private int x;
    private int z;
    private int width;
    private boolean empty;
    private boolean liveChunksUpdated;
    boolean remoteWorld;
    private boolean[] liveChunkUpdateQueued;
    private boolean[] chunkUpdateQueued;
    private CompressibleGLBufferedImage image;
    private CompressibleMapData data;
    VoxelMapMutableBlockPos blockPos;
    VoxelMapMutableBlockPos loopBlockPos;
    Future<?> future;
    private ReentrantLock threadLock;
    boolean displayOptionsChanged;
    boolean imageChanged;
    boolean refreshQueued;
    boolean refreshingImage;
    boolean dataUpdated;
    boolean dataUpdateQueued;
    boolean loaded;
    boolean closed;
    private static final Object anvilLock;
    private static final ReadWriteLock tickLock;
    private static int loadedChunkCount;
    private static boolean debug;
    private boolean queuedToCompress;
    
    public CachedRegion() {
        this.mostRecentView = 0L;
        this.mostRecentChange = 0L;
        this.subworldNamePathPart = "";
        this.underground = false;
        this.width = 256;
        this.empty = true;
        this.liveChunksUpdated = false;
        this.liveChunkUpdateQueued = new boolean[256];
        this.chunkUpdateQueued = new boolean[256];
        this.blockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.loopBlockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.future = null;
        this.threadLock = new ReentrantLock();
        this.displayOptionsChanged = false;
        this.imageChanged = false;
        this.refreshQueued = false;
        this.refreshingImage = false;
        this.dataUpdated = false;
        this.dataUpdateQueued = false;
        this.loaded = false;
        this.closed = false;
        this.queuedToCompress = false;
    }
    
    @SuppressWarnings("unchecked")
	public CachedRegion(final IPersistentMap persistentMap, final String key, final ClientLevel world, final String worldName, final String subworldName, final int x, final int z) {
        this.mostRecentView = 0L;
        this.mostRecentChange = 0L;
        this.subworldNamePathPart = "";
        this.underground = false;
        this.width = 256;
        this.empty = true;
        this.liveChunksUpdated = false;
        this.liveChunkUpdateQueued = new boolean[256];
        this.chunkUpdateQueued = new boolean[256];
        this.blockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.loopBlockPos = new VoxelMapMutableBlockPos(0, 0, 0);
        this.future = null;
        this.threadLock = new ReentrantLock();
        this.displayOptionsChanged = false;
        this.imageChanged = false;
        this.refreshQueued = false;
        this.refreshingImage = false;
        this.dataUpdated = false;
        this.dataUpdateQueued = false;
        this.loaded = false;
        this.closed = false;
        this.queuedToCompress = false;
        this.persistentMap = persistentMap;
        this.key = key;
        this.world = world;
        this.subworldName = subworldName;
        this.worldNamePathPart = TextUtils.scrubNameFile(worldName);
        if (subworldName != "") {
            this.subworldNamePathPart = TextUtils.scrubNameFile(subworldName) + "/";
        }
        final String dimensionName = AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(world).getStorageName();
        this.dimensionNamePathPart = TextUtils.scrubNameFile(dimensionName);
        boolean knownUnderground = false;
        knownUnderground = (knownUnderground || dimensionName.toLowerCase().contains("erebus"));
        this.underground = ((!world.effects().forceBrightLightmap() && !world.dimensionType().hasSkyLight()) || world.dimensionType().hasCeiling() || knownUnderground);
        this.remoteWorld = !Minecraft.getInstance().isLocalServer();
        persistentMap.getSettingsAndLightingChangeNotifier().addObserver(this);
        this.x = x;
        this.z = z;
        if (!this.remoteWorld) {
            this.worldServer = Minecraft.getInstance().getSingleplayerServer().getLevel(world.dimension());
            this.chunkProvider = this.worldServer.getChunkSource();
            this.executorClass = this.chunkProvider.getClass().getDeclaredClasses()[0];
            this.executor = (BlockableEventLoop<Runnable>) ReflectionUtils.getPrivateFieldValueByType(this.chunkProvider, ServerChunkCache.class, this.executorClass);
            this.chunkLoader = this.chunkProvider.chunkMap;
        }
        Arrays.fill(this.liveChunkUpdateQueued, false);
        Arrays.fill(this.chunkUpdateQueued, false);
    }
    
    public void renameSubworld(final String oldName, final String newName) {
        if (oldName.equals(this.subworldName)) {
            this.closed = true;
            this.threadLock.lock();
            try {
                this.subworldName = newName;
                if (this.subworldName != "") {
                    this.subworldNamePathPart = TextUtils.scrubNameFile(this.subworldName) + "/";
                }
            }
            catch (final Exception ex) {}
            finally {
                this.threadLock.unlock();
                this.closed = false;
            }
        }
    }
    
    public void registerChangeAt(int chunkX, int chunkZ) {
        chunkX -= this.x * 16;
        chunkZ -= this.z * 16;
        this.dataUpdateQueued = true;
        final int index = chunkZ * 16 + chunkX;
        this.liveChunkUpdateQueued[index] = true;
    }
    
    @Override
    public void notifyOfActionableChange(final ISettingsAndLightingChangeNotifier notifier) {
        this.displayOptionsChanged = true;
    }
    
    public void refresh(final boolean forceCompress) {
        this.mostRecentView = System.currentTimeMillis();
        if (this.future != null && (this.future.isDone() || this.future.isCancelled())) {
            this.refreshQueued = false;
        }
        if (this.refreshQueued) {
            return;
        }
        this.refreshQueued = true;
        if (!this.loaded || this.dataUpdated || this.dataUpdateQueued || this.displayOptionsChanged) {
            final RefreshRunnable regionProcessingRunnable = new RefreshRunnable(forceCompress);
            this.future = ThreadManager.executorService.submit(regionProcessingRunnable);
        }
        else {
            this.refreshQueued = false;
        }
    }
    
    public void handleChangedChunk(final LevelChunk chunk) {
        final int chunkX = chunk.getPos().x - this.x * 16;
        final int chunkZ = chunk.getPos().z - this.z * 16;
        final int index = chunkZ * 16 + chunkX;
        if (this.chunkUpdateQueued[index]) {
            return;
        }
        this.chunkUpdateQueued[index] = true;
        this.mostRecentView = System.currentTimeMillis();
        this.mostRecentChange = this.mostRecentView;
        final FillChunkRunnable fillChunkRunnable = new FillChunkRunnable(chunk);
        ThreadManager.executorService.execute(fillChunkRunnable);
    }
    
    @Override
    public void notifyOfThreadComplete(final AbstractNotifyingRunnable runnable) {
    }
    
    private void load() {
        this.data = new CompressibleMapData(256, 256);
        this.image = new CompressibleGLBufferedImage(256, 256, 6);
        this.loadCachedData();
        this.loadCurrentData(this.world);
        if (!this.remoteWorld) {
            this.loadAnvilData();
        }
        this.loaded = true;
    }
    
    private void loadCurrentData(final Level world) {
        for (int chunkX = 0; chunkX < 16; ++chunkX) {
            for (int chunkZ = 0; chunkZ < 16; ++chunkZ) {
                final LevelChunk chunk = world.getChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ);
                if (chunk != null && !chunk.isEmpty() && world.hasChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ)) {
                    this.loadChunkData(chunk, chunkX, chunkZ);
                }
            }
        }
    }
    
    private void loadModifiedData() {
        for (int chunkX = 0; chunkX < 16; ++chunkX) {
            for (int chunkZ = 0; chunkZ < 16; ++chunkZ) {
                if (this.liveChunkUpdateQueued[chunkZ * 16 + chunkX]) {
                    final LevelChunk chunk = this.world.getChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ);
                    if (chunk != null && !chunk.isEmpty() && this.world.hasChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ)) {
                        this.loadChunkData(chunk, chunkX, chunkZ);
                    }
                    this.liveChunkUpdateQueued[chunkZ * 16 + chunkX] = false;
                }
            }
        }
    }
    
    private void loadChunkData(final LevelChunk chunk, final int chunkX, final int chunkZ) {
        final boolean isEmpty = this.closed || this.isChunkEmptyOrUnlit(chunk);
        final boolean isSurroundedByLoaded = this.isSurroundedByLoaded(chunk);
        if (!this.closed && this.world == GameVariableAccessShim.getWorld() && !isEmpty && isSurroundedByLoaded) {
            this.doLoadChunkData(chunk, chunkX, chunkZ);
        }
    }
    
    private void loadChunkDataSkipLightCheck(final LevelChunk chunk, final int chunkX, final int chunkZ) {
        if (!this.closed && this.world == GameVariableAccessShim.getWorld() && !this.isChunkEmpty(chunk)) {
            this.doLoadChunkData(chunk, chunkX, chunkZ);
        }
    }
    
    private void doLoadChunkData(final LevelChunk chunk, final int chunkX, final int chunkZ) {
        for (int t = 0; t < 16; ++t) {
            for (int s = 0; s < 16; ++s) {
                this.persistentMap.getAndStoreData(this.data, chunk.getLevel(), chunk, this.blockPos, this.underground, this.x * 256, this.z * 256, chunkX * 16 + t, chunkZ * 16 + s);
            }
        }
        this.empty = false;
        this.liveChunksUpdated = true;
        this.dataUpdated = true;
    }
    
    private boolean isChunkEmptyOrUnlit(final LevelChunk chunk) {
        if (this.closed || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
            return true;
        }
        final boolean overworld = this.world.dimensionType().hasSkyLight();
        for (int t = 0; t < 16; ++t) {
            for (int s = 0; s < 16; ++s) {
                if (overworld) {
                    if (chunk.getLevel().getBrightness(LightLayer.SKY, (BlockPos)this.blockPos.withXYZ(chunk.getPos().x * 16 + t, chunk.getHighestSectionPosition() + 15, chunk.getPos().z * 16 + s)) != 0) {
                        return false;
                    }
                }
                else if (chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, t, s) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isChunkEmpty(final LevelChunk chunk) {
        if (this.closed || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
            return true;
        }
        for (int t = 0; t < 16; ++t) {
            for (int s = 0; s < 16; ++s) {
                if (chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, t, s) != 0) {
                    return false;
                }
                if (chunk.getLevel().getBrightness(LightLayer.SKY, (BlockPos)this.blockPos.withXYZ(chunk.getPos().x * 16 + t, chunk.getHighestSectionPosition() + 15, chunk.getPos().z * 16 + s)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean isSurroundedByLoaded(final LevelChunk chunk) {
        final int chunkX = chunk.getPos().x;
        final int chunkZ = chunk.getPos().z;
        boolean neighborsLoaded = chunk != null && !chunk.isEmpty() && Minecraft.getInstance().level.hasChunk(chunkX, chunkZ);
        for (int t = chunkX - 1; t <= chunkX + 1 && neighborsLoaded; ++t) {
            LevelChunk neighborChunk;
            for (int s = chunkZ - 1; s <= chunkZ + 1 && neighborsLoaded; neighborsLoaded = (neighborsLoaded && neighborChunk != null && !neighborChunk.isEmpty() && Minecraft.getInstance().level.hasChunk(t, s)), ++s) {
                neighborChunk = game.level.getChunk(t, s);
            }
        }
        return neighborsLoaded;
    }
    
    private void loadAnvilData() {
        if (this.remoteWorld) {
            return;
        }
        boolean full = true;
        for (int t = 0; t < 16; ++t) {
            for (int s = 0; s < 16; ++s) {
                if (!this.closed && this.data.getHeight(t * 16, s * 16) == 0 && this.data.getLight(t * 16, s * 16) == 0) {
                    full = false;
                }
            }
        }
        if (this.closed || full) {
            return;
        }
        final File directory = new File(DimensionType.getStorageFolder(this.worldServer.dimension(), this.worldServer.getServer().getWorldPath(LevelResource.ROOT).normalize()).toFile(), "region");
        final File regionFile = new File(directory, "r." + (int)Math.floor(this.x / 2) + "." + (int)Math.floor(this.z / 2) + ".mca");
        if (!regionFile.exists()) {
            return;
        }
        boolean dataChanged = false;
        boolean loadedChunks = false;
        final LevelChunk[] chunks = new LevelChunk[256];
        Arrays.fill(chunks, null);
        CachedRegion.tickLock.readLock().lock();
        try {
            synchronized (CachedRegion.anvilLock) {
                if (CachedRegion.debug) {
                    System.out.println(Thread.currentThread().getName() + " starting load");
                }
                final long loadTime = System.currentTimeMillis();
                final CompletableFuture<Void> loadFuture = CompletableFuture.runAsync(() -> {
                    for (int t3 = 0; t3 < 16; ++t3) {
                        for (int s3 = 0; s3 < 16; ++s3) {
                            if (!this.closed && this.data.getHeight(t3 * 16, s3 * 16) == 0 && this.data.getLight(t3 * 16, s3 * 16) == 0) {
                                try {
                                    final int index2 = t3 + s3 * 16;
                                    final ChunkPos chunkPos = new ChunkPos(this.x * 16 + t3, this.z * 16 + s3);
                                    final CompoundTag rawNbt = this.chunkLoader.read(chunkPos);
                                    if (rawNbt != null) {
                                        final CompoundTag nbt = this.chunkLoader.upgradeChunkTag(this.worldServer.dimension(), () -> this.worldServer.getDataStorage(), rawNbt, null);
                                        if (!this.closed && nbt.contains("Level", 10)) {
                                            final CompoundTag level = nbt.getCompound("Level");
                                            final int chunkX = level.getInt("xPos");
                                            final int chunkZ = level.getInt("zPos");
                                            if (chunkPos.x == chunkX && chunkPos.z == chunkZ) {
                                                if (level.contains("Status", 8) && ChunkStatus.byName(level.getString("Status")).isOrAfter(ChunkStatus.SPAWN)) {
                                                    if (!(!level.contains("Sections"))) {
                                                        final ListTag sections = level.getList("Sections", 10);
                                                        if (!sections.isEmpty() && sections.size() != 0) {
                                                            boolean hasInfo = false;
                                                            for (int i = 0; i < sections.size() && !hasInfo && !this.closed; ++i) {
                                                                final CompoundTag section = sections.getCompound(i);
                                                                if (section.contains("Palette", 9) && section.contains("BlockStates", 12)) {
                                                                    hasInfo = true;
                                                                }
                                                            }
                                                            if (!(!hasInfo)) {
                                                                if (!level.contains("isLightOn") || !level.getBoolean("isLightOn")) {
                                                                }
                                                                if (level.contains("LightPopulated")) {
                                                                }
                                                                if (!nbt.contains("DataVersion") || nbt.getInt("DataVersion") < 1900) {
                                                                }
                                                                chunks[index2] = this.worldServer.getChunk(chunkPos.x, chunkPos.z);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                catch (final IOException e2) {
                                    System.out.println("failed checking NBT while loading from anvil: " + e2.getMessage());
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                    return;
                }, (Executor)this.executor);
                while (!this.closed && !loadFuture.isDone()) {
                    try {
                        Thread.sleep(3L);
                    }
                    catch (final InterruptedException ex) {}
                }
                loadFuture.cancel(false);
                if (CachedRegion.debug) {
                    System.out.println(Thread.currentThread().getName() + " finished load after " + (System.currentTimeMillis() - loadTime) + " milliseconds");
                }
            }
            if (CachedRegion.debug) {
                System.out.println(Thread.currentThread().getName() + " starting calculation");
            }
            final long calcTime = System.currentTimeMillis();
            for (int t2 = 0; t2 < 16; ++t2) {
                for (int s2 = 0; s2 < 16; ++s2) {
                    final int index = t2 + s2 * 16;
                    if (!this.closed && chunks[index] != null) {
                        loadedChunks = true;
                        ++CachedRegion.loadedChunkCount;
                        LevelChunk loadedChunk = null;
                        if (chunks[index] instanceof LevelChunk) {
                            loadedChunk = chunks[index];
                        }
                        else {
                            System.out.println("non world chunk at " + chunks[index].getPos().x + "," + chunks[index].getPos().z);
                        }
                        if (!this.closed && loadedChunk != null && loadedChunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                            final CompletableFuture<ChunkAccess> lightFuture = this.chunkProvider.getLightEngine().lightChunk(loadedChunk, false);
                            while (!this.closed && !lightFuture.isDone()) {
                                try {
                                    Thread.sleep(3L);
                                }
                                catch (final InterruptedException ex2) {}
                            }
                            loadedChunk = (LevelChunk) lightFuture.getNow(loadedChunk);
                            lightFuture.cancel(false);
                        }
                        if (!this.closed && loadedChunk != null && loadedChunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                            this.loadChunkDataSkipLightCheck(loadedChunk, t2, s2);
                            dataChanged = true;
                        }
                    }
                }
            }
            if (CachedRegion.debug) {
                System.out.println(Thread.currentThread().getName() + " finished calculating after " + (System.currentTimeMillis() - calcTime) + " milliseconds");
            }
        }
        catch (final Exception e) {
            System.out.println("error in anvil loading");
        }
        finally {
            CachedRegion.tickLock.readLock().unlock();
        }
        if (!this.closed && dataChanged) {
            this.saveData(false);
        }
        if (!this.closed && loadedChunks && CachedRegion.loadedChunkCount > 4096) {
            CachedRegion.loadedChunkCount = 0;
            CachedRegion.tickLock.writeLock().lock();
            try {
                final CompletableFuture<Void> tickFuture = CompletableFuture.runAsync(() -> this.chunkProvider.tick(() -> true, true), (Executor)this.executor);
                final long tickTime = System.currentTimeMillis();
                if (CachedRegion.debug) {
                    System.out.println(Thread.currentThread().getName() + " starting chunk GC tick");
                }
                while (!this.closed && !tickFuture.isDone()) {
                    try {
                        Thread.sleep(3L);
                    }
                    catch (final InterruptedException ex3) {}
                }
                if (CachedRegion.debug) {
                    System.out.println(Thread.currentThread().getName() + " finished chunk GC tick after " + (System.currentTimeMillis() - tickTime) + " milliseconds");
                }
            }
            catch (final Exception e) {
                System.out.println("error ticking from anvil loading");
            }
            finally {
                CachedRegion.tickLock.writeLock().unlock();
            }
        }
    }
    
    private void loadCachedData() {
        try {
            final File cachedRegionFileDir = new File(game.gameDirectory, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
            cachedRegionFileDir.mkdirs();
            final File cachedRegionFile = new File(cachedRegionFileDir, "/" + this.key + ".zip");
            if (cachedRegionFile.exists()) {
                final ZipFile zFile = new ZipFile(cachedRegionFile);
                BiMap<BlockState, Integer> stateToInt = null;
                int total = 0;
                final byte[] decompressedByteData = new byte[this.data.getWidth() * this.data.getHeight() * 17 * 4];
                ZipEntry ze = zFile.getEntry("data");
                InputStream is = zFile.getInputStream(ze);
                int count;
                for (byte[] byteData = new byte[2048]; (count = is.read(byteData, 0, 2048)) != -1 && count + total <= this.data.getWidth() * this.data.getHeight() * 17 * 4; total += count) {
                    System.arraycopy(byteData, 0, decompressedByteData, total, count);
                }
                is.close();
                ze = zFile.getEntry("key");
                is = zFile.getInputStream(ze);
                stateToInt = HashBiMap.create();
                final Scanner sc = new Scanner(is);
                while (sc.hasNextLine()) {
                    BlockStateParser.parseLine(sc.nextLine(), stateToInt);
                }
                sc.close();
                is.close();
                int version = 1;
                ze = zFile.getEntry("control");
                if (ze != null) {
                    is = zFile.getInputStream(ze);
                    if (is != null) {
                        final Properties properties = new Properties();
                        properties.load(is);
                        final String versionString = properties.getProperty("version", "1");
                        try {
                            version = Integer.parseInt(versionString);
                        }
                        catch (final NumberFormatException e) {
                            version = 1;
                        }
                        is.close();
                    }
                }
                zFile.close();
                if (total == this.data.getWidth() * this.data.getHeight() * 18 && stateToInt != null) {
                    final byte[] byteData = new byte[this.data.getWidth() * this.data.getHeight() * 18];
                    System.arraycopy(decompressedByteData, 0, byteData, 0, byteData.length);
                    this.data.setData(byteData, stateToInt, version);
                    this.empty = false;
                    this.dataUpdated = true;
                }
                else {
                    System.out.println("failed to load data from " + cachedRegionFile.getPath());
                }
                if (stateToInt == null || version < 2) {
                    this.liveChunksUpdated = true;
                }
            }
        }
        catch (final Exception e2) {
            System.err.println("Failed to load region file for " + this.x + "," + this.z + " in " + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
            e2.printStackTrace();
        }
    }
    
    private void saveData(final boolean newThread) {
        if (this.liveChunksUpdated && !this.worldNamePathPart.equals("")) {
            if (newThread) {
                ThreadManager.executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        CachedRegion.this.threadLock.lock();
                        try {
                            CachedRegion.this.doSave();
                        }
                        catch (final IOException e) {
                            System.err.println("Failed to save region file for " + CachedRegion.this.x + "," + CachedRegion.this.z + " in " + CachedRegion.this.worldNamePathPart + "/" + CachedRegion.this.subworldNamePathPart + CachedRegion.this.dimensionNamePathPart);
                            e.printStackTrace();
                        }
                        finally {
                            CachedRegion.this.threadLock.unlock();
                        }
                    }
                });
            }
            else {
                try {
                    this.doSave();
                }
                catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            this.liveChunksUpdated = false;
        }
    }
    
    private void doSave() throws IOException {
        final BiMap<BlockState, Integer> stateToInt = this.data.getStateToInt();
        final byte[] byteArray = this.data.getData();
        final int length = byteArray.length;
        final int n = this.data.getWidth() * this.data.getHeight();
        if (length == n * 18) {
            final File cachedRegionFileDir = new File(game.gameDirectory, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
            cachedRegionFileDir.mkdirs();
            final File cachedRegionFile = new File(cachedRegionFileDir, "/" + this.key + ".zip");
            final FileOutputStream fos = new FileOutputStream(cachedRegionFile);
            final ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry("data");
            ze.setSize(byteArray.length);
            zos.putNextEntry(ze);
            zos.write(byteArray);
            zos.closeEntry();
            if (stateToInt != null) {
                final Iterator<Map.Entry<BlockState, Integer>> iterator = stateToInt.entrySet().iterator();
                final StringBuffer stringBuffer = new StringBuffer();
                while (iterator.hasNext()) {
                    final Map.Entry<BlockState, Integer> entry = iterator.next();
                    final String nextLine = entry.getValue() + " " + entry.getKey().toString() + "\r\n";
                    stringBuffer.append(nextLine);
                }
                final byte[] keyByteArray = String.valueOf(stringBuffer).getBytes();
                ze = new ZipEntry("key");
                ze.setSize(keyByteArray.length);
                zos.putNextEntry(ze);
                zos.write(keyByteArray);
                zos.closeEntry();
            }
            final StringBuffer stringBuffer2 = new StringBuffer();
            final String nextLine2 = "version:2\r\n";
            stringBuffer2.append(nextLine2);
            final byte[] keyByteArray = String.valueOf(stringBuffer2).getBytes();
            ze = new ZipEntry("control");
            ze.setSize(keyByteArray.length);
            zos.putNextEntry(ze);
            zos.write(keyByteArray);
            zos.closeEntry();
            zos.close();
            fos.close();
        }
        else {
            System.err.println("Data array wrong size: " + byteArray.length + "for " + this.x + "," + this.z + " in " + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
        }
    }
    
    private void fillImage() {
        int color24 = 0;
        for (int t = 0; t < 256; ++t) {
            for (int s = 0; s < 256; ++s) {
            	color24 = this.persistentMap.getPixelColor(this.data, this.world, this.blockPos, this.loopBlockPos, this.underground, 8, this.x * 256, this.z * 256, t, s);
                this.image.setRGB(t, s, color24);
            }
        }
        this.saveImage();
    }
    
    private void saveImage() {
        if (!this.empty) {
            final File imageFileDir = new File(game.gameDirectory, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart + "/images/z1");
            imageFileDir.mkdirs();
            final File imageFile = new File(imageFileDir, this.key + ".png");
            if (this.liveChunksUpdated || !imageFile.exists()) {
                ThreadManager.executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        CachedRegion.this.threadLock.lock();
                        try {
                            final BufferedImage realBufferedImage = new BufferedImage(CachedRegion.this.width, CachedRegion.this.width, 6);
                            final byte[] dstArray = ((DataBufferByte)realBufferedImage.getRaster().getDataBuffer()).getData();
                            System.arraycopy(CachedRegion.this.image.getData(), 0, dstArray, 0, CachedRegion.this.image.getData().length);
                            ImageIO.write(realBufferedImage, "png", imageFile);
                        }
                        catch (final IOException var9) {
                            var9.printStackTrace();
                        }
                        finally {
                            CachedRegion.this.threadLock.unlock();
                        }
                    }
                });
            }
        }
    }
    
    public long getMostRecentView() {
        return this.mostRecentView;
    }
    
    public long getMostRecentChange() {
        return this.mostRecentChange;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getGLID() {
        if (this.image != null) {
            if (!this.refreshingImage) {
                synchronized (this.image) {
                    if (this.imageChanged) {
                        this.imageChanged = false;
                        this.image.write();
                    }
                }
            }
            return this.image.getIndex();
        }
        return 0;
    }
    
    public CompressibleMapData getMapData() {
        return this.data;
    }
    
    public boolean isLoaded() {
        return this.loaded;
    }
    
    public boolean isEmpty() {
        return this.empty;
    }
    
    public boolean isGroundAt(final int blockX, final int blockZ) {
        return this.isLoaded() && this.getHeightAt(blockX, blockZ) > 0;
    }
    
    public int getHeightAt(final int blockX, final int blockZ) {
        final int x = blockX - this.x * 256;
        final int z = blockZ - this.z * 256;
        int y = (this.data == null) ? 0 : this.data.getHeight(x, z);
        if (this.underground && y == 255) {
            y = CommandUtils.getSafeHeight(blockX, 64, blockZ, this.world);
        }
        return y;
    }
    
    public void compress() {
        if (this.data != null && !this.isCompressed() && !this.queuedToCompress) {
            this.queuedToCompress = true;
            ThreadManager.executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (CachedRegion.this.threadLock.tryLock()) {
                        try {
                            CachedRegion.this.compressData();
                        }
                        catch (final Exception ex) {}
                        finally {
                            CachedRegion.this.threadLock.unlock();
                        }
                    }
                    CachedRegion.this.queuedToCompress = false;
                }
            });
        }
    }
    
    private void compressData() {
        this.data.compress();
    }
    
    private boolean isCompressed() {
        return this.data.isCompressed();
    }
    
    public synchronized void cleanup() {
        this.closed = true;
        Arrays.fill(this.liveChunkUpdateQueued, false);
        this.queuedToCompress = true;
        if (this.future != null) {
            this.future.cancel(false);
        }
        this.persistentMap.getSettingsAndLightingChangeNotifier().removeObserver(this);
        if (this.image != null) {
            this.image.baleet();
        }
        this.saveData(true);
        if (this.persistentMap.getOptions().outputImages) {
            this.saveImage();
        }
    }
    
    public synchronized void save() {
        this.saveData(true);
    }
    
    static {
        CachedRegion.emptyRegion = new EmptyCachedRegion();
        anvilLock = new Object();
        tickLock = new ReentrantReadWriteLock();
        CachedRegion.loadedChunkCount = 0;
        CachedRegion.debug = false;
    }
    
    private class RefreshRunnable extends AbstractNotifyingRunnable
    {
        private boolean forceCompress;
        
        public RefreshRunnable(final boolean forceCompress) {
            this.forceCompress = false;
            this.forceCompress = forceCompress;
        }
        
        @Override
        public void doRun() {
            CachedRegion.this.threadLock.lock();
            CachedRegion.this.mostRecentChange = System.currentTimeMillis();
            try {
                if (!CachedRegion.this.loaded) {
                    CachedRegion.this.load();
                }
                if (CachedRegion.this.dataUpdateQueued) {
                    CachedRegion.this.loadModifiedData();
                    CachedRegion.this.dataUpdateQueued = false;
                }
                while (CachedRegion.this.dataUpdated || CachedRegion.this.displayOptionsChanged) {
                    CachedRegion.this.dataUpdated = false;
                    CachedRegion.this.displayOptionsChanged = false;
                    CachedRegion.this.refreshingImage = true;
                    synchronized (CachedRegion.this.image) {
                        CachedRegion.this.fillImage();
                        CachedRegion.this.imageChanged = true;
                    }
                    CachedRegion.this.refreshingImage = false;
                }
                if (this.forceCompress) {
                    CachedRegion.this.compressData();
                }
            }
            catch (final Exception e) {
                System.out.println("Exception loading chunk: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            finally {
                CachedRegion.this.threadLock.unlock();
                CachedRegion.this.refreshQueued = false;
            }
        }
    }
    
    private class FillChunkRunnable implements Runnable
    {
        private LevelChunk chunk;
        private int index;
        
        public FillChunkRunnable(final LevelChunk chunk) {
            this.chunk = chunk;
            final int chunkX = chunk.getPos().x - CachedRegion.this.x * 16;
            final int chunkZ = chunk.getPos().z - CachedRegion.this.z * 16;
            this.index = chunkZ * 16 + chunkX;
        }
        
        @Override
        public void run() {
            CachedRegion.this.threadLock.lock();
            try {
                if (!CachedRegion.this.loaded) {
                    CachedRegion.this.load();
                }
                final int chunkX = this.chunk.getPos().x - CachedRegion.this.x * 16;
                final int chunkZ = this.chunk.getPos().z - CachedRegion.this.z * 16;
                CachedRegion.this.loadChunkData(this.chunk, chunkX, chunkZ);
            }
            catch (final Exception ex) {}
            finally {
                CachedRegion.this.threadLock.unlock();
                CachedRegion.this.chunkUpdateQueued[this.index] = false;
            }
        }
    }
}
