// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;

public class Popup
{
    Minecraft mc;
    Font fontRendererObj;
    int x;
    int y;
    PopupEntry[] entries;
    int w;
    int h;
    public int clickedX;
    public int clickedY;
    public int clickedDirectX;
    public int clickedDirectY;
    boolean shouldClose;
    PopupGuiScreen parentGui;
    int padding;
    
    public Popup(final int x, final int y, final int directX, final int directY, final ArrayList<PopupEntry> entries, final PopupGuiScreen parentGui) {
        this.shouldClose = false;
        this.padding = 6;
        this.mc = Minecraft.getInstance();
        this.fontRendererObj = this.mc.font;
        this.parentGui = parentGui;
        this.clickedX = x;
        this.clickedY = y;
        this.clickedDirectX = directX;
        this.clickedDirectY = directY;
        this.x = x - 1;
        this.y = y - 1;
        entries.toArray(this.entries = new PopupEntry[entries.size()]);
        this.w = 0;
        this.h = this.entries.length * 20;
        for (int t = 0; t < this.entries.length; ++t) {
            final int entryWidth = this.fontRendererObj.width(this.entries[t].name);
            if (entryWidth > this.w) {
                this.w = entryWidth;
            }
        }
        this.w += this.padding * 2;
        if (x + this.w > parentGui.width) {
            this.x = x - this.w + 2;
        }
        if (y + this.h > parentGui.height) {
            this.y = y - this.h + 2;
        }
    }
    
    public boolean clickedMe(final double mouseX, final double mouseY) {
        final boolean clicked = mouseX > this.x && mouseX < this.x + this.w && mouseY > this.y && mouseY < this.y + this.h;
        if (clicked) {
            for (int t = 0; t < this.entries.length; ++t) {
                if (this.entries[t].enabled) {
                    final boolean entryClicked = mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y + t * 20 && mouseY <= this.y + (t + 1) * 20;
                    if (entryClicked) {
                        this.shouldClose = this.entries[t].causesClose;
                        this.parentGui.popupAction(this, this.entries[t].action);
                    }
                }
            }
        }
        return clicked;
    }
    
    public boolean overMe(final int x, final int y) {
        return x > this.x && x < this.x + this.w && y > this.y && y < this.y + this.h;
    }
    
    public boolean shouldClose() {
        return this.shouldClose;
    }
    
    public void drawPopup(final PoseStack matrixStack, final int mouseX, final int mouseY) {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        GLShim.glDisable(2929);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        final float var6 = 32.0f;
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        vertexBuffer.vertex((double)this.x, (double)(this.y + this.h), 0.0).uv(this.x / var6, this.y / var6).color(64, 64, 64, 255).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)(this.y + this.h), 0.0).uv((this.x + this.w) / var6, this.y / var6).color(64, 64, 64, 255).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)(this.y + 0), 0.0).uv((this.x + this.w) / var6, (this.y + this.h) / var6).color(64, 64, 64, 255).endVertex();
        vertexBuffer.vertex((double)this.x, (double)(this.y + 0), 0.0).uv(this.x / var6, (this.y + this.h) / var6).color(64, 64, 64, 255).endVertex();
        tessellator.end();
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        GLShim.glDisable(3553);
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertexBuffer.vertex((double)this.x, (double)(this.y + 4), 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)(this.y + 4), 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)this.y, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)this.x, (double)this.y, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)this.x, (double)(this.y + this.h), 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)(this.y + this.h), 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)(this.y + this.h - 4), 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)this.x, (double)(this.y + this.h - 4), 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)this.x, (double)this.y, 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)this.x, (double)(this.y + this.h), 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)(this.x + 4), (double)(this.y + this.h), 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + 4), (double)this.y, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w - 4), (double)this.y, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w - 4), (double)(this.y + this.h), 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)(this.y + this.h), 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)this.y, 0.0).color(0, 0, 0, 255).endVertex();
        tessellator.end();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertexBuffer.vertex((double)(this.x + this.w - 4), (double)this.y, 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w - 4), (double)(this.y + this.h), 0.0).color(0, 0, 0, 0).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)(this.y + this.h), 0.0).color(0, 0, 0, 255).endVertex();
        vertexBuffer.vertex((double)(this.x + this.w), (double)this.y, 0.0).color(0, 0, 0, 255).endVertex();
        tessellator.end();
        GLShim.glEnable(3553);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GLShim.glDisable(3042);
        for (int t = 0; t < this.entries.length; ++t) {
            final int color = this.entries[t].enabled ? ((mouseX >= this.x && mouseX <= this.x + this.w && mouseY >= this.y + t * 20 && mouseY <= this.y + (t + 1) * 20) ? 16777120 : 14737632) : 10526880;
            this.fontRendererObj.drawShadow(matrixStack, this.entries[t].name, (float)(this.x + this.padding), (float)(this.y + this.padding + t * 20), color);
        }
    }
    
    public static class PopupEntry
    {
        public String name;
        public int action;
        boolean causesClose;
        boolean enabled;
        
        public PopupEntry(final String name, final int action, final boolean causesClose, final boolean enabled) {
            this.name = name;
            this.action = action;
            this.causesClose = causesClose;
            this.enabled = enabled;
        }
    }
}
