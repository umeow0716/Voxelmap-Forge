// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

public abstract class GuiSlotMinimap<E extends AbstractSelectionList.Entry<E>> extends AbstractSelectionList<E>
{
    protected int slotWidth;
    protected boolean centerListVertically;
    private boolean showTopBottomBG;
    private boolean showSlotBG;
    private boolean hasListHeader;
    protected int headerPadding;
    protected long lastClicked;
    public boolean doubleclick;
    
    public GuiSlotMinimap(final Minecraft par1Minecraft, final int width, final int height, final int top, final int bottom, final int slotHeight) {
        super(par1Minecraft, width, height, top, bottom, slotHeight);
        this.slotWidth = 220;
        this.centerListVertically = true;
        this.showTopBottomBG = true;
        this.showSlotBG = true;
        this.lastClicked = 0L;
        this.doubleclick = false;
        this.setBlitOffset(0);
    }
    
    public void setDimensions(final int width, final int height, final int top, final int bottom) {
        this.width = width;
        this.height = height;
        this.y0 = top;
        this.y1 = bottom;
        this.x0 = 0;
        this.x1 = width;
    }
    
    public void setShowTopBottomBG(final boolean showTopBottomBG) {
        this.showTopBottomBG = showTopBottomBG;
    }
    
    public void setShowSlotBG(final boolean showSlotBG) {
        this.showSlotBG = showSlotBG;
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
    	this.renderBackground(matrixStack);
        final int scrollBarLeft = this.getScrollbarPosition();
        final int scrollBarRight = scrollBarLeft + 6;
        this.setScrollAmount(this.getScrollAmount());
        GLShim.glDisable(2896);
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        if (this.showSlotBG) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            final float f = 32.0f;
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            vertexBuffer.vertex((double)this.x0, (double)this.y1, 0.0).uv(this.x0 / f, (this.y1 + (int)this.getScrollAmount()) / f).color(32, 32, 32, 255).endVertex();
            vertexBuffer.vertex((double)this.x1, (double)this.y1, 0.0).uv(this.x1 / f, (this.y1 + (int)this.getScrollAmount()) / f).color(32, 32, 32, 255).endVertex();
            vertexBuffer.vertex((double)this.x1, (double)this.y0, 0.0).uv(this.x1 / f, (this.y0 + (int)this.getScrollAmount()) / f).color(32, 32, 32, 255).endVertex();
            vertexBuffer.vertex((double)this.x0, (double)this.y0, 0.0).uv(this.x0 / f, (this.y0 + (int)this.getScrollAmount()) / f).color(32, 32, 32, 255).endVertex();
            tessellator.end();
        }
        final int leftEdge = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
        final int topOfListYPos = this.y0 + 4 - (int)this.getScrollAmount();
        if (this.hasListHeader) {
            this.renderHeader(matrixStack, leftEdge, topOfListYPos, tessellator);
        }
        
        this.renderList(matrixStack, leftEdge, topOfListYPos, mouseX, mouseY, partialTicks);
        
        GLShim.glDisable(2929);
        final byte topBottomFadeHeight = 4;
        if (this.showTopBottomBG) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            vertexBuffer.vertex((double)this.x0, (double)this.y0, -100.0).uv(0.0f, this.y0 / 32.0f).color(64, 64, 64, 255).endVertex();
            vertexBuffer.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0).uv(this.width / 32.0f, this.y0 / 32.0f).color(64, 64, 64, 255).endVertex();
            vertexBuffer.vertex((double)(this.x0 + this.width), 0.0, -100.0).uv(this.width / 32.0f, 0.0f).color(64, 64, 64, 255).endVertex();
            vertexBuffer.vertex((double)this.x0, 0.0, -100.0).uv(0.0f, 0.0f).color(64, 64, 64, 255).endVertex();
            vertexBuffer.vertex((double)this.x0, (double)this.height, -100.0).uv(0.0f, this.height / 32.0f).color(64, 64, 64, 255).endVertex();
            vertexBuffer.vertex((double)(this.x0 + this.width), (double)this.height, -100.0).uv(this.width / 32.0f, this.height / 32.0f).color(64, 64, 64, 255).endVertex();
            vertexBuffer.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0).uv(this.width / 32.0f, this.y1 / 32.0f).color(64, 64, 64, 255).endVertex();
            vertexBuffer.vertex((double)this.x0, (double)this.y1, -100.0).uv(0.0f, this.y1 / 32.0f).color(64, 64, 64, 255).endVertex();
            tessellator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            GLShim.glEnable(3042);
            RenderSystem.blendFuncSeparate(770, 771, 0, 1);
            GLShim.glDisable(3553);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiSlotMinimap.BACKGROUND_LOCATION);
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            vertexBuffer.vertex((double)this.x0, (double)(this.y0 + topBottomFadeHeight), 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 0).endVertex();
            vertexBuffer.vertex((double)this.x1, (double)(this.y0 + topBottomFadeHeight), 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 0).endVertex();
            vertexBuffer.vertex((double)this.x1, (double)this.y0, 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)this.x0, (double)this.y0, 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)this.x0, (double)this.y1, 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)this.x1, (double)this.y1, 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)this.x1, (double)(this.y1 - topBottomFadeHeight), 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 0).endVertex();
            vertexBuffer.vertex((double)this.x0, (double)(this.y1 - topBottomFadeHeight), 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 0).endVertex();
            tessellator.end();
        }
        final int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            GLShim.glDisable(3553);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int k1 = (this.y1 - this.y0) * (this.y1 - this.y0) / this.getMaxPosition();
            k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
            int l1 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k1) / maxScroll + this.y0;
            if (l1 < this.y0) {
                l1 = this.y0;
            }
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            vertexBuffer.vertex((double)scrollBarLeft, (double)this.y1, 0.0).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarRight, (double)this.y1, 0.0).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarRight, (double)this.y0, 0.0).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarLeft, (double)this.y0, 0.0).color(0, 0, 0, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarLeft, (double)(l1 + k1), 0.0).color(128, 128, 128, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarRight, (double)(l1 + k1), 0.0).color(128, 128, 128, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarRight, (double)l1, 0.0).color(128, 128, 128, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarLeft, (double)l1, 0.0).color(128, 128, 128, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarLeft, (double)(l1 + k1 - 1), 0.0).color(192, 192, 192, 255).endVertex();
            vertexBuffer.vertex((double)(scrollBarRight - 1), (double)(l1 + k1 - 1), 0.0).color(192, 192, 192, 255).endVertex();
            vertexBuffer.vertex((double)(scrollBarRight - 1), (double)l1, 0.0).color(192, 192, 192, 255).endVertex();
            vertexBuffer.vertex((double)scrollBarLeft, (double)l1, 0.0).color(192, 192, 192, 255).endVertex();
            tessellator.end();
        }
        this.renderDecorations(matrixStack, mouseX, mouseY);
        GLShim.glEnable(3553);
        GLShim.glDisable(3042);
    }
    
    public int getRowWidth() {
        return this.slotWidth;
    }
    
    public void setSlotWidth(final int slotWidth) {
        this.slotWidth = slotWidth;
    }
    
    protected int getScrollbarPosition() {
        if (this.slotWidth >= 220) {
            return this.width / 2 + 124;
        }
        return this.x1 - 6;
    }
    
    public void setLeftPos(final int left) {
        this.x0 = left;
        this.x1 = left + this.width;
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        this.doubleclick = (System.currentTimeMillis() - this.lastClicked < 250L);
        this.lastClicked = System.currentTimeMillis();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
