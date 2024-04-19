// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.text.Collator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionContainer implements Comparable<DimensionContainer>
{
    public DimensionType type;
    public String name;
    public ResourceLocation resourceLocation;
    private static final Collator collator;
    
    public DimensionContainer(final DimensionType type, final String name, final ResourceLocation resourceLocation) {
        this.name = "notLoaded";
        this.type = type;
        this.name = name;
        this.resourceLocation = resourceLocation;
    }
    
    public String getStorageName() {
        String storageName = null;
        if (this.resourceLocation != null) {
            if (this.resourceLocation.getNamespace().equals("minecraft")) {
                storageName = this.resourceLocation.getPath();
            }
            else {
                storageName = this.resourceLocation.toString();
            }
        }
        else {
            storageName = "UNKNOWN";
        }
        return storageName;
    }
    
    public String getDisplayName() {
        return TextUtils.prettify(this.name);
    }
    
    @Override
    public int compareTo(final DimensionContainer other) {
        return DimensionContainer.collator.compare(this.name, other.name);
    }
    
    static {
        collator = I18nUtils.getLocaleAwareCollator();
    }
}
