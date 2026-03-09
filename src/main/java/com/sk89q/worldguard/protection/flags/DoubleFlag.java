package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public class DoubleFlag extends Flag<Double>
{
    public DoubleFlag(final String name, final char legacyCode) {
        super(name, legacyCode);
    }
    
    public DoubleFlag(final String name) {
        super(name);
    }
    
    @Override
    public Double parseInput(final WorldGuardPlugin plugin, final CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        try {
            return Double.parseDouble(input);
        }
        catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Not a number: " + input);
        }
    }
    
    @Override
    public Double unmarshal(final Object o) {
        if (o instanceof Double) {
            return (Double)o;
        }
        return null;
    }
    
    @Override
    public Object marshal(final Double o) {
        return o;
    }
}
