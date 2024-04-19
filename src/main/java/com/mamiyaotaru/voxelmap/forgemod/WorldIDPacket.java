// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import java.nio.charset.StandardCharsets;

import net.minecraft.network.FriendlyByteBuf;

public class WorldIDPacket
{
    private String worldID;
    
    public WorldIDPacket() {
        this.worldID = "";
    }
    
    public WorldIDPacket(final String worldID) {
        this.worldID = worldID;
    }
    
    public String getWorldID() {
        return this.worldID;
    }
    
    public static void encode(final WorldIDPacket packet, final FriendlyByteBuf buf) {
        System.out.println("encoding id packet");
        buf.writeByte(42);
        buf.writeUtf(packet.worldID);
    }
    
    public static WorldIDPacket decode(final FriendlyByteBuf buffer) {
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
        return new WorldIDPacket(subWorldName);
    }
}
