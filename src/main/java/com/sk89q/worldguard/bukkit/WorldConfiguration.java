package com.sk89q.worldguard.bukkit;

import org.bukkit.util.config.*;
import com.sk89q.worldguard.chest.*;
import java.util.logging.*;
import com.sk89q.worldguard.blacklist.loggers.*;
import java.io.*;
import java.util.*;
import com.sk89q.worldguard.blacklist.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;

public class WorldConfiguration
{
    public static final String CONFIG_HEADER = "#\r\n# WorldGuard's world configuration file\r\n#\r\n# This is a world configuration file. Anything placed into here will only\r\n# affect this world. If you don't put anything in this file, then the\r\n# settings will be inherited from the main configuration file.\r\n#\r\n# If you see {} below, that means that there are NO entries in this file.\r\n# Remove the {} and add your own entries.\r\n#\r\n";
    private static final Logger logger;
    private WorldGuardPlugin plugin;
    private String worldName;
    private Configuration parentConfig;
    private Configuration config;
    private File configFile;
    private File blacklistFile;
    private Blacklist blacklist;
    private ChestProtection chestProtection;
    public boolean opPermissions;
    public boolean fireSpreadDisableToggle;
    public boolean enforceOneSession;
    public boolean itemDurability;
    public boolean classicWater;
    public boolean simulateSponge;
    public int spongeRadius;
    public boolean pumpkinScuba;
    public boolean redstoneSponges;
    public boolean noPhysicsGravel;
    public boolean noPhysicsSand;
    public boolean allowPortalAnywhere;
    public Set<Integer> preventWaterDamage;
    public boolean blockTNT;
    public boolean blockLighter;
    public boolean disableFireSpread;
    public Set<Integer> disableFireSpreadBlocks;
    public boolean preventLavaFire;
    public Set<Integer> allowedLavaSpreadOver;
    public boolean blockCreeperExplosions;
    public boolean blockCreeperBlockDamage;
    public int loginProtection;
    public int spawnProtection;
    public boolean kickOnDeath;
    public boolean exactRespawn;
    public boolean teleportToHome;
    public boolean disableContactDamage;
    public boolean disableFallDamage;
    public boolean disableLavaDamage;
    public boolean disableFireDamage;
    public boolean disableLightningDamage;
    public boolean disableDrowningDamage;
    public boolean disableSuffocationDamage;
    public boolean teleportOnSuffocation;
    public boolean disableVoidDamage;
    public boolean teleportOnVoid;
    public boolean disableExplosionDamage;
    public boolean disableMobDamage;
    public boolean useRegions;
    public boolean highFreqFlags;
    public int regionWand;
    public Set<CreatureType> blockCreatureSpawn;
    public boolean useiConomy;
    public boolean buyOnClaim;
    public double buyOnClaimPrice;
    public int maxClaimVolume;
    public boolean claimOnlyInsideExistingRegions;
    public int maxRegionCountPerPlayer;
    public boolean antiWolfDumbness;
    public boolean signChestProtection;
    public boolean removeInfiniteStacks;
    public boolean disableCreatureCropTrampling;
    public boolean disablePlayerCropTrampling;
    public boolean preventLightningFire;
    public Set<Integer> disallowedLightningBlocks;
    public boolean disableThunder;
    public boolean disableWeather;
    public boolean alwaysRaining;
    public boolean alwaysThundering;
    public boolean disablePigZap;
    public boolean disableCreeperPower;
    public boolean disableHealthRegain;
    public boolean disableMushroomSpread;
    public boolean disableIceMelting;
    public boolean disableSnowMelting;
    public boolean disableSnowFormation;
    public boolean disableIceFormation;
    public boolean disableLeafDecay;
    
    public WorldConfiguration(final WorldGuardPlugin plugin, final String worldName) {
        this.chestProtection = new SignChestProtection();
        this.regionWand = 287;
        final File baseFolder = new File(plugin.getDataFolder(), "worlds/" + worldName);
        this.configFile = new File(baseFolder, "config.yml");
        this.blacklistFile = new File(baseFolder, "blacklist.txt");
        this.plugin = plugin;
        this.worldName = worldName;
        this.parentConfig = plugin.getConfiguration();
        WorldGuardPlugin.createDefaultConfiguration(this.configFile, "config_world.yml");
        WorldGuardPlugin.createDefaultConfiguration(this.blacklistFile, "blacklist.txt");
        this.config = new Configuration(this.configFile);
        this.loadConfiguration();
        WorldConfiguration.logger.info("WorldGuard: Loaded configuration for world '" + worldName + '\"');
    }
    
