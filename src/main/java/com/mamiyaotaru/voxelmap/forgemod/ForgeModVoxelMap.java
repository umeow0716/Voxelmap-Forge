// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.minecraftforge.fml.common.Mod;

@Mod("voxelmap")
public class ForgeModVoxelMap
{
    public static final String MODID = "voxelmap";
    public static SimpleChannel WORLD_ID;
    public static SimpleChannel WORLD_INFO;
    public static ForgeModVoxelMap instance;
    public static IProxy proxy;
    
    public ForgeModVoxelMap() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }
    
    public void preInit(final FMLCommonSetupEvent event) {
        (ForgeModVoxelMap.WORLD_ID = NetworkRegistry.newSimpleChannel(new ResourceLocation("worldinfo", "world_id"), () -> "1", PROTOCOL_VERSION -> true, PROTOCOL_VERSION -> true)).registerMessage(0, WorldIDPacket.class, WorldIDPacket::encode, WorldIDPacket::decode, WorldIDHandler::handle);
    }
    
    public void init(final FMLClientSetupEvent event) {
        ForgeModVoxelMap.instance = this;
        Minecraft.getInstance().execute((Runnable)new Runnable() {
            @Override
            public void run() {
                ForgeModVoxelMap.proxy.postInit(event);
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ForgeModVoxelMap.proxy.onShutDown();
            }
        });
    }
    
    static {
        LogManager.getLogger();
        ForgeModVoxelMap.proxy = (IProxy)DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    }
    
    public static class WorldIDHandler
    {
        public static void handle(final WorldIDPacket packet, final Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> ForgeModVoxelMap.proxy.newWorldName(packet.getWorldID()));
            ctx.get().setPacketHandled(true);
        }
    }
    
    public static class WorldInfoHandler
    {
        public static void handle(final WorldInfoPacket packet, final Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> ForgeModVoxelMap.proxy.newWorldName(packet.getWorldID()));
            ctx.get().setPacketHandled(true);
        }
    }
}
