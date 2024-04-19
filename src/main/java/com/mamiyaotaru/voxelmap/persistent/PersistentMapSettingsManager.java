// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import com.mamiyaotaru.voxelmap.interfaces.ISubSettingsManager;

public class PersistentMapSettingsManager implements ISubSettingsManager
{
    protected int mapX;
    protected int mapZ;
    protected float zoom;
    private float minZoomPower;
    private float maxZoomPower;
    protected float minZoom;
    protected float maxZoom;
    protected int cacheSize;
    protected boolean outputImages;
    public boolean showWaypoints;
    public boolean showWaypointNames;
    protected final int MINMINZOOMPOWER = -3;
    protected final int MAXMAXZOOMPOWER = 5;
    protected final int MAXCACHESIZE = 5000;
    
    public PersistentMapSettingsManager() {
        this.zoom = 4.0f;
        this.minZoomPower = -1.0f;
        this.maxZoomPower = 4.0f;
        this.minZoom = 0.5f;
        this.maxZoom = 16.0f;
        this.cacheSize = 500;
        this.outputImages = false;
        this.showWaypoints = true;
        this.showWaypointNames = true;
    }
    
    @Override
    public void loadSettings(final File settingsFile) {
        try {
            final BufferedReader in = new BufferedReader(new FileReader(settingsFile));
            String sCurrentLine;
            while ((sCurrentLine = in.readLine()) != null) {
                final String[] curLine = sCurrentLine.split(":");
                if (curLine[0].equals("Worldmap Zoom")) {
                    this.zoom = Float.parseFloat(curLine[1]);
                }
                else if (curLine[0].equals("Worldmap Minimum Zoom")) {
                    this.minZoom = Float.parseFloat(curLine[1]);
                }
                else if (curLine[0].equals("Worldmap Maximum Zoom")) {
                    this.maxZoom = Float.parseFloat(curLine[1]);
                }
                else if (curLine[0].equals("Worldmap Cache Size")) {
                    this.cacheSize = Integer.parseInt(curLine[1]);
                }
                else if (curLine[0].equals("Show Worldmap Waypoints")) {
                    this.showWaypoints = Boolean.parseBoolean(curLine[1]);
                }
                else if (curLine[0].equals("Show Worldmap Waypoint Names")) {
                    this.showWaypointNames = Boolean.parseBoolean(curLine[1]);
                }
                else {
                    if (!curLine[0].equals("Output Images")) {
                        continue;
                    }
                    this.outputImages = Boolean.parseBoolean(curLine[1]);
                }
            }
            in.close();
        }
        catch (final Exception ex) {}
        for (int power = -3; power <= 5; ++power) {
            if (Math.pow(2.0, power) == this.minZoom) {
                this.minZoomPower = (float)power;
            }
            if (Math.pow(2.0, power) == this.maxZoom) {
                this.maxZoomPower = (float)power;
            }
        }
        this.bindCacheSize();
        this.bindZoom();
    }
    
    @Override
    public void saveAll(final PrintWriter out) {
        out.println("Worldmap Zoom:" + Float.toString(this.zoom));
        out.println("Worldmap Minimum Zoom:" + Float.toString(this.minZoom));
        out.println("Worldmap Maximum Zoom:" + Float.toString(this.maxZoom));
        out.println("Worldmap Cache Size:" + Integer.toString(this.cacheSize));
        out.println("Show Worldmap Waypoints:" + Boolean.toString(this.showWaypoints));
        out.println("Show Worldmap Waypoint Names:" + Boolean.toString(this.showWaypointNames));
    }
    
    @Override
    public String getKeyText(final EnumOptionsMinimap par1EnumOptions) {
        final String s = I18nUtils.getString(par1EnumOptions.getName(), new Object[0]) + ": ";
        if (par1EnumOptions.isFloat()) {
            final float f = this.getOptionFloatValue(par1EnumOptions);
            if (par1EnumOptions == EnumOptionsMinimap.MINZOOM) {
                return s + (float)Math.pow(2.0, f) + "x";
            }
            if (par1EnumOptions == EnumOptionsMinimap.MAXZOOM) {
                return s + (float)Math.pow(2.0, f) + "x";
            }
            if (par1EnumOptions == EnumOptionsMinimap.CACHESIZE) {
                return s + (int)f;
            }
        }
        if (!par1EnumOptions.isBoolean()) {
            return s;
        }
        final boolean flag = this.getOptionBooleanValue(par1EnumOptions);
        if (flag) {
            return s + I18nUtils.getString("options.on", new Object[0]);
        }
        return s + I18nUtils.getString("options.off", new Object[0]);
    }
    
