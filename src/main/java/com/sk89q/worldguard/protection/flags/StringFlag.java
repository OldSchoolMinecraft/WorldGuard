package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public class StringFlag extends Flag<String>
{
    public StringFlag(final String name, final char legacyCode) {
        super(name, legacyCode);
    }
    
    public StringFlag(final String name) {
        super(name);
    }
    
    @Override
    public String parseInput(final WorldGuardPlugin plugin, final CommandSender sender, final String input) throws InvalidFlagFormat {
        return input;
    }
    
    @Override
    public String unmarshal(final Object o) {
        if (o instanceof String) {
            return (String)o;
        }
        return null;
    }
    
    @Override
    public Object marshal(final String o) {
        return o;
    }
}
