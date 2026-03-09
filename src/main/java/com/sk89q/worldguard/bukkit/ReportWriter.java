package com.sk89q.worldguard.bukkit;

import java.text.*;
import com.sk89q.worldguard.util.*;
import java.lang.reflect.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import java.util.*;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.protection.flags.*;
import java.io.*;

public class ReportWriter
{
    private static final SimpleDateFormat dateFmt;
    private Date date;
    private StringBuilder output;
    
    public ReportWriter(final WorldGuardPlugin plugin) {
        this.date = new Date();
        this.output = new StringBuilder();
        this.appendReportHeader(plugin);
        this.appendServerInformation(plugin.getServer());
        this.appendPluginInformation(plugin.getServer().getPluginManager().getPlugins());
        this.appendWorldInformation(plugin.getServer().getWorlds());
        this.appendGlobalConfiguration(plugin.getGlobalStateManager());
        this.appendWorldConfigurations(plugin, plugin.getServer().getWorlds(), plugin.getGlobalRegionManager(), plugin.getGlobalStateManager());
        this.appendln("-------------");
        this.appendln("END OF REPORT");
        this.appendln();
    }
    
    protected static String repeat(final String str, final int n) {
        if (str == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    protected void appendln(final String text) {
        this.output.append(text);
        this.output.append("\r\n");
    }
    
    protected void appendln(final String text, final Object... args) {
        this.output.append(String.format(text, args));
        this.output.append("\r\n");
    }
    
    protected void append(final LogListBlock log) {
        this.output.append(log.toString());
    }
    
    protected void appendln() {
        this.output.append("\r\n");
    }
    
    protected void appendHeader(final String text) {
        final String rule = repeat("-", text.length());
        this.output.append(rule);
        this.output.append("\r\n");
        this.appendln(text);
        this.output.append(rule);
        this.output.append("\r\n");
        this.appendln();
    }
    
    private void appendReportHeader(final WorldGuardPlugin plugin) {
        this.appendln("WorldGuard Configuration Report");
        this.appendln("Generated " + ReportWriter.dateFmt.format(this.date));
        this.appendln();
        this.appendln("Version: " + plugin.getDescription().getVersion());
        this.appendln();
    }
    
    private void appendGlobalConfiguration(final ConfigurationManager config) {
        this.appendHeader("Global Configuration");
        final LogListBlock log = new LogListBlock();
        final LogListBlock configLog = log.putChild("Configuration");
        final Class<? extends ConfigurationManager> cls = config.getClass();
        for (final Field field : cls.getFields()) {
            try {
                final Object val = field.get(config);
                configLog.put(field.getName(), val);
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException ex) {}
        }
        this.append(log);
        this.appendln();
    }
    
    private void appendServerInformation(final Server server) {
        this.appendHeader("Server Information");
        final LogListBlock log = new LogListBlock();
        final Runtime runtime = Runtime.getRuntime();
        log.put("Java", "%s %s (%s)", System.getProperty("java.vendor"), System.getProperty("java.version"), System.getProperty("java.vendor.url"));
        log.put("Operating system", "%s %s (%s)", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        log.put("Available processors", runtime.availableProcessors());
        log.put("Free memory", runtime.freeMemory() / 1024L / 1024L + " MB");
        log.put("Max memory", runtime.maxMemory() / 1024L / 1024L + " MB");
        log.put("Total memory", runtime.totalMemory() / 1024L / 1024L + " MB");
        log.put("Server ID", server.getServerId());
        log.put("Server name", server.getServerName());
        log.put("Implementation", server.getVersion());
        log.put("Player count", "%d/%d", server.getOnlinePlayers().length, server.getMaxPlayers());
        this.append(log);
        this.appendln();
    }
    
    private void appendPluginInformation(final Plugin[] plugins) {
        this.appendHeader("Plugins");
        final LogListBlock log = new LogListBlock();
        for (final Plugin plugin : plugins) {
            log.put(plugin.getDescription().getName(), plugin.getDescription().getVersion());
        }
        this.append(log);
        this.appendln();
    }
    
    private void appendWorldInformation(final List<World> worlds) {
        this.appendHeader("Worlds");
        final LogListBlock log = new LogListBlock();
        int i = 0;
        for (final World world : worlds) {
            final int loadedChunkCount = world.getLoadedChunks().length;
            final LogListBlock worldLog = log.putChild(world.getName() + " (" + i + ")");
            final LogListBlock infoLog = worldLog.putChild("Information");
            final LogListBlock entitiesLog = worldLog.putChild("Entities");
            infoLog.put("ID", world.getId());
            infoLog.put("Environment", world.getEnvironment().toString());
            infoLog.put("Player count", world.getPlayers().size());
            infoLog.put("Entity count", world.getEntities().size());
            infoLog.put("Loaded chunk count", loadedChunkCount);
            infoLog.put("Spawn location", world.getSpawnLocation());
            infoLog.put("Raw time", world.getFullTime());
            final Map<Class<? extends Entity>, Integer> entityCounts = new HashMap<Class<? extends Entity>, Integer>();
            for (final Entity entity : world.getEntities()) {
                final Class<? extends Entity> cls = entity.getClass();
                if (entityCounts.containsKey(cls)) {
                    entityCounts.put(cls, entityCounts.get(cls) + 1);
                }
                else {
                    entityCounts.put(cls, 1);
                }
            }
            for (final Map.Entry<Class<? extends Entity>, Integer> entry : entityCounts.entrySet()) {
                entitiesLog.put(entry.getKey().getSimpleName(), "%d [%f]", entry.getValue(), (float)(entry.getValue() / (double)loadedChunkCount));
            }
            ++i;
        }
        this.append(log);
        this.appendln();
    }
    
    private void appendWorldConfigurations(final WorldGuardPlugin plugin, final List<World> worlds, final GlobalRegionManager regionMgr, final ConfigurationManager mgr) {
        this.appendHeader("World Configurations");
        final LogListBlock log = new LogListBlock();
        final int i = 0;
        for (final World world : worlds) {
            final LogListBlock worldLog = log.putChild(world.getName() + " (" + i + ")");
            final LogListBlock infoLog = worldLog.putChild("Information");
            final LogListBlock configLog = worldLog.putChild("Configuration");
            final LogListBlock blacklistLog = worldLog.putChild("Blacklist");
            final LogListBlock regionsLog = worldLog.putChild("Region manager");
            infoLog.put("Configuration file", new File(plugin.getDataFolder(), "worlds/" + world.getName() + "/config.yml").getAbsoluteFile());
            infoLog.put("Blacklist file", new File(plugin.getDataFolder(), "worlds/" + world.getName() + "/blacklist.txt").getAbsoluteFile());
            infoLog.put("Regions file", new File(plugin.getDataFolder(), "worlds/" + world.getName() + "/regions.yml").getAbsoluteFile());
            final WorldConfiguration config = mgr.get(world);
            final Class<? extends WorldConfiguration> cls = config.getClass();
            for (final Field field : cls.getFields()) {
                try {
                    final Object val = field.get(config);
                    configLog.put(field.getName(), String.valueOf(val));
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException ex) {}
            }
            if (config.getBlacklist() == null) {
                blacklistLog.put("State", "DISABLED");
            }
            else {
                blacklistLog.put("State", "Enabled");
                blacklistLog.put("Number of items", config.getBlacklist().getItemCount());
                blacklistLog.put("Is whitelist", config.getBlacklist().isWhitelist());
            }
            final RegionManager worldRegions = regionMgr.get(world);
            regionsLog.put("Type", worldRegions.getClass().getCanonicalName());
            regionsLog.put("Number of regions", worldRegions.getRegions().size());
            final LogListBlock globalRegionLog = regionsLog.putChild("Global region");
            final ProtectedRegion globalRegion = worldRegions.getRegion("__global__");
            if (globalRegion == null) {
                globalRegionLog.put("Status", "UNDEFINED");
            }
            else {
                for (final Flag<?> flag : DefaultFlag.getFlags()) {
                    if (flag instanceof StateFlag) {
                        globalRegionLog.put(flag.getName(), globalRegion.getFlag(flag));
                    }
                }
            }
        }
        this.append(log);
        this.appendln();
    }
    
    public void write(final File file) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            final BufferedWriter out = new BufferedWriter(writer);
            out.write(this.output.toString());
            out.close();
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException ex) {}
            }
        }
    }
    
    @Override
    public String toString() {
        return this.output.toString();
    }
    
    static {
        dateFmt = new SimpleDateFormat("yyyy-MM-dd kk:mm Z");
    }
}
