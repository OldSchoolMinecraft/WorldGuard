package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import org.bukkit.command.*;
import com.sk89q.worldguard.util.*;
import java.io.*;
import org.bukkit.entity.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.bukkit.selections.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.bukkit.*;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldguard.domains.*;
import java.util.*;
import com.sk89q.worldguard.protection.flags.*;

public class RegionCommands
{
    @Command(aliases = { "define", "def", "d" }, usage = "<id> [<owner1> [<owner2> [<owners...>]]]", desc = "Defines a region", flags = "", min = 1, max = -1)
    @CommandPermissions({ "worldguard.region.define" })
    public static void define(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException
    {
        final Player player = plugin.checkPlayer(sender);
        final WorldEditPlugin worldEdit = plugin.getWorldEdit();
        final String id = args.getString(0);
        if (!ProtectedRegion.isValidId(id)) {
            throw new CommandException("Invalid region ID specified!");
        }
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("A region cannot be named __global__");
        }
        final Selection sel = worldEdit.getSelection(player);
        if (sel == null) {
            throw new CommandException("Select a region with WorldEdit first.");
        }
        ProtectedRegion region;
        if (sel instanceof Polygonal2DSelection) {
            final Polygonal2DSelection polySel = (Polygonal2DSelection)sel;
            final int minY = polySel.getNativeMinimumPoint().getBlockY();
            final int maxY = polySel.getNativeMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        }
        else {
            if (!(sel instanceof CuboidSelection)) {
                throw new CommandException("The type of region selected in WorldEdit is unsupported in WorldGuard!");
            }
            final BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
            final BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
            region = new ProtectedCuboidRegion(id, min, max);
        }
        if (args.argsLength() > 1) {
            region.setOwners(RegionUtil.parseDomainString(args.getSlice(1), 1));
        }
        final RegionManager mgr = plugin.getGlobalRegionManager().get(sel.getWorld());
        mgr.addRegion(region);
        try {
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "redefine", "update", "move" }, usage = "<id>", desc = "Re-defines the shape of a region", flags = "", min = 1, max = 1)
    public static void redefine(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final WorldEditPlugin worldEdit = plugin.getWorldEdit();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("The region cannot be named __global__");
        }
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion existing = mgr.getRegion(id);
        if (existing == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        if (existing.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.redefine.own");
        }
        else if (existing.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.redefine.member");
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.redefine");
        }
        final Selection sel = worldEdit.getSelection(player);
        if (sel == null) {
            throw new CommandException("Select a region with WorldEdit first.");
        }
        ProtectedRegion region;
        if (sel instanceof Polygonal2DSelection) {
            final Polygonal2DSelection polySel = (Polygonal2DSelection)sel;
            final int minY = polySel.getNativeMinimumPoint().getBlockY();
            final int maxY = polySel.getNativeMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        }
        else {
            if (!(sel instanceof CuboidSelection)) {
                throw new CommandException("The type of region selected in WorldEdit is unsupported in WorldGuard!");
            }
            final BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
            final BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
            region = new ProtectedCuboidRegion(id, min, max);
        }
        region.setMembers(existing.getMembers());
        region.setOwners(existing.getOwners());
        region.setFlags(existing.getFlags());
        region.setPriority(existing.getPriority());
        try {
            region.setParent(existing.getParent());
        }
        catch (ProtectedRegion.CircularInheritanceException ex) {}
        mgr.addRegion(region);
        sender.sendMessage(ChatColor.YELLOW + "Region updated with new area.");
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "claim" }, usage = "<id> [<owner1> [<owner2> [<owners...>]]]", desc = "Claim a region", flags = "", min = 1, max = -1)
    @CommandPermissions({ "worldguard.region.claim" })
    public static void claim(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final WorldEditPlugin worldEdit = plugin.getWorldEdit();
        final String id = args.getString(0);
        if (!ProtectedRegion.isValidId(id)) {
            throw new CommandException("Invalid region ID specified!");
        }
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("A region cannot be named __global__");
        }
        final Selection sel = worldEdit.getSelection(player);
        if (sel == null) {
            throw new CommandException("Select a region with WorldEdit first.");
        }
        ProtectedRegion region;
        if (sel instanceof Polygonal2DSelection) {
            final Polygonal2DSelection polySel = (Polygonal2DSelection)sel;
            final int minY = polySel.getNativeMinimumPoint().getBlockY();
            final int maxY = polySel.getNativeMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        }
        else {
            if (!(sel instanceof CuboidSelection)) {
                throw new CommandException("The type of region selected in WorldEdit is unsupported in WorldGuard!");
            }
            final BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
            final BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
            region = new ProtectedCuboidRegion(id, min, max);
        }
        if (args.argsLength() > 1) {
            region.setOwners(RegionUtil.parseDomainString(args.getSlice(1), 1));
        }
        final WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());
        final RegionManager mgr = plugin.getGlobalRegionManager().get(sel.getWorld());
        if (!plugin.hasPermission(sender, "worldguard.region.unlimited") && wcfg.maxRegionCountPerPlayer >= 0 && mgr.getRegionCountOfPlayer(localPlayer) >= wcfg.maxRegionCountPerPlayer) {
            throw new CommandException("You own too many regions, delete one first to claim a new one.");
        }
        final ProtectedRegion existing = mgr.getRegion(id);
        if (existing != null && !existing.getOwners().contains(localPlayer)) {
            throw new CommandException("This region already exists and you don't own it.");
        }
        final ApplicableRegionSet regions = mgr.getApplicableRegions(region);
        if (regions.size() > 0) {
            if (!regions.isOwnerOfAll(localPlayer)) {
                throw new CommandException("This region overlaps with someone else's region.");
            }
        }
        else if (wcfg.claimOnlyInsideExistingRegions) {
            throw new CommandException("You may only claim regions inside existing regions that you or your group own.");
        }
        if (!plugin.hasPermission(sender, "worldguard.region.unlimited") && region.volume() > wcfg.maxClaimVolume) {
            player.sendMessage(ChatColor.RED + "This region is too large to claim.");
            player.sendMessage(ChatColor.RED + "Max. volume: " + wcfg.maxClaimVolume + ", your volume: " + region.volume());
            return;
        }
        region.getOwners().addPlayer(player.getName());
        mgr.addRegion(region);
        try {
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "select", "sel", "s" }, usage = "<id>", desc = "Load a region as a WorldEdit selection", flags = "", min = 1, max = 1)
    public static void select(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final WorldEditPlugin worldEdit = plugin.getWorldEdit();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.select.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.select.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.select." + id.toLowerCase());
        }
        if (region instanceof ProtectedCuboidRegion) {
            final ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion)region;
            final Vector pt1 = (Vector)cuboid.getMinimumPoint();
            final Vector pt2 = (Vector)cuboid.getMaximumPoint();
            final CuboidSelection selection = new CuboidSelection(world, pt1, pt2);
            worldEdit.setSelection(player, (Selection)selection);
            sender.sendMessage(ChatColor.YELLOW + "Region selected as a cuboid.");
        }
        else if (region instanceof ProtectedPolygonalRegion) {
            final ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion)region;
            final Polygonal2DSelection selection2 = new Polygonal2DSelection(world, (List)poly2d.getPoints(), poly2d.getMinimumPoint().getBlockY(), poly2d.getMaximumPoint().getBlockY());
            worldEdit.setSelection(player, (Selection)selection2);
            sender.sendMessage(ChatColor.YELLOW + "Region selected as a polygon.");
        }
        else {
            if (region instanceof GlobalProtectedRegion) {
                throw new CommandException("Can't select global regions.");
            }
            throw new CommandException("Unknown region type: " + region.getClass().getCanonicalName());
        }
    }
    
    @Command(aliases = { "info", "i" }, usage = "[world] <id>", desc = "Get information about a region", flags = "", min = 1, max = 2)
    public static void info(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        Player player = null;
        LocalPlayer localPlayer = null;
        World world;
        String id;
        if (args.argsLength() == 1) {
            player = plugin.checkPlayer(sender);
            localPlayer = plugin.wrapPlayer(player);
            world = player.getWorld();
            id = args.getString(0).toLowerCase();
        }
        else {
            world = plugin.matchWorld(sender, args.getString(0));
            id = args.getString(1).toLowerCase();
        }
        if (!ProtectedRegion.isValidId(id)) {
            throw new CommandException("Invalid region ID specified!");
        }
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        if (!mgr.hasRegion(id)) {
            throw new CommandException("A region with ID '" + id + "' doesn't exist.");
        }
        final ProtectedRegion region = mgr.getRegion(id);
        if (player != null) {
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.info.own");
            }
            else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.info.member");
            }
            else {
                plugin.checkPermission(sender, "worldguard.region.info");
            }
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.info");
        }
        final DefaultDomain owners = region.getOwners();
        final DefaultDomain members = region.getMembers();
        sender.sendMessage(ChatColor.YELLOW + "Region: " + id + ChatColor.GRAY + " (type: " + region.getTypeName() + ")");
        sender.sendMessage(ChatColor.BLUE + "Priority: " + region.getPriority());
        final StringBuilder s = new StringBuilder();
        for (final Flag<?> flag : DefaultFlag.getFlags()) {
            final Object val = region.getFlag(flag);
            if (val != null) {
                if (s.length() > 0) {
                    s.append(", ");
                }
                s.append(flag.getName() + ": " + String.valueOf(val));
            }
        }
        sender.sendMessage(ChatColor.BLUE + "Flags: " + s.toString());
        sender.sendMessage(ChatColor.BLUE + "Parent: " + ((region.getParent() == null) ? "(none)" : region.getParent().getId()));
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Owners: " + owners.toUserFriendlyString());
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Members: " + members.toUserFriendlyString());
        final BlockVector min = region.getMinimumPoint();
        final BlockVector max = region.getMaximumPoint();
        String c = "(" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ")";
        c = c + " (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")";
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Bounds: " + c);
    }
    
    @Command(aliases = { "list" }, usage = "[.player] [page] [world]", desc = "Get a list of regions", flags = "", min = 0, max = 3)
    public static void list(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        int page = 0;
        int argl = 0;
        String name = "";
        boolean own = false;
        LocalPlayer localPlayer = null;
        if (args.argsLength() > 0 && args.getString(0).startsWith(".")) {
            name = args.getString(0).substring(1).toLowerCase();
            argl = 1;
            if (name.equals("me") || name.isEmpty() || name.equals(plugin.checkPlayer(sender).getDisplayName().toLowerCase())) {
                plugin.checkPermission(sender, "worldguard.region.list.own");
                name = plugin.checkPlayer(sender).getDisplayName().toLowerCase();
                localPlayer = plugin.wrapPlayer(plugin.checkPlayer(sender));
                own = true;
            }
        }
        if (!own) {
            plugin.checkPermission(sender, "worldguard.region.list");
        }
        if (args.argsLength() > 0 + argl) {
            page = Math.max(0, args.getInteger(0 + argl) - 1);
        }
        World world;
        if (args.argsLength() > 1 + argl) {
            world = plugin.matchWorld(sender, args.getString(1 + argl));
        }
        else {
            world = plugin.checkPlayer(sender).getWorld();
        }
        final int listSize = 10;
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final Map<String, ProtectedRegion> regions = mgr.getRegions();
        int size = regions.size();
        String[] regionIDList = new String[size];
        int index = 0;
        boolean show = false;
        String prefix = "";
        for (final String id : regions.keySet()) {
            show = false;
            prefix = "";
            if (name.isEmpty()) {
                show = true;
            }
            else if (own) {
                if (regions.get(id).isOwner(localPlayer)) {
                    show = true;
                    prefix += "+";
                }
                else if (regions.get(id).isMember(localPlayer)) {
                    show = true;
                    prefix += "-";
                }
            }
            else {
                if (regions.get(id).getOwners().getPlayers().contains(name)) {
                    show = true;
                    prefix += "+";
                }
                if (regions.get(id).getMembers().getPlayers().contains(name)) {
                    show = true;
                    prefix += "-";
                }
            }
            if (show) {
                regionIDList[index] = prefix + " " + id;
                ++index;
            }
        }
        if (!name.isEmpty()) {
            regionIDList = Arrays.copyOf(regionIDList, index);
        }
        Arrays.sort(regionIDList);
        size = index;
        final int pages = (int)Math.ceil(size / (float)listSize);
        sender.sendMessage(ChatColor.RED + ((name == "") ? "Regions (page " : ("Regions for " + name + " (page ")) + (page + 1) + " of " + pages + "):");
        if (page < pages) {
            for (int i = page * listSize; i < page * listSize + listSize; ++i) {
                if (i >= size) {
                    break;
                }
                sender.sendMessage(ChatColor.YELLOW.toString() + (i + 1) + "." + regionIDList[i]);
            }
        }
    }
    
    @Command(aliases = { "flag", "f" }, usage = "<id> <flag> [value]", desc = "Set flags", flags = "", min = 2, max = -1)
    public static void flag(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        final String flagName = args.getString(1);
        String value = null;
        if (args.argsLength() >= 3) {
            value = args.getJoinedStrings(2);
        }
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            if (!id.equalsIgnoreCase("__global__")) {
                throw new CommandException("Could not find a region by that ID.");
            }
            region = new GlobalProtectedRegion(id);
            mgr.addRegion(region);
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.flag.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.flag.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.flag." + id.toLowerCase());
        }
        Flag<?> foundFlag = null;
        for (final Flag<?> flag : DefaultFlag.getFlags()) {
            if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
                foundFlag = flag;
                break;
            }
        }
        if (foundFlag == null) {
            final StringBuilder list = new StringBuilder();
            for (final Flag<?> flag2 : DefaultFlag.getFlags()) {
                if (list.length() > 0) {
                    list.append(", ");
                }
                Label_0528: {
                    if (region.isOwner(localPlayer)) {
                        if (!plugin.hasPermission(sender, "worldguard.region.flag.flags." + flag2.getName() + ".owner." + id.toLowerCase())) {
                            break Label_0528;
                        }
                    }
                    else if (region.isMember(localPlayer)) {
                        if (!plugin.hasPermission(sender, "worldguard.region.flag.flags." + flag2.getName() + ".member." + id.toLowerCase())) {
                            break Label_0528;
                        }
                    }
                    else if (!plugin.hasPermission(sender, "worldguard.region.flag.flags." + flag2.getName() + "." + id.toLowerCase())) {
                        break Label_0528;
                    }
                    list.append(flag2.getName());
                }
            }
            player.sendMessage(ChatColor.RED + "Unknown flag specified: " + flagName);
            player.sendMessage(ChatColor.RED + "Available flags: " + (Object)list);
            return;
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.flag.flags." + foundFlag.getName() + ".owner." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.flag.flags." + foundFlag.getName() + ".member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.flag.flags." + foundFlag.getName() + "." + id.toLowerCase());
        }
        if (value != null) {
            try {
                setFlag(region, foundFlag, plugin, sender, value);
            }
            catch (InvalidFlagFormat e) {
                throw new CommandException(e.getMessage());
            }
            sender.sendMessage(ChatColor.YELLOW + "Region flag '" + foundFlag.getName() + "' set.");
        }
        else {
            region.setFlag(foundFlag, null);
            sender.sendMessage(ChatColor.YELLOW + "Region flag '" + foundFlag.getName() + "' cleared.");
        }
        try {
            mgr.save();
        }
        catch (IOException e2) {
            throw new CommandException("Failed to write regions file: " + e2.getMessage());
        }
    }
    
    public static <V> void setFlag(final ProtectedRegion region, final Flag<V> flag, final WorldGuardPlugin plugin, final CommandSender sender, final String value) throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(plugin, sender, value));
    }
    
    @Command(aliases = { "setpriority", "priority", "pri" }, usage = "<id> <priority>", desc = "Set the priority of a region", flags = "", min = 2, max = 2)
    public static void setPriority(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        final int priority = args.getInteger(1);
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("The region cannot be named __global__");
        }
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.setpriority.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.setpriority.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.setpriority." + id.toLowerCase());
        }
        region.setPriority(priority);
        sender.sendMessage(ChatColor.YELLOW + "Priority of '" + region.getId() + "' set to " + priority + ".");
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "setparent", "parent", "par" }, usage = "<id> [parent-id]", desc = "Set the parent of a region", flags = "", min = 1, max = 2)
    public static void setParent(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);
        final String id = args.getString(0);
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("The region cannot be named __global__");
        }
        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final ProtectedRegion region = mgr.getRegion(id);
        if (args.argsLength() == 1) {
            try {
                region.setParent(null);
            }
            catch (ProtectedRegion.CircularInheritanceException ex) {}
            sender.sendMessage(ChatColor.YELLOW + "Parent of '" + region.getId() + "' cleared.");
        }
        else {
            final String parentId = args.getString(1);
            final ProtectedRegion parent = mgr.getRegion(parentId);
            if (region == null) {
                throw new CommandException("Could not find a target region by that ID.");
            }
            if (parent == null) {
                throw new CommandException("Could not find the parent region by that ID.");
            }
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.own." + id.toLowerCase());
            }
            else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.member." + id.toLowerCase());
            }
            else {
                plugin.checkPermission(sender, "worldguard.region.setparent." + id.toLowerCase());
            }
            if (parent.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.own." + id.toLowerCase());
            }
            else if (parent.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.member." + id.toLowerCase());
            }
            else {
                plugin.checkPermission(sender, "worldguard.region.setparent." + id.toLowerCase());
            }
            try {
                region.setParent(parent);
            }
            catch (ProtectedRegion.CircularInheritanceException e2) {
                throw new CommandException("Circular inheritance detected!");
            }
            sender.sendMessage(ChatColor.YELLOW + "Parent of '" + region.getId() + "' set to '" + parent.getId() + "'.");
        }
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "remove", "delete", "del", "rem" }, usage = "<id>", desc = "Remove a region", flags = "", min = 1, max = 1)
    public static void remove(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
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
            plugin.checkPermission(sender, "worldguard.region.remove.own." + id.toLowerCase());
        }
        else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.remove.member." + id.toLowerCase());
        }
        else {
            plugin.checkPermission(sender, "worldguard.region.remove." + id.toLowerCase());
        }
        mgr.removeRegion(id);
        sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' removed.");
        try {
            mgr.save();
        }
        catch (IOException e) {
            throw new CommandException("Failed to write regions file: " + e.getMessage());
        }
    }
    
    @Command(aliases = { "load", "reload" }, usage = "[world]", desc = "Reload regions from file", flags = "", min = 0, max = 1)
    public static void load(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        World world = null;
        if (args.argsLength() > 0) {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        if (world != null) {
            final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            try {
                mgr.load();
                sender.sendMessage(ChatColor.YELLOW + "Regions for '" + world.getName() + "' load.");
            }
            catch (IOException e) {
                throw new CommandException("Failed to read regions file: " + e.getMessage());
            }
        }
        else {
            for (final World w : plugin.getServer().getWorlds()) {
                final RegionManager mgr2 = plugin.getGlobalRegionManager().get(w);
                try {
                    mgr2.load();
                }
                catch (IOException e2) {
                    throw new CommandException("Failed to read regions file: " + e2.getMessage());
                }
            }
            sender.sendMessage(ChatColor.YELLOW + "Region databases loaded.");
        }
    }
    
    @Command(aliases = { "save", "write" }, usage = "[world]", desc = "Re-save regions to file", flags = "", min = 0, max = 1)
    public static void save(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        World world = null;
        if (args.argsLength() > 0) {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        if (world != null) {
            final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            try {
                mgr.save();
                sender.sendMessage(ChatColor.YELLOW + "Regions for '" + world.getName() + "' saved.");
            }
            catch (IOException e) {
                throw new CommandException("Failed to write regions file: " + e.getMessage());
            }
        }
        else {
            for (final World w : plugin.getServer().getWorlds()) {
                final RegionManager mgr2 = plugin.getGlobalRegionManager().get(w);
                try {
                    mgr2.save();
                }
                catch (IOException e2) {
                    throw new CommandException("Failed to write regions file: " + e2.getMessage());
                }
            }
            sender.sendMessage(ChatColor.YELLOW + "Region databases saved.");
        }
    }
}
