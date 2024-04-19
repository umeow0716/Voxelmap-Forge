// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import java.util.Arrays;
import java.util.Map;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.TreeMap;
import java.util.HashMap;
import java.util.Random;
import java.util.IdentityHashMap;

public class BiomeRepository
{
    public static Biome DEFAULT;
    public static Biome FOREST;
    public static Biome SWAMP;
    public static Biome SWAMP_HILLS;
    private static IdentityHashMap<Biome, Integer> biomeToInt;
    private static int count;
    private static Biome[] biomes;
    private static Random generator;
    private static HashMap<Integer, Integer> IDtoColor;
    private static TreeMap<String, Integer> nameToColor;
    private static boolean dirty;
    
    public static void getBiomes() {
        BiomeRepository.DEFAULT = (Biome)BuiltinRegistries.BIOME.get(Biomes.OCEAN.location());
        BiomeRepository.FOREST = (Biome)BuiltinRegistries.BIOME.get(Biomes.FOREST.location());
        BiomeRepository.SWAMP = (Biome)BuiltinRegistries.BIOME.get(Biomes.SWAMP.location());
        BiomeRepository.SWAMP_HILLS = (Biome)BuiltinRegistries.BIOME.get(Biomes.SWAMP_HILLS.location());
    }
    
    public static int getBiomeId(final Biome biome) {
        Integer id = BiomeRepository.biomeToInt.get(biome);
        if (id == null) {
            id = BiomeRepository.count;
            BiomeRepository.biomes[id] = biome;
            BiomeRepository.biomeToInt.put(biome, id);
            ++BiomeRepository.count;
        }
        return id;
    }
    
    public static Biome betBiomeByID(final int id) {
        return BiomeRepository.biomes[id];
    }
    
    public static void loadBiomeColors() {
        final File saveDir = new File(Minecraft.getInstance().gameDirectory, "/voxelmap/");
        final File settingsFile = new File(saveDir, "biomecolors.txt");
        if (settingsFile.exists()) {
            try {
                final BufferedReader br = new BufferedReader(new FileReader(settingsFile));
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    final String[] curLine = sCurrentLine.split("=");
                    if (curLine.length == 2) {
                        final String name = curLine[0];
                        int color = 0;
                        try {
                            color = Integer.decode(curLine[1]);
                        }
                        catch (final NumberFormatException e) {
                            System.out.println("Error decoding integer string for biome colors; " + curLine[1]);
                            color = 0;
                        }
                        if (BiomeRepository.nameToColor.put(name, color) == null) {
                            continue;
                        }
                        BiomeRepository.dirty = true;
                    }
                }
                br.close();
            }
            catch (final Exception e2) {
                System.err.println("biome load error: " + e2.getLocalizedMessage());
                e2.printStackTrace();
            }
        }
        try {
            final InputStream is = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("voxelmap", "conf/biomecolors.txt")).getInputStream();
            final BufferedReader br2 = new BufferedReader(new InputStreamReader(is));
            String sCurrentLine2;
            while ((sCurrentLine2 = br2.readLine()) != null) {
                final String[] curLine2 = sCurrentLine2.split("=");
                if (curLine2.length == 2) {
                    final String name2 = curLine2[0];
                    int color2 = 0;
                    try {
                        color2 = Integer.decode(curLine2[1]);
                    }
                    catch (final NumberFormatException e3) {
                        System.out.println("Error decoding integer string for biome colors; " + curLine2[1]);
                        color2 = 0;
                    }
                    if (BiomeRepository.nameToColor.get(name2) != null) {
                        continue;
                    }
                    BiomeRepository.nameToColor.put(name2, color2);
                    BiomeRepository.dirty = true;
                }
            }
            br2.close();
            is.close();
        }
        catch (final IOException e4) {
            System.out.println("Error loading biome color config file from litemod!");
            e4.printStackTrace();
        }
    }
    
    public static void saveBiomeColors() {
        if (BiomeRepository.dirty) {
            final File saveDir = new File(Minecraft.getInstance().gameDirectory, "/voxelmap/");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            final File settingsFile = new File(saveDir, "biomecolors.txt");
            try {
                final PrintWriter out = new PrintWriter(new FileWriter(settingsFile));
                for (final Map.Entry<String, Integer> entry : BiomeRepository.nameToColor.entrySet()) {
                    final String name = entry.getKey();
                    final Integer color = entry.getValue();
                    String hexColor;
                    for (hexColor = Integer.toHexString(color); hexColor.length() < 6; hexColor = "0" + hexColor) {}
                    hexColor = "0x" + hexColor;
                    out.println(name + "=" + hexColor);
                }
                out.close();
            }
            catch (final Exception e) {
                System.err.println("biome save error: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        BiomeRepository.dirty = false;
    }
    
    public static int getBiomeColor(final int biomeID) {
        Integer color = BiomeRepository.IDtoColor.get(biomeID);
        if (color == null) {
            final Biome biome = (Biome)Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).byId(biomeID);
            if (biome != null) {
                final String identifier = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome).toString();
                color = BiomeRepository.nameToColor.get(identifier);
                if (color == null) {
                    final String friendlyName = getName(biome);
                    color = BiomeRepository.nameToColor.get(friendlyName);
                    if (color != null) {
                        BiomeRepository.nameToColor.remove(friendlyName);
                        BiomeRepository.nameToColor.put(identifier, color);
                        BiomeRepository.dirty = true;
                    }
                }
                if (color == null) {
                    final int r = BiomeRepository.generator.nextInt(255);
                    final int g = BiomeRepository.generator.nextInt(255);
                    final int b = BiomeRepository.generator.nextInt(255);
                    color = (r << 16 | g << 8 | b);
                    BiomeRepository.nameToColor.put(identifier, color);
                    BiomeRepository.dirty = true;
                }
            }
            else {
                System.out.println("non biome");
                color = 0;
            }
            BiomeRepository.IDtoColor.put(biomeID, color);
        }
        return color;
    }
    
    private static String getName(final Biome biome) {
        final ResourceLocation resourceLocation = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
        final String translationKey = Util.makeDescriptionId("biome", resourceLocation);
        String name = I18nUtils.getString(translationKey, new Object[0]);
        if (name.equals(translationKey)) {
            name = TextUtils.prettify(resourceLocation.getPath().toString());
        }
        return name;
    }
    
    public static String getName(final int biomeID) {
        String name = null;
        final Biome biome = (Biome)Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).byId(biomeID);
        if (biome != null) {
            name = getName(biome);
        }
        if (name == null) {
            name = "Unknown";
        }
        return name;
    }
    
    static {
        BiomeRepository.biomeToInt = new IdentityHashMap<Biome, Integer>(256);
        BiomeRepository.count = 1;
        BiomeRepository.biomes = new Biome[65536];
        BiomeRepository.biomeToInt.put(BiomeRepository.DEFAULT, 0);
        Arrays.fill(BiomeRepository.biomes, BiomeRepository.DEFAULT);
        BiomeRepository.generator = new Random();
        BiomeRepository.IDtoColor = new HashMap<Integer, Integer>(256);
        BiomeRepository.nameToColor = new TreeMap<String, Integer>();
        BiomeRepository.dirty = false;
    }
}
