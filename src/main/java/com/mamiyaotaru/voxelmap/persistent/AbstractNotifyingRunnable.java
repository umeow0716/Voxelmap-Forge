// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;

public abstract class AbstractNotifyingRunnable implements Runnable
{
    private final Set<IThreadCompleteListener> listeners;
    
    public AbstractNotifyingRunnable() {
        this.listeners = new CopyOnWriteArraySet<IThreadCompleteListener>();
    }
    
    public final void addListener(final IThreadCompleteListener listener) {
        this.listeners.add(listener);
    }
    
    public final void removeListener(final IThreadCompleteListener listener) {
        this.listeners.remove(listener);
    }
    
    private final void notifyListeners() {
        for (final IThreadCompleteListener listener : this.listeners) {
            listener.notifyOfThreadComplete(this);
        }
    }
    
    @Override
    public final void run() {
        try {
            this.doRun();
        }
        finally {
            this.notifyListeners();
        }
    }
    
    public abstract void doRun();
}
