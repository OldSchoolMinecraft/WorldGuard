package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public class EnumFlag<T extends Enum<T>> extends Flag<T>
{
    private Class<T> enumClass;
    
    public EnumFlag(final String name, final char legacyCode, final Class<T> enumClass) {
        super(name, legacyCode);
        this.enumClass = enumClass;
    }
    
    public EnumFlag(final String name, final Class<T> enumClass) {
        super(name);
        this.enumClass = enumClass;
    }
    
    private T findValue(final String input) throws IllegalArgumentException {
        try {
            return Enum.valueOf(this.enumClass, input);
        }
        catch (IllegalArgumentException e) {
            final T val = this.detectValue(input);
            if (val != null) {
                return val;
            }
            throw e;
        }
    }
    
    public T detectValue(final String input) {
        return null;
    }
    
    @Override
    public T parseInput(final WorldGuardPlugin plugin, final CommandSender sender, final String input) throws InvalidFlagFormat {
        try {
            return this.findValue(input);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidFlagFormat("Unknown value '" + input + "' in " + this.enumClass.getName());
        }
    }
    
    @Override
    public T unmarshal(final Object o) {
        try {
            return Enum.valueOf(this.enumClass, String.valueOf(o));
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    @Override
    public Object marshal(final T o) {
        return o.name();
    }
}
