// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeListener;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeNotifier;

public class SettingsAndLightingChangeNotifier implements ISettingsAndLightingChangeNotifier
{
    @Override
    public final void addObserver(final ISettingsAndLightingChangeListener listener) {
        SettingsAndLightingChangeNotifier.listeners.add(listener);
    }
    
    @Override
    public final void removeObserver(final ISettingsAndLightingChangeListener listener) {
        SettingsAndLightingChangeNotifier.listeners.remove(listener);
    }
    
    @Override
    public void notifyOfChanges() {
        for (final ISettingsAndLightingChangeListener listener : SettingsAndLightingChangeNotifier.listeners) {
            listener.notifyOfActionableChange(this);
        }
    }
}