    private boolean getBoolean(final String node, final boolean def) {
        if (this.config.getProperty(node) != null) {
            return this.config.getBoolean(node, def);
        }
        return this.parentConfig.getBoolean(node, def);
    }
    
    private String getString(final String node, final String def) {
        if (this.config.getProperty(node) != null) {
            return this.config.getString(node, def);
        }
        return this.parentConfig.getString(node, def);
    }
    
    private int getInt(final String node, final int def) {
        if (this.config.getProperty(node) != null) {
            return this.config.getInt(node, def);
        }
        return this.parentConfig.getInt(node, def);
    }
    
    private double getDouble(final String node, final double def) {
        if (this.config.getProperty(node) != null) {
            return this.config.getDouble(node, def);
        }
        return this.parentConfig.getDouble(node, def);
    }
    
    private List<Integer> getIntList(final String node, final List<Integer> def) {
        List<Integer> res;
        if (this.config.getProperty(node) != null) {
            res = (List<Integer>)this.config.getIntList(node, (List)def);
        }
        else {
            res = (List<Integer>)this.parentConfig.getIntList(node, (List)def);
        }
        if (res == null || res.size() == 0) {
            this.parentConfig.setProperty(node, (Object)new ArrayList());
        }
        return res;
    }
    
    private List<String> getStringList(final String node, final List<String> def) {
        List<String> res;
        if (this.config.getProperty(node) != null) {
            res = (List<String>)this.config.getStringList(node, (List)def);
        }
        else {
            res = (List<String>)this.parentConfig.getStringList(node, (List)def);
        }
        if (res == null || res.size() == 0) {
            this.parentConfig.setProperty(node, (Object)new ArrayList());
        }
        return res;
    }
    
