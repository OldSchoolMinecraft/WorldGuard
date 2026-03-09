package com.sk89q.worldguard.bukkit;

import com.sk89q.worldedit.Vector;
import org.bukkit.block.*;
import com.sk89q.worldedit.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.inventory.*;
import com.sk89q.worldedit.blocks.*;
import org.bukkit.*;

public class BukkitUtil
{
    private BukkitUtil() {
    }
    
    public static BlockVector toVector(final Block block) {
        return new BlockVector(block.getX(), block.getY(), block.getZ());
    }
    
    public static Vector toVector(final Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }
    
    public static Vector toVector(final org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }
    
    public static Location toLocation(final World world, final Vector vec) {
        return new Location(world, vec.getX(), vec.getY(), vec.getZ());
    }
    
    public static Player matchSinglePlayer(final Server server, final String name) {
        final List<Player> players = (List<Player>)server.matchPlayer(name);
        if (players.size() == 0) {
            return null;
        }
        return players.get(0);
    }
    
    public static void dropSign(final Block block) {
        block.setTypeId(0);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN));
    }
    
    public static void setBlockToWater(final World world, final int ox, final int oy, final int oz) {
        final Block block = world.getBlockAt(ox, oy, oz);
        final int id = block.getTypeId();
        if (id == 0) {
            block.setTypeId(8);
        }
    }
    
    public static boolean isBlockWater(final World world, final int ox, final int oy, final int oz) {
        final Block block = world.getBlockAt(ox, oy, oz);
        final int id = block.getTypeId();
        return id == 8 || id == 9;
    }
    
    public static void findFreePosition(final Player player) {
        final Location loc = player.getLocation();
        final int x = loc.getBlockX();
        final int origY;
        int y = origY = Math.max(0, loc.getBlockY());
        final int z = loc.getBlockZ();
        final World world = player.getWorld();
        byte free = 0;
        while (y <= 129) {
            if (BlockType.canPassThrough(world.getBlockTypeIdAt(x, y, z))) {
                ++free;
            }
            else {
                free = 0;
            }
            if (free == 2) {
                if (y - 1 != origY || y == 1) {
                    loc.setX(x + 0.5);
                    loc.setY((double)y);
                    loc.setZ(z + 0.5);
                    if (y <= 2 && world.getBlockAt(x, 0, z).getType() == Material.AIR) {
                        world.getBlockAt(x, 0, z).setTypeId(20);
                        loc.setY(2.0);
                    }
                    player.setFallDistance(0.0f);
                    player.teleport(loc);
                }
                return;
            }
            ++y;
        }
    }
    
    public static String replaceColorMacros(String str) {
        str = str.replace("&r", ChatColor.RED.toString());
        str = str.replace("&R", ChatColor.DARK_RED.toString());
        str = str.replace("&y", ChatColor.YELLOW.toString());
        str = str.replace("&Y", ChatColor.GOLD.toString());
        str = str.replace("&g", ChatColor.GREEN.toString());
        str = str.replace("&G", ChatColor.DARK_GREEN.toString());
        str = str.replace("&c", ChatColor.AQUA.toString());
        str = str.replace("&C", ChatColor.DARK_AQUA.toString());
        str = str.replace("&b", ChatColor.BLUE.toString());
        str = str.replace("&B", ChatColor.DARK_BLUE.toString());
        str = str.replace("&p", ChatColor.LIGHT_PURPLE.toString());
        str = str.replace("&P", ChatColor.DARK_PURPLE.toString());
        str = str.replace("&0", ChatColor.BLACK.toString());
        str = str.replace("&1", ChatColor.DARK_GRAY.toString());
        str = str.replace("&2", ChatColor.GRAY.toString());
        str = str.replace("&w", ChatColor.WHITE.toString());
        return str;
    }
}
