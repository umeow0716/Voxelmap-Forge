// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Iterator;
import java.util.Collections;
import java.text.Collator;
import java.util.Comparator;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class GuiButtonRowListPlayers extends ObjectSelectionList<GuiButtonRowListPlayers.Row>
{
    private final Minecraft client;
    private ArrayList<PlayerInfo> players;
    private ArrayList<PlayerInfo> playersFiltered;
    final GuiSelectPlayer parentGui;
    Row everyoneRow;
    final TranslatableComponent ALL;
    final TranslatableComponent TITLE;
    final TranslatableComponent EXPLANATION;
    final TranslatableComponent AFFIRM;
    final TranslatableComponent DENY;
    
    public GuiButtonRowListPlayers(final GuiSelectPlayer par1GuiSelectPlayer) {
        super(Minecraft.getInstance(), par1GuiSelectPlayer.getWidth(), par1GuiSelectPlayer.getHeight(), 89, par1GuiSelectPlayer.getHeight() - 65 + 4, 25);
        this.client = Minecraft.getInstance();
        this.ALL = new TranslatableComponent("minimap.waypointshare.all");
        this.TITLE = new TranslatableComponent("minimap.waypointshare.sharewitheveryone");
        this.EXPLANATION = new TranslatableComponent("minimap.waypointshare.sharewitheveryone2");
        this.AFFIRM = new TranslatableComponent("gui.yes");
        this.DENY = new TranslatableComponent("gui.cancel");
        this.parentGui = par1GuiSelectPlayer;
        final ClientPacketListener netHandlerPlayClient = client.player.connection;
        this.players = new ArrayList<PlayerInfo>(netHandlerPlayClient.getOnlinePlayers());
        this.sort();
        final Button everyoneButton = new Button(this.parentGui.getWidth() / 2 - 75, 0, 150, 20, this.ALL, null) {
            public void onPress() {
            }
        };
        this.everyoneRow = new Row(everyoneButton, -1);
        this.updateFilter("");
    }
    
    private TextComponent getPlayerName(final PlayerInfo ScoreboardEntryIn) {
        return (TextComponent) ((ScoreboardEntryIn.getTabListDisplayName() != null) ? ScoreboardEntryIn.getTabListDisplayName() : new TextComponent(ScoreboardEntryIn.getProfile().getName()));
    }
    
    private Button createButtonFor(final Minecraft mcIn, final int x, final int y, final PlayerInfo ScoreboardEntry) {
        if (ScoreboardEntry == null) {
            return null;
        }
        final TextComponent name = this.getPlayerName(ScoreboardEntry);
        return new Button(x, y, 150, 20, name, button -> {});
    }
    
    public Row getListEntry(final int index) {
        return this.getListEntry(index);
    }
    
    public int getRowWidth() {
        return 400;
    }
    
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }
    
    protected void sort() {
        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(this.players, new Comparator<PlayerInfo>() {
            @Override
            public int compare(final PlayerInfo player1, final PlayerInfo player2) {
                final String name1 = GuiButtonRowListPlayers.this.getPlayerName(player1).getString();
                final String name2 = GuiButtonRowListPlayers.this.getPlayerName(player2).getString();
                return collator.compare(name1, name2);
            }
        });
    }
    
    protected void updateFilter(final String filterString) {
        this.playersFiltered = new ArrayList<PlayerInfo>(this.players);
        final Iterator<PlayerInfo> iterator = this.playersFiltered.iterator();
        while (iterator.hasNext()) {
            final PlayerInfo ScoreboardEntry = iterator.next();
            final String name = this.getPlayerName(ScoreboardEntry).getString();
            if (!name.toLowerCase().contains(filterString)) {
                iterator.remove();
            }
        }
        this.clearEntries();
        this.addEntry(this.everyoneRow);
        for (int i = 0; i < this.playersFiltered.size(); i += 2) {
            final PlayerInfo ScoreboardEntry2 = this.playersFiltered.get(i);
            final PlayerInfo ScoreboardEntry3 = (i < this.playersFiltered.size() - 1) ? this.playersFiltered.get(i + 1) : null;
            final Button guibutton1 = this.createButtonFor(this.client, this.parentGui.getWidth() / 2 - 155, 0, ScoreboardEntry2);
            final Button guibutton2 = this.createButtonFor(this.client, this.parentGui.getWidth() / 2 - 155 + 160, 0, ScoreboardEntry3);
            this.addEntry(new Row(guibutton1, i, guibutton2, i + 1));
        }
    }
    
    public void buttonClicked(final int id) {
        if (id == -1) {
            this.parentGui.allClicked = true;
            final ConfirmScreen confirmScreen = new ConfirmScreen((BooleanConsumer)this.parentGui, this.TITLE, this.EXPLANATION, this.AFFIRM, this.DENY);
            this.client.setScreen(confirmScreen);
        }
        else {
            final PlayerInfo ScoreboardEntry = this.playersFiltered.get(id);
            final String name = this.getPlayerName(ScoreboardEntry).getString();
            this.parentGui.sendMessageToPlayer(name);
        }
    }
    
    public class Row extends ObjectSelectionList.Entry<Row>
    {
        private final Minecraft client;
        private Button button;
        private Button button1;
        private Button button2;
        private int id;
        private int id1;
        private int id2;
        
        public Row(final Button button, final int id) {
            this.client = Minecraft.getInstance();
            this.button = null;
            this.button1 = null;
            this.button2 = null;
            this.id = 0;
            this.id1 = 0;
            this.id2 = 0;
            this.button = button;
            this.id = id;
        }
        
        public Row(final Button button1, final int id1, final Button button2, final int id2) {
            this.client = Minecraft.getInstance();
            this.button = null;
            this.button1 = null;
            this.button2 = null;
            this.id = 0;
            this.id1 = 0;
            this.id2 = 0;
            this.button1 = button1;
            this.id1 = id1;
            this.button2 = button2;
            this.id2 = id2;
        }
        
        public void render(final PoseStack PoseStack, final int slotIndex, final int y, final int x, final int listWidth, final int itemHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
            this.drawButton(PoseStack, this.button, this.id, slotIndex, x, y, listWidth, itemHeight, mouseX, mouseY, isSelected, partialTicks);
            this.drawButton(PoseStack, this.button1, this.id1, slotIndex, x, y, listWidth, itemHeight, mouseX, mouseY, isSelected, partialTicks);
            this.drawButton(PoseStack, this.button2, this.id2, slotIndex, x, y, listWidth, itemHeight, mouseX, mouseY, isSelected, partialTicks);
        }
        
        private void drawButton(final PoseStack PoseStack, final Button button, final int id, final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
            if (button != null) {
                button.y = y;
                button.render(PoseStack, mouseX, mouseY, partialTicks);
                if (id != -1) {
                    this.drawIconForButton(PoseStack, button, id);
                }
                if (button.isHovered() && mouseY >= GuiButtonRowListPlayers.this.y0 && mouseY <= GuiButtonRowListPlayers.this.y1) {
                    final TranslatableComponent tooltip = new TranslatableComponent("minimap.waypointshare.sharewithname", new Object[] { button.getMessage() });
                    GuiSelectPlayer.setTooltip(GuiButtonRowListPlayers.this.parentGui, tooltip);
                }
            }
        }
        
        private void drawIconForButton(final PoseStack PoseStack, final Button button, final int id) {
            final PlayerInfo playerInfo = GuiButtonRowListPlayers.this.playersFiltered.get(id);
            final GameProfile gameProfile = playerInfo.getProfile();
            final Player entityPlayer = this.client.level.getPlayerByUUID(gameProfile.getId());
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, playerInfo.getSkinLocation());
            Screen.blit(PoseStack, button.x + 6, button.y + 6, 8, 8, 8.0f, 8.0f, 8, 8, 64, 64);
            if (entityPlayer != null && entityPlayer.isModelPartShown(PlayerModelPart.HAT)) {
                Screen.blit(PoseStack, button.x + 6, button.y + 6, 8, 8, 40.0f, 8.0f, 8, 8, 64, 64);
            }
        }
        
        public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseEvent) {
            if (this.button != null && this.button.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.id);
                return true;
            }
            if (this.button1 != null && this.button1.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.id1);
                return true;
            }
            if (this.button2 != null && this.button2.mouseClicked(mouseX, mouseY, mouseEvent)) {
                GuiButtonRowListPlayers.this.buttonClicked(this.id2);
                return true;
            }
            return false;
        }
        
        public boolean mouseReleased(final double mouseX, final double mouseY, final int mouseEvent) {
            if (this.button != null) {
                this.button.mouseReleased(mouseX, mouseY, mouseEvent);
                return true;
            }
            if (this.button1 != null) {
                this.button1.mouseReleased(mouseX, mouseY, mouseEvent);
                return true;
            }
            if (this.button2 != null) {
                this.button2.mouseReleased(mouseX, mouseY, mouseEvent);
                return true;
            }
            return false;
        }

		@Override
		public Component getNarration() {
			// TODO Auto-generated method stub
			return null;
		}
    }
}
