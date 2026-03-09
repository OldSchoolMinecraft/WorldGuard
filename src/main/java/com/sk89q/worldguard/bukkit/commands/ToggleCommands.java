package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.*;
import org.bukkit.*;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.entity.*;
import com.sk89q.worldguard.bukkit.*;
import java.util.*;

public class ToggleCommands
{
    @Command(aliases = { "stopfire" }, usage = "[<world>]", desc = "Disables all fire spread temporarily", flags = "", min = 0, max = 1)
    @CommandPermissions({ "worldguard.fire-toggle.stop" })
    public static void stopFire(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException
    {
        World world;
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        }
        else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        final WorldConfiguration wcfg = plugin.getGlobalStateManager().get(world);
        if (!wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Fire spread has been globally disabled for '" + world.getName() + "' by " + plugin.toName(sender) + ".");
        }
        else {
            sender.sendMessage(ChatColor.YELLOW + "Fire spread was already globally disabled.");
        }
        wcfg.fireSpreadDisableToggle = true;
    }
    
    @Command(aliases = { "allowfire" }, usage = "[<world>]", desc = "Allows all fire spread temporarily", flags = "", min = 0, max = 1)
    @CommandPermissions({ "worldguard.fire-toggle.stop" })
    public static void allowFire(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        World world;
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        }
        else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        final WorldConfiguration wcfg = plugin.getGlobalStateManager().get(world);
        if (wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Fire spread has been globally for '" + world.getName() + "' re-enabled by " + plugin.toName(sender) + ".");
        }
        else {
            sender.sendMessage(ChatColor.YELLOW + "Fire spread was already globally enabled.");
        }
        wcfg.fireSpreadDisableToggle = false;
    }
    
    @Command(aliases = { "halt-activity" }, usage = "", desc = "Attempts to cease as much activity in order to stop lag", flags = "c", min = 0, max = 0)
    @CommandPermissions({ "worldguard.halt-activity" })
    public static void stopLag(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final ConfigurationManager configManager = plugin.getGlobalStateManager();
        configManager.activityHaltToggle = !args.hasFlag('c');
        if (configManager.activityHaltToggle) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.YELLOW + "ALL intensive server activity halted.");
            }
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "ALL intensive server activity halted by " + plugin.toName(sender) + ".");
            for (final World world : plugin.getServer().getWorlds()) {
                int removed = 0;
                for (final Entity entity : world.getEntities()) {
                    if (entity instanceof Item || (entity instanceof LivingEntity && !(entity instanceof Tameable) && !(entity instanceof Player))) {
                        entity.remove();
                        ++removed;
                    }
                }
                if (removed > 10) {
                    sender.sendMessage("" + removed + " entities (>10) auto-removed from " + world.toString());
                }
            }
        }
        else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.YELLOW + "ALL intensive server activity no longer halted.");
            }
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "ALL intensive server activity is now allowed.");
        }
    }
}
