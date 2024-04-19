// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiSelectPlayer extends GuiScreenMinimap implements BooleanConsumer
{
	private final Minecraft game = Minecraft.getInstance();
    private final Screen parentScreen;
    protected Component screenTitle;
    private boolean sharingWaypoint;
    private GuiButtonRowListPlayers playerList;
    protected boolean allClicked;
    protected EditBox message;
    protected EditBox filter;
    private Component tooltip;
    private String locInfo;
    final MutableComponent SHARE_MESSAGE;
    final TranslatableComponent SHARE_WITH;
    final TranslatableComponent SHARE_WAYPOINT;
    final TranslatableComponent SHARE_COORDINATES;
    
    public GuiSelectPlayer(final Screen parentScreen, final IVoxelMap master, final String locInfo, final boolean sharingWaypoint) {
    	this.screenTitle = new TextComponent("players");
        this.sharingWaypoint = true;
        this.allClicked = false;
        this.tooltip = null;
        this.SHARE_MESSAGE = new TranslatableComponent("minimap.waypointshare.sharemessage").append(":");
        this.SHARE_WITH = new TranslatableComponent("minimap.waypointshare.sharewith");
        this.SHARE_WAYPOINT = new TranslatableComponent("minimap.waypointshare.title");
        this.SHARE_COORDINATES = new TranslatableComponent("minimap.waypointshare.titlecoordinate");
        this.parentScreen = parentScreen;
        this.locInfo = locInfo;
        this.sharingWaypoint = sharingWaypoint;
    }
    
    public void tick() {
        this.message.tick();
        this.filter.tick();
    }
    
    public void init() {
        this.screenTitle = (this.sharingWaypoint ? this.SHARE_WAYPOINT : this.SHARE_COORDINATES);
        this.game.keyboardHandler.setSendRepeatsToGui(true);
        this.playerList = new GuiButtonRowListPlayers(this);
        final int messageStringWidth = this.getFontRenderer().width(I18nUtils.getString("minimap.waypointshare.sharemessage", new Object[0]) + ":");
        (this.message = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 153 + messageStringWidth + 5, 34, 305 - messageStringWidth - 5, 20, null)).setMaxLength(78);
        this.addRenderableWidget(this.message);
        final int filterStringWidth = this.getFontRenderer().width(I18nUtils.getString("minimap.waypoints.filter", new Object[0]) + ":");
        (this.filter = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 55, 305 - filterStringWidth - 5, 20, null)).setMaxLength(35);
        this.addRenderableWidget(this.filter);
        this.addRenderableWidget(new Button(this.width / 2 - 75, this.height - 27, 150, 20, new TranslatableComponent("gui.cancel"), button -> this.getMinecraft().setScreen(this.parentScreen)));
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        final boolean OK = super.keyPressed(keysm, scancode, b);
        if (this.filter.isFocused()) {
            this.playerList.updateFilter(this.filter.getValue().toLowerCase());
        }
        return OK;
    }
    
    public boolean charTyped(final char character, final int keycode) {
        final boolean OK = super.charTyped(character, keycode);
        if (this.filter.isFocused()) {
            this.playerList.updateFilter(this.filter.getValue().toLowerCase());
        }
        return OK;
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        this.playerList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseButton) {
        this.playerList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }
    
    public boolean mouseDragged(final double mouseX, final double mouseY, final int mouseEvent, final double deltaX, final double deltaY) {
        return this.playerList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }
    
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        return this.playerList.mouseScrolled(mouseX, mouseY, amount);
    }
    
    public void accept(final boolean par1) {
        if (this.allClicked) {
            this.allClicked = false;
            if (par1) {
                final String combined = this.message.getValue() + " " + this.locInfo;
                if (combined.length() > 100) {
                    this.minecraft.player.chat(this.message.getValue());
                    this.minecraft.player.chat(this.locInfo);
                }
                else {
                    this.minecraft.player.chat(combined);
                }
                this.getMinecraft().setScreen(null);
            }
            else {
                this.getMinecraft().setScreen(this);
            }
        }
    }
    
    protected void sendMessageToPlayer(final String name) {
        final String combined = "/msg " + name + " " + this.message.getValue() + " " + this.locInfo;
        if (combined.length() > 100) {
            this.game.player.chat("/msg " + name + " " + this.message.getValue());
            this.game.player.chat("/msg " + name + " " + this.locInfo);
        }
        else {
            this.game.player.chat(combined);
        }
        this.game.setScreen(this.parentScreen);
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.tooltip = null;
        this.playerList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.getFontRenderer(), this.SHARE_MESSAGE, this.getWidth() / 2 - 153, 39, 10526880);
        this.message.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.SHARE_WITH, this.getWidth() / 2, 75, 16777215);
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter", new Object[0]) + ":", this.getWidth() / 2 - 153, this.getHeight() - 50, 10526880);
        this.filter.render(matrixStack, mouseX, mouseY, partialTicks);
        if (this.tooltip != null) {
            this.renderTooltip(matrixStack, this.tooltip, mouseX, mouseY);
        }
    }
    
    static Component setTooltip(final GuiSelectPlayer par0GuiWaypoints, final Component par1Str) {
        return par0GuiWaypoints.tooltip = par1Str;
    }
    
    @Override
    public void removed() {
        this.game.keyboardHandler.setSendRepeatsToGui(false);
    }
}
