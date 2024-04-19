// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.Locale;
import java.util.TreeSet;
import java.io.Serializable;

public class Waypoint implements Serializable, Comparable<Waypoint>
{
    private static final long serialVersionUID = 8136790917447997951L;
    private static final Minecraft game = Minecraft.getInstance();
    public String name;
    public String imageSuffix;
    public String world;
    public TreeSet<DimensionContainer> dimensions;
    public int x;
    public int z;
    public int y;
    public boolean enabled;
    public boolean inWorld;
    public boolean inDimension;
    public float red;
    public float green;
    public float blue;
    
    public Waypoint(final String name, final int x, final int z, final int y, final boolean enabled, final float red, final float green, final float blue, final String suffix, final String world, final TreeSet<DimensionContainer> dimensions) {
    	this.imageSuffix = "";
        this.world = "";
        this.dimensions = new TreeSet<DimensionContainer>();
        this.inWorld = true;
        this.inDimension = true;
        this.red = 0.0f;
        this.green = 1.0f;
        this.blue = 0.0f;
        this.name = name;
        this.x = x;
        this.z = z;
        this.y = y;
        this.enabled = enabled;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.imageSuffix = suffix.toLowerCase(Locale.ROOT);
        this.world = world;
        this.dimensions = dimensions;
    }
    
    public int getUnifiedColor() {
        return -16777216 + ((int)(this.red * 255.0f) << 16) + ((int)(this.green * 255.0f) << 8) + (int)(this.blue * 255.0f);
    }
    
    public boolean isActive() {
        return this.enabled && this.inWorld && this.inDimension;
    }
    
	public int getX() {
        return (int)(this.x / game.player.level.dimensionType().coordinateScale());
    }
    
	public int getZ() {
        return (int)(this.z / game.player.level.dimensionType().coordinateScale());
    }
    
    public int getY() {
        return this.y;
    }
    
	public void setX(final int x) {
        this.x = (int)(x * game.player.level.dimensionType().coordinateScale());
    }
    
	public void setZ(final int z) {
        this.z = (int)(z * game.player.level.dimensionType().coordinateScale());
    }
    
    public void setY(final int y) {
        this.y = y;
    }
    
    @Override
    public int compareTo(final Waypoint arg0) {
        final double myDistance = this.getDistanceSqToEntity(game.player);
        final double comparedDistance = arg0.getDistanceSqToEntity(game.player);
        return Double.compare(myDistance, comparedDistance);
    }
    
    public double getDistanceSqToEntity(final Entity par1Entity) {
        final double var2 = this.getX() + 0.5 - par1Entity.getX();
        final double var3 = this.getY() + 0.5 - par1Entity.getY();
        final double var4 = this.getZ() + 0.5 - par1Entity.getZ();
        return var2 * var2 + var3 * var3 + var4 * var4;
    }
    
    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof Waypoint)) {
            return false;
        }
        final Waypoint otherWaypoint = (Waypoint)otherObject;
        return this.name.equals(otherWaypoint.name) && this.imageSuffix.equals(otherWaypoint.imageSuffix) && this.world.equals(otherWaypoint.world) && this.x == otherWaypoint.x && this.y == otherWaypoint.y && this.z == otherWaypoint.z && this.red == otherWaypoint.red && this.green == otherWaypoint.green && this.blue == otherWaypoint.blue && this.dimensions.equals(otherWaypoint.dimensions);
    }
}
