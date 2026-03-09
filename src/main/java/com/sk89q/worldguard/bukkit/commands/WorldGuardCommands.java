package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.logging.*;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldguard.bukkit.*;
import java.io.*;
import com.sk89q.worldguard.util.*;

public class WorldGuardCommands
{
    @Command(aliases = { "version" }, usage = "", desc = "Get the WorldGuard version", flags = "", min = 0, max = 0)
    public static void version(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException
    {
        sender.sendMessage(ChatColor.YELLOW + "WorldGuard " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "http://www.sk89q.com");
    }
    
    @Command(aliases = { "reload" }, usage = "", desc = "Reload WorldGuard configuration", flags = "", min = 0, max = 0)
    @CommandPermissions({ "worldguard.reload" })
    public static void relload(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;
        if (sender instanceof Player) {
            handler = new LoggerToChatHandler(sender);
            handler.setLevel(Level.ALL);
            minecraftLogger = Logger.getLogger("Minecraft");
            minecraftLogger.addHandler(handler);
        }
        try {
            plugin.getGlobalStateManager().unload();
            plugin.getGlobalRegionManager().unload();
            plugin.getGlobalStateManager().load();
            plugin.getGlobalRegionManager().preload();
            sender.sendMessage("WorldGuard configuration reloaded.");
        }
        catch (Throwable t) {
            sender.sendMessage("Error while reloading: " + t.getMessage());
        }
        finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }
    }
    
    @Command(aliases = { "report" }, usage = "", desc = "Writes a report on WorldGuard", flags = "p", min = 0, max = 0)
    @CommandPermissions({ "worldguard.report" })
    public static void report(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
        final File dest = new File(plugin.getDataFolder(), "report.txt");
        final ReportWriter report = new ReportWriter(plugin);
        try {
            report.write(dest);
            sender.sendMessage(ChatColor.YELLOW + "WorldGuard report written to " + dest.getAbsolutePath());
        }
        catch (IOException e) {
            throw new CommandException("Failed to write report: " + e.getMessage());
        }
        if (args.hasFlag('p')) {
            plugin.checkPermission(sender, "worldguard.report.pastebin");
            sender.sendMessage(ChatColor.YELLOW + "Now uploading to Pastebin...");
            PastebinPoster.paste(report.toString(), new PastebinPoster.PasteCallback() {
                public void handleSuccess(final String url) {
                    sender.sendMessage(ChatColor.YELLOW + "WorldGuard report (1 hour): " + url);
                }
                
                public void handleError(final String err) {
                    sender.sendMessage(ChatColor.YELLOW + "WorldGuard report pastebin error: " + err);
                }
            });
        }
    }
}
