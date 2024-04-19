// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class GameVariableAccessShim
{
    private static Minecraft minecraft;
    
    public static Minecraft getMinecraft() {
        return GameVariableAccessShim.minecraft;
    }
    
    public static ClientLevel getWorld() {
        return GameVariableAccessShim.minecraft.level;
    }
    
    public static File getDataDir() {
        return GameVariableAccessShim.minecraft.gameDirectory;
    }
    
    public static int xCoord() {
        return (int)((GameVariableAccessShim.minecraft.getCameraEntity().getX() < 0.0) ? (GameVariableAccessShim.minecraft.getCameraEntity().getX() - 1.0) : GameVariableAccessShim.minecraft.getCameraEntity().getX());
    }
    
    public static int zCoord() {
        return (int)((GameVariableAccessShim.minecraft.getCameraEntity().getZ() < 0.0) ? (GameVariableAccessShim.minecraft.getCameraEntity().getZ() - 1.0) : GameVariableAccessShim.minecraft.getCameraEntity().getZ());
    }
    
    public static int yCoord() {
        return (int)Math.ceil(GameVariableAccessShim.minecraft.getCameraEntity().getY());
    }
    
    public static double xCoordDouble() {
        if (GameVariableAccessShim.minecraft.screen == null || !GameVariableAccessShim.minecraft.screen.isPauseScreen()) {
            return GameVariableAccessShim.minecraft.getCameraEntity().xo + (GameVariableAccessShim.minecraft.getCameraEntity().getX() - GameVariableAccessShim.minecraft.getCameraEntity().xo) * GameVariableAccessShim.minecraft.getFrameTime();
        }
        return GameVariableAccessShim.minecraft.getCameraEntity().getX();
    }
    
    public static double zCoordDouble() {
        if (GameVariableAccessShim.minecraft.screen == null || !GameVariableAccessShim.minecraft.screen.isPauseScreen()) {
            return GameVariableAccessShim.minecraft.getCameraEntity().zo + (GameVariableAccessShim.minecraft.getCameraEntity().getZ() - GameVariableAccessShim.minecraft.getCameraEntity().zo) * GameVariableAccessShim.minecraft.getFrameTime();
        }
        return GameVariableAccessShim.minecraft.getCameraEntity().getZ();
    }
    
    public static double yCoordDouble() {
        if (GameVariableAccessShim.minecraft.screen == null || !GameVariableAccessShim.minecraft.screen.isPauseScreen()) {
            return GameVariableAccessShim.minecraft.getCameraEntity().yo + (GameVariableAccessShim.minecraft.getCameraEntity().getY() - GameVariableAccessShim.minecraft.getCameraEntity().yo) * GameVariableAccessShim.minecraft.getFrameTime();
        }
        return GameVariableAccessShim.minecraft.getCameraEntity().getY();
    }
    
    public static float rotationYaw() {
        return GameVariableAccessShim.minecraft.getCameraEntity().yRotO + (GameVariableAccessShim.minecraft.getCameraEntity().getYRot() - GameVariableAccessShim.minecraft.getCameraEntity().yRotO) * GameVariableAccessShim.minecraft.getFrameTime();
    }
    
    static {
        GameVariableAccessShim.minecraft = Minecraft.getInstance();
    }
}
