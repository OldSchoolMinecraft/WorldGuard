package com.sk89q.worldguard.bukkit;

import com.sk89q.worldguard.blacklist.*;

public class BukkitBlacklist extends Blacklist
{
    private WorldGuardPlugin plugin;
    
    public BukkitBlacklist(final Boolean useAsWhitelist, final WorldGuardPlugin plugin) {
        super(useAsWhitelist);
        this.plugin = plugin;
    }
    
    @Override
    public void broadcastNotification(final String msg) {
        this.plugin.broadcastNotification(msg);
    }
}
