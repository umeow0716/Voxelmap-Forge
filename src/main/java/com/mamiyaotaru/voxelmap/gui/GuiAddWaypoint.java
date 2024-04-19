// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.mamiyaotaru.voxelmap.gui.overridden.Popup;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiButton;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.gui.overridden.IPopupGuiScreen;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiAddWaypoint extends GuiScreenMinimap implements IPopupGuiScreen
{
	private static final Minecraft game = Minecraft.getInstance();
    IVoxelMap master;
    IWaypointManager waypointManager;
    IColorManager colorManager;
    private IGuiWaypoints parentGui;
    private PopupGuiButton doneButton;
    private GuiSlotDimensions dimensionList;
    protected DimensionContainer selectedDimension;
    private Component tooltip;
    private EditBox waypointName;
    private EditBox waypointX;
    private EditBox waypointZ;
    private EditBox waypointY;
    private PopupGuiButton buttonEnabled;
    protected Waypoint waypoint;
    private boolean choosingColor;
    private boolean choosingIcon;
    private float red;
    private float green;
    private float blue;
    private String suffix;
    private boolean enabled;
    private boolean editing;
    private final ResourceLocation pickerResourceLocation;
    private final ResourceLocation blank;
    
    public GuiAddWaypoint(final IGuiWaypoints par1GuiScreen, final IVoxelMap master, final Waypoint par2Waypoint, final boolean editing) {
        this.selectedDimension = null;
        this.tooltip = null;
        this.choosingColor = false;
        this.choosingIcon = false;
        this.editing = false;
        this.pickerResourceLocation = new ResourceLocation("voxelmap", "images/colorpicker.png");
        this.blank = new ResourceLocation("textures/misc/white.png");
        this.master = master;
        this.waypointManager = master.getWaypointManager();
        this.colorManager = master.getColorManager();
        this.parentGui = par1GuiScreen;
        this.waypoint = par2Waypoint;
        this.red = this.waypoint.red;
        this.green = this.waypoint.green;
        this.blue = this.waypoint.blue;
        this.suffix = this.waypoint.imageSuffix;
        this.enabled = this.waypoint.enabled;
        this.editing = editing;
    }
    
    public void tick() {
        this.waypointName.tick();
        this.waypointX.tick();
        this.waypointY.tick();
        this.waypointZ.tick();
    }
    
    public void init() {
    	game.keyboardHandler.setSendRepeatsToGui(true);
        this.clearWidgets();
        (this.waypointName = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 0 + 13, 200, 20, (Component)null)).setValue(this.waypoint.name);
        (this.waypointX = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 41 + 13, 56, 20, (Component)null)).setMaxLength(128);
        this.waypointX.setValue("" + this.waypoint.getX());
        (this.waypointZ = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 28, this.getHeight() / 6 + 41 + 13, 56, 20, (Component)null)).setMaxLength(128);
        this.waypointZ.setValue("" + this.waypoint.getZ());
        (this.waypointY = new EditBox(this.getFontRenderer(), this.getWidth() / 2 + 44, this.getHeight() / 6 + 41 + 13, 56, 20, (Component)null)).setMaxLength(128);
        this.waypointY.setValue("" + this.waypoint.getY());
        this.addRenderableWidget(this.waypointName);
        this.addRenderableWidget(this.waypointX);
        this.addRenderableWidget(this.waypointZ);
        this.addRenderableWidget(this.waypointY);
        final int buttonListY = this.getHeight() / 6 + 82 + 6;
        this.addRenderableWidget((this.buttonEnabled = new PopupGuiButton(this.getWidth() / 2 - 101, buttonListY + 0, 100, 20, (Component)new TextComponent("Enabled: " + (this.waypoint.enabled ? "On" : "Off")), button -> this.waypoint.enabled = !this.waypoint.enabled, this)));
        this.addRenderableWidget(new PopupGuiButton(this.getWidth() / 2 - 101, buttonListY + 24, 100, 20, (Component)new TextComponent(I18nUtils.getString("minimap.waypoints.sortbycolor", new Object[0]) + ":     "), button -> this.choosingColor = true, this));
        this.addRenderableWidget(new PopupGuiButton(this.getWidth() / 2 - 101, buttonListY + 48, 100, 20, (Component)new TextComponent(I18nUtils.getString("minimap.waypoints.sortbyicon", new Object[0]) + ":     "), button -> this.choosingIcon = true, this));
        this.addRenderableWidget((this.doneButton = new PopupGuiButton(this.getWidth() / 2 - 155, this.getHeight() / 6 + 168, 150, 20, (Component)new TranslatableComponent("addServer.add"), button -> this.acceptWaypoint(), this)));
        this.addRenderableWidget(new PopupGuiButton(this.getWidth() / 2 + 5, this.getHeight() / 6 + 168, 150, 20, (Component)new TranslatableComponent("gui.cancel"), button -> this.cancelWaypoint(), this));
        this.doneButton.active = (this.waypointName.getValue().length() > 0);
        this.setFocused((GuiEventListener)this.waypointName);
        this.waypointName.setFocus(true);
        this.dimensionList = new GuiSlotDimensions(this);
    }
    
    @Override
    public void removed() {
        game.keyboardHandler.setSendRepeatsToGui(false);
    }
    
    protected void cancelWaypoint() {
        this.waypoint.red = this.red;
        this.waypoint.green = this.green;
        this.waypoint.blue = this.blue;
        this.waypoint.imageSuffix = this.suffix;
        this.waypoint.enabled = this.enabled;
        if (this.parentGui != null) {
            this.parentGui.accept(false);
        }
        else {
            this.getMinecraft().setScreen((Screen)null);
        }
    }
    
    protected void acceptWaypoint() {
        this.waypoint.name = this.waypointName.getValue();
        this.waypoint.setX(Integer.parseInt(this.waypointX.getValue()));
        this.waypoint.setZ(Integer.parseInt(this.waypointZ.getValue()));
        this.waypoint.setY(Integer.parseInt(this.waypointY.getValue()));
        if (this.parentGui != null) {
            this.parentGui.accept(true);
        }
        else {
            if (this.editing) {
                this.waypointManager.saveWaypoints();
            }
            else {
                this.waypointManager.addWaypoint(this.waypoint);
            }
            this.getMinecraft().setScreen((Screen)null);
        }
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        boolean OK = false;
        if (!this.popupOpen()) {
            OK = super.keyPressed(keysm, scancode, b);
            boolean acceptable = this.waypointName.getValue().length() > 0;
            try {
                Integer.parseInt(this.waypointX.getValue());
                Integer.parseInt(this.waypointZ.getValue());
                Integer.parseInt(this.waypointY.getValue());
            }
            catch (final NumberFormatException e) {
                acceptable = false;
            }
            this.doneButton.active = acceptable;
            if ((keysm == 257 || keysm == 335) && acceptable) {
                this.acceptWaypoint();
            }
        }
        return OK;
    }
    
    public boolean charTyped(final char character, final int keycode) {
        boolean OK = false;
        if (!this.popupOpen()) {
            OK = super.charTyped(character, keycode);
            boolean acceptable = this.waypointName.getValue().length() > 0;
            try {
                Integer.parseInt(this.waypointX.getValue());
                Integer.parseInt(this.waypointZ.getValue());
                Integer.parseInt(this.waypointY.getValue());
            }
            catch (final NumberFormatException e) {
                acceptable = false;
            }
            this.doneButton.active = acceptable;
        }
        return OK;
    }
    
    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        if (!this.popupOpen()) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointName.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointX.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointZ.mouseClicked(mouseX, mouseY, mouseButton);
            this.waypointY.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else if (this.choosingColor) {
            if (mouseX >= this.getWidth() / 2 - 128 && mouseX < this.getWidth() / 2 + 128 && mouseY >= this.getHeight() / 2 - 128 && mouseY < this.getHeight() / 2 + 128) {
                final int color = this.colorManager.getColorPicker().getRGB((int)mouseX - (this.getWidth() / 2 - 128), (int)mouseY - (this.getHeight() / 2 - 128));
                this.waypoint.red = (color >> 16 & 0xFF) / 255.0f;
                this.waypoint.green = (color >> 8 & 0xFF) / 255.0f;
                this.waypoint.blue = (color >> 0 & 0xFF) / 255.0f;
                this.choosingColor = false;
            }
        }
        else if (this.choosingIcon) {
            final float scScale = (float)this.getMinecraft().getWindow().getGuiScale();
            final TextureAtlas chooser = this.waypointManager.getTextureAtlasChooser();
            float scale = scScale / 2.0f;
            float displayWidthFloat = chooser.getWidth() / scale;
            float displayHeightFloat = chooser.getHeight() / scale;
            if (displayWidthFloat > this.getMinecraft().getWindow().getWidth()) {
                final float adj = displayWidthFloat / this.getMinecraft().getWindow().getWidth();
                scale *= adj;
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }
            if (displayHeightFloat > this.getMinecraft().getWindow().getHeight()) {
                final float adj = displayHeightFloat / this.getMinecraft().getWindow().getHeight();
                scale *= adj;
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }
            final int displayWidth = (int)displayWidthFloat;
            final int displayHeight = (int)displayHeightFloat;
            if (mouseX >= this.getWidth() / 2 - displayWidth / 2 && mouseX < this.getWidth() / 2 + displayWidth / 2 && mouseY >= this.getHeight() / 2 - displayHeight / 2 && mouseY < this.getHeight() / 2 + displayHeight / 2) {
                final float x = ((float)mouseX - (this.getWidth() / 2 - displayWidth / 2)) * scale;
                final float y = ((float)mouseY - (this.getHeight() / 2 - displayHeight / 2)) * scale;
                final Sprite icon = chooser.getIconAt(x, y);
                if (icon != chooser.getMissingImage()) {
                    this.waypoint.imageSuffix = icon.getIconName().replace("voxelmap:images/waypoints/waypoint", "").replace(".png", "");
                    this.choosingIcon = false;
                }
            }
        }
        if (!this.popupOpen() && this.dimensionList != null) {
            this.dimensionList.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return true;
    }
    
    public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseButton) {
        if (!this.popupOpen() && this.dimensionList != null) {
            this.dimensionList.mouseReleased(mouseX, mouseY, mouseButton);
        }
        return true;
    }
    
    public boolean mouseDragged(final double mouseX, final double mouseY, final int mouseEvent, final double deltaX, final double deltaY) {
        return this.popupOpen() || this.dimensionList == null || this.dimensionList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }
    
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        return this.popupOpen() || this.dimensionList == null || this.dimensionList.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean overPopup(final int x, final int y) {
        return this.choosingColor || this.choosingIcon;
    }
    
    @Override
    public boolean popupOpen() {
        return this.choosingColor || this.choosingIcon;
    }
    
    @Override
    public void popupAction(final Popup popup, final int action) {
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.tooltip = null;
        this.buttonEnabled.setMessage(new TextComponent(I18nUtils.getString("minimap.waypoints.enabled", new Object[0]) + " " + (this.waypoint.enabled ? I18nUtils.getString("options.on", new Object[0]) : I18nUtils.getString("options.off", new Object[0]))));
        if (!this.choosingColor && !this.choosingIcon) {
            this.renderBackground(matrixStack);
        }
        
        /* DimensionList Widget*/
        this.dimensionList.render(matrixStack, mouseX, mouseY, partialTicks);
        
        /* Title {New Waypoint} */
        drawCenteredString(matrixStack, this.getFontRenderer(), ((this.parentGui != null && this.parentGui.isEditing()) || this.editing) ? I18nUtils.getString("minimap.waypoints.edit", new Object[0]) : I18nUtils.getString("minimap.waypoints.new", new Object[0]), this.getWidth() / 2, 20, 16777215);
        
        /* Waypoint Name */
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.name", new Object[0]), this.getWidth() / 2 - 100, this.getHeight() / 6 + 0, 10526880);
        this.waypointName.render(matrixStack, mouseX, mouseY, partialTicks);
        
        /* Waypoint X */
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("X", new Object[0]), this.getWidth() / 2 - 100, this.getHeight() / 6 + 41, 10526880);
        this.waypointX.render(matrixStack, mouseX, mouseY, partialTicks);
        
        /* Weypoint Z */
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("Z", new Object[0]), this.getWidth() / 2 - 28, this.getHeight() / 6 + 41, 10526880);
        this.waypointZ.render(matrixStack, mouseX, mouseY, partialTicks);
        
        /* Waypoint Y */
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("Y", new Object[0]), this.getWidth() / 2 + 44, this.getHeight() / 6 + 41, 10526880);
        this.waypointY.render(matrixStack, mouseX, mouseY, partialTicks);
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        
        final int buttonListY = this.getHeight() / 6 + 82 + 6;
        /* Chooing Color */
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(this.waypoint.red, this.waypoint.green, this.waypoint.blue, 1.0f);
        RenderSystem.setShaderTexture(0, this.blank);
        this.blit(matrixStack, this.getWidth() / 2 - 25, buttonListY + 24 + 5, 0, 0, 16, 10);
        
        /* Choosing Icon */
        final TextureAtlas chooser = this.waypointManager.getTextureAtlasChooser();
        RenderSystem.setShaderTexture(0, chooser.getId());
        Sprite icon = chooser.getAtlasSprite("voxelmap:images/waypoints/waypoint" + this.waypoint.imageSuffix + ".png");
        this.drawTexturedModalRect((float)(this.getWidth() / 2 - 25), (float)(buttonListY + 48 + 2), icon, 16.0f, 16.0f);
        
        if (this.choosingColor || this.choosingIcon) {
            this.renderBackground(matrixStack);
        }
        if (this.choosingColor) {
        	/* Draw Palette */
        	RenderSystem.texParameter(3553, 10241, 9728);
        	RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, this.pickerResourceLocation);
            this.blit(matrixStack, this.getWidth() / 2 - 128, this.getHeight() / 2 - 128, 0, 0, 256, 256);
        }
        if (this.choosingIcon) {
        	
        	/* Calculation Icon List Height and Width*/
        	final float scScale = (float)this.getMinecraft().getWindow().getGuiScale();
            float scale = scScale / 2.0f;
            float displayWidthFloat = chooser.getWidth() / scale;
            float displayHeightFloat = chooser.getHeight() / scale;
            if (displayWidthFloat > this.getMinecraft().getWindow().getWidth()) {
                final float adj = displayWidthFloat / this.getMinecraft().getWindow().getWidth();
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }
            if (displayHeightFloat > this.getMinecraft().getWindow().getHeight()) {
                final float adj = displayHeightFloat / this.getMinecraft().getWindow().getHeight();
                displayWidthFloat /= adj;
                displayHeightFloat /= adj;
            }
            final int displayWidth = (int)displayWidthFloat;
            final int displayHeight = (int)displayHeightFloat;
          
            /* White Background */
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, this.blank);
            this.blit(matrixStack, this.getWidth() / 2 - displayWidth / 2, this.getHeight() / 2 - displayHeight / 2, 0, 0, displayWidth, displayHeight);   
            
            /* Setup to draw Icon List*/
            RenderSystem.enableBlend();
            
            /* Drawing Icon List */
            RenderSystem.texParameter(3553, 10241, 9729);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(this.waypoint.red, this.waypoint.green, this.waypoint.blue, 1.0f);
            RenderSystem.setShaderTexture(0, chooser.getId());
            blit(matrixStack, this.getWidth() / 2 - displayWidth / 2, this.getHeight() / 2 - displayHeight / 2, displayWidth, displayHeight, 0.0f, 0.0f, chooser.getWidth(), chooser.getHeight(), chooser.getImageWidth(), chooser.getImageHeight());
            // this.drawTexturedModalRect((float)(this.getWidth() / 2 - 25), (float)(buttonListY + 48 + 2), icon, 16.0f, 16.0f);
            RenderSystem.disableBlend();
            RenderSystem.texParameter(3553, 10241, 9728);
            
            /* Hover on Icon */
            if (mouseX >= this.getWidth() / 2 - displayWidth / 2 && mouseX <= this.getWidth() / 2 + displayWidth / 2 && mouseY >= this.getHeight() / 2 - displayHeight / 2 && mouseY <= this.getHeight() / 2 + displayHeight / 2) {
                final float x = (mouseX - (this.getWidth() / 2 - displayWidth / 2)) * scale;
                final float y = (mouseY - (this.getHeight() / 2 - displayHeight / 2)) * scale;
                icon = chooser.getIconAt(x, y);
                if (icon != chooser.getMissingImage()) {
                	Component text = new TextComponent(icon.getIconName().replace("voxelmap:images/waypoints/waypoint", "").replace(".png", ""));
                    if(text.getString() == "") {
                    	this.tooltip = new TextComponent("default");
                    } else {
                    	this.tooltip = text;
                    }
                }
            }
        }
        if (this.tooltip != null) {
            this.renderTooltip(matrixStack, this.tooltip, mouseX, mouseY);
        }
    }
    
    public void setSelectedDimension(final DimensionContainer dimension) {
        this.selectedDimension = dimension;
    }
    
    public void toggleDimensionSelected() {
        if (this.waypoint.dimensions.size() > 1 && this.waypoint.dimensions.contains(this.selectedDimension) && this.selectedDimension != this.master.getDimensionManager().getDimensionContainerByWorld((Level)Minecraft.getInstance().level)) {
            this.waypoint.dimensions.remove(this.selectedDimension);
        }
        else if (!this.waypoint.dimensions.contains(this.selectedDimension)) {
            this.waypoint.dimensions.add(this.selectedDimension);
        }
    }
    
    static Component setTooltip(final GuiAddWaypoint par0GuiWaypoint, final Component par1Str) {
        return par0GuiWaypoint.tooltip = par1Str;
    }
    
    public void drawTexturedModalRect(final float xCoord, final float yCoord, final Sprite icon, final float widthIn, final float heightIn) {
    	final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        vertexBuffer.vertex((double)(xCoord + 0.0f), (double)(yCoord + heightIn), (double)this.getBlitOffset()).uv(icon.getMinU(), icon.getMaxV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + widthIn), (double)(yCoord + heightIn), (double)this.getBlitOffset()).uv(icon.getMaxU(), icon.getMaxV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + widthIn), (double)(yCoord + 0.0f), (double)this.getBlitOffset()).uv(icon.getMaxU(), icon.getMinV()).endVertex();
        vertexBuffer.vertex((double)(xCoord + 0.0f), (double)(yCoord + 0.0f), (double)this.getBlitOffset()).uv(icon.getMinU(), icon.getMinV()).endVertex();
        tessellator.end();
    }
}