    private void loadConfiguration() {
        this.config.load();
        this.opPermissions = this.getBoolean("op-permissions", true);
        this.enforceOneSession = this.getBoolean("protection.enforce-single-session", true);
        this.itemDurability = this.getBoolean("protection.item-durability", true);
        this.removeInfiniteStacks = this.getBoolean("protection.remove-infinite-stacks", false);
        this.classicWater = this.getBoolean("simulation.classic-water", false);
        this.simulateSponge = this.getBoolean("simulation.sponge.enable", true);
        this.spongeRadius = Math.max(1, this.getInt("simulation.sponge.radius", 3)) - 1;
        this.redstoneSponges = this.getBoolean("simulation.sponge.redstone", false);
        this.pumpkinScuba = this.getBoolean("pumpkin-scuba", false);
        this.disableHealthRegain = this.getBoolean("default.disable-health-regain", false);
        this.noPhysicsGravel = this.getBoolean("physics.no-physics-gravel", false);
        this.noPhysicsSand = this.getBoolean("physics.no-physics-sand", false);
        this.allowPortalAnywhere = this.getBoolean("physics.allow-portal-anywhere", false);
        this.preventWaterDamage = new HashSet<Integer>(this.getIntList("physics.disable-water-damage-blocks", null));
        this.blockTNT = this.getBoolean("ignition.block-tnt", false);
        this.blockLighter = this.getBoolean("ignition.block-lighter", false);
        this.preventLavaFire = this.getBoolean("fire.disable-lava-fire-spread", true);
        this.disableFireSpread = this.getBoolean("fire.disable-all-fire-spread", false);
        this.disableFireSpreadBlocks = new HashSet<Integer>(this.getIntList("fire.disable-fire-spread-blocks", null));
        this.allowedLavaSpreadOver = new HashSet<Integer>(this.getIntList("fire.lava-spread-blocks", null));
        this.blockCreeperExplosions = this.getBoolean("mobs.block-creeper-explosions", false);
        this.blockCreeperBlockDamage = this.getBoolean("mobs.block-creeper-block-damage", false);
        this.antiWolfDumbness = this.getBoolean("mobs.anti-wolf-dumbness", false);
        this.loginProtection = this.getInt("spawn.login-protection", 3);
        this.spawnProtection = this.getInt("spawn.spawn-protection", 0);
        this.kickOnDeath = this.getBoolean("spawn.kick-on-death", false);
        this.exactRespawn = this.getBoolean("spawn.exact-respawn", false);
        this.teleportToHome = this.getBoolean("spawn.teleport-to-home-on-death", false);
        this.disableFallDamage = this.getBoolean("player-damage.disable-fall-damage", false);
        this.disableLavaDamage = this.getBoolean("player-damage.disable-lava-damage", false);
        this.disableFireDamage = this.getBoolean("player-damage.disable-fire-damage", false);
        this.disableLightningDamage = this.getBoolean("player-damage.disable-lightning-damage", false);
        this.disableDrowningDamage = this.getBoolean("player-damage.disable-drowning-damage", false);
        this.disableSuffocationDamage = this.getBoolean("player-damage.disable-suffocation-damage", false);
        this.disableContactDamage = this.getBoolean("player-damage.disable-contact-damage", false);
        this.teleportOnSuffocation = this.getBoolean("player-damage.teleport-on-suffocation", false);
        this.disableVoidDamage = this.getBoolean("player-damage.disable-void-damage", false);
        this.teleportOnVoid = this.getBoolean("player-damage.teleport-on-void-falling", false);
        this.disableExplosionDamage = this.getBoolean("player-damage.disable-explosion-damage", false);
        this.disableMobDamage = this.getBoolean("player-damage.disable-mob-damage", false);
        this.signChestProtection = this.getBoolean("chest-protection.enable", false);
        this.disableCreatureCropTrampling = this.getBoolean("crops.disable-creature-trampling", false);
        this.disablePlayerCropTrampling = this.getBoolean("crops.disable-player-trampling", false);
        this.disallowedLightningBlocks = new HashSet<Integer>(this.getIntList("weather.prevent-lightning-strike-blocks", null));
        this.preventLightningFire = this.getBoolean("weather.disable-lightning-strike-fire", false);
        this.disableThunder = this.getBoolean("weather.disable-thunderstorm", false);
        this.disableWeather = this.getBoolean("weather.disable-weather", false);
        this.disablePigZap = this.getBoolean("weather.disable-pig-zombification", false);
        this.disableCreeperPower = this.getBoolean("weather.disable-powered-creepers", false);
        this.alwaysRaining = this.getBoolean("weather.always-raining", false);
        this.alwaysThundering = this.getBoolean("weather.always-thundering", false);
        this.disableMushroomSpread = this.getBoolean("dynamics.disable-mushroom-spread", false);
        this.disableIceMelting = this.getBoolean("dynamics.disable-ice-melting", false);
        this.disableSnowMelting = this.getBoolean("dynamics.disable-snow-melting", false);
        this.disableSnowFormation = this.getBoolean("dynamics.disable-snow-formation", false);
        this.disableIceFormation = this.getBoolean("dynamics.disable-ice-formation", false);
        this.disableLeafDecay = this.getBoolean("dynamics.disable-leaf-decay", false);
        this.useRegions = this.getBoolean("regions.enable", true);
        this.highFreqFlags = this.getBoolean("regions.high-frequency-flags", false);
        this.regionWand = this.getInt("regions.wand", 287);
        this.maxClaimVolume = this.getInt("regions.max-claim-volume", 30000);
        this.claimOnlyInsideExistingRegions = this.getBoolean("regions.claim-only-inside-existing-regions", false);
        this.maxRegionCountPerPlayer = this.getInt("regions.max-region-count-per-player", 7);
        this.useiConomy = this.getBoolean("iconomy.enable", false);
        this.buyOnClaim = this.getBoolean("iconomy.buy-on-claim", false);
        this.buyOnClaimPrice = this.getDouble("iconomy.buy-on-claim-price", 1.0);
        this.blockCreatureSpawn = new HashSet<CreatureType>();
        for (final String creatureName : this.getStringList("mobs.block-creature-spawn", null)) {
            final CreatureType creature = CreatureType.fromName(creatureName);
            if (creature == null) {
                WorldConfiguration.logger.warning("WorldGuard: Unknown mob type '" + creatureName + "'");
            }
            else {
                this.blockCreatureSpawn.add(creature);
            }
        }
        final boolean useBlacklistAsWhitelist = this.getBoolean("blacklist.use-as-whitelist", false);
        final boolean logConsole = this.getBoolean("blacklist.logging.console.enable", true);
        final boolean logDatabase = this.getBoolean("blacklist.logging.database.enable", false);
        final String dsn = this.getString("blacklist.logging.database.dsn", "jdbc:mysql://localhost:3306/minecraft");
        final String user = this.getString("blacklist.logging.database.user", "root");
        final String pass = this.getString("blacklist.logging.database.pass", "");
        final String table = this.getString("blacklist.logging.database.table", "blacklist_events");
        final boolean logFile = this.getBoolean("blacklist.logging.file.enable", false);
        final String logFilePattern = this.getString("blacklist.logging.file.path", "worldguard/logs/%Y-%m-%d.log");
        final int logFileCacheSize = Math.max(1, this.getInt("blacklist.logging.file.open-files", 10));
        try {
            if (this.blacklist != null) {
                this.blacklist.getLogger().close();
            }
            final Blacklist blist = new BukkitBlacklist(useBlacklistAsWhitelist, this.plugin);
            blist.load(this.blacklistFile);
            if (blist.isEmpty()) {
                this.blacklist = null;
            }
            else {
                this.blacklist = blist;
                WorldConfiguration.logger.log(Level.INFO, "WorldGuard: Blacklist loaded.");
                final BlacklistLogger blacklistLogger = blist.getLogger();
                if (logDatabase) {
                    blacklistLogger.addHandler(new DatabaseLoggerHandler(dsn, user, pass, table, this.worldName));
                }
                if (logConsole) {
                    blacklistLogger.addHandler(new ConsoleLoggerHandler(this.worldName));
                }
                if (logFile) {
                    final FileLoggerHandler handler = new FileLoggerHandler(logFilePattern, logFileCacheSize, this.worldName);
                    blacklistLogger.addHandler(handler);
                }
            }
        }
        catch (FileNotFoundException e2) {
            WorldConfiguration.logger.log(Level.WARNING, "WorldGuard blacklist does not exist.");
        }
        catch (IOException e) {
            WorldConfiguration.logger.log(Level.WARNING, "Could not load WorldGuard blacklist: " + e.getMessage());
        }
        if (this.getBoolean("summary-on-start", true)) {
            WorldConfiguration.logger.log(Level.INFO, this.enforceOneSession ? ("WorldGuard: (" + this.worldName + ") Single session is enforced.") : ("WorldGuard: (" + this.worldName + ") Single session is NOT ENFORCED."));
            WorldConfiguration.logger.log(Level.INFO, this.blockTNT ? ("WorldGuard: (" + this.worldName + ") TNT ignition is blocked.") : ("WorldGuard: (" + this.worldName + ") TNT ignition is PERMITTED."));
            WorldConfiguration.logger.log(Level.INFO, this.blockLighter ? ("WorldGuard: (" + this.worldName + ") Lighters are blocked.") : ("WorldGuard: (" + this.worldName + ") Lighters are PERMITTED."));
            WorldConfiguration.logger.log(Level.INFO, this.preventLavaFire ? ("WorldGuard: (" + this.worldName + ") Lava fire is blocked.") : ("WorldGuard: (" + this.worldName + ") Lava fire is PERMITTED."));
            if (this.disableFireSpread) {
                WorldConfiguration.logger.log(Level.INFO, "WorldGuard: (" + this.worldName + ") All fire spread is disabled.");
            }
            else if (this.disableFireSpreadBlocks.size() > 0) {
                WorldConfiguration.logger.log(Level.INFO, "WorldGuard: (" + this.worldName + ") Fire spread is limited to " + this.disableFireSpreadBlocks.size() + " block types.");
            }
            else {
                WorldConfiguration.logger.log(Level.INFO, "WorldGuard: (" + this.worldName + ") Fire spread is UNRESTRICTED.");
            }
        }
        try {
            this.config.setHeader("#\r\n# WorldGuard's world configuration file\r\n#\r\n# This is a world configuration file. Anything placed into here will only\r\n# affect this world. If you don't put anything in this file, then the\r\n# settings will be inherited from the main configuration file.\r\n#\r\n# If you see {} below, that means that there are NO entries in this file.\r\n# Remove the {} and add your own entries.\r\n#\r\n");
        }
        catch (Throwable t) {}
        this.config.save();
    }
    
