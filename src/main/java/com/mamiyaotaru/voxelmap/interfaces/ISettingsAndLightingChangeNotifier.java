// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;

public interface ISettingsAndLightingChangeNotifier
{
    public static final Set<ISettingsAndLightingChangeListener> listeners = new CopyOnWriteArraySet<ISettingsAndLightingChangeListener>();
    
    void addObserver(final ISettingsAndLightingChangeListener p0);
    
    void removeObserver(final ISettingsAndLightingChangeListener p0);
    
    void notifyOfChanges();
}
