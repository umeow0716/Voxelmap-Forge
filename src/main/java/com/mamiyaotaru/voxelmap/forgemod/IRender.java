// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface IRender
{
     <T extends Entity> ResourceLocation publicGetEntityTexture(final T p0);
}
