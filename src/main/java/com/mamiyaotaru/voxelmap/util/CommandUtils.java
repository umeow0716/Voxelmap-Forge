// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.gui.GuiSelectPlayer;
import com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.TreeSet;
import java.util.regex.Matcher;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Random;

public class CommandUtils
{
	private static final Minecraft game = Minecraft.getInstance();
    private static final int NEW_WAYPOINT_COMMAND_LENGTH;
    private static final int TELEPORT_COMMAND_LENGTH;
    private static Random generator;
    public static Pattern pattern;
    
    public static boolean checkForWaypoints(final Component chat) {
        final String message = chat.getString();
        final ArrayList<String> waypointStrings = getWaypointStrings(message);
        if (waypointStrings.size() > 0) {
            final ArrayList<TextComponent> textComponents = new ArrayList<TextComponent>();
            int count = 0;
            for (final String waypointString : waypointStrings) {
                final int waypointStringLocation = message.indexOf(waypointString);
                if (waypointStringLocation > count) {
                    textComponents.add(new TextComponent(message.substring(count, waypointStringLocation)));
                }
                final TextComponent clickableWaypoint = new TextComponent(waypointString);
                Style chatStyle = clickableWaypoint.getStyle();
                chatStyle = chatStyle.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/newWaypoint " + waypointString.substring(1, waypointString.length() - 1)));
                chatStyle = chatStyle.withColor(ChatFormatting.AQUA);
                final TextComponent hover = new TextComponent(I18nUtils.getString("minimap.waypointshare.tooltip1", new Object[0]) + "\n" + I18nUtils.getString("minimap.waypointshare.tooltip2", new Object[0]));
                chatStyle = chatStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                clickableWaypoint.setStyle(chatStyle);
                textComponents.add(clickableWaypoint);
                count = waypointStringLocation + waypointString.length();
            }
            if (count < message.length() - 1) {
                textComponents.add(new TextComponent(message.substring(count, message.length())));
            }
            final TextComponent finalTextComponent = new TextComponent("");
            for (final TextComponent textComponent : textComponents) {
                finalTextComponent.append(textComponent);
            }
            game.gui.getChat().addMessage(finalTextComponent);
            return false;
        }
        return true;
    }
    
    public static ArrayList<String> getWaypointStrings(final String message) {
        final ArrayList<String> list = new ArrayList<String>();
        if (message.contains("[") && message.contains("]")) {
            final Matcher matcher = CommandUtils.pattern.matcher(message);
            while (matcher.find()) {
                final String match = matcher.group();
                if (createWaypointFromChat(match.substring(1, match.length() - 1)) != null) {
                    list.add(match);
                }
            }
        }
        return list;
    }
    
