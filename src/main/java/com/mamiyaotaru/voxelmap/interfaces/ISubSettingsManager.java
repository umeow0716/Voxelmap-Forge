// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.interfaces;

import java.io.PrintWriter;
import java.io.File;

public interface ISubSettingsManager extends ISettingsManager
{
    void loadSettings(final File p0);
    
    void saveAll(final PrintWriter p0);
}
