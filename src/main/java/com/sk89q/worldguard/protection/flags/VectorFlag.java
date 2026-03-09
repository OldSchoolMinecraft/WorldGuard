package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import org.bukkit.command.*;
import com.sk89q.worldguard.bukkit.*;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.command.CommandException;

import java.util.*;

public class VectorFlag extends Flag<Vector>
{
    public VectorFlag(final String name, final char legacyCode) {
        super(name, legacyCode);
    }
    
    public VectorFlag(final String name) {
        super(name);
    }
    
    @Override
    public Vector parseInput(final WorldGuardPlugin plugin, final CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        try {
            return BukkitUtil.toVector(plugin.checkPlayer(sender).getLocation());
        }
        catch (CommandException e) {
            throw new InvalidFlagFormat(e.getMessage());
        }
    }
    
    @Override
    public Vector unmarshal(final Object o) {
        if (!(o instanceof Map)) {
            return null;
        }
        final Map<?, ?> map = (Map<?, ?>)o;
        final Object rawX = map.get("x");
        final Object rawY = map.get("y");
        final Object rawZ = map.get("z");
        if (rawX == null || rawY == null || rawZ == null) {
            return null;
        }
        return new Vector(this.toNumber(rawX), this.toNumber(rawY), this.toNumber(rawZ));
    }
    
    @Override
    public Object marshal(final Vector o) {
        final Map<String, Object> vec = new HashMap<String, Object>();
        vec.put("x", o.getX());
        vec.put("y", o.getY());
        vec.put("z", o.getZ());
        return vec;
    }
    
    private double toNumber(final Object o) {
        if (o instanceof Integer) {
            return (int)o;
        }
        if (o instanceof Long) {
            return (double)(long)o;
        }
        if (o instanceof Float) {
            return (float)o;
        }
        if (o instanceof Double) {
            return (double)o;
        }
        return 0.0;
    }
}
