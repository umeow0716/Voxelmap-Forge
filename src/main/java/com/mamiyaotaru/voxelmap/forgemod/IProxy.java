// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public interface IProxy
{
    void postInit(final FMLClientSetupEvent p0);
    
    void newWorldName(final String p0);
    
    void onShutDown();
}
