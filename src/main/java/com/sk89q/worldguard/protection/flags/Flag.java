package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public abstract class Flag<T>
{
    private String name;
    private Character legacyCode;
    
    public Flag(final String name, final char legacyCode) {
        this.name = name;
        this.legacyCode = legacyCode;
    }
    
    public Flag(final String name) {
        this.name = name;
        this.legacyCode = null;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Character getLegacyCode() {
        return this.legacyCode;
    }
    
    public abstract T parseInput(final WorldGuardPlugin p0, final CommandSender p1, final String p2) throws InvalidFlagFormat;
    
    public abstract T unmarshal(final Object p0);
    
    public abstract Object marshal(final T p0);
}
