// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public interface IMap extends IChangeObserver
{
    void forceFullRender(final boolean p0);
    
    void drawMinimap(final PoseStack p0, final Minecraft p1);
    
    float getPercentX();
    
    float getPercentY();
    
    void newWorld(final ClientLevel p0);
    
    void onTickInGame(final PoseStack p0, final Minecraft p1);
    
    int[] getLightmapArray();
    
    void newWorldName();
}
