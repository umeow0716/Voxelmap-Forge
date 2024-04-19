// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import java.nio.charset.StandardCharsets;

import net.minecraft.network.FriendlyByteBuf;

public class WorldInfoPacket
{
    private String worldID;
    
    public WorldInfoPacket() {
    }
    
    public WorldInfoPacket(final String worldID) {
        this.worldID = worldID;
    }
    
    public String getWorldID() {
        return this.worldID;
    }
    
    public static void encode(final WorldInfoPacket packet, final FriendlyByteBuf buf) {
        buf.writeByte(42);
        buf.writeUtf(packet.worldID);
    }
    
    public static WorldInfoPacket decode(final FriendlyByteBuf buffer) {
        String subWorldName = "";
        try {
            final byte length = buffer.readByte();
            final byte[] bytes = new byte[length];
            buffer.readBytes(bytes);
            subWorldName = new String(bytes, StandardCharsets.UTF_8);
        }
        catch (final Exception e) {
            subWorldName = "";
            System.err.println(String.format("Failed to read message: %s", e));
        }
        return new WorldInfoPacket(subWorldName);
    }
}
