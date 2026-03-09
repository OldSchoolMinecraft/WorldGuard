package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public class CommandStringFlag extends Flag<String>
{
    public CommandStringFlag(final String name, final char legacyCode) {
        super(name, legacyCode);
    }
    
    public CommandStringFlag(final String name) {
        super(name);
    }
    
    @Override
    public String parseInput(final WorldGuardPlugin plugin, final CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        if (!input.startsWith("/")) {
            input = "/" + input;
        }
        return input.toLowerCase();
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
