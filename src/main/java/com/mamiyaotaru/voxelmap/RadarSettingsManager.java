// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.util.CustomMob;
import java.io.PrintWriter;
import com.mamiyaotaru.voxelmap.util.CustomMobsManager;
import com.mamiyaotaru.voxelmap.util.EnumMobs;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import net.minecraft.client.Minecraft;
import com.mamiyaotaru.voxelmap.interfaces.ISubSettingsManager;

public class RadarSettingsManager implements ISubSettingsManager
{
    public final int SIMPLE = 1;
    public final int FULL = 2;
    public Minecraft game;
    private boolean somethingChanged;
    public int radarMode;
    public boolean showRadar;
    public boolean showHostiles;
    public boolean showPlayers;
    public boolean showNeutrals;
    public boolean showPlayerNames;
    public boolean showMobNames;
    public boolean outlines;
    public boolean filtering;
    public boolean showHelmetsPlayers;
    public boolean showHelmetsMobs;
    public boolean randomobs;
    public boolean showFacing;
    public Boolean radarAllowed;
    public Boolean radarPlayersAllowed;
    public Boolean radarMobsAllowed;
    float fontScale;
    
    public RadarSettingsManager() {
        this.radarMode = 2;
        this.showRadar = true;
        this.showHostiles = true;
        this.showPlayers = true;
        this.showNeutrals = false;
        this.showPlayerNames = true;
        this.showMobNames = false;
        this.outlines = true;
        this.filtering = true;
        this.showHelmetsPlayers = true;
        this.showHelmetsMobs = true;
        this.randomobs = true;
        this.showFacing = true;
        this.radarAllowed = true;
        this.radarPlayersAllowed = true;
        this.radarMobsAllowed = true;
        this.fontScale = 1.0f;
        this.game = Minecraft.getInstance();
    }
    
    @Override
    public void loadSettings(final File settingsFile) {
        try {
            final BufferedReader in = new BufferedReader(new FileReader(settingsFile));
            String sCurrentLine;
            while ((sCurrentLine = in.readLine()) != null) {
                final String[] curLine = sCurrentLine.split(":");
                if (curLine[0].equals("Radar Mode")) {
                    this.radarMode = Math.max(1, Math.min(2, Integer.parseInt(curLine[1])));
                }
                else if (curLine[0].equals("Show Radar")) {
                    this.showRadar = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Hostiles")) {
                    this.showHostiles = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Players")) {
                    this.showPlayers = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Neutrals")) {
                    this.showNeutrals = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Filter Mob Icons")) {
                    this.filtering = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Outline Mob Icons")) {
                    this.outlines = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Player Helmets")) {
                    this.showHelmetsPlayers = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Mob Helmets")) {
                    this.showHelmetsMobs = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Player Names")) {
                    this.showPlayerNames = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Font Scale")) {
                    this.fontScale = Float.parseFloat(curLine[1]);
                }
                else if (curLine[0].equals("Randomobs")) {
                    this.randomobs = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Facing")) {
                    this.showFacing = Boolean.parseBoolean(curLine[1]);
                }
                else {
                    if (!curLine[0].equals("Hidden Mobs")) {
                        continue;
                    }
                    this.applyHiddenMobSettings(curLine[1]);
                }
            }
            in.close();
        }
        catch (final Exception ex) {}
    }
    
    private void applyHiddenMobSettings(final String hiddenMobs) {
        final String[] mobsToHide = hiddenMobs.split(",");
        for (int t = 0; t < mobsToHide.length; ++t) {
            boolean builtIn = false;
            for (final EnumMobs mob : EnumMobs.values()) {
                if (mob.id.equals(mobsToHide[t])) {
                    mob.enabled = false;
                    builtIn = true;
                }
            }
            if (!builtIn) {
                CustomMobsManager.add(mobsToHide[t], false);
            }
        }
    }
    