    private static Waypoint createWaypointFromChat(final String details) {
        Waypoint waypoint = null;
        final String[] pairs = details.split(",");
        try {
            String name = "";
            Integer x = null;
            Integer z = null;
            Integer y = 64;
            boolean enabled = true;
            float red = CommandUtils.generator.nextFloat();
            float green = CommandUtils.generator.nextFloat();
            float blue = CommandUtils.generator.nextFloat();
            String suffix = "";
            String world = "";
            final TreeSet<DimensionContainer> dimensions = new TreeSet<DimensionContainer>();
            for (int t = 0; t < pairs.length; ++t) {
                final int splitIndex = pairs[t].indexOf(":");
                if (splitIndex != -1) {
                    final String key = pairs[t].substring(0, splitIndex).toLowerCase().trim();
                    final String value = pairs[t].substring(splitIndex + 1).trim();
                    if (key.equals("name")) {
                        name = TextUtils.descrubName(value);
                    }
                    else if (key.equals("x")) {
                        x = Integer.parseInt(value);
                    }
                    else if (key.equals("z")) {
                        z = Integer.parseInt(value);
                    }
                    else if (key.equals("y")) {
                        y = Integer.parseInt(value);
                    }
                    else if (key.equals("enabled")) {
                        enabled = Boolean.parseBoolean(value);
                    }
                    else if (key.equals("red")) {
                        red = Float.parseFloat(value);
                    }
                    else if (key.equals("green")) {
                        green = Float.parseFloat(value);
                    }
                    else if (key.equals("blue")) {
                        blue = Float.parseFloat(value);
                    }
                    else if (key.equals("color")) {
                        final int color = Integer.decode(value);
                        red = (color >> 16 & 0xFF) / 255.0f;
                        green = (color >> 8 & 0xFF) / 255.0f;
                        blue = (color >> 0 & 0xFF) / 255.0f;
                    }
                    else if (key.equals("suffix") || key.equals("icon")) {
                        suffix = value;
                    }
                    else if (key.equals("world")) {
                        world = TextUtils.descrubName(value);
                    }
                    else if (key.equals("dimensions")) {
                        final String[] dimensionStrings = value.split("#");
                        for (int s = 0; s < dimensionStrings.length; ++s) {
                            dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByIdentifier(dimensionStrings[s]));
                        }
                    }
                    else if (key.equals("dimension") || key.equals("dim")) {
                        dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByIdentifier(value));
                    }
                }
            }
            if (world == "") {
                world = AbstractVoxelMap.getInstance().getWaypointManager().getCurrentSubworldDescriptor(false);
            }
            if (dimensions.size() == 0) {
                dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(game.level));
            }
            if (x != null && z != null) {
                if (dimensions.size() == 1 && dimensions.first().type.coordinateScale() != 1.0) {
                    final double dimensionScale = dimensions.first().type.coordinateScale();
                    x = (int)(x * dimensionScale);
                    z = (int)(z * dimensionScale);
                }
                waypoint = new Waypoint(name, x, z, y, enabled, red, green, blue, suffix, world, dimensions);
            }
        }
        catch (final NumberFormatException e) {
            waypoint = null;
        }
        return waypoint;
    }
    
    public static void waypointClicked(final String command) {
        final boolean control = InputConstants.isKeyDown(game.getWindow().getWindow(), InputConstants.getKey("key.keyboard.left.control").getValue()) || InputConstants.isKeyDown(game.getWindow().getWindow(), InputConstants.getKey("key.keyboard.right.control").getValue());
        final String details = command.substring(CommandUtils.NEW_WAYPOINT_COMMAND_LENGTH);
        final Waypoint newWaypoint = createWaypointFromChat(details);
        if (newWaypoint != null) {
            for (final Waypoint existingWaypoint : AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints()) {
                if (newWaypoint.getX() == existingWaypoint.getX() && newWaypoint.getZ() == existingWaypoint.getZ()) {
                    if (control) {
                        game.setScreen(new GuiAddWaypoint(null, AbstractVoxelMap.getInstance(), existingWaypoint, true));
                    }
                    else {
                        AbstractVoxelMap.getInstance().getWaypointManager().setHighlightedWaypoint(existingWaypoint, false);
                    }
                    return;
                }
            }
            if (control) {
                game.setScreen(new GuiAddWaypoint(null, AbstractVoxelMap.getInstance(), newWaypoint, false));
            }
            else {
                AbstractVoxelMap.getInstance().getWaypointManager().setHighlightedWaypoint(newWaypoint, false);
            }
        }
    }
    
    public static void sendWaypoint(final Waypoint waypoint, final Screen parentScreen) {
        final ResourceLocation resourceLocation = AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld(game.level).resourceLocation;
        final int color = ((int)(waypoint.red * 255.0f) & 0xFF) << 16 | ((int)(waypoint.green * 255.0f) & 0xFF) << 8 | ((int)(waypoint.blue * 255.0f) & 0xFF);
        String hexColor;
        for (hexColor = Integer.toHexString(color); hexColor.length() < 6; hexColor = "0" + hexColor) {}
        hexColor = "#" + hexColor;
        String world = AbstractVoxelMap.getInstance().getWaypointManager().getCurrentSubworldDescriptor(false);
        if (waypoint.world != null && waypoint.world != "") {
            world = waypoint.world;
        }
        final String suffix = waypoint.imageSuffix;
        final Object[] args = { TextUtils.scrubNameRegex(waypoint.name), waypoint.getX(), waypoint.getY(), waypoint.getZ(), resourceLocation.toString() };
        String message = String.format("[name:%s, x:%s, y:%s, z:%s, dim:%s", args);
        if (world != null && !world.equals("")) {
            message = message + ", world:" + world;
        }
        if (suffix != null && !suffix.equals("")) {
            message = message + ", icon:" + suffix;
        }
        message += "]";
        game.setScreen(new GuiSelectPlayer(parentScreen, AbstractVoxelMap.getInstance(), message, true));
    }
    
    public static void sendCoordinate(final int x, final int y, final int z) {
        final String message = String.format("[x:%s, y:%s, z:%s]", x, y, z);
        game.setScreen((Screen) new GuiSelectPlayer(null, AbstractVoxelMap.getInstance(), message, false));
    }
    
    public static void teleport(final String command) {
        final String details = command.substring(CommandUtils.TELEPORT_COMMAND_LENGTH);
        final ArrayList<Waypoint> waypoints = AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints();
        for (final Waypoint wp : waypoints) {
            if (wp.name.equalsIgnoreCase(details) && wp.inDimension && wp.inWorld) {
                final boolean mp = !game.isLocalServer();
                final int y = (wp.getY() > 0) ? wp.getY() : (game.player.level.dimensionType().hasCeiling() ? 64 : 256);
                game.player.chat("/tp " + game.player.getName().getString() + " " + wp.getX() + " " + y + " " + wp.getZ());
                if (mp) {
                    game.player.chat("/tppos " + wp.getX() + " " + y + " " + wp.getZ());
                }
            }
        }
    }
    
    public static int getSafeHeight(final int x, int y, final int z, final Level worldObj) {
        final boolean inNetherDimension = worldObj.dimensionType().hasCeiling();
        final BlockPos blockPos = new BlockPos(x, y, z);
        worldObj.getChunkAt(blockPos);
        worldObj.getChunkSource().getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.FULL, true);
        if (inNetherDimension) {
            int safeY = -1;
            for (int t = 0; t < 127; ++t) {
                if (y + t < 127 && isBlockStandable(worldObj, x, y + t, z) && isBlockOpen(worldObj, x, y + t + 1, z) && isBlockOpen(worldObj, x, y + t + 2, z)) {
                    safeY = y + t + 1;
                    t = 128;
                }
                if (y - t > 0 && isBlockStandable(worldObj, x, y - t, z) && isBlockOpen(worldObj, x, y - t + 1, z) && isBlockOpen(worldObj, x, y - t + 2, z)) {
                    safeY = y - t + 1;
                    t = 128;
                }
            }
            y = safeY;
        }
        else if (y <= 0) {
            y = worldObj.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
        }
        return y;
    }
    
    private static boolean isBlockStandable(final Level worldObj, final int par1, final int par2, final int par3) {
        final BlockPos blockPos = new BlockPos(par1, par2, par3);
        final BlockState blockState = worldObj.getBlockState(blockPos);
        final Block block = blockState.getBlock();
        return block != null && blockState.getMaterial().isSolidBlocking();
    }
    
    private static boolean isBlockOpen(final Level worldObj, final int par1, final int par2, final int par3) {
        final BlockPos blockPos = new BlockPos(par1, par2, par3);
        final BlockState blockState = worldObj.getBlockState(blockPos);
        final Block block = blockState.getBlock();
        return block == null || !blockState.isViewBlocking(worldObj, blockPos);
    }
    
    static {
        NEW_WAYPOINT_COMMAND_LENGTH = "/newWaypoint ".length();
        TELEPORT_COMMAND_LENGTH = "/ztp ".length();
        CommandUtils.generator = new Random();
        CommandUtils.pattern = Pattern.compile("\\[(\\w+\\s*:\\s*[-#]?[^\\[\\]]+)(,\\s*\\w+\\s*:\\s*[-#]?[^\\[\\]]+)+\\]", 2);
    }
}
