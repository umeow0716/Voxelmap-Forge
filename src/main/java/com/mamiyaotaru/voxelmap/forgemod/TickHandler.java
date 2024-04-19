// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.forgemod;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import com.mamiyaotaru.voxelmap.VoxelMap;

public class TickHandler
{
    private VoxelMap voxelMap;
    private Timer timer;
    
    public TickHandler(final VoxelMap voxelMap) {
        this.timer = null;
        this.voxelMap = voxelMap;
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (this.timer == null) {
            this.timer = (Timer)ReflectionUtils.getPrivateFieldValueByType(Minecraft.getInstance(), Minecraft.class, Timer.class);
        }
        final boolean clock = this.timer.tickDelta > 0.0f;
        this.voxelMap.onTick(Minecraft.getInstance(), clock);
    }
    
    @SubscribeEvent
    public void onRenderOverlay(final RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            this.voxelMap.onTickInGame(event.getMatrixStack(), Minecraft.getInstance());
        }
    }
    
    @SubscribeEvent
    public void onRenderHand(final RenderLevelLastEvent event) {
    	try {
    		this.voxelMap.getWaypointManager().renderWaypoints(event.getPartialTick(), event.getPoseStack(), AbstractVoxelMap.getInstance().getMapOptions().showBeacons, AbstractVoxelMap.getInstance().getMapOptions().showWaypoints, true, true);
        }
        catch (final Exception ex) {}
    }
    
    @SubscribeEvent
    public void onChat(final ClientChatReceivedEvent event) {
        final Component chat = event.getMessage();
        if (!CommandUtils.checkForWaypoints(chat)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onSendChatMessage(final ClientChatEvent event) {
        final String message = event.getMessage();
        if (message.startsWith("/newWaypoint")) {
            CommandUtils.waypointClicked(message);
            event.setCanceled(true);
        }
        else if (message.startsWith("/ztp")) {
            CommandUtils.teleport(message);
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onChunkEvent(final ChunkEvent event) {
        final ChunkPos pos = event.getChunk().getPos();
        this.voxelMap.getWorldUpdateListener().notifyObservers(pos.x, pos.z);
    }
    
    @SubscribeEvent
    public void onBlockEvent(final BlockEvent event) {
        final BlockPos pos = event.getPos();
        final int chunkX = (int)Math.floor(pos.getX() / 16.0f);
        final int chunkZ = (int)Math.floor(pos.getZ() / 16.0f);
        for (int x = chunkX - 1; x <= chunkX + 1; ++x) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; ++z) {
                this.voxelMap.getWorldUpdateListener().notifyObservers(x, z);
            }
        }
    }
}