    @Override
    public float getOptionFloatValue(final EnumOptionsMinimap par1EnumOptions) {
        if (par1EnumOptions == EnumOptionsMinimap.MINZOOM) {
            return this.minZoomPower;
        }
        if (par1EnumOptions == EnumOptionsMinimap.MAXZOOM) {
            return this.maxZoomPower;
        }
        if (par1EnumOptions == EnumOptionsMinimap.CACHESIZE) {
            return (float)this.cacheSize;
        }
        return 0.0f;
    }
    
    public boolean getOptionBooleanValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case SHOWWAYPOINTS: {
                return this.showWaypoints;
            }
            case SHOWWAYPOINTNAMES: {
                return this.showWaypointNames;
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName() + ". (possibly not a boolean)");
            }
        }
    }
    
    @Override
    public void setOptionFloatValue(final EnumOptionsMinimap par1EnumOptions, final float par2) {
        if (par1EnumOptions == EnumOptionsMinimap.MINZOOM) {
            this.minZoomPower = (float)((int)(par2 * 8.0f) - 3);
            this.minZoom = (float)Math.pow(2.0, this.minZoomPower);
            if (this.maxZoom < this.minZoom) {
                this.maxZoom = this.minZoom;
                this.maxZoomPower = this.minZoomPower;
            }
        }
        else if (par1EnumOptions == EnumOptionsMinimap.MAXZOOM) {
            this.maxZoomPower = (float)((int)(par2 * 8.0f) - 3);
            this.maxZoom = (float)Math.pow(2.0, this.maxZoomPower);
            if (this.minZoom > this.maxZoom) {
                this.minZoom = this.maxZoom;
                this.minZoomPower = this.maxZoomPower;
            }
        }
        else if (par1EnumOptions == EnumOptionsMinimap.CACHESIZE) {
            this.cacheSize = (int)(par2 * 5000.0f);
            this.cacheSize = Math.max(this.cacheSize, 30);
            for (int minCacheSize = (int)((1600.0f / this.minZoom / 256.0f + 4.0f) * (1100.0f / this.minZoom / 256.0f + 3.0f) * 1.35f); this.cacheSize < minCacheSize; minCacheSize = (int)((1600.0f / this.minZoom / 256.0f + 4.0f) * (1100.0f / this.minZoom / 256.0f + 3.0f) * 1.35f)) {
                ++this.minZoomPower;
                this.minZoom = (float)Math.pow(2.0, this.minZoomPower);
            }
            if (this.maxZoom < this.minZoom) {
                this.maxZoom = this.minZoom;
                this.maxZoomPower = this.minZoomPower;
            }
        }
        this.bindZoom();
        this.bindCacheSize();
    }
    
    public void setOptionValue(final EnumOptionsMinimap par1EnumOptions) {
        switch (par1EnumOptions) {
            case SHOWWAYPOINTS: {
                this.showWaypoints = !this.showWaypoints;
                break;
            }
            case SHOWWAYPOINTNAMES: {
                this.showWaypointNames = !this.showWaypointNames;
                break;
            }
            default: {
                throw new IllegalArgumentException("Add code to handle EnumOptionMinimap: " + par1EnumOptions.getName());
            }
        }
    }
    
    private void bindCacheSize() {
        final int minCacheSize = (int)((1600.0f / this.minZoom / 256.0f + 4.0f) * (1100.0f / this.minZoom / 256.0f + 3.0f) * 1.35f);
        this.cacheSize = Math.max(this.cacheSize, minCacheSize);
    }
    
    private void bindZoom() {
        this.zoom = Math.max(this.zoom, this.minZoom);
        this.zoom = Math.min(this.zoom, this.maxZoom);
    }
}
