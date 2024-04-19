// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.CommandUtils;
import com.mamiyaotaru.voxelmap.util.GameVariableAccessShim;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import java.util.TreeSet;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import java.util.Random;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiWaypoints extends GuiScreenMinimap implements IGuiWaypoints
{
	private final Minecraft game = Minecraft.getInstance();
    private final Screen parentScreen;
    private IVoxelMap master;
    protected final MapSettingsManager options;
    protected final IWaypointManager waypointManager;
    protected TranslatableComponent screenTitle;
    private GuiSlotWaypoints waypointList;
    private Button buttonEdit;
    private boolean editClicked;
    private Button buttonDelete;
    private boolean deleteClicked;
    private Button buttonHighlight;
    private Button buttonShare;
    private Button buttonTeleport;
    private Button buttonSortName;
    private Button buttonSortCreated;
    private Button buttonSortDistance;
    private Button buttonSortColor;
    protected EditBox filter;
    private boolean addClicked;
    private Component tooltip;
    protected Waypoint selectedWaypoint;
    protected Waypoint highlightedWaypoint;
    protected Waypoint newWaypoint;
    private Random generator;
    private boolean changedSort;
    
    public GuiWaypoints(final Screen parentScreen, final IVoxelMap master) {
        this.editClicked = false;
        this.deleteClicked = false;
        this.addClicked = false;
        this.tooltip = null;
        this.selectedWaypoint = null;
        this.highlightedWaypoint = null;
        this.newWaypoint = null;
        this.generator = new Random();
        this.changedSort = false;
        this.master = master;
        this.parentScreen = parentScreen;
        this.options = master.getMapOptions();
        this.waypointManager = master.getWaypointManager();
        this.highlightedWaypoint = this.waypointManager.getHighlightedWaypoint();
    }
    
    public void tick() {
        this.filter.tick();
    }
    
    public void init() {
        this.screenTitle = new TranslatableComponent("minimap.waypoints.title");
        this.game.keyboardHandler.setSendRepeatsToGui(true);
        this.waypointList = new GuiSlotWaypoints(this);
        this.addRenderableWidget((this.buttonSortName = new Button(this.getWidth() / 2 - 154, 34, 77, 20, new TranslatableComponent("minimap.waypoints.sortbyname"), button -> {
            this.options.getClass();
            this.sortClicked(2);
        })));
        this.addRenderableWidget((this.buttonSortDistance = new Button(this.getWidth() / 2 - 77, 34, 77, 20, new TranslatableComponent("minimap.waypoints.sortbydistance"), button -> {
            this.options.getClass();
            this.sortClicked(3);
        })));
        this.addRenderableWidget((this.buttonSortCreated = new Button(this.getWidth() / 2, 34, 77, 20, new TranslatableComponent("minimap.waypoints.sortbycreated"), button -> {
            this.options.getClass();
            this.sortClicked(1);
        })));
        this.addRenderableWidget((this.buttonSortColor = new Button(this.getWidth() / 2 + 77, 34, 77, 20, new TranslatableComponent("minimap.waypoints.sortbycolor"), button -> {
            this.options.getClass();
            this.sortClicked(4);
        })));
        final int filterStringWidth = this.getFontRenderer().width(I18nUtils.getString("minimap.waypoints.filter", new Object[0]) + ":");
        (this.filter = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 80, 305 - filterStringWidth - 5, 20, null)).setMaxLength(35);
        this.addRenderableWidget(this.filter);
        this.addRenderableWidget((this.buttonEdit = new Button(this.getWidth() / 2 - 154, this.getHeight() - 52, 74, 20, new TranslatableComponent("selectServer.edit"), button -> this.editWaypoint(this.selectedWaypoint))));
        this.addRenderableWidget((this.buttonDelete = new Button(this.getWidth() / 2 - 76, this.getHeight() - 52, 74, 20, new TranslatableComponent("selectServer.delete"), button -> this.deleteClicked())));
        this.addRenderableWidget((this.buttonHighlight = new Button(this.getWidth() / 2 + 2, this.getHeight() - 52, 74, 20, new TranslatableComponent("minimap.waypoints.highlight"), button -> this.setHighlightedWaypoint())));
        this.addRenderableWidget((this.buttonTeleport = new Button(this.getWidth() / 2 + 80, this.getHeight() - 52, 74, 20, new TranslatableComponent("minimap.waypoints.teleportto"), button -> this.teleportClicked())));
        this.addRenderableWidget((this.buttonShare = new Button(this.getWidth() / 2 - 154, this.getHeight() - 28, 74, 20, new TranslatableComponent("minimap.waypoints.share"), button -> CommandUtils.sendWaypoint(this.selectedWaypoint, this))));
        this.addRenderableWidget(new Button(this.getWidth() / 2 - 76, this.getHeight() - 28, 74, 20, new TranslatableComponent("minimap.waypoints.newwaypoint"), button -> this.addWaypoint()));
        this.addRenderableWidget(new Button(this.getWidth() / 2 + 2, this.getHeight() - 28, 74, 20, new TranslatableComponent("menu.options"), button -> this.getMinecraft().setScreen((Screen)new GuiWaypointsOptions(this, this.options))));
        this.addRenderableWidget(new Button(this.getWidth() / 2 + 80, this.getHeight() - 28, 74, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parentScreen)));
        this.setFocused(this.filter);
        this.filter.setFocus(true);
        final boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.active = isSomethingSelected;
        this.buttonDelete.active = isSomethingSelected;
        this.buttonHighlight.active = isSomethingSelected;
        this.buttonShare.active = isSomethingSelected;
        this.buttonTeleport.active = (isSomethingSelected && this.canTeleport());
        this.sort();
    }
    
    private void sort() {
        final int sortKey = Math.abs(this.options.sort);
        final boolean ascending = this.options.sort > 0;
        this.waypointList.sortBy(sortKey, ascending);
        final String arrow = ascending ? "\u2191" : "\u2193";
        final int n = sortKey;
        this.options.getClass();
        if (n == 2) {
            this.buttonSortName.setMessage(new TextComponent(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbyname", new Object[0]) + " " + arrow));
        }
        else {
            this.buttonSortName.setMessage(new TranslatableComponent("minimap.waypoints.sortbyname"));
        }
        final int n2 = sortKey;
        this.options.getClass();
        if (n2 == 3) {
            this.buttonSortDistance.setMessage(new TextComponent(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbydistance", new Object[0]) + " " + arrow));
        }
        else {
            this.buttonSortDistance.setMessage(new TranslatableComponent("minimap.waypoints.sortbydistance"));
        }
        final boolean b = sortKey != 0;
        this.options.getClass();
        if (b) {
            this.buttonSortCreated.setMessage(new TextComponent(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbycreated", new Object[0]) + " " + arrow));
        }
        else {
            this.buttonSortCreated.setMessage(new TranslatableComponent("minimap.waypoints.sortbycreated"));
        }
        final int n3 = sortKey;
        this.options.getClass();
        if (n3 == 4) {
            this.buttonSortColor.setMessage(new TextComponent(arrow + " " + I18nUtils.getString("minimap.waypoints.sortbycolor", new Object[0]) + " " + arrow));
        }
        else {
            this.buttonSortColor.setMessage(new TranslatableComponent("minimap.waypoints.sortbycolor"));
        }
    }
    
    private void deleteClicked() {
        final String var2 = this.selectedWaypoint.name;
        if (var2 != null) {
            this.deleteClicked = true;
            final TranslatableComponent title = new TranslatableComponent("minimap.waypoints.deleteconfirm");
            final TranslatableComponent explanation = new TranslatableComponent("selectServer.deleteWarning", new Object[] { var2 });
            final TranslatableComponent affirm = new TranslatableComponent("selectServer.deleteButton");
            final TranslatableComponent deny = new TranslatableComponent("gui.cancel");
            final ConfirmScreen confirmScreen = new ConfirmScreen((BooleanConsumer)this, title, explanation, affirm, deny);
            this.getMinecraft().setScreen((Screen)confirmScreen);
        }
    }
    
    private void teleportClicked() {
        final boolean mp = !this.minecraft.isLocalServer();
        final int y = (this.selectedWaypoint.getY() > 0) ? this.selectedWaypoint.getY() : (this.options.game.player.level.dimensionType().hasCeiling() ? 64 : 255);
        this.options.game.player.chat("/tp " + this.options.game.player.getName().getString() + " " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
        if (mp) {
            this.options.game.player.chat("/tppos " + this.selectedWaypoint.getX() + " " + y + " " + this.selectedWaypoint.getZ());
        }
        this.getMinecraft().setScreen((Screen)null);
    }
    
    protected void sortClicked(final int id) {
        this.options.setSort(id);
        this.changedSort = true;
        this.sort();
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        final boolean OK = super.keyPressed(keysm, scancode, b);
        if (this.filter.isFocused()) {
            this.waypointList.updateFilter(this.filter.getValue().toLowerCase());
        }
        return OK;
    }
    
    public boolean charTyped(final char character, final int keycode) {
        final boolean OK = super.charTyped(character, keycode);
        if (this.filter.isFocused()) {
            this.waypointList.updateFilter(this.filter.getValue().toLowerCase());
        }
        return OK;
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        this.waypointList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseButton) {
        this.waypointList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }
    
    public boolean mouseDragged(final double mouseX, final double mouseY, final int mouseEvent, final double deltaX, final double deltaY) {
        return this.waypointList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }
    
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        return this.waypointList.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean isEditing() {
        return this.editClicked;
    }
    
    public void accept(final boolean par1) {
        if (this.deleteClicked) {
            this.deleteClicked = false;
            if (par1) {
                this.waypointManager.deleteWaypoint(this.selectedWaypoint);
                this.selectedWaypoint = null;
            }
            this.getMinecraft().setScreen((Screen)this);
        }
        if (this.editClicked) {
            this.editClicked = false;
            if (par1) {
                this.waypointManager.saveWaypoints();
            }
            this.getMinecraft().setScreen((Screen)this);
        }
        if (this.addClicked) {
            this.addClicked = false;
            if (par1) {
                this.waypointManager.addWaypoint(this.newWaypoint);
                this.setSelectedWaypoint(this.newWaypoint);
            }
            this.getMinecraft().setScreen((Screen)this);
        }
    }
    
    protected void setSelectedWaypoint(final Waypoint waypoint) {
        this.selectedWaypoint = waypoint;
        final boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.active = isSomethingSelected;
        this.buttonDelete.active = isSomethingSelected;
        this.buttonHighlight.active = isSomethingSelected;
        this.buttonHighlight.setMessage(new TranslatableComponent((isSomethingSelected && this.selectedWaypoint == this.highlightedWaypoint) ? "minimap.waypoints.removehighlight" : "minimap.waypoints.highlight"));
        this.buttonShare.active = isSomethingSelected;
        this.buttonTeleport.active = (isSomethingSelected && this.canTeleport());
    }
    
    protected void setHighlightedWaypoint() {
        this.waypointManager.setHighlightedWaypoint(this.selectedWaypoint, true);
        this.highlightedWaypoint = this.waypointManager.getHighlightedWaypoint();
        final boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonHighlight.setMessage(new TranslatableComponent((isSomethingSelected && this.selectedWaypoint == this.highlightedWaypoint) ? "minimap.waypoints.removehighlight" : "minimap.waypoints.highlight"));
    }
    
    protected void editWaypoint(final Waypoint waypoint) {
        this.editClicked = true;
        this.getMinecraft().setScreen((Screen)new GuiAddWaypoint(this, this.master, waypoint, true));
    }
    
    protected void addWaypoint() {
        this.addClicked = true;
        float r;
        float g;
        float b;
        if (this.waypointManager.getWaypoints().size() == 0) {
            r = 0.0f;
            g = 1.0f;
            b = 0.0f;
        }
        else {
            r = this.generator.nextFloat();
            g = this.generator.nextFloat();
            b = this.generator.nextFloat();
        }
        final TreeSet<DimensionContainer> dimensions = new TreeSet<DimensionContainer>();
        dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(this.game.level));
        final double dimensionScale = this.options.game.player.level.dimensionType().coordinateScale();
        this.newWaypoint = new Waypoint("", (int)(GameVariableAccessShim.xCoord() * dimensionScale), (int)(GameVariableAccessShim.zCoord() * dimensionScale), GameVariableAccessShim.yCoord(), true, r, g, b, "", this.master.getWaypointManager().getCurrentSubworldDescriptor(false), dimensions);
        this.getMinecraft().setScreen((Screen)new GuiAddWaypoint(this, this.master, this.newWaypoint, false));
    }
    
    protected void toggleWaypointVisibility() {
        this.selectedWaypoint.enabled = !this.selectedWaypoint.enabled;
        this.waypointManager.saveWaypoints();
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.tooltip = null;
        this.waypointList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter", new Object[0]) + ":", this.getWidth() / 2 - 153, this.getHeight() - 75, 10526880);
        this.filter.render(matrixStack, mouseX, mouseY, partialTicks);
        if (this.tooltip != null) {
            this.renderTooltip(matrixStack, this.tooltip, mouseX, mouseY);
        }
    }
    
    static Component setTooltip(final GuiWaypoints par0GuiWaypoints, final Component par1Str) {
        return par0GuiWaypoints.tooltip = par1Str;
    }
    
    public boolean canTeleport() {
        boolean allowed = false;
        final boolean singlePlayer = this.options.game.isLocalServer();
        if (singlePlayer) {
            try {
                allowed = this.getMinecraft().getSingleplayerServer().getPlayerList().isOp(this.game.player.getGameProfile());
            }
            catch (final Exception e) {
                allowed = this.getMinecraft().getSingleplayerServer().getWorldData().getAllowCommands();
            }
        }
        else {
            allowed = true;
        }
        return allowed;
    }
    
    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        if (this.changedSort) {
            super.removed();
        }
    }
}
