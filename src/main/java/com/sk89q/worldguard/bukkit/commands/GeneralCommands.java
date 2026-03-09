package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import com.sk89q.worldguard.bukkit.*;
import java.util.*;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldedit.blocks.*;
import org.bukkit.inventory.*;

public class GeneralCommands
{
    @Command(aliases = { "god" }, usage = "[player]", desc = "Enable godmode on a player", flags = "s", min = 0, max = 1)
    public static void god(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException
    {
        final ConfigurationManager config = plugin.getGlobalStateManager();
        Iterable<Player> targets = null;
        boolean included = false;
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            plugin.checkPermission(sender, "worldguard.god");
        }
        else if (args.argsLength() == 1) {
            targets = plugin.matchPlayers(sender, args.getString(0));
            plugin.checkPermission(sender, "worldguard.god.other");
        }
        for (final Player player : targets) {
            config.enableGodMode(player);
            player.setFireTicks(0);
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "God mode enabled! Use /ungod to disable.");
                included = true;
            }
            else {
                player.sendMessage(ChatColor.YELLOW + "God enabled by " + plugin.toName(sender) + ".");
            }
        }
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players now have god mode.");
        }
    }
    
    @Command(aliases = { "ungod" }, usage = "[player]", desc = "Disable godmode on a player", flags = "s", min = 0, max = 1)
    public static void ungod(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final ConfigurationManager config = plugin.getGlobalStateManager();
        Iterable<Player> targets = null;
        boolean included = false;
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            plugin.checkPermission(sender, "worldguard.god");
        }
        else if (args.argsLength() == 1) {
            targets = plugin.matchPlayers(sender, args.getString(0));
            plugin.checkPermission(sender, "worldguard.god.other");
        }
        for (final Player player : targets) {
            config.disableGodMode(player);
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "God mode disabled!");
                included = true;
            }
            else {
                player.sendMessage(ChatColor.YELLOW + "God disabled by " + plugin.toName(sender) + ".");
            }
        }
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players no longer have god mode.");
        }
    }
    
    @Command(aliases = { "heal" }, usage = "[player]", desc = "Heal a player", flags = "s", min = 0, max = 1)
    public static void heal(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        Iterable<Player> targets = null;
        boolean included = false;
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            plugin.checkPermission(sender, "worldguard.heal");
        }
        else if (args.argsLength() == 1) {
            targets = plugin.matchPlayers(sender, args.getString(0));
            plugin.checkPermission(sender, "worldguard.heal.other");
        }
        for (final Player player : targets) {
            player.setHealth(20);
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "Healed!");
                included = true;
            }
            else {
                player.sendMessage(ChatColor.YELLOW + "Healed by " + plugin.toName(sender) + ".");
            }
        }
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players healed.");
        }
    }
    
    @Command(aliases = { "slay" }, usage = "[player]", desc = "Slay a player", flags = "s", min = 0, max = 1)
    public static void slay(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        Iterable<Player> targets = null;
        boolean included = false;
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            plugin.checkPermission(sender, "worldguard.slay");
        }
        else if (args.argsLength() == 1) {
            targets = plugin.matchPlayers(sender, args.getString(0));
            plugin.checkPermission(sender, "worldguard.slay.other");
        }
        for (final Player player : targets) {
            player.setHealth(0);
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "Slain!");
                included = true;
            }
            else {
                player.sendMessage(ChatColor.YELLOW + "Slain by " + plugin.toName(sender) + ".");
            }
        }
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players slain.");
        }
    }
    
    @Command(aliases = { "locate" }, usage = "[player]", desc = "Locate a player", flags = "", min = 0, max = 1)
    @CommandPermissions({ "worldguard.locate" })
    public static void locate(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        if (args.argsLength() == 0) {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            sender.sendMessage(ChatColor.YELLOW.toString() + "Compass reset to spawn.");
        }
        else {
            final Player target = plugin.matchSinglePlayer(sender, args.getString(0));
            player.setCompassTarget(target.getLocation());
            sender.sendMessage(ChatColor.YELLOW.toString() + "Compass repointed.");
        }
    }
    
    @Command(aliases = { "stack" }, usage = "", desc = "Stack items", flags = "", min = 0, max = 0)
    @CommandPermissions({ "worldguard.stack" })
    public static void stack(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);
        final ItemStack[] items = player.getInventory().getContents();
        final int len = items.length;
        int affected = 0;
        for (int i = 0; i < len; ++i) {
            final ItemStack item = items[i];
            if (item != null && item.getAmount() > 0) {
                if (!ItemType.shouldNotStack(item.getTypeId())) {
                    if (item.getTypeId() < 325 || item.getTypeId() > 327) {
                        if (item.getAmount() < 64) {
                            int needed = 64 - item.getAmount();
                            for (int j = i + 1; j < len; ++j) {
                                final ItemStack item2 = items[j];
                                if (item2 != null && item2.getAmount() > 0) {
                                    if (!ItemType.shouldNotStack(item.getTypeId())) {
                                        if (item2.getTypeId() == item.getTypeId() && (!ItemType.usesDamageValue(item.getTypeId()) || item.getDurability() == item2.getDurability())) {
                                            if (item2.getAmount() > needed) {
                                                item.setAmount(64);
                                                item2.setAmount(item2.getAmount() - needed);
                                                break;
                                            }
                                            items[j] = null;
                                            item.setAmount(item.getAmount() + item2.getAmount());
                                            needed = 64 - item.getAmount();
                                            ++affected;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (affected > 0) {
            player.getInventory().setContents(items);
        }
        player.sendMessage(ChatColor.YELLOW + "Items compacted into stacks!");
    }
}
