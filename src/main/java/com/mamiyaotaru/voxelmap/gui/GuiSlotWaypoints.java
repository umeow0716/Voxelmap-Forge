// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.TextUtils;
import java.awt.Color;
import java.text.Collator;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import java.util.Collections;
import java.util.Comparator;
import com.mamiyaotaru.voxelmap.textures.Sprite;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Iterator;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;
import net.minecraft.resources.ResourceLocation;

class GuiSlotWaypoints extends GuiSlotMinimap<GuiSlotWaypoints.WaypointItem>
{
    private ArrayList<WaypointItem> waypoints;
    private ArrayList<WaypointItem> waypointsFiltered;
    final GuiWaypoints parentGui;
    private String filterString;
    final TranslatableComponent ENABLE;
    final TranslatableComponent DISABLE;
    
    public GuiSlotWaypoints(final GuiWaypoints par1GuiWaypoints) {
        super(par1GuiWaypoints.options.game, par1GuiWaypoints.getWidth(), par1GuiWaypoints.getHeight(), 54, par1GuiWaypoints.getHeight() - 90 + 4, 18);
        this.filterString = "";
        this.ENABLE = new TranslatableComponent("minimap.waypoints.enable");
        this.DISABLE = new TranslatableComponent("minimap.waypoints.disable");
        this.parentGui = par1GuiWaypoints;
        this.waypoints = new ArrayList<WaypointItem>();
        for (final Waypoint pt : this.parentGui.waypointManager.getWaypoints()) {
            if (pt.inWorld && pt.inDimension) {
                this.waypoints.add(new WaypointItem(this.parentGui, pt));
            }
        }
        (this.waypointsFiltered = new ArrayList<WaypointItem>(this.waypoints)).forEach(this::addEntry);
    }
    
