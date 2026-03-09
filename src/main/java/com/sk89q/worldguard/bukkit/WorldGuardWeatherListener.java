package com.sk89q.worldguard.bukkit;

import java.util.logging.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.event.weather.*;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.*;

public class WorldGuardWeatherListener extends WeatherListener
{
    private static final Logger logger;
    private WorldGuardPlugin plugin;
    
    public WorldGuardWeatherListener(final WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerEvents() {
        final PluginManager pm = this.plugin.getServer().getPluginManager();
        this.registerEvent("LIGHTNING_STRIKE", Event.Priority.High);
        this.registerEvent("THUNDER_CHANGE", Event.Priority.High);
        this.registerEvent("WEATHER_CHANGE", Event.Priority.High);
    }
    
    private void registerEvent(final String typeName, final Event.Priority priority) {
        try {
            final Event.Type type = Event.Type.valueOf(typeName);
            final PluginManager pm = this.plugin.getServer().getPluginManager();
            pm.registerEvent(type, (Listener)this, priority, (Plugin)this.plugin);
        }
        catch (IllegalArgumentException e) {
            WorldGuardWeatherListener.logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }
    
    public void onWeatherChange(final WeatherChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getWorld());
        if (event.toWeatherState()) {
            if (wcfg.disableWeather) {
                event.setCancelled(true);
            }
        }
        else if (!wcfg.disableWeather && wcfg.alwaysRaining) {
            event.setCancelled(true);
        }
    }
    
    public void onThunderChange(final ThunderChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getWorld());
        if (event.toThunderState()) {
            if (wcfg.disableThunder) {
                event.setCancelled(true);
            }
        }
        else if (!wcfg.disableWeather && wcfg.alwaysThundering) {
            event.setCancelled(true);
        }
    }
    
    public void onLightningStrike(final LightningStrikeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getWorld());
        if (wcfg.disallowedLightningBlocks.size() > 0) {
            final int targetId = event.getLightning().getLocation().getBlock().getTypeId();
            if (wcfg.disallowedLightningBlocks.contains(targetId)) {
                event.setCancelled(true);
            }
        }
        final Location loc = event.getLightning().getLocation();
        if (wcfg.useRegions) {
            final Vector pt = BukkitUtil.toVector(loc);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(loc.getWorld());
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            if (!set.allows(DefaultFlag.LIGHTNING)) {
                event.setCancelled(true);
            }
        }
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
