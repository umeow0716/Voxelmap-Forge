// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;

public class GuiSubworldEdit extends GuiScreenMinimap implements BooleanConsumer
{
	private final Minecraft game = Minecraft.getInstance();;
    private Screen parent;
    private IWaypointManager waypointManager;
    private ArrayList<String> knownSubworldNames;
    private String originalSubworldName;
    private String currentSubworldName;
    private EditBox subworldNameField;
    private Button doneButton;
    private Button deleteButton;
    private boolean deleteClicked;
    
    public GuiSubworldEdit(final Screen parent, final IVoxelMap master, final String subworldName) {
        this.originalSubworldName = "";
        this.currentSubworldName = "";
        this.deleteClicked = false;
        this.parent = parent;
        this.waypointManager = master.getWaypointManager();
        this.originalSubworldName = subworldName;
        this.knownSubworldNames = new ArrayList<String>(this.waypointManager.getKnownSubworldNames());
    }
    
    public void tick() {
        this.subworldNameField.tick();
    }
    
    public void init() {
        this.game.keyboardHandler.setSendRepeatsToGui(true);
        this.getButtonList().clear();
        this.setFocused((GuiEventListener) (this.subworldNameField = new EditBox(this.getFontRenderer(), this.getWidth() / 2 - 100, this.getHeight() / 6 + 0 + 13, 200, 20, (Component)null)));
        this.subworldNameField.setFocus(true);
        this.subworldNameField.setValue(this.originalSubworldName);
        this.addRenderableWidget(this.subworldNameField);
        this.addRenderableWidget((this.doneButton = new Button(this.getWidth() / 2 - 155, this.getHeight() / 6 + 168, 150, 20, new TranslatableComponent("gui.done"), button -> this.changeNameClicked())));
        this.addRenderableWidget(new Button(this.getWidth() / 2 + 5, this.getHeight() / 6 + 168, 150, 20, new TranslatableComponent("gui.cancel"), button -> this.getMinecraft().setScreen(this.parent)));
        final int buttonListY = this.getHeight() / 6 + 82 + 6;
        this.addRenderableWidget((this.deleteButton = new Button(this.getWidth() / 2 - 50, buttonListY + 24, 100, 20, new TranslatableComponent("selectServer.delete"), button -> this.deleteClicked())));
        this.doneButton.active = this.isNameAcceptable();
        this.deleteButton.active = this.originalSubworldName.equals(this.subworldNameField.getValue());
    }
    
    @Override
    public void removed() {
        this.game.keyboardHandler.setSendRepeatsToGui(false);
    }
    
    private void changeNameClicked() {
        if (!this.currentSubworldName.equals(this.originalSubworldName)) {
            this.waypointManager.changeSubworldName(this.originalSubworldName, this.currentSubworldName);
        }
        this.getMinecraft().setScreen(this.parent);
    }
    
    private void deleteClicked() {
        this.deleteClicked = true;
        final TranslatableComponent title = new TranslatableComponent("worldmap.subworld.deleteconfirm");
        final TranslatableComponent explanation = new TranslatableComponent("selectServer.deleteWarning", new Object[] { this.originalSubworldName });
        final TranslatableComponent affirm = new TranslatableComponent("selectServer.deleteButton");
        final TranslatableComponent deny = new TranslatableComponent("gui.cancel");
        final ConfirmScreen confirmScreen = new ConfirmScreen((BooleanConsumer)this, title, explanation, affirm, deny);
        this.getMinecraft().setScreen((Screen)confirmScreen);
    }
    
    public void accept(final boolean par1) {
        if (this.deleteClicked) {
            this.deleteClicked = false;
            if (par1) {
                this.waypointManager.deleteSubworld(this.originalSubworldName);
            }
            this.getMinecraft().setScreen(this.parent);
        }
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        final boolean OK = super.keyPressed(keysm, scancode, b);
        final boolean acceptable = this.isNameAcceptable();
        this.doneButton.active = this.isNameAcceptable();
        this.deleteButton.active = this.originalSubworldName.equals(this.subworldNameField.getValue());
        if ((keysm == 257 || keysm == 335) && acceptable) {
            this.changeNameClicked();
        }
        return OK;
    }
    
    public boolean charTyped(final char character, final int keycode) {
        final boolean OK = super.charTyped(character, keycode);
        final boolean acceptable = this.isNameAcceptable();
        this.doneButton.active = this.isNameAcceptable();
        this.deleteButton.active = this.originalSubworldName.equals(this.subworldNameField.getValue());
        if (character == '\r' && acceptable) {
            this.changeNameClicked();
        }
        return OK;
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int par3) {
        this.subworldNameField.mouseClicked(mouseX, mouseY, par3);
        return super.mouseClicked(mouseX, mouseY, par3);
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.drawMap(matrixStack);
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.edit", new Object[0]), this.getWidth() / 2, 20, 16777215);
        drawString(matrixStack, this.getFontRenderer(), I18nUtils.getString("worldmap.subworld.name", new Object[0]), this.getWidth() / 2 - 100, this.getHeight() / 6 + 0, 10526880);
        this.subworldNameField.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    private boolean isNameAcceptable() {
        boolean acceptable = true;
        this.currentSubworldName = this.subworldNameField.getValue();
        acceptable = (acceptable && this.currentSubworldName.length() > 0);
        acceptable = (acceptable && (this.currentSubworldName.equals(this.originalSubworldName) || !this.knownSubworldNames.contains(this.currentSubworldName)));
        return acceptable;
    }
}