    public Blacklist getBlacklist() {
        return this.blacklist;
    }
    
    public String getWorldName() {
        return this.worldName;
    }
    
    public boolean isChestProtected(final Block block, final Player player) {
        return this.signChestProtection && !this.plugin.hasPermission((CommandSender)player, "worldguard.chest-protection.override") && !this.plugin.hasPermission((CommandSender)player, "worldguard.override.chest-protection") && this.chestProtection.isProtected(block, player);
    }
    
    public boolean isChestProtected(final Block block) {
        return this.signChestProtection && this.chestProtection.isProtected(block, null);
    }
    
    public boolean isChestProtectedPlacement(final Block block, final Player player) {
        return this.signChestProtection && !this.plugin.hasPermission((CommandSender)player, "worldguard.chest-protection.override") && !this.plugin.hasPermission((CommandSender)player, "worldguard.override.chest-protection") && this.chestProtection.isProtectedPlacement(block, player);
    }
    
    public boolean isAdjacentChestProtected(final Block block, final Player player) {
        return this.signChestProtection && !this.plugin.hasPermission((CommandSender)player, "worldguard.chest-protection.override") && !this.plugin.hasPermission((CommandSender)player, "worldguard.override.chest-protection") && this.chestProtection.isAdjacentChestProtected(block, player);
    }
    
    public ChestProtection getChestProtection() {
        return this.chestProtection;
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
