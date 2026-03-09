package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;
import java.util.*;

public class SetFlag<T> extends Flag<Set<T>>
{
    private Flag<T> subFlag;
    
    public SetFlag(final String name, final char legacyCode, final Flag<T> subFlag) {
        super(name, legacyCode);
        this.subFlag = subFlag;
    }
    
    public SetFlag(final String name, final Flag<T> subFlag) {
        super(name);
        this.subFlag = subFlag;
    }
    
    @Override
    public Set<T> parseInput(final WorldGuardPlugin plugin, final CommandSender sender, final String input) throws InvalidFlagFormat {
        final Set<T> items = new HashSet<T>();
        for (final String str : input.split(",")) {
            items.add(this.subFlag.parseInput(plugin, sender, str.trim()));
        }
        return new HashSet<T>((Collection<? extends T>)items);
    }
    
    @Override
    public Set<T> unmarshal(final Object o) {
        if (o instanceof Collection) {
            final Collection collection = (Collection)o;
            final Set<T> items = new HashSet<T>();
            for (final Object sub : collection) {
                final T item = this.subFlag.unmarshal(sub);
                if (item != null) {
                    items.add(item);
                }
            }
            return items;
        }
        return null;
    }
    
    @Override
    public Object marshal(final Set<T> o) {
        final List<Object> list = new ArrayList<Object>();
        for (final T item : o) {
            list.add(this.subFlag.marshal(item));
        }
        return list;
    }
}
