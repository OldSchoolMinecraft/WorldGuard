package com.sk89q.worldguard.protection.flags;

import org.bukkit.entity.*;

public class CreatureTypeFlag extends EnumFlag<CreatureType>
{
    public CreatureTypeFlag(final String name, final char legacyCode) {
        super(name, legacyCode, CreatureType.class);
    }
    
    public CreatureTypeFlag(final String name) {
        super(name, CreatureType.class);
    }
    
    @Override
    public CreatureType detectValue(final String input) {
        CreatureType lowMatch = null;
        for (final CreatureType type : CreatureType.values()) {
            if (type.name().equalsIgnoreCase(input.trim())) {
                return type;
            }
            if (type.name().toLowerCase().startsWith(input.toLowerCase().trim())) {
                lowMatch = type;
            }
        }
        return lowMatch;
    }
}
