package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public class IntegerFlag extends Flag<Integer>
{
    public IntegerFlag(final String name, final char legacyCode) {
        super(name, legacyCode);
    }
    
    public IntegerFlag(final String name) {
        super(name);
    }
    
    @Override
    public Integer parseInput(final WorldGuardPlugin plugin, final CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        try {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Not a number: " + input);
        }
    }
    
    @Override
    public Integer unmarshal(final Object o) {
        if (o instanceof Integer) {
            return (Integer)o;
        }
        return null;
    }
    
    @Override
    public Object marshal(final Integer o) {
        return o;
    }
}
