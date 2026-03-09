package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;
import com.sk89q.minecraft.util.commands.*;

public class ProtectionCommands
{
    @Command(aliases = { "region" }, desc = "Region management commands")
    @NestedCommand({ RegionCommands.class, RegionMemberCommands.class })
    public static void region(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException
    {
    }
    
    @Command(aliases = { "worldguard" }, desc = "WorldGuard commands")
    @NestedCommand({ WorldGuardCommands.class })
    public static void worldGuard(final CommandContext args, final WorldGuardPlugin plugin, final CommandSender sender) throws CommandException {
    }
}
