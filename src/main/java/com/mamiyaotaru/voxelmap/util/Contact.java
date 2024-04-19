// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.textures.Sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class Contact
{
    public double x;
    public double z;
    public int y;
    public int yFudge;
    public float angle;
    public double distance;
    public float brightness;
    public EnumMobs type;
    public boolean vanillaType;
    public boolean custom;
    public UUID uuid;
    public String name;
    public int rotationFactor;
    public String skinURL;
    public Entity entity;
    public Sprite icon;
    public Sprite armorIcon;
    public int armorColor;
    
    public Contact(final Entity entity, final EnumMobs type) {
        this.yFudge = 0;
        this.custom = false;
        this.uuid = null;
        this.name = "_";
        this.rotationFactor = 0;
        this.skinURL = "";
        this.entity = null;
        this.icon = null;
        this.armorIcon = null;
        this.armorColor = -1;
        this.entity = entity;
        this.type = type;
        this.vanillaType = (type != EnumMobs.GENERICNEUTRAL && type != EnumMobs.GENERICHOSTILE && type != EnumMobs.GENERICTAME && type != EnumMobs.UNKNOWN);
    }
    
    public void setUUID(final UUID uuid) {
        this.uuid = uuid;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void setRotationFactor(final int rotationFactor) {
        this.rotationFactor = rotationFactor;
    }
    
    public void setArmorColor(final int armorColor) {
        this.armorColor = armorColor;
    }
    
    public void updateLocation() {
        this.x = this.entity.xo + (this.entity.getX() - this.entity.xo) * Minecraft.getInstance().getFrameTime();
        this.y = (int)this.entity.getY() + this.yFudge;
        this.z = this.entity.zo + (this.entity.getZ() - this.entity.zo) * Minecraft.getInstance().getFrameTime();
    }
}
