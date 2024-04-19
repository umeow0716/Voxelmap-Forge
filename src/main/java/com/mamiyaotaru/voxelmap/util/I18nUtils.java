// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.text.Collator;

public class I18nUtils
{
    public static String getString(final String translateMe, final Object... args) {
        return I18n.get(translateMe, args);
    }
    
    public static Collator getLocaleAwareCollator() {
        String mcLocale = "en_US";
        try {
            mcLocale = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        }
        catch (final NullPointerException ex) {}
        final String[] bits = mcLocale.split("_");
        final Locale locale = new Locale(bits[0], (bits.length > 1) ? bits[1] : "");
        return Collator.getInstance(locale);
    }
}
