package com.sk89q.worldguard.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.plugin.java.*;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.bukkit.commands.*;
import com.sk89q.bukkit.migration.*;
import org.bukkit.plugin.*;
import java.util.logging.*;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import java.util.*;
import org.bukkit.command.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldguard.*;
import java.io.*;
import org.bukkit.*;
import org.bukkit.block.*;
import com.sk89q.worldguard.protection.managers.*;
import org.bukkit.util.Vector;

public class WorldGuardPlugin extends JavaPlugin
{
    private static final Logger logger;
    private final CommandsManager<CommandSender> commands;
    private final GlobalRegionManager globalRegionManager;
    private final ConfigurationManager configuration;
    private PermissionsResolverManager perms;
    private FlagStateManager flagStateManager;
    
    public WorldGuardPlugin() {
        this.configuration = new ConfigurationManager(this);
        this.globalRegionManager = new GlobalRegionManager(this);
        final WorldGuardPlugin plugin = this;
        (this.commands = new CommandsManager<CommandSender>() {
            public boolean hasPermission(final CommandSender player, final String perm) {
                return plugin.hasPermission(player, perm);
            }
        }).register((Class)ToggleCommands.class);
        this.commands.register((Class)ProtectionCommands.class);
        this.commands.register((Class)GeneralCommands.class);
    }
    
    public void onEnable() {
        this.getDataFolder().mkdirs();
        (this.perms = new PermissionsResolverManager(this.getConfiguration(), this.getServer(), "WorldGuard", WorldGuardPlugin.logger)).load();
        LegacyWorldGuardMigration.migrateBlacklist(this);
        this.configuration.load();
        this.globalRegionManager.preload();
        LegacyWorldGuardMigration.migrateRegions(this);
        new PermissionsResolverServerListener(this.perms).register((Plugin)this);
        this.flagStateManager = new FlagStateManager(this);
        if (this.configuration.useRegionsScheduler) {
            this.getServer().getScheduler().scheduleAsyncRepeatingTask((Plugin)this, (Runnable)this.flagStateManager, 20L, 20L);
        }
        if (this.configuration.suppressTickSyncWarnings) {
            Logger.getLogger("Minecraft").setFilter(new TickSyncDelayLoggerFilter());
        }
        else {
            final Filter filter = Logger.getLogger("Minecraft").getFilter();
            if (filter != null && filter instanceof TickSyncDelayLoggerFilter) {
                Logger.getLogger("Minecraft").setFilter(null);
            }
        }
        new WorldGuardPlayerListener(this).registerEvents();
        new WorldGuardBlockListener(this).registerEvents();
        new WorldGuardEntityListener(this).registerEvents();
        new WorldGuardWeatherListener(this).registerEvents();
        new WorldGuardWorldListener(this).registerEvents();
        WorldGuardPlugin.logger.info("WorldGuard " + this.getDescription().getVersion() + " enabled.");
    }
    
