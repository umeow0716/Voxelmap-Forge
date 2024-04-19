// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.gui;

import com.mamiyaotaru.voxelmap.util.GLUtils;
import com.mamiyaotaru.voxelmap.util.GLShim;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import com.mamiyaotaru.voxelmap.util.TextUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Iterator;
import java.util.Collections;
import java.text.Collator;
import java.util.Comparator;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.CustomMob;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import java.util.ArrayList;
import com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap;

class GuiSlotMobs extends GuiSlotMinimap<GuiSlotMobs.MobItem>
{
    private ArrayList<MobItem> mobs;
    private ArrayList<MobItem> mobsFiltered;
    private RadarSettingsManager options;
    final GuiMobs parentGui;
    final TranslatableComponent ENABLE;
    final TranslatableComponent DISABLE;
    final TranslatableComponent ENABLED;
    final TranslatableComponent DISABLED;
    
    public GuiSlotMobs(final GuiMobs par1GuiMobs) {
        super(par1GuiMobs.options.game, par1GuiMobs.getWidth(), par1GuiMobs.getHeight(), 32, par1GuiMobs.getHeight() - 65 + 4, 18);
        this.ENABLE = new TranslatableComponent("options.minimap.mobs.enable");
        this.DISABLE = new TranslatableComponent("options.minimap.mobs.disable");
        this.ENABLED = new TranslatableComponent("options.minimap.mobs.enabled");
        this.DISABLED = new TranslatableComponent("options.minimap.mobs.disabled");
        this.parentGui = par1GuiMobs;
        this.options = this.parentGui.options;
        this.mobs = new ArrayList<MobItem>();
        for (final EnumMobs mob : EnumMobs.values()) {
            if (mob.isTopLevelUnit && ((mob.isHostile && this.options.showHostiles) || (mob.isNeutral && this.options.showNeutrals))) {
                this.mobs.add(new MobItem(this.parentGui, mob.id));
            }
        }
        for (final CustomMob mob2 : CustomMobsManager.mobs) {
            if ((mob2.isHostile && this.options.showHostiles) || (mob2.isNeutral && this.options.showNeutrals)) {
                this.mobs.add(new MobItem(this.parentGui, mob2.id));
            }
        }
        final Collator collator = I18nUtils.getLocaleAwareCollator();
        Collections.sort(this.mobs, new Comparator<MobItem>() {
            @Override
            public int compare(final MobItem mob1, final MobItem mob2) {
                return collator.compare(mob1.name, mob2.name);
            }
        });
        (this.mobsFiltered = new ArrayList<MobItem>(this.mobs)).forEach(this::addEntry);
    }
    
    private static String getTranslatedName(String name) {
        if (name.indexOf(".") == -1) {
            name = "entity.minecraft." + name.toLowerCase();
        }
        name = I18nUtils.getString(name, new Object[0]);
        name = name.replaceAll("^entity.minecraft.", "");
        name = name.replace("_", " ");
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        name = TextUtils.scrubCodes(name);
        return name;
    }
    
    public void setSelected(final MobItem item) {
        super.setSelected(item);
        if (this.getSelected() instanceof MobItem) {
            NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", new Object[] { ((MobItem)this.getSelected()).name }).getString());
        }
        this.parentGui.setSelectedMob(item.id);
    }
    
    protected boolean isSelectedItem(final int par1) {
        return this.mobsFiltered.get(par1).id.equals(this.parentGui.selectedMobId);
    }
    
    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight;
    }
    
    public void renderBackground(final PoseStack matrixStack) {
        this.parentGui.renderBackground(matrixStack);
    }
    
    protected void updateFilter(final String filterString) {
        this.clearEntries();
        this.mobsFiltered = new ArrayList<MobItem>(this.mobs);
        final Iterator<MobItem> iterator = this.mobsFiltered.iterator();
        while (iterator.hasNext()) {
            final String mobName = iterator.next().name;
            if (!mobName.toLowerCase().contains(filterString)) {
                if (mobName == this.parentGui.selectedMobId) {
                    this.parentGui.setSelectedMob(null);
                }
                iterator.remove();
            }
        }
        this.mobsFiltered.forEach(this::addEntry);
    }
    
    public class MobItem extends AbstractSelectionList.Entry<MobItem>
    {
        private final GuiMobs parentGui;
        private final String id;
        private final String name;
        
        protected MobItem(final GuiMobs mobsScreen, final String id) {
            this.parentGui = mobsScreen;
            this.id = id;
            this.name = getTranslatedName(id);
        }
        
        public void render(final PoseStack matrixStack, final int slotIndex, final int slotYPos, final int leftEdge, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean mouseOver, final float partialTicks) {
            boolean isHostile = false;
            boolean isNeutral = false;
            boolean isEnabled = true;
            final EnumMobs mob = EnumMobs.getMobByName(this.id);
            if (mob != null) {
                isHostile = mob.isHostile;
                isNeutral = mob.isNeutral;
                isEnabled = mob.enabled;
            }
            else {
                final CustomMob customMob = CustomMobsManager.getCustomMobByType(this.id);
                if (customMob != null) {
                    isHostile = customMob.isHostile;
                    isNeutral = customMob.isNeutral;
                    isEnabled = customMob.enabled;
                }
            }
            final int red = isHostile ? 255 : 0;
            final int green = isNeutral ? 255 : 0;
            final int color = -16777216 + (red << 16) + (green << 8) + 0;
            GuiMobs.drawCenteredString(matrixStack, this.parentGui.getFontRenderer(), this.name, this.parentGui.getWidth() / 2, slotYPos + 3, color);
            final byte padding = 3;
            if (mouseX >= leftEdge - padding && mouseY >= slotYPos && mouseX <= leftEdge + 215 + padding && mouseY <= slotYPos + GuiSlotMobs.this.itemHeight) {
                Component tooltip;
                if (mouseX >= leftEdge + 215 - 16 - padding && mouseX <= leftEdge + 215 + padding) {
                    tooltip = (isEnabled ? GuiSlotMobs.this.DISABLE : GuiSlotMobs.this.ENABLE);
                }
                else {
                    tooltip = (isEnabled ? GuiSlotMobs.this.ENABLED : GuiSlotMobs.this.DISABLED);
                }
                GuiMobs.setTooltip(this.parentGui, tooltip);
            }
            GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GLUtils.img2("textures/mob_effect/" + (isEnabled ? "night_vision.png" : "blindness.png"));
            GuiComponent.blit(matrixStack, leftEdge + 198, slotYPos - 2, GuiSlotMobs.this.getBlitOffset(), 0.0f, 0.0f, 18, 18, 18, 18);
        }
        
        public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseEvent) {
            GuiSlotMobs.this.setSelected(this);
            final int leftEdge = this.parentGui.getWidth() / 2 - 92 - 16;
            final byte padding = 3;
            final int width = 215;
            if (mouseX >= leftEdge + width - 16 - padding && mouseX <= leftEdge + width + padding) {
                this.parentGui.toggleMobVisibility();
            }
            else if (GuiSlotMobs.this.doubleclick) {
                this.parentGui.toggleMobVisibility();
            }
            return true;
        }
    }

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		// TODO Auto-generated method stub
		
	}
}
