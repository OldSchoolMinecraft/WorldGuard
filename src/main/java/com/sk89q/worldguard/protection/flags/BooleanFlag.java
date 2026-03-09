package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public class BooleanFlag extends Flag<Boolean>
{
    public BooleanFlag(final String name, final char legacyCode) {
        super(name, legacyCode);
    }
    
    public BooleanFlag(final String name) {
        super(name);
    }
    
    @Override
    public Boolean parseInput(final WorldGuardPlugin plugin, final CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("on") || input.equalsIgnoreCase("1")) {
            return true;
        }
        if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("off") || input.equalsIgnoreCase("0")) {
            return false;
        }
        throw new InvalidFlagFormat("Not a yes/no value: " + input);
    }
    
    @Override
    public Boolean unmarshal(final Object o) {
        if (o instanceof Boolean) {
            return (Boolean)o;
        }
        return null;
    }
    
    @Override
    public Object marshal(final Boolean o) {
        return o;
    }
}
