// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.CustomMob;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiMobs extends GuiScreenMinimap
{
	private final Minecraft game = Minecraft.getInstance();
    private final Screen parentScreen;
    protected final RadarSettingsManager options;
    protected TranslatableComponent screenTitle;
    private GuiSlotMobs mobsList;
    private Button buttonEnable;
    private Button buttonDisable;
    protected EditBox filter;
    private Component tooltip;
    protected String selectedMobId;
    
    public GuiMobs(final Screen parentScreen, final RadarSettingsManager options) {
        this.tooltip = null;
        this.selectedMobId = null;
        this.parentScreen = parentScreen;
        this.options = options;
    }
    
    public void tick() {
        this.filter.tick();
    }
    
    public void init() {
        this.screenTitle = new TranslatableComponent("options.minimap.mobs.title");
        this.game.keyboardHandler.setSendRepeatsToGui(true);
        this.mobsList = new GuiSlotMobs(this);
        final int filterStringWidth = this.getFontRenderer().width(I18nUtils.getString("minimap.waypoints.filter", new Object[0]) + ":");
        (this.filter = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 153 + filterStringWidth + 5, this.getHeight() - 56, 305 - filterStringWidth - 5, 20, null)).setMaxLength(35);
        this.addRenderableWidget(this.filter);
        this.addRenderableWidget((this.buttonEnable = new Button(this.getWidth() / 2 - 154, this.getHeight() - 28, 100, 20, new TranslatableComponent("options.minimap.mobs.enable"), button -> this.setMobEnabled(this.selectedMobId, true))));
        this.addRenderableWidget((this.buttonDisable = new Button(this.getWidth() / 2 - 50, this.getHeight() - 28, 100, 20, new TranslatableComponent("options.minimap.mobs.disable"), button -> this.setMobEnabled(this.selectedMobId, false))));
        this.addRenderableWidget(new Button(this.getWidth() / 2 + 4 + 50, this.getHeight() - 28, 100, 20, new TranslatableComponent("gui.done"), button -> this.getMinecraft().setScreen(this.parentScreen)));
        this.setFocused((GuiEventListener) this.filter);
        this.filter.setFocus(true);
        final boolean isSomethingSelected = this.selectedMobId != null;
        this.buttonEnable.active = isSomethingSelected;
        this.buttonDisable.active = isSomethingSelected;
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        final boolean OK = super.keyPressed(keysm, scancode, b);
        if (this.filter.isFocused()) {
            this.mobsList.updateFilter(this.filter.getValue().toLowerCase());
        }
        return OK;
    }
    
    public boolean charTyped(final char character, final int keycode) {
        final boolean OK = super.charTyped(character, keycode);
        if (this.filter.isFocused()) {
            this.mobsList.updateFilter(this.filter.getValue().toLowerCase());
        }
        return OK;
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        this.mobsList.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseButton) {
        this.mobsList.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }
    
    public boolean mouseDragged(final double mouseX, final double mouseY, final int mouseEvent, final double deltaX, final double deltaY) {
        return this.mobsList.mouseDragged(mouseX, mouseY, mouseEvent, deltaX, deltaY);
    }
    
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        return this.mobsList.mouseScrolled(mouseX, mouseY, amount);
    }
    
    protected void setSelectedMob(final String id) {
        this.selectedMobId = id;
    }
    
    private boolean isMobEnabled(final String mobId) {
        final EnumMobs mob = EnumMobs.getMobByName(mobId);
        if (mob != null) {
            return mob.enabled;
        }
        final CustomMob customMob = CustomMobsManager.getCustomMobByType(mobId);
        return customMob != null && customMob.enabled;
    }
    
    private void setMobEnabled(final String mobId, final boolean enabled) {
        for (final EnumMobs mob : EnumMobs.values()) {
            if (mob.id.equals(mobId)) {
                mob.enabled = enabled;
            }
        }
        for (final CustomMob mob2 : CustomMobsManager.mobs) {
            if (mob2.id.equals(mobId)) {
                mob2.enabled = enabled;
            }
        }
    }
    
    protected void toggleMobVisibility() {
        final EnumMobs mob = EnumMobs.getMobByName(this.selectedMobId);
        if (mob != null) {
            this.setMobEnabled(this.selectedMobId, !mob.enabled);
        }
        else {
            final CustomMob customMob = CustomMobsManager.getCustomMobByType(this.selectedMobId);
            if (customMob != null) {
                this.setMobEnabled(this.selectedMobId, !customMob.enabled);
            }
        }
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialticks) {
        super.drawMap(matrixStack);
        this.tooltip = null;
        this.mobsList.render(matrixStack, mouseX, mouseY, partialticks);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.screenTitle, this.getWidth() / 2, 20, 16777215);
        final boolean isSomethingSelected = this.selectedMobId != null;
        this.buttonEnable.active = (isSomethingSelected && !this.isMobEnabled(this.selectedMobId));
        this.buttonDisable.active = (isSomethingSelected && this.isMobEnabled(this.selectedMobId));
        super.render(matrixStack, mouseX, mouseY, partialticks);
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("minimap.waypoints.filter", new Object[0]) + ":", this.getWidth() / 2 - 153, this.getHeight() - 51, 10526880);
        this.filter.render(matrixStack, mouseX, mouseY, partialticks);
        if (this.tooltip != null) {
            this.renderTooltip(matrixStack, this.tooltip, mouseX, mouseY);
        }
    }
    
    static Component setTooltip(final GuiMobs par0GuiWaypoints, final Component par1Str) {
        return par0GuiWaypoints.tooltip = par1Str;
    }
    
    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        super.removed();
    }
}
