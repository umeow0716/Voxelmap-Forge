// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Iterator;

public class TextUtils
{
    public static String scrubCodes(String string) {
    	string = string.replaceAll("(§.)", "");
        return string;
    }
    
    public static String scrubName(String input) {
        input = input.replace(",", "~comma~");
        input = input.replace(":", "~colon~");
        return input;
    }
    
    public static String scrubNameRegex(String input) {
    	input = input.replace(",", "﹐");
    	input = input.replace("[", "⟦");
    	input = input.replace("]", "⟧");
        return input;
    }
    
    public static String scrubNameFile(String input) {
        input = input.replace("<", "~less~");
        input = input.replace(">", "~greater~");
        input = input.replace(":", "~colon~");
        input = input.replace("\"", "~quote~");
        input = input.replace("/", "~slash~");
        input = input.replace("\\", "~backslash~");
        input = input.replace("|", "~pipe~");
        input = input.replace("?", "~question~");
        input = input.replace("*", "~star~");
        return input;
    }
    
    public static String descrubName(String input) {
        input = input.replace("~less~", "<");
        input = input.replace("~greater~", ">");
        input = input.replace("~colon~", ":");
        input = input.replace("~quote~", "\"");
        input = input.replace("~slash~", "/");
        input = input.replace("~backslash~", "\\");
        input = input.replace("~pipe~", "|");
        input = input.replace("~question~", "?");
        input = input.replace("~star~", "*");
        input = input.replace("~comma~", ",");
        input = input.replace("~colon~", ":");
        input = input.replace("﹐", ",");
        input = input.replace("⟦", "[");
        input = input.replace("⟧", "]");
        return input;
    }
    
    public static String prettify(final String input) {
        final String[] words = input.split("_");
        for (int t = 0; t < words.length; ++t) {
            words[t] = words[t].substring(0, 1).toUpperCase() + words[t].substring(1).toLowerCase();
        }
        return String.join(" ", (CharSequence[])words);
    }
    
    public static String asFormattedString(Component text) {
        final StringBuilder stringBuilder = new StringBuilder();
        String lastStyleString = "";
        final Iterator<Component> iterator = stream(text).iterator();
        while (iterator.hasNext()) {
            text = iterator.next();
            final String contentString = text.getContents();
            if (!contentString.isEmpty()) {
                final String styleString = asString(text.getStyle());
                if (!styleString.equals(lastStyleString)) {
                    if (!lastStyleString.isEmpty()) {
                        stringBuilder.append(ChatFormatting.RESET);
                    }
                    stringBuilder.append(styleString);
                    lastStyleString = styleString;
                }
                stringBuilder.append(contentString);
            }
        }
        if (!lastStyleString.isEmpty()) {
            stringBuilder.append(ChatFormatting.RESET);
        }
        return stringBuilder.toString();
    }
    
    private static List<Component> stream(final Component text) {
        final List<Component> stream = new ArrayList<Component>();
        stream.add(text);
        for (final Component sibling : text.getSiblings()) {
            stream.addAll(stream(sibling));
        }
        return stream;
    }
    
    private static String asString(final Style style) {
        if (style.isEmpty()) {
            return "";
        }
        final StringBuilder stringBuilder = new StringBuilder();
        if (style.getColor() != null) {
            final ChatFormatting colorFormat = ChatFormatting.getByName(style.getColor().serialize());
            if (colorFormat != null) {
                stringBuilder.append(colorFormat);
            }
        }
        if (style.isBold()) {
            stringBuilder.append(ChatFormatting.BOLD);
        }
        if (style.isItalic()) {
            stringBuilder.append(ChatFormatting.ITALIC);
        }
        if (style.isUnderlined()) {
            stringBuilder.append(ChatFormatting.UNDERLINE);
        }
        if (style.isObfuscated()) {
            stringBuilder.append(ChatFormatting.OBFUSCATED);
        }
        if (style.isStrikethrough()) {
            stringBuilder.append(ChatFormatting.STRIKETHROUGH);
        }
        return stringBuilder.toString();
    }
}
