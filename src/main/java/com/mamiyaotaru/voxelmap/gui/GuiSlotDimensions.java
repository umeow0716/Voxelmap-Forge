// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;

class GuiSlotDimensions extends GuiSlotMinimap<GuiSlotDimensions.DimensionItem>
{
    private IDimensionManager dimensionManager;
    final GuiAddWaypoint parentGui;
    private ArrayList<DimensionItem> dimensions;
    final TranslatableComponent APPLIES;
    final TranslatableComponent NOT_APPLIES;
    
    public GuiSlotDimensions(final GuiAddWaypoint par1GuiWaypoints) {
        super(Minecraft.getInstance(), 101, par1GuiWaypoints.getHeight(), par1GuiWaypoints.getHeight() / 6 + 82 + 6, par1GuiWaypoints.getHeight() / 6 + 164 + 3, 18);
        this.APPLIES = new TranslatableComponent("minimap.waypoints.dimension.applies");
        this.NOT_APPLIES = new TranslatableComponent("minimap.waypoints.dimension.notapplies");
        this.parentGui = par1GuiWaypoints;
        this.setSlotWidth(88);
        this.setLeftPos(this.parentGui.getWidth() / 2);
        this.setRenderSelection(false);
        this.setShowTopBottomBG(false);
        this.setShowSlotBG(false);
        this.dimensionManager = this.parentGui.master.getDimensionManager();
        this.dimensions = new ArrayList<DimensionItem>();
        DimensionItem first = null;
        for (final DimensionContainer dim : this.dimensionManager.getDimensions()) {
            final DimensionItem item = new DimensionItem(this.parentGui, dim);
            this.dimensions.add(item);
            if (dim.equals(this.parentGui.waypoint.dimensions.first())) {
                first = item;
            }
        }
        this.dimensions.forEach(this::addEntry);
        if (first != null) {
            this.ensureVisible(first);
        }
    }
    
    public void setSelected(final DimensionItem item) {
        super.setSelected(item);
        if (this.getSelected() instanceof DimensionItem) {
            NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", new Object[] { ((DimensionItem)this.getSelected()).dim.name }).getString());
        }
        this.parentGui.setSelectedDimension(item.dim);
    }
    
    protected boolean isSelectedItem(final int par1) {
        return this.dimensions.get(par1).dim.equals(this.parentGui.selectedDimension);
    }
    
    public void renderBackground(final PoseStack matrixStack) {
    }
    
    public class DimensionItem extends AbstractSelectionList.Entry<DimensionItem>
    {
        private final GuiAddWaypoint parentGui;
        private final DimensionContainer dim;
        
        protected DimensionItem(final GuiAddWaypoint waypointScreen, final DimensionContainer dim) {
            this.parentGui = waypointScreen;
            this.dim = dim;
        }
        
        public void render(final PoseStack matrixStack, final int slotIndex, final int slotYPos, int leftEdge, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean mouseOver, final float partialTicks) {
            GuiAddWaypoint.drawCenteredString(matrixStack, this.parentGui.getFontRenderer(), this.dim.getDisplayName(), this.parentGui.getWidth() / 2 + GuiSlotDimensions.this.slotWidth / 2, slotYPos + 3, 16777215);
            final byte padding = 4;
            final byte iconWidth = 16;
            leftEdge = this.parentGui.getWidth() / 2;
            final int width = GuiSlotDimensions.this.slotWidth;
            if (mouseX >= leftEdge + padding && mouseY >= slotYPos && mouseX <= leftEdge + width + padding && mouseY <= slotYPos + GuiSlotDimensions.this.itemHeight) {
                TranslatableComponent tooltip = null;
                if (!this.parentGui.popupOpen() && mouseX >= leftEdge + width - iconWidth - padding && mouseX <= leftEdge + width) {
                    tooltip = (this.parentGui.waypoint.dimensions.contains(this.dim) ? GuiSlotDimensions.this.APPLIES : GuiSlotDimensions.this.NOT_APPLIES);
                }
                else {
                    tooltip = null;
                }
                GuiAddWaypoint.setTooltip(this.parentGui, tooltip);
            }
            GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GLUtils.img2(new ResourceLocation("minecraft", "textures/gui/container/beacon.png"));
            final int xOffset = this.parentGui.waypoint.dimensions.contains(this.dim) ? 91 : 113;
            final int yOffset = 222;
            this.parentGui.blit(matrixStack, leftEdge + width - iconWidth, slotYPos - 2, xOffset, yOffset, 16, 16);
        }
        
        public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
            GuiSlotDimensions.this.setSelected(this);
            final int leftEdge = this.parentGui.getWidth() / 2;
            final byte padding = 4;
            final byte iconWidth = 16;
            final int width = GuiSlotDimensions.this.slotWidth;
            if (mouseX >= leftEdge + width - iconWidth - padding && mouseX <= leftEdge + width) {
                this.parentGui.toggleDimensionSelected();
            }
            else if (GuiSlotDimensions.this.doubleclick) {
                this.parentGui.toggleDimensionSelected();
            }
            return true;
        }
    }

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		// TODO Auto-generated method stub
		
	}
}
