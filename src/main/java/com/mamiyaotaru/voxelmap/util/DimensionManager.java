// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.Collections;
import java.text.Collator;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;

public class DimensionManager implements IDimensionManager
{
    IVoxelMap master;
    public ArrayList<DimensionContainer> dimensions;
    private ArrayList<ResourceKey<Level>> vanillaLevels;
    
    public DimensionManager(final IVoxelMap master) {
        this.vanillaLevels = new ArrayList<ResourceKey<Level>>();
        this.master = master;
        this.dimensions = new ArrayList<DimensionContainer>();
        this.vanillaLevels.add((ResourceKey<Level>)Level.OVERWORLD);
        this.vanillaLevels.add((ResourceKey<Level>)Level.NETHER);
        this.vanillaLevels.add((ResourceKey<Level>)Level.END);
    }
    
    @Override
    public ArrayList<DimensionContainer> getDimensions() {
        return this.dimensions;
    }
    
    @Override
    public void populateDimensions(final Level world) {
        this.dimensions.clear();
        final Registry<DimensionType> dimensionTypeRegistry = (Registry<DimensionType>) Minecraft.getInstance().getConnection().registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        for (final ResourceKey<Level> vanillaLevelKey : this.vanillaLevels) {
            final ResourceKey<DimensionType> typeKey = (ResourceKey<DimensionType>) ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, vanillaLevelKey.location());
            final DimensionType dimensionType = (DimensionType)dimensionTypeRegistry.get(typeKey);
            final DimensionContainer dimensionContainer = new DimensionContainer(dimensionType, vanillaLevelKey.location().getPath(), vanillaLevelKey.location());
            this.dimensions.add(dimensionContainer);
        }
        this.sort();
    }
    
    public void enteredLevel(final Level world) {
        final ResourceLocation resourceLocation = world.dimension().location();
        DimensionContainer dim = this.getDimensionContainerByResourceLocation(resourceLocation);
        if (dim == null) {
            dim = new DimensionContainer(world.dimensionType(), resourceLocation.getPath(), resourceLocation);
            this.dimensions.add(dim);
            this.sort();
        }
        if (dim.type == null) {
            try {
                dim.type = world.dimensionType();
            }
            catch (final Exception ex) {}
        }
    }
    
    private void sort() {
        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(this.dimensions, new Comparator<DimensionContainer>() {
            @Override
            public int compare(final DimensionContainer dim1, final DimensionContainer dim2) {
                if (dim1.resourceLocation.equals((Object)Level.OVERWORLD.location())) {
                    return -1;
                }
                if (dim1.resourceLocation.equals((Object)Level.NETHER.location()) && !dim2.resourceLocation.equals((Object)Level.OVERWORLD.location())) {
                    return -1;
                }
                if (dim1.resourceLocation.equals((Object)Level.END.location()) && !dim2.resourceLocation.equals((Object)Level.OVERWORLD.location()) && !dim2.resourceLocation.equals((Object)Level.NETHER.location())) {
                    return -1;
                }
                return collator.compare(dim1.name, dim2.name);
            }
        });
    }
    
    @Override
    public DimensionContainer getDimensionContainerByWorld(final Level world) {
        final ResourceLocation resourceLocation = world.dimension().location();
        DimensionContainer dim = this.getDimensionContainerByResourceLocation(resourceLocation);
        if (dim == null) {
            dim = new DimensionContainer(world.dimensionType(), resourceLocation.getPath(), resourceLocation);
            this.dimensions.add(dim);
            this.sort();
        }
        return dim;
    }
    
    @Override
    public DimensionContainer getDimensionContainerByIdentifier(final String ident) {
        DimensionContainer dim = null;
        final ResourceLocation resourceLocation = new ResourceLocation(ident);
        dim = this.getDimensionContainerByResourceLocation(resourceLocation);
        if (dim == null) {
            dim = new DimensionContainer(null, resourceLocation.getPath(), resourceLocation);
            this.dimensions.add(dim);
            this.sort();
        }
        return dim;
    }
    
    @Override
    public DimensionContainer getDimensionContainerByResourceLocation(final ResourceLocation resourceLocation) {
        for (final DimensionContainer dim : this.dimensions) {
            if (resourceLocation.equals((Object)dim.resourceLocation)) {
                return dim;
            }
        }
        return null;
    }

    @Override
    public void enteredWorld(final Level world) {
        final ResourceLocation resourceLocation = world.dimension().location();
        DimensionContainer dim = this.getDimensionContainerByResourceLocation(resourceLocation);
        if (dim == null) {
            dim = new DimensionContainer(world.dimensionType(), resourceLocation.getPath(), resourceLocation);
            this.dimensions.add(dim);
            this.sort();
        }
        if (dim.type == null) {
            try {
                dim.type = world.dimensionType();
            }
            catch (final Exception ex) {}
        }
    }
}
