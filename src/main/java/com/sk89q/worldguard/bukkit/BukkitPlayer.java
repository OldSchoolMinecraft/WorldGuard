package com.sk89q.worldguard.bukkit;

import com.sk89q.worldguard.*;
import com.sk89q.worldguard.LocalPlayer;
import org.bukkit.entity.*;
import com.sk89q.worldedit.*;
import org.bukkit.*;
import org.bukkit.command.*;

public class BukkitPlayer extends LocalPlayer
{
    private Player player;
    private WorldGuardPlugin plugin;
    
    public BukkitPlayer(final WorldGuardPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    @Override
    public String getName() {
        return this.player.getName();
    }
    
    @Override
    public boolean hasGroup(final String group) {
        return this.plugin.inGroup(this.player, group);
    }
    
    @Override
    public Vector getPosition() {
        final Location loc = this.player.getLocation();
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }
    
    @Override
    public void kick(final String msg) {
        this.player.kickPlayer(msg);
    }
    
    @Override
    public void ban(final String msg) {
        this.player.kickPlayer(msg);
    }
    
    @Override
    public String[] getGroups() {
        return this.plugin.getGroups(this.player);
    }
    
    @Override
    public void printRaw(final String msg) {
        this.player.sendMessage(msg);
    }
    
    @Override
    public boolean hasPermission(final String perm) {
        return this.plugin.hasPermission((CommandSender)this.player, perm);
    }
}