    @Override
    public void saveAll(final PrintWriter out) {
        out.println("Radar Mode:" + this.radarMode);
        out.println("Show Radar:" + Boolean.toString(this.showRadar));
        out.println("Show Hostiles:" + Boolean.toString(this.showHostiles));
        out.println("Show Players:" + Boolean.toString(this.showPlayers));
        out.println("Show Neutrals:" + Boolean.toString(this.showNeutrals));
        out.println("Filter Mob Icons:" + Boolean.toString(this.filtering));
        out.println("Outline Mob Icons:" + Boolean.toString(this.outlines));
        out.println("Show Player Helmets:" + Boolean.toString(this.showHelmetsPlayers));
        out.println("Show Mob Helmets:" + Boolean.toString(this.showHelmetsMobs));
        out.println("Show Player Names:" + Boolean.toString(this.showPlayerNames));
        out.println("Font Scale:" + Float.toString(this.fontScale));
        out.println("Randomobs:" + Boolean.toString(this.randomobs));
        out.println("Show Facing:" + Boolean.toString(this.showFacing));
        out.print("Hidden Mobs:");
        for (final EnumMobs mob : EnumMobs.values()) {
            if (mob.isTopLevelUnit && !mob.enabled) {
                out.print(mob.id + ",");
            }
        }
        for (final CustomMob mob2 : CustomMobsManager.mobs) {
            if (!mob2.enabled) {
                out.print(mob2.id + ",");
            }
        }
        out.println();
    }
    
    @Override
    public String getKeyText(final EnumOptionsMinimap par1EnumOptions) {
        final String s = I18nUtils.getString(par1EnumOptions.getName(), new Object[0]) + ": ";
        if (par1EnumOptions.isBoolean()) {
            if (this.getOptionBooleanValue(par1EnumOptions)) {
                return s + I18nUtils.getString("options.on", new Object[0]);
            }
            return s + I18nUtils.getString("options.off", new Object[0]);
        }
        else {
            if (par1EnumOptions.isList()) {
                final String state = this.getOptionListValue(par1EnumOptions);
                return s + state;
            }
            return s;
        }
    }
    
    public boolean getOptionBooleanValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case SHOWRADAR: {
                return this.showRadar;
            }
            case SHOWHOSTILES: {
                return this.showHostiles;
            }
            case SHOWPLAYERS: {
                return this.showPlayers;
            }
            case SHOWNEUTRALS: {
                return this.showNeutrals;
            }
            case SHOWPLAYERHELMETS: {
                return this.showHelmetsPlayers;
            }
            case SHOWMOBHELMETS: {
                return this.showHelmetsMobs;
            }
            case SHOWPLAYERNAMES: {
                return this.showPlayerNames;
            }
            case RADAROUTLINES: {
                return this.outlines;
            }
            case RADARFILTERING: {
                return this.filtering;
            }
            case RANDOMOBS: {
                return this.randomobs;
            }
            case SHOWFACING: {
                return this.showFacing;
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a boolean)");
            }
        }
    }
    
    public String getOptionListValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case RADARMODE: {
                final int radarMode = this.radarMode;
                this.getClass();
                if (radarMode == 2) {
                    return I18nUtils.getString("options.minimap.radar.radarmode.full", new Object[0]);
                }
                return I18nUtils.getString("options.minimap.radar.radarmode.simple", new Object[0]);
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a list value applicable to minimap)");
            }
        }
    }
    
    @Override
    public void setOptionFloatValue(final EnumOptionsMinimap idFloat, final float sliderValue) {
    }
    
    public void setOptionValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case RADARMODE: {
                if (this.radarMode == 2) {
                    this.radarMode = 1;
                    break;
                }
                this.radarMode = 2;
                break;
            }
            case SHOWRADAR: {
                this.showRadar = !this.showRadar;
                break;
            }
            case SHOWHOSTILES: {
                this.showHostiles = !this.showHostiles;
                break;
            }
            case SHOWPLAYERS: {
                this.showPlayers = !this.showPlayers;
                break;
            }
            case SHOWNEUTRALS: {
                this.showNeutrals = !this.showNeutrals;
                break;
            }
            case SHOWPLAYERHELMETS: {
                this.showHelmetsPlayers = !this.showHelmetsPlayers;
                break;
            }
            case SHOWMOBHELMETS: {
                this.showHelmetsMobs = !this.showHelmetsMobs;
                break;
            }
            case SHOWPLAYERNAMES: {
                this.showPlayerNames = !this.showPlayerNames;
                break;
            }
            case RADAROUTLINES: {
                this.outlines = !this.outlines;
                break;
            }
            case RADARFILTERING: {
                this.filtering = !this.filtering;
                break;
            }
            case RANDOMOBS: {
                this.randomobs = !this.randomobs;
                break;
            }
            case SHOWFACING: {
                this.showFacing = !this.showFacing;
                break;
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName());
            }
        }
        this.somethingChanged = true;
    }
    
    public boolean isChanged() {
        if (this.somethingChanged) {
            this.somethingChanged = false;
            return true;
        }
        return false;
    }
    
    @Override
    public float getOptionFloatValue(final EnumOptionsMinimap option) {
        return 0.0f;
    }
}
