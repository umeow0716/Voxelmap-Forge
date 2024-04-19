// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mamiyaotaru.voxelmap.VoxelMap;

public class GuiScreenMinimap extends Screen
{
    protected GuiScreenMinimap() {
        this(new TextComponent(""));
    }
    
    protected GuiScreenMinimap(final TextComponent textComponent_1) {
        super(textComponent_1);
        this.setBlitOffset(0);
    }
    
    public void drawMap(final PoseStack PoseStack) {
        if (!VoxelMap.instance.getMapOptions().showUnderMenus) {
            VoxelMap.instance.getMap().drawMinimap(PoseStack, this.minecraft);
            GLShim.glClear(256);
        }
    }
    
    public void removed() {
        MapSettingsManager.instance.saveAll();
    }
    
    public void renderTooltip(final PoseStack PoseStack, final TextComponent text, final int x, final int y) {
        if (text != null && text.getString() != null && !text.getString().equals("")) {
            super.renderTooltip(PoseStack, text, x, y);
        }
    }
    
    public Minecraft getMinecraft() {
        return this.minecraft;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public List<? extends GuiEventListener> getButtonList() {
        return this.children();
    }
    
    public Font getFontRenderer() {
        return this.font;
    }
}
