// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import com.mamiyaotaru.voxelmap.persistent.ThreadManager;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.mamiyaotaru.voxelmap.VoxelMap;

public class ClientProxy implements IProxy
{
    VoxelMap voxelMap;
    
    @Override
    public void postInit(final FMLClientSetupEvent event) {
        (this.voxelMap = new VoxelMap()).lateInit(false, false);
        MinecraftForge.EVENT_BUS.register((Object)new TickHandler(this.voxelMap));
    }
    
    @Override
    public void newWorldName(final String worldName) {
        this.voxelMap.newSubWorldName(worldName, true);
    }
    
    @Override
    public void onShutDown() {
        System.out.print("Saving all world maps");
        this.voxelMap.getPersistentMap().saveCachedRegions();
        this.voxelMap.getMapOptions().saveAll();
        BiomeRepository.saveBiomeColors();
        final long shutdownTime = System.currentTimeMillis();
        while (ThreadManager.executorService.getQueue().size() + ThreadManager.executorService.getActiveCount() > 0 && System.currentTimeMillis() - shutdownTime < 10000L) {
            System.out.print(".");
            try {
                Thread.sleep(200L);
            }
            catch (final InterruptedException ex) {}
        }
        System.out.println();
    }
}
