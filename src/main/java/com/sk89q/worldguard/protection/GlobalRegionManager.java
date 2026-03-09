package com.sk89q.worldguard.protection;

import java.util.logging.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.databases.*;
import java.io.*;
import java.util.*;
import com.sk89q.worldguard.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.block.*;
import org.bukkit.*;
import com.sk89q.worldguard.bukkit.*;
import com.sk89q.worldguard.protection.flags.*;

public class GlobalRegionManager
{
    private static final Logger logger;
    private WorldGuardPlugin plugin;
    private ConfigurationManager config;
    private HashMap<String, RegionManager> managers;
    private HashMap<String, Long> lastModified;
    
    public GlobalRegionManager(final WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getGlobalStateManager();
        this.managers = new HashMap<String, RegionManager>();
        this.lastModified = new HashMap<String, Long>();
    }
    
    public void unload() {
        this.managers.clear();
        this.lastModified.clear();
    }
    
    protected File getPath(final String name) {
        return new File(this.plugin.getDataFolder(), "worlds" + File.separator + name + File.separator + "regions.yml");
    }
    
    public void unload(final String name) {
        final RegionManager manager = this.managers.get(name);
        if (manager != null) {
            this.managers.remove(name);
            this.lastModified.remove(name);
        }
    }
    
    public void unloadAll() {
        this.managers.clear();
        this.lastModified.clear();
    }
    
    public RegionManager load(final World world) {
        final String name = world.getName();
        final File file = this.getPath(name);
        try {
            final RegionManager manager = new FlatRegionManager(new YAMLDatabase(file));
            this.managers.put(name, manager);
            manager.load();
            GlobalRegionManager.logger.info("WorldGuard: " + manager.getRegions().size() + " regions loaded for '" + name + "'");
            this.lastModified.put(name, file.lastModified());
            return manager;
        }
        catch (FileNotFoundException e2) {}
        catch (IOException e) {
            GlobalRegionManager.logger.warning("WorldGuard: Failed to load regions from file " + file.getAbsolutePath() + " : " + e.getMessage());
        }
        return null;
    }
    
    public void preload() {
        for (final World world : this.plugin.getServer().getWorlds()) {
            this.load(world);
        }
    }
    
    public void reloadChanged() {
        for (final String name : this.managers.keySet()) {
            final File file = this.getPath(name);
            Long oldDate = this.lastModified.get(name);
            if (oldDate == null) {
                oldDate = 0L;
            }
            try {
                if (file.lastModified() <= oldDate) {
                    continue;
                }
                final World world = this.plugin.getServer().getWorld(name);
                if (world == null) {
                    continue;
                }
                this.load(world);
            }
            catch (Exception ex) {}
        }
    }
    
    public RegionManager get(final World world) {
        RegionManager manager = this.managers.get(world.getName());
        if (manager == null) {
            manager = this.load(world);
        }
        return manager;
    }
    
    public boolean hasBypass(final LocalPlayer player, final World world) {
        return player.hasPermission("worldguard.region.bypass." + world.getName());
    }
    
    public boolean hasBypass(final Player player, final World world) {
        return this.plugin.hasPermission((CommandSender)player, "worldguard.region.bypass." + world.getName());
    }
    
    public boolean canBuild(final Player player, final Block block) {
        return this.canBuild(player, block.getLocation());
    }
    
    public boolean canBuild(final Player player, final Location loc) {
        final World world = loc.getWorld();
        final WorldConfiguration worldConfig = this.config.get(world);
        if (!worldConfig.useRegions) {
            return true;
        }
        final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
        if (!this.hasBypass(player, world)) {
            final RegionManager mgr = this.get(world);
            if (!mgr.getApplicableRegions(BukkitUtil.toVector(loc)).canBuild(localPlayer)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean allows(final StateFlag flag, final Location loc) {
        final World world = loc.getWorld();
        final WorldConfiguration worldConfig = this.config.get(world);
        if (!worldConfig.useRegions) {
            return true;
        }
        final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
        return mgr.getApplicableRegions(BukkitUtil.toVector(loc)).allows(flag);
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