    public void onDisable() {
        this.globalRegionManager.unload();
        this.configuration.unload();
        WorldGuardPlugin.logger.info("WorldGuard " + this.getDescription().getVersion() + " disabled.");
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, this, sender);
        }
        catch (CommandPermissionsException e5) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        }
        catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        }
        catch (CommandUsageException e2) {
            sender.sendMessage(ChatColor.RED + e2.getMessage());
            sender.sendMessage(ChatColor.RED + e2.getUsage());
        }
        catch (WrappedCommandException e3) {
            if (e3.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            }
            else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e3.printStackTrace();
            }
        }
        catch (CommandException | com.sk89q.minecraft.util.commands.CommandException e4) {
            sender.sendMessage(ChatColor.RED + e4.getMessage());
        }
        return true;
    }
    
    public GlobalRegionManager getGlobalRegionManager() {
        return this.globalRegionManager;
    }
    
    @Deprecated
    public ConfigurationManager getGlobalConfiguration() {
        return this.getGlobalStateManager();
    }
    
    public FlagStateManager getFlagStateManager() {
        return this.flagStateManager;
    }
    
    public ConfigurationManager getGlobalStateManager() {
        return this.configuration;
    }
    
    public boolean inGroup(final Player player, final String group) {
        try {
            return this.perms.inGroup(player.getName(), group);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    public String[] getGroups(final Player player) {
        try {
            return this.perms.getGroups(player.getName());
        }
        catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }
    
    public String toUniqueName(final CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player)sender).getName();
        }
        return "*Console*";
    }
    
    public String toName(final CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player)sender).getName();
        }
        return "*Console*";
    }
    
    public boolean hasPermission(final CommandSender sender, final String perm) {
        if (sender.isOp()) {
            if (!(sender instanceof Player)) {
                return true;
            }
            if (this.getGlobalStateManager().get(((Player)sender).getWorld()).opPermissions) {
                return true;
            }
        }
        return sender instanceof Player && this.perms.hasPermission(((Player)sender).getName(), perm);
    }
    
    public void checkPermission(final CommandSender sender, final String perm) throws CommandPermissionsException {
        if (!this.hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }
    
    public Player checkPlayer(final CommandSender sender) throws CommandException {
        if (sender instanceof Player) {
            return (Player)sender;
        }
        throw new CommandException("A player is expected.");
    }
    
    public List<Player> matchPlayerNames(String filter) {
        final Player[] players = this.getServer().getOnlinePlayers();
        filter = filter.toLowerCase();
        if (filter.charAt(0) == '@' && filter.length() >= 2) {
            filter = filter.substring(1);
            for (final Player player : players) {
                if (player.getName().equalsIgnoreCase(filter)) {
                    final List<Player> list = new ArrayList<Player>();
                    list.add(player);
                    return list;
                }
            }
            return new ArrayList<Player>();
        }
        if (filter.charAt(0) == '*' && filter.length() >= 2) {
            filter = filter.substring(1);
            final List<Player> list2 = new ArrayList<Player>();
            for (final Player player2 : players) {
                if (player2.getName().toLowerCase().contains(filter)) {
                    list2.add(player2);
                }
            }
            return list2;
        }
        final List<Player> list2 = new ArrayList<Player>();
        for (final Player player2 : players) {
            if (player2.getName().toLowerCase().startsWith(filter)) {
                list2.add(player2);
            }
        }
        return list2;
    }
    
    protected Iterable<Player> checkPlayerMatch(final List<Player> players) throws CommandException {
        if (players.size() == 0) {
            throw new CommandException("No players matched query.");
        }
        return players;
    }
    
    public Iterable<Player> matchPlayers(final CommandSender source, final String filter) throws CommandException {
        if (this.getServer().getOnlinePlayers().length == 0) {
            throw new CommandException("No players matched query.");
        }
        if (filter.equals("*")) {
            return this.checkPlayerMatch(Arrays.asList(this.getServer().getOnlinePlayers()));
        }
        if (filter.charAt(0) != '#') {
            final List<Player> players = this.matchPlayerNames(filter);
            return this.checkPlayerMatch(players);
        }
        if (filter.equalsIgnoreCase("#world")) {
            final List<Player> players = new ArrayList<Player>();
            final Player sourcePlayer = this.checkPlayer(source);
            final World sourceWorld = sourcePlayer.getWorld();
            for (final Player player : this.getServer().getOnlinePlayers()) {
                if (player.getWorld().equals(sourceWorld)) {
                    players.add(player);
                }
            }
            return this.checkPlayerMatch(players);
        }
        if (filter.equalsIgnoreCase("#near")) {
            final List<Player> players = new ArrayList<Player>();
            final Player sourcePlayer = this.checkPlayer(source);
            final World sourceWorld = sourcePlayer.getWorld();
            final Vector sourceVector = sourcePlayer.getLocation().toVector();
            for (final Player player2 : this.getServer().getOnlinePlayers()) {
                if (player2.getWorld().equals(sourceWorld) && player2.getLocation().toVector().distanceSquared(sourceVector) < 900.0) {
                    players.add(player2);
                }
            }
            return this.checkPlayerMatch(players);
        }
        throw new CommandException("Invalid group '" + filter + "'.");
    }
    
    public Player matchSinglePlayer(final CommandSender sender, final String filter) throws CommandException {
        final Iterator<Player> players = this.matchPlayers(sender, filter).iterator();
        final Player match = players.next();
        if (players.hasNext()) {
            throw new CommandException("More than one player found! Use @<name> for exact matching.");
        }
        return match;
    }
    
    public CommandSender matchPlayerOrConsole(final CommandSender sender, final String filter) throws CommandException {
        if (filter.equalsIgnoreCase("#console") || filter.equalsIgnoreCase("*console*") || filter.equalsIgnoreCase("!")) {
            return (CommandSender)new ConsoleCommandSender(this.getServer());
        }
        return (CommandSender)this.matchSinglePlayer(sender, filter);
    }
    
    public Iterable<Player> matchPlayers(final Player player) {
        return Arrays.asList(player);
    }
    
    public World matchWorld(final CommandSender sender, final String filter) throws CommandException {
        final List<World> worlds = (List<World>)this.getServer().getWorlds();
        if (filter.charAt(0) != '#') {
            for (final World world : worlds) {
                if (world.getName().equals(filter)) {
                    return world;
                }
            }
            throw new CommandException("No world by that exact name found.");
        }
        if (filter.equalsIgnoreCase("#main")) {
            return worlds.get(0);
        }
        if (filter.equalsIgnoreCase("#normal")) {
            for (final World world : worlds) {
                if (world.getEnvironment() == World.Environment.NORMAL) {
                    return world;
                }
            }
            throw new CommandException("No normal world found.");
        }
        if (filter.equalsIgnoreCase("#nether")) {
            for (final World world : worlds) {
                if (world.getEnvironment() == World.Environment.NETHER) {
                    return world;
                }
            }
            throw new CommandException("No nether world found.");
        }
        if (!filter.matches("^#player$")) {
            throw new CommandException("Invalid identifier '" + filter + "'.");
        }
        final String[] parts = filter.split(":", 2);
        if (parts.length == 1) {
            throw new CommandException("Argument expected for #player.");
        }
        return this.matchPlayers(sender, parts[1]).iterator().next().getWorld();
    }
    
    public WorldEditPlugin getWorldEdit() throws CommandException {
        final Plugin worldEdit = this.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            throw new CommandException("WorldEdit does not appear to be installed.");
        }
        if (worldEdit instanceof WorldEditPlugin) {
            return (WorldEditPlugin)worldEdit;
        }
        throw new CommandException("WorldEdit detection failed (report error).");
    }
    
    public LocalPlayer wrapPlayer(final Player player) {
        return new BukkitPlayer(this, player);
    }
    
    public static void createDefaultConfiguration(final File actual, final String defaultName) {
        final File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (actual.exists()) {
            return;
        }
        final InputStream input = WorldGuardPlugin.class.getResourceAsStream("/defaults/" + defaultName);
        if (input != null) {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(actual);
                final byte[] buf = new byte[8192];
                int length = 0;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }
                WorldGuardPlugin.logger.info("WorldGuard: Default configuration file written: " + actual.getAbsolutePath());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                }
                catch (IOException ex) {}
                try {
                    if (output != null) {
                        output.close();
                    }
                }
                catch (IOException ex2) {}
            }
        }
    }
    
    public void broadcastNotification(final String msg) {
        for (final Player player : this.getServer().getOnlinePlayers()) {
            if (this.hasPermission((CommandSender)player, "worldguard.notify")) {
                player.sendMessage(msg);
            }
        }
    }
    
    public void forgetPlayer(final Player player) {
        this.flagStateManager.forget(player);
    }
    
    public boolean canBuild(final Player player, final Location loc) {
        return this.getGlobalRegionManager().canBuild(player, loc);
    }
    
    public boolean canBuild(final Player player, final Block block) {
        return this.getGlobalRegionManager().canBuild(player, block);
    }
    
    public RegionManager getRegionManager(final World world) {
        if (!this.getGlobalStateManager().get(world).useRegions) {
            return null;
        }
        return this.getGlobalRegionManager().get(world);
    }
    
    public String replaceMacros(final CommandSender sender, String message) {
        final Player[] online = this.getServer().getOnlinePlayers();
        message = message.replace("%name%", this.toName(sender));
        message = message.replace("%id%", this.toUniqueName(sender));
        message = message.replace("%online%", String.valueOf(online.length));
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            final World world = player.getWorld();
            message = message.replace("%world%", world.getName());
            message = message.replace("%health%", String.valueOf(player.getHealth()));
        }
        return message;
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