    public void setSelected(final WaypointItem item) {
        super.setSelected(item);
        if (this.getSelected() instanceof WaypointItem) {
            NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", new Object[] { ((WaypointItem)this.getSelected()).waypoint.name }).getString());
        }
        this.parentGui.setSelectedWaypoint(item.waypoint);
    }
    
    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    protected boolean isSelectedItem(final int par1) {
        return this.waypointsFiltered.get(par1).waypoint.equals(this.parentGui.selectedWaypoint);
    }
    
    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight;
    }
    
    public void renderBackground(final PoseStack matrixStack) {
        this.parentGui.renderBackground(matrixStack);
    }
    
    public void drawTexturedModalRect(final int xCoord, final int yCoord, final Sprite textureSprite, final int widthIn, final int heightIn) {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuilder();
        vertexbuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        vertexbuffer.vertex((double)(xCoord + 0), (double)(yCoord + heightIn), 1.0).uv(textureSprite.getMinU(), textureSprite.getMaxV()).endVertex();
        vertexbuffer.vertex((double)(xCoord + widthIn), (double)(yCoord + heightIn), 1.0).uv(textureSprite.getMaxU(), textureSprite.getMaxV()).endVertex();
        vertexbuffer.vertex((double)(xCoord + widthIn), (double)(yCoord + 0), 1.0).uv(textureSprite.getMaxU(), textureSprite.getMinV()).endVertex();
        vertexbuffer.vertex((double)(xCoord + 0), (double)(yCoord + 0), 1.0).uv(textureSprite.getMinU(), textureSprite.getMinV()).endVertex();
        tessellator.end();
    }
    
    protected void sortBy(final int sortKey, final boolean ascending) {
        final int order = ascending ? 1 : -1;
        this.parentGui.options.getClass();
        if (sortKey == 1) {
            final ArrayList<Waypoint> masterWaypointsList = this.parentGui.waypointManager.getWaypoints();
            Collections.sort(this.waypoints, new Comparator<WaypointItem>() {
                @Override
                public int compare(final WaypointItem waypointEntry1, final WaypointItem waypointEntry2) {
                    return Double.compare(masterWaypointsList.indexOf(waypointEntry1.waypoint), masterWaypointsList.indexOf(waypointEntry2.waypoint)) * order;
                }
            });
        }
        else {
            this.parentGui.options.getClass();
            if (sortKey == 3) {
                if (ascending) {
                    Collections.sort(this.waypoints);
                }
                else {
                    Collections.sort(this.waypoints, Collections.reverseOrder());
                }
            }
            else {
                this.parentGui.options.getClass();
                if (sortKey == 2) {
                    final Collator collator = I18nUtils.getLocaleAwareCollator();
                    Collections.sort(this.waypoints, new Comparator<WaypointItem>() {
                        @Override
                        public int compare(final WaypointItem waypointEntry1, final WaypointItem waypointEntry2) {
                            return collator.compare(waypointEntry1.waypoint.name, waypointEntry2.waypoint.name) * order;
                        }
                    });
                }
                else {
                    this.parentGui.options.getClass();
                    if (sortKey == 4) {
                        Collections.sort(this.waypoints, new Comparator<WaypointItem>() {
                            @Override
                            public int compare(final WaypointItem waypointEntry1, final WaypointItem waypointEntry2) {
                                final Waypoint waypoint1 = waypointEntry1.waypoint;
                                final Waypoint waypoint2 = waypointEntry2.waypoint;
                                final float hue1 = Color.RGBtoHSB((int)(waypoint1.red * 255.0f), (int)(waypoint1.green * 255.0f), (int)(waypoint1.blue * 255.0f), null)[0];
                                final float hue2 = Color.RGBtoHSB((int)(waypoint2.red * 255.0f), (int)(waypoint2.green * 255.0f), (int)(waypoint2.blue * 255.0f), null)[0];
                                return Double.compare(hue1, hue2) * order;
                            }
                        });
                    }
                }
            }
        }
        this.updateFilter(this.filterString);
    }
    
    protected void updateFilter(final String filterString) {
        this.clearEntries();
        this.filterString = filterString;
        this.waypointsFiltered = new ArrayList<WaypointItem>(this.waypoints);
        final Iterator<WaypointItem> iterator = this.waypointsFiltered.iterator();
        while (iterator.hasNext()) {
            final Waypoint waypoint = iterator.next().waypoint;
            if (!TextUtils.scrubCodes(waypoint.name).toLowerCase().contains(filterString)) {
                if (waypoint == this.parentGui.selectedWaypoint) {
                    this.parentGui.setSelectedWaypoint(null);
                }
                iterator.remove();
            }
        }
        this.waypointsFiltered.forEach(this::addEntry);
    }
    
    public class WaypointItem extends AbstractSelectionList.Entry<WaypointItem> implements Comparable<WaypointItem>
    {
        private final GuiWaypoints parentGui;
        private final Waypoint waypoint;
        
        protected WaypointItem(final GuiWaypoints waypointScreen, final Waypoint waypoint) {
            this.parentGui = waypointScreen;
            this.waypoint = waypoint;
        }
        
        public void render(final PoseStack matrixStack, final int slotIndex, final int slotYPos, final int leftEdge, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean mouseOver, final float partialTicks) {
            GuiWaypoints.drawCenteredString(matrixStack, this.parentGui.getFontRenderer(), this.waypoint.name, this.parentGui.getWidth() / 2, slotYPos + 3, this.waypoint.getUnifiedColor());
            final byte padding = 3;
            if (mouseX >= leftEdge - padding && mouseY >= slotYPos && mouseX <= leftEdge + 215 + padding && mouseY <= slotYPos + entryHeight) {
                Component tooltip;
                if (mouseX >= leftEdge + 215 - 16 - padding && mouseX <= leftEdge + 215 + padding) {
                    tooltip = (Component) (this.waypoint.enabled ? GuiSlotWaypoints.this.DISABLE : GuiSlotWaypoints.this.ENABLE);
                }
                else {
                    String tooltipText = "X: " + this.waypoint.getX() + " Z: " + this.waypoint.getZ();
                    if (this.waypoint.getY() > 0) {
                        tooltipText = tooltipText + " Y: " + this.waypoint.getY();
                    }
                    tooltip = (Component) new TextComponent(tooltipText);
                }
                if (mouseX >= GuiSlotWaypoints.this.x0 && mouseX <= GuiSlotWaypoints.this.x1 && mouseY >= GuiSlotWaypoints.this.y0 && mouseY <= GuiSlotWaypoints.this.y1) {
                    GuiWaypoints.setTooltip(GuiSlotWaypoints.this.parentGui, tooltip);
                }
            }
            GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GLUtils.img2("textures/mob_effect/" + (this.waypoint.enabled ? "night_vision.png" : "blindness.png"));
            GuiComponent.blit(matrixStack, leftEdge + 198, slotYPos - 2, GuiSlotWaypoints.this.getBlitOffset(), 0.0f, 0.0f, 18, 18, 18, 18);
            if (this.waypoint == this.parentGui.highlightedWaypoint) {
                final int x = leftEdge + 199;
                final int y = slotYPos - 1;
                GLShim.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
                final TextureAtlas textureAtlas = this.parentGui.waypointManager.getTextureAtlas();
                GLUtils.img2(new ResourceLocation("voxelmap", "images/waypoints/target.png"));
                final Sprite icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/target.png");
                GuiSlotWaypoints.this.drawTexturedModalRect(x, y, icon, 16, 16);
                GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        
        public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
            GuiSlotWaypoints.this.setSelected(this);
            final int leftEdge = this.parentGui.getWidth() / 2 - 92 - 16;
            final byte padding = 3;
            final int width = 215;
            if (mouseX >= leftEdge + width - 16 - padding && mouseX <= leftEdge + width + padding) {
                if (GuiSlotWaypoints.this.doubleclick) {
                    this.parentGui.setHighlightedWaypoint();
                }
                this.parentGui.toggleWaypointVisibility();
            }
            else if (GuiSlotWaypoints.this.doubleclick) {
                this.parentGui.editWaypoint(this.parentGui.selectedWaypoint);
            }
            return true;
        }
        
        public int compareTo(final WaypointItem arg0) {
            return this.waypoint.compareTo(arg0.waypoint);
        }
    }

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		// TODO Auto-generated method stub
		
	}
}
