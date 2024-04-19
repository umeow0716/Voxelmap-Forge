// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import java.util.Collections;
import java.text.Collator;
import java.util.Comparator;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;

import net.minecraft.client.CameraType;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiScreenMinimap;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.Input;
import net.minecraft.network.chat.FormattedText;

public class GuiSubworldsSelect extends GuiScreenMinimap implements BooleanConsumer
{
	private final Minecraft game = Minecraft.getInstance();
    private TranslatableComponent title;
    private TranslatableComponent select;
    private boolean multiworld;
    private EditBox newNameField;
    private boolean newWorld;
    private float yaw;
    private CameraType thirdPersonViewOrig;
    private Button[] selectButtons;
    private Button[] editButtons;
    private String[] worlds;
    private Screen parent;
    LocalPlayer thePlayer;
    LocalPlayer camera;
    private IVoxelMap master;
    private IWaypointManager waypointManager;
    
    public GuiSubworldsSelect(final Screen parent, final IVoxelMap master) {
        this.multiworld = false;
        this.newWorld = false;
        this.minecraft = Minecraft.getInstance();
        this.parent = parent;
        this.thePlayer = this.game.player;
        this.camera = new LocalPlayer(this.game, this.game.level, this.game.getConnection(), this.thePlayer.getStats(), new ClientRecipeBook(), false, false);
        this.camera.input = (Input) new KeyboardInput(this.game.options);
        this.camera.moveTo(this.thePlayer.getX(), this.thePlayer.getY() - this.thePlayer.getMyRidingOffset(), this.thePlayer.getZ(), this.thePlayer.getYRot(), 0.0f);
        this.yaw = this.thePlayer.getYRot();
        this.thirdPersonViewOrig = this.game.options.getCameraType();
        this.master = master;
        this.waypointManager = master.getWaypointManager();
    }
    
    public void init() {
        final ArrayList<String> knownSubworldNames = new ArrayList<String>(this.waypointManager.getKnownSubworldNames());
        if (!this.multiworld && !this.waypointManager.isMultiworld() && !this.getMinecraft().isConnectedToRealms()) {
            final ConfirmScreen confirmScreen = new ConfirmScreen((BooleanConsumer)this, new TranslatableComponent("worldmap.multiworld.isthismultiworld"), new TranslatableComponent("worldmap.multiworld.explanation"), new TranslatableComponent("gui.yes"), new TranslatableComponent("gui.no"));
            this.game.setScreen((Screen)confirmScreen);
        }
        else {
            this.game.options.setCameraType(CameraType.FIRST_PERSON);
            this.game.setCameraEntity((Entity) this.camera);
        }
        this.title = new TranslatableComponent("worldmap.multiworld.title");
        this.select = new TranslatableComponent("worldmap.multiworld.select");
        this.getButtonList().clear();
        final int centerX = this.width / 2;
        int buttonsPerRow = this.width / 150;
        if (buttonsPerRow == 0) {
            buttonsPerRow = 1;
        }
        final int buttonWidth = this.width / buttonsPerRow - 5;
        final int xSpacing = (this.width - buttonsPerRow * buttonWidth) / 2;
        this.addRenderableWidget((new Button(centerX - 100, this.height - 30, 200, 20, new TranslatableComponent("gui.cancel"), button -> this.getMinecraft().setScreen((Screen)null))));
        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(knownSubworldNames, new Comparator<String>() {
            @Override
            public int compare(final String name1, final String name2) {
                return -collator.compare(name1, name2);
            }
        });
        final int numKnownSubworlds = knownSubworldNames.size();
        final int completeRows = (int)Math.floor((numKnownSubworlds + 1) / (float)buttonsPerRow);
        final int lastRowShiftBy = (int)(Math.ceil((numKnownSubworlds + 1) / (float)buttonsPerRow) * buttonsPerRow - (numKnownSubworlds + 1));
        this.worlds = new String[numKnownSubworlds];
        this.selectButtons = new Button[numKnownSubworlds + 1];
        this.editButtons = new Button[numKnownSubworlds + 1];
        for (int t = 0; t < numKnownSubworlds; ++t) {
            int shiftBy = 1;
            if (t / buttonsPerRow >= completeRows) {
                shiftBy = lastRowShiftBy + 1;
            }
            this.worlds[t] = knownSubworldNames.get(t);
            final int tt = t;
            this.selectButtons[t] = new Button((buttonsPerRow - shiftBy - t % buttonsPerRow) * buttonWidth + xSpacing, this.height - 60 - t / buttonsPerRow * 21, buttonWidth - 32, 20, new TextComponent(this.worlds[t]), button -> this.worldSelected(this.worlds[tt]));
            this.editButtons[t] = new Button((buttonsPerRow - shiftBy - t % buttonsPerRow) * buttonWidth + xSpacing + buttonWidth - 32, this.height - 60 - t / buttonsPerRow * 21, 30, 20, new TextComponent("\u2692"), button -> this.editWorld(this.worlds[tt]));
            this.addRenderableWidget(this.selectButtons[t]);
            this.addRenderableWidget(this.editButtons[t]);
        }
        final int numButtons = this.selectButtons.length - 1;
        if (!this.newWorld) {
            this.addRenderableWidget((this.selectButtons[numButtons] = new Button((buttonsPerRow - 1 - lastRowShiftBy - numButtons % buttonsPerRow) * buttonWidth + xSpacing, this.height - 60 - numButtons / buttonsPerRow * 21, buttonWidth - 2, 20, new TextComponent("< " + I18nUtils.getString("worldmap.multiworld.newname", new Object[0]) + " >"), button -> {
                this.newWorld = true;
                this.newNameField.setFocus(true);
            })));
        }
        this.newNameField = new EditBox(this.getFontRenderer(), (buttonsPerRow - 1 - lastRowShiftBy - numButtons % buttonsPerRow) * buttonWidth + xSpacing + 1, this.height - 60 - numButtons / buttonsPerRow * 21 + 1, buttonWidth - 4, 18, null);
    }
    
