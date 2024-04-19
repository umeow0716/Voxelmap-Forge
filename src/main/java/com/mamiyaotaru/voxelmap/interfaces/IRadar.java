// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

public interface IRadar
{
    void onResourceManagerReload(final ResourceManager p0);
    
    void onTickInGame(final PoseStack matrixStack, final Minecraft p0, final LayoutVariables p1);
}
