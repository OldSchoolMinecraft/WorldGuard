package com.sk89q.worldguard.bukkit;

import java.io.*;
import org.bukkit.*;
import org.bukkit.util.config.*;
import java.util.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.blacklist.*;
import org.bukkit.entity.*;

public class ConfigurationManager
{
    private static final String CONFIG_HEADER = "#\r\n# WorldGuard's main configuration file\r\n#\r\n# This is the global configuration file. Anything placed into here will\r\n# be applied to all worlds. However, each world has its own configuration\r\n# file to allow you to replace any setting in here for that world only.\r\n#\r\n# About editing this file:\r\n# - DO NOT USE TABS. You MUST use spaces or Bukkit will complain. If\r\n#   you use an editor like Notepad++ (recommended for Windows users), you\r\n#   must configure it to \"replace tabs with spaces.\" In Notepad++, this can\r\n#   be changed in Settings > Preferences > Language Menu.\r\n# - Don't get rid of the indents. They are indented so some entries are\r\n#   in categories (like \"enforce-single-session\" is in the \"protection\"\r\n#   category.\r\n# - If you want to check the format of this file before putting it\r\n#   into WorldGuard, paste it into http://yaml-online-parser.appspot.com/\r\n#   and see if it gives \"ERROR:\".\r\n# - Lines starting with # are comments and so they are ignored.\r\n#\r\n";
    private WorldGuardPlugin plugin;
    private Map<String, WorldConfiguration> worlds;
    private Set<String> hasGodMode;
    private Set<String> hasAmphibious;
    public boolean suppressTickSyncWarnings;
    public boolean useRegionsScheduler;
    public boolean activityHaltToggle;
    public boolean autoGodMode;
    
    public ConfigurationManager(final WorldGuardPlugin plugin) {
        this.hasGodMode = new HashSet<String>();
        this.hasAmphibious = new HashSet<String>();
        this.activityHaltToggle = false;
        this.plugin = plugin;
        this.worlds = new HashMap<String, WorldConfiguration>();
    }
    
    public void load() {
        WorldGuardPlugin.createDefaultConfiguration(new File(this.plugin.getDataFolder(), "config.yml"), "config.yml");
        final Configuration config = this.plugin.getConfiguration();
        config.load();
        this.suppressTickSyncWarnings = config.getBoolean("suppress-tick-sync-warnings", false);
        this.useRegionsScheduler = config.getBoolean("regions.use-scheduler", true);
        this.autoGodMode = config.getBoolean("auto-invincible-permission", false);
        for (final World world : this.plugin.getServer().getWorlds()) {
            this.get(world);
        }
        try {
            config.setHeader("#\r\n# WorldGuard's main configuration file\r\n#\r\n# This is the global configuration file. Anything placed into here will\r\n# be applied to all worlds. However, each world has its own configuration\r\n# file to allow you to replace any setting in here for that world only.\r\n#\r\n# About editing this file:\r\n# - DO NOT USE TABS. You MUST use spaces or Bukkit will complain. If\r\n#   you use an editor like Notepad++ (recommended for Windows users), you\r\n#   must configure it to \"replace tabs with spaces.\" In Notepad++, this can\r\n#   be changed in Settings > Preferences > Language Menu.\r\n# - Don't get rid of the indents. They are indented so some entries are\r\n#   in categories (like \"enforce-single-session\" is in the \"protection\"\r\n#   category.\r\n# - If you want to check the format of this file before putting it\r\n#   into WorldGuard, paste it into http://yaml-online-parser.appspot.com/\r\n#   and see if it gives \"ERROR:\".\r\n# - Lines starting with # are comments and so they are ignored.\r\n#\r\n");
        }
        catch (Throwable t) {}
        config.save();
    }
    
    public void unload() {
        this.worlds.clear();
    }
    
    public WorldConfiguration get(final World world) {
        final String worldName = world.getName();
        WorldConfiguration config = this.worlds.get(worldName);
        if (config == null) {
            config = new WorldConfiguration(this.plugin, worldName);
            this.worlds.put(worldName, config);
        }
        return config;
    }
    
    public void forgetPlayer(final LocalPlayer player) {
        for (final Map.Entry<String, WorldConfiguration> entry : this.worlds.entrySet()) {
            final Blacklist bl = entry.getValue().getBlacklist();
            if (bl != null) {
                bl.forgetPlayer(player);
            }
        }
        this.hasGodMode.remove(player.getName());
        this.hasAmphibious.remove(player.getName());
    }
    
    public void enableGodMode(final Player player) {
        this.hasGodMode.add(player.getName());
    }
    
    public void disableGodMode(final Player player) {
        this.hasGodMode.remove(player.getName());
    }
    
    public boolean hasGodMode(final Player player) {
        return this.hasGodMode.contains(player.getName());
    }
    
    public void enableAmphibiousMode(final Player player) {
        this.hasAmphibious.add(player.getName());
    }
    
    public void disableAmphibiousMode(final Player player) {
        this.hasAmphibious.remove(player.getName());
    }
    
    public boolean hasAmphibiousMode(final Player player) {
        return this.hasAmphibious.contains(player.getName());
    }
}
