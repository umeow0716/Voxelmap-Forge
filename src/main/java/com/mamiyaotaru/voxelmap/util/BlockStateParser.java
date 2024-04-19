// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.Optional;
import com.google.common.collect.BiMap;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateParser
{
    public static void parseLine(final String line, final BiMap<BlockState, Integer> map) {
        final String[] lineParts = line.split(" ");
        final int id = Integer.parseInt(lineParts[0]);
        final BlockState blockState = parseStateString(lineParts[1]);
        if (blockState != null) {
            map.forcePut(blockState, id);
        }
    }
    
    private static BlockState parseStateString(final String stateString) {
        BlockState blockState = null;
        final int bracketIndex = stateString.indexOf("[");
        String resourceString = stateString.substring(0, (bracketIndex == -1) ? stateString.length() : bracketIndex);
        final int curlyBracketOpenIndex = resourceString.indexOf("{");
        final int curlyBracketCloseIndex = resourceString.indexOf("}");
        resourceString = resourceString.substring((curlyBracketOpenIndex == -1) ? 0 : (curlyBracketOpenIndex + 1), (curlyBracketCloseIndex == -1) ? resourceString.length() : curlyBracketCloseIndex);
        final String[] resourceStringParts = resourceString.split(":");
        ResourceLocation resourceLocation = null;
        if (resourceStringParts.length == 1) {
            resourceLocation = new ResourceLocation(resourceStringParts[0]);
        }
        else if (resourceStringParts.length == 2) {
            resourceLocation = new ResourceLocation(resourceStringParts[0], resourceStringParts[1]);
        }
        
        final Block block = Registry.BLOCK.get(resourceLocation);
        if (block != Blocks.AIR || resourceString.equals("minecraft:air")) {
            blockState = block.defaultBlockState();
            if (bracketIndex != -1) {
                final String propertiesString = stateString.substring(stateString.indexOf("[") + 1, stateString.lastIndexOf("]"));
                final String[] propertiesStringParts = propertiesString.split(",");
                for (int t = 0; t < propertiesStringParts.length; ++t) {
                    final String[] propertyStringParts = propertiesStringParts[t].split("=");
                    final Property<?> property = (Property<?>)block.getStateDefinition().getProperty(propertyStringParts[0]);
                    if (property != null) {
                        blockState = withValue(blockState, property, propertyStringParts[1]);
                    }
                }
            }
        }
        return blockState;
    }
    
    public static BlockState parseStateString(final String name, final String propertiesString) {
        BlockState blockState = null;
        final ResourceLocation resourceLocation = new ResourceLocation(name);
        final Block block = (Block)Registry.BLOCK.get(resourceLocation);
        if (block != Blocks.AIR || name.equals("minecraft:air")) {
            blockState = block.defaultBlockState();
            if (propertiesString != null && !propertiesString.equals("")) {
                final String[] propertiesStringParts = propertiesString.split(",");
                for (int t = 0; t < propertiesStringParts.length; ++t) {
                    final String[] propertyStringParts = propertiesStringParts[t].split("=");
                    final Property<?> property = (Property<?>)block.getStateDefinition().getProperty(propertyStringParts[0]);
                    if (property != null) {
                        blockState = withValue(blockState, property, propertyStringParts[1]);
                    }
                }
            }
        }
        return blockState;
    }
    
    private static <T extends Comparable<T>> BlockState withValue(BlockState blockState, final Property<T> property, final String valueString) {
        final Optional<T> value = property.getValue(valueString);
        if (value.isPresent()) {
            blockState = blockState.setValue(property, value.get());
        }
        return blockState;
    }
}