    public void accept(final boolean par1) {
        if (!par1) {
            this.getMinecraft().setScreen(this.parent);
        }
        else {
            this.multiworld = true;
            this.getMinecraft().setScreen((Screen)this);
        }
    }
    
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        if (this.newWorld) {
            this.newNameField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public boolean keyPressed(final int keysm, final int scancode, final int b) {
        if (this.newNameField.isFocused()) {
            this.newNameField.keyPressed(keysm, scancode, b);
            if ((keysm == 257 || keysm == 335) && this.newNameField.isFocused()) {
                final String newName = this.newNameField.getValue();
                if (newName != null && !newName.isEmpty()) {
                    this.worldSelected(newName);
                }
            }
        }
        return super.keyPressed(keysm, scancode, b);
    }
    
    public boolean charTyped(final char typedChar, final int keyCode) {
        if (this.newNameField.isFocused()) {
            this.newNameField.charTyped(typedChar, keyCode);
            if (keyCode == 28) {
                final String newName = this.newNameField.getValue();
                if (newName != null && !newName.isEmpty()) {
                    this.worldSelected(newName);
                }
            }
        }
        return super.charTyped(typedChar, keyCode);
    }
    
    public void tick() {
        this.newNameField.tick();
        super.tick();
    }
    
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        int titleStringWidth = this.getFontRenderer().width((FormattedText) this.title);
        titleStringWidth = Math.max(titleStringWidth, this.getFontRenderer().width((FormattedText) this.select));
        fill(matrixStack, this.width / 2 - titleStringWidth / 2 - 5, 0, this.width / 2 + titleStringWidth / 2 + 5, 27, -1073741824);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.title, this.width / 2, 5, 16777215);
        drawCenteredString(matrixStack, this.getFontRenderer(), this.select, this.width / 2, 15, 16711680);
        final LocalPlayer camera = this.camera;
        final LocalPlayer camera2 = this.camera;
        final float n = 0.0f;
        camera2.setXRot(n);
        camera.xRotO = n;
        final LocalPlayer camera3 = this.camera;
        final LocalPlayer camera4 = this.camera;
        final float yaw = this.yaw;
        camera4.setYRot(yaw);
        camera3.yRotO = yaw;
        final float var4 = 0.475f;
        final LocalPlayer camera5 = this.camera;
        final LocalPlayer camera6 = this.camera;
        final double getY = this.thePlayer.getY();
        camera6.yo = getY;
        camera5.yOld = getY;
        final LocalPlayer camera7 = this.camera;
        final LocalPlayer camera8 = this.camera;
        final double n2 = this.thePlayer.getX() - var4 * Math.sin(this.yaw / 180.0 * 3.141592653589793);
        camera8.xo = n2;
        camera7.xOld = n2;
        final LocalPlayer camera9 = this.camera;
        final LocalPlayer camera10 = this.camera;
        final double n3 = this.thePlayer.getZ() + var4 * Math.cos(this.yaw / 180.0 * 3.141592653589793);
        camera10.zo = n3;
        camera9.zOld = n3;
        this.camera.setPos(this.camera.xo, this.camera.yo, this.camera.zo);
        final float var5 = 1.0f;
        this.yaw += (float)(var5 * (1.0 + 0.699999988079071 * Math.cos((this.yaw + 45.0f) / 45.0 * 3.141592653589793)));
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (this.newWorld) {
            this.newNameField.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
    
    @Override
    public void removed() {
        super.removed();
        this.game.options.setCameraType(this.thirdPersonViewOrig);
        this.getMinecraft().setCameraEntity((Entity)this.thePlayer);
    }
    
    private void worldSelected(final String selectedSubworldName) {
        this.waypointManager.setSubworldName(selectedSubworldName, false);
        this.getMinecraft().setScreen(this.parent);
    }
    
    private void editWorld(final String subworldNameToEdit) {
        this.getMinecraft().setScreen((Screen) new GuiSubworldEdit(this, this.master, subworldNameToEdit));
    }
}
