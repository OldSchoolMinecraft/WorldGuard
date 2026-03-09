package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;
import com.sk89q.worldguard.util.*;
import java.io.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.minecraft.util.commands.*;

public class RegionMemberCommands
{
    @Command(aliases = { "addmember", "addmember" }, usage = "<id> <members...>", desc = "Add a member to a region", flags = "", min = 2, max = -1)
    public static void addMember(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException
    {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.addmember.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.addmember.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.addmember." + id.toLowerCase());
        }
        RegionUtil.addToDomain(region.getMembers(), args.getPaddedSlice(2, 0), 0);
        sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "addowner", "addowner" }, usage = "<id> <owners...>", desc = "Add an owner to a region", flags = "", min = 2, max = -1)
    public static void addOwner(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.addowner.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.addowner.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.addowner." + id.toLowerCase());
        }
        RegionUtil.addToDomain(region.getOwners(), args.getPaddedSlice(2, 0), 0);
        sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "removemember", "remmember", "removemem", "remmem" }, usage = "<id> <owners...>", desc = "Remove an owner to a region", flags = "", min = 2, max = -1)
    public static void removeMember(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.removemember.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.removemember.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.removemember." + id.toLowerCase());
        }
        RegionUtil.removeFromDomain(region.getMembers(), args.getPaddedSlice(2, 0), 0);
        sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "removeowner", "remowner" }, usage = "<id> <owners...>", desc = "Remove an owner to a region", flags = "", min = 2, max = -1)
    public static void removeOwner(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.removeowner.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.removeowner.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.removeowner." + id.toLowerCase());
        }
        RegionUtil.removeFromDomain(region.getOwners(), args.getPaddedSlice(2, 0), 0);
        sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
}
