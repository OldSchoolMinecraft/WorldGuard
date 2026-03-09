package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.protection.*;

public class RegionGroupFlag extends Flag<RegionGroupFlag.RegionGroup>
{
    private RegionGroup def;
    
    public RegionGroupFlag(final String name, final char legacyCode, final RegionGroup def) {
        super(name, legacyCode);
        this.def = def;
    }
    
    public RegionGroupFlag(final String name, final RegionGroup def) {
        super(name);
        this.def = def;
    }
    
    public RegionGroup getDefault() {
        return this.def;
    }
    
    @Override
    public RegionGroup parseInput(final WorldGuardPlugin plugin, final CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        if (input.equalsIgnoreCase("members") || input.equalsIgnoreCase("member")) {
            return RegionGroup.MEMBERS;
        }
        if (input.equalsIgnoreCase("owners") || input.equalsIgnoreCase("owner")) {
            return RegionGroup.OWNERS;
        }
        if (input.equalsIgnoreCase("nonowners") || input.equalsIgnoreCase("nonowner")) {
            return RegionGroup.NON_OWNERS;
        }
        if (input.equalsIgnoreCase("nonmembers") || input.equalsIgnoreCase("nonmember")) {
            return RegionGroup.NON_MEMBERS;
        }
        if (input.equalsIgnoreCase("everyone") || input.equalsIgnoreCase("anyone")) {
            return null;
        }
        throw new InvalidFlagFormat("Not none/allow/deny: " + input);
    }
    
    @Override
    public RegionGroup unmarshal(final Object o) {
        final String str = o.toString();
        if (str.equalsIgnoreCase("members")) {
            return RegionGroup.MEMBERS;
        }
        if (str.equalsIgnoreCase("owners")) {
            return RegionGroup.OWNERS;
        }
        if (str.equalsIgnoreCase("nonmembers")) {
            return RegionGroup.NON_MEMBERS;
        }
        if (str.equalsIgnoreCase("nonowners")) {
            return RegionGroup.NON_OWNERS;
        }
        return null;
    }
    
    @Override
    public Object marshal(final RegionGroup o) {
        if (o == RegionGroup.MEMBERS) {
            return "members";
        }
        if (o == RegionGroup.OWNERS) {
            return "owners";
        }
        if (o == RegionGroup.NON_MEMBERS) {
            return "nonmembers";
        }
        if (o == RegionGroup.NON_OWNERS) {
            return "nonowners";
        }
        return null;
    }
    
    public static boolean isMember(final ProtectedRegion region, final RegionGroup group, final LocalPlayer player) {
        if (group == RegionGroup.OWNERS) {
            if (region.isOwner(player)) {
                return true;
            }
        }
        else if (group == RegionGroup.MEMBERS) {
            if (region.isMember(player)) {
                return true;
            }
        }
        else if (group == RegionGroup.NON_OWNERS) {
            if (!region.isOwner(player)) {
                return true;
            }
        }
        else if (group == RegionGroup.NON_MEMBERS && !region.isMember(player)) {
            return true;
        }
        return false;
    }
    
    public static boolean isMember(final ApplicableRegionSet set, final RegionGroup group, final LocalPlayer player) {
        if (group == RegionGroup.OWNERS) {
            if (set.isOwnerOfAll(player)) {
                return true;
            }
        }
        else if (group == RegionGroup.MEMBERS) {
            if (set.isMemberOfAll(player)) {
                return true;
            }
        }
        else if (group == RegionGroup.NON_OWNERS) {
            if (!set.isOwnerOfAll(player)) {
                return true;
            }
        }
        else if (group == RegionGroup.NON_MEMBERS && !set.isMemberOfAll(player)) {
            return true;
        }
        return false;
    }
    
    public enum RegionGroup
    {
        MEMBERS, 
        OWNERS, 
        NON_MEMBERS, 
        NON_OWNERS;
    }
}
