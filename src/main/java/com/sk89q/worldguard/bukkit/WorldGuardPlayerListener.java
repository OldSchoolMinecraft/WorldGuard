package com.sk89q.worldguard.bukkit;

import java.util.logging.*;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.*;
import com.sk89q.worldguard.protection.regions.*;
import org.bukkit.*;
import org.bukkit.block.*;
import com.sk89q.worldguard.blacklist.events.*;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.event.player.*;
import java.util.*;

public class WorldGuardPlayerListener extends PlayerListener
{
    private static final Logger logger;
    private WorldGuardPlugin plugin;
    
    public WorldGuardPlayerListener(final WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerEvents() {
        final PluginManager pm = this.plugin.getServer().getPluginManager();
        this.registerEvent("PLAYER_INTERACT", Event.Priority.High);
        this.registerEvent("PLAYER_DROP_ITEM", Event.Priority.High);
        this.registerEvent("PLAYER_PICKUP_ITEM", Event.Priority.High);
        this.registerEvent("PLAYER_JOIN", Event.Priority.Normal);
        this.registerEvent("PLAYER_LOGIN", Event.Priority.Normal);
        this.registerEvent("PLAYER_QUIT", Event.Priority.Normal);
        this.registerEvent("PLAYER_BUCKET_FILL", Event.Priority.High);
        this.registerEvent("PLAYER_BUCKET_EMPTY", Event.Priority.High);
        this.registerEvent("PLAYER_RESPAWN", Event.Priority.High);
        this.registerEvent("PLAYER_ITEM_HELD", Event.Priority.High);
        this.registerEvent("PLAYER_BED_ENTER", Event.Priority.High);
        this.registerEvent("PLAYER_MOVE", Event.Priority.High);
        this.registerEvent("PLAYER_COMMAND_PREPROCESS", Event.Priority.High);
    }
    
    private void registerEvent(final String typeName, final Event.Priority priority) {
        try {
            final Event.Type type = Event.Type.valueOf(typeName);
            final PluginManager pm = this.plugin.getServer().getPluginManager();
            pm.registerEvent(type, (Listener)this, priority, (Plugin)this.plugin);
        }
        catch (IllegalArgumentException e) {
            WorldGuardPlayerListener.logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }
    
    public void onPlayerLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(player.getWorld());
        if (wcfg.enforceOneSession) {
            final String name = player.getName();
            for (final Player pl : this.plugin.getServer().getOnlinePlayers()) {
                if (pl.getName().equalsIgnoreCase(name)) {
                    pl.kickPlayer("Logged in from another location.");
                }
            }
        }
    }
    
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(player.getWorld());
        if (cfg.activityHaltToggle) {
            player.sendMessage(ChatColor.YELLOW + "Intensive server activity has been HALTED.");
            int removed = 0;
            for (final Entity entity : player.getWorld().getEntities()) {
                if (entity instanceof Item || (entity instanceof LivingEntity && !(entity instanceof Tameable) && !(entity instanceof Player))) {
                    entity.remove();
                    ++removed;
                }
            }
            if (removed > 10) {
                WorldGuardPlayerListener.logger.info("WG Halt-Act: " + removed + " entities (>10) auto-removed from " + player.getWorld().toString());
            }
        }
        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW + "Fire spread is currently globally disabled for this world.");
        }
        if (this.plugin.inGroup(player, "wg-invincible") || (cfg.autoGodMode && this.plugin.hasPermission((CommandSender)player, "worldguard.auto-invincible"))) {
            cfg.enableGodMode(player);
        }
        if (this.plugin.inGroup(player, "wg-amphibious")) {
            cfg.enableAmphibiousMode(player);
        }
        if (wcfg.useRegions) {
            final FlagStateManager.PlayerFlagState state = this.plugin.getFlagStateManager().getState(player);
            final Location loc = player.getLocation();
            state.lastWorld = loc.getWorld();
            state.lastBlockX = loc.getBlockX();
            state.lastBlockY = loc.getBlockY();
            state.lastBlockZ = loc.getBlockZ();
        }
    }
    
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.useRegions) {
            final boolean hasBypass = this.plugin.getGlobalRegionManager().hasBypass(player, world);
            final FlagStateManager.PlayerFlagState state = this.plugin.getFlagStateManager().getState(player);
            if (state.lastWorld != null && !hasBypass) {
                final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
                final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
                final Location loc = player.getLocation();
                final Vector pt = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                if (state.lastExitAllowed == null) {
                    state.lastExitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
                }
                if ((!state.lastExitAllowed || !set.allows(DefaultFlag.ENTRY, localPlayer)) && state.lastWorld.equals(world)) {
                    final Location newLoc = new Location(world, state.lastBlockX + 0.5, (double)state.lastBlockY, state.lastBlockZ + 0.5);
                    player.teleport(newLoc);
                }
            }
        }
        cfg.forgetPlayer(this.plugin.wrapPlayer(player));
        this.plugin.forgetPlayer(player);
    }
    
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            this.handleBlockRightClick(event);
        }
        else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            this.handleAirRightClick(event);
        }
        else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.handleBlockLeftClick(event);
        }
        else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            this.handleAirLeftClick(event);
        }
        else if (event.getAction() == Action.PHYSICAL) {
            this.handlePhysicalInteract(event);
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.removeInfiniteStacks && !this.plugin.hasPermission((CommandSender)player, "worldguard.override.infinite-stack")) {
            final int slot = player.getInventory().getHeldItemSlot();
            final ItemStack heldItem = player.getInventory().getItem(slot);
            if (heldItem.getAmount() < 0) {
                player.getInventory().setItem(slot, (ItemStack)null);
                player.sendMessage(ChatColor.RED + "Infinite stack removed.");
            }
        }
    }
    
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.useRegions && (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
            final FlagStateManager.PlayerFlagState state = this.plugin.getFlagStateManager().getState(player);
            final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
            final boolean hasBypass = this.plugin.getGlobalRegionManager().hasBypass(player, world);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
            final Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            final boolean entryAllowed = set.allows(DefaultFlag.ENTRY, localPlayer);
            if (!hasBypass && !entryAllowed) {
                player.sendMessage(ChatColor.DARK_RED + "You are not permitted to enter this area.");
                final Location newLoc = event.getFrom();
                newLoc.setX(newLoc.getBlockX() + 0.5);
                newLoc.setY((double)newLoc.getBlockY());
                newLoc.setZ(newLoc.getBlockZ() + 0.5);
                event.setTo(newLoc);
                return;
            }
            if (state.lastExitAllowed == null) {
                state.lastExitAllowed = mgr.getApplicableRegions(BukkitUtil.toVector(event.getFrom())).allows(DefaultFlag.EXIT, localPlayer);
            }
            final boolean exitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
            if (!hasBypass && exitAllowed && !state.lastExitAllowed) {
                player.sendMessage(ChatColor.DARK_RED + "You are not permitted to leave this area.");
                final Location newLoc2 = event.getFrom();
                newLoc2.setX(newLoc2.getBlockX() + 0.5);
                newLoc2.setY((double)newLoc2.getBlockY());
                newLoc2.setZ(newLoc2.getBlockZ() + 0.5);
                event.setTo(newLoc2);
                return;
            }
            final String greeting = set.getFlag(DefaultFlag.GREET_MESSAGE);
            final String farewell = set.getFlag(DefaultFlag.FAREWELL_MESSAGE);
            final Boolean notifyEnter = set.getFlag(DefaultFlag.NOTIFY_ENTER);
            final Boolean notifyLeave = set.getFlag(DefaultFlag.NOTIFY_LEAVE);
            if (state.lastFarewell != null && (farewell == null || !state.lastFarewell.equals(farewell))) {
                final String replacedFarewell = this.plugin.replaceMacros((CommandSender)player, BukkitUtil.replaceColorMacros(state.lastFarewell));
                player.sendMessage(ChatColor.AQUA + " ** " + replacedFarewell);
            }
            if (greeting != null && (state.lastGreeting == null || !state.lastGreeting.equals(greeting))) {
                final String replacedGreeting = this.plugin.replaceMacros((CommandSender)player, BukkitUtil.replaceColorMacros(greeting));
                player.sendMessage(ChatColor.AQUA + " ** " + replacedGreeting);
            }
            if ((notifyLeave == null || !notifyLeave) && state.notifiedForLeave != null && state.notifiedForLeave) {
                this.plugin.broadcastNotification(ChatColor.GRAY + "WG: " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GOLD + " left NOTIFY region");
            }
            if (notifyEnter != null && notifyEnter && (state.notifiedForEnter == null || !state.notifiedForEnter)) {
                final StringBuilder regionList = new StringBuilder();
                for (final ProtectedRegion region : set) {
                    if (regionList.length() != 0) {
                        regionList.append(", ");
                    }
                    regionList.append(region.getId());
                }
                this.plugin.broadcastNotification(ChatColor.GRAY + "WG: " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GOLD + " entered NOTIFY region: " + ChatColor.WHITE + (Object)regionList);
            }
            state.lastGreeting = greeting;
            state.lastFarewell = farewell;
            state.notifiedForEnter = notifyEnter;
            state.notifiedForLeave = notifyLeave;
            state.lastExitAllowed = exitAllowed;
            state.lastWorld = event.getTo().getWorld();
            state.lastBlockX = event.getTo().getBlockX();
            state.lastBlockY = event.getTo().getBlockY();
            state.lastBlockZ = event.getTo().getBlockZ();
        }
    }
    
    private void handleAirLeftClick(final PlayerInteractEvent event) {
    }
    
    private void handleBlockLeftClick(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Material type = block.getType();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.useRegions) {
            final Vector pt = (Vector)BukkitUtil.toVector(block);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
            if ((type == Material.STONE_BUTTON || type == Material.LEVER || type == Material.WOODEN_DOOR || type == Material.TRAP_DOOR || type == Material.NOTE_BLOCK) && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.allows(DefaultFlag.USE) && !set.canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use that in this area.");
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
            }
        }
    }
    
    private void handleAirRightClick(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final ItemStack item = player.getItemInHand();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new ItemUseBlacklistEvent(this.plugin.wrapPlayer(player), BukkitUtil.toVector(player.getLocation()), item.getTypeId()), false, false)) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }
    
    private void handleBlockRightClick(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Block block = event.getClickedBlock();
        final World world = block.getWorld();
        final Material type = block.getType();
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if ((type == Material.CHEST || type == Material.JUKEBOX || type == Material.DISPENSER || type == Material.FURNACE || type == Material.BURNING_FURNACE) && wcfg.removeInfiniteStacks && !this.plugin.hasPermission((CommandSender)player, "worldguard.override.infinite-stack")) {
            for (int slot = 0; slot < 40; ++slot) {
                final ItemStack heldItem = player.getInventory().getItem(slot);
                if (heldItem != null && heldItem.getAmount() < 0) {
                    player.getInventory().setItem(slot, (ItemStack)null);
                    player.sendMessage(ChatColor.RED + "Infinite stack in slot #" + slot + " removed.");
                }
            }
        }
        if (wcfg.useRegions) {
            final Vector pt = (Vector)BukkitUtil.toVector(block);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
            if (item.getTypeId() == wcfg.regionWand) {
                if (set.size() > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Can you build? " + (set.canBuild(localPlayer) ? "Yes" : "No"));
                    final StringBuilder str = new StringBuilder();
                    final Iterator<ProtectedRegion> it = set.iterator();
                    while (it.hasNext()) {
                        str.append(it.next().getId());
                        if (it.hasNext()) {
                            str.append(", ");
                        }
                    }
                    player.sendMessage(ChatColor.YELLOW + "Applicable regions: " + str.toString());
                }
                else {
                    player.sendMessage(ChatColor.YELLOW + "WorldGuard: No defined regions here!");
                }
                event.setCancelled(true);
                return;
            }
            if (item.getType() == Material.INK_SACK && item.getData() != null && item.getData().getData() == 15 && type == Material.GRASS && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.canBuild(localPlayer)) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
            }
            if ((type == Material.CHEST || type == Material.JUKEBOX || type == Material.DISPENSER || type == Material.FURNACE || type == Material.BURNING_FURNACE) && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.allows(DefaultFlag.CHEST_ACCESS) && !set.canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission to open that in this area.");
                event.setCancelled(true);
                return;
            }
            if ((type == Material.LEVER || type == Material.STONE_BUTTON || type == Material.NOTE_BLOCK || type == Material.DIODE_BLOCK_OFF || type == Material.DIODE_BLOCK_ON || type == Material.WOODEN_DOOR || type == Material.TRAP_DOOR || type == Material.WORKBENCH) && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.allows(DefaultFlag.USE) && !set.canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use that in this area.");
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
                return;
            }
            if (type == Material.CAKE_BLOCK && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You're not invited to this tea party!");
                event.setCancelled(true);
                return;
            }
            if ((type == Material.RAILS || type == Material.POWERED_RAIL || type == Material.DETECTOR_RAIL) && item.getType() == Material.MINECART && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.canBuild(localPlayer) && !set.allows(DefaultFlag.PLACE_VEHICLE)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                event.setCancelled(true);
                return;
            }
            if (item.getType() == Material.BOAT && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.canBuild(localPlayer) && !set.allows(DefaultFlag.PLACE_VEHICLE)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                event.setCancelled(true);
                return;
            }
        }
        if (wcfg.getBlacklist() != null && block.getType() != Material.CHEST && block.getType() != Material.DISPENSER && block.getType() != Material.FURNACE && block.getType() != Material.BURNING_FURNACE) {
            if (!wcfg.getBlacklist().check(new ItemUseBlacklistEvent(this.plugin.wrapPlayer(player), (Vector)BukkitUtil.toVector(block), item.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
            if (!wcfg.getBlacklist().check(new BlockInteractBlacklistEvent(this.plugin.wrapPlayer(player), (Vector)BukkitUtil.toVector(block), block.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
        if ((block.getType() == Material.CHEST || block.getType() == Material.DISPENSER || block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE) && wcfg.isChestProtected(block, player)) {
            player.sendMessage(ChatColor.DARK_RED + "The chest is protected.");
            event.setCancelled(true);
        }
    }
    
    private void handlePhysicalInteract(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Material type = block.getType();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (block.getType() == Material.SOIL && wcfg.disablePlayerCropTrampling) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.useRegions) {
            final Vector pt = (Vector)BukkitUtil.toVector(block);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
            if ((type == Material.STONE_PLATE || type == Material.WOOD_PLATE) && !this.plugin.getGlobalRegionManager().hasBypass(player, world) && !set.allows(DefaultFlag.USE) && !set.canBuild(localPlayer)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
            }
        }
    }
    
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());
        if (wcfg.getBlacklist() != null) {
            final Item ci = event.getItemDrop();
            if (!wcfg.getBlacklist().check(new ItemDropBlacklistEvent(this.plugin.wrapPlayer(event.getPlayer()), BukkitUtil.toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, false)) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());
        if (wcfg.getBlacklist() != null) {
            final Item ci = event.getItem();
            if (!wcfg.getBlacklist().check(new ItemAcquireBlacklistEvent(this.plugin.wrapPlayer(event.getPlayer()), BukkitUtil.toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, true)) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (!this.plugin.getGlobalRegionManager().canBuild(player, event.getBlockClicked())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }
        if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new ItemUseBlacklistEvent(this.plugin.wrapPlayer(player), BukkitUtil.toVector(player.getLocation()), event.getBucket().getId()), false, false)) {
            event.setCancelled(true);
        }
    }
    
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (!this.plugin.getGlobalRegionManager().canBuild(player, event.getBlockClicked())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }
        if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new ItemUseBlacklistEvent(this.plugin.wrapPlayer(player), BukkitUtil.toVector(player.getLocation()), event.getBucket().getId()), false, false)) {
            event.setCancelled(true);
        }
    }
    
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final Location location = player.getLocation();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(player.getWorld());
        if (wcfg.useRegions) {
            final Vector pt = BukkitUtil.toVector(location);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(player.getWorld());
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            final Vector spawn = set.getFlag(DefaultFlag.SPAWN_LOC);
            if (spawn != null) {
                final RegionGroupFlag.RegionGroup group = set.getFlag(DefaultFlag.SPAWN_PERM);
                final Location spawnLoc = BukkitUtil.toLocation(player.getWorld(), spawn);
                if (group != null) {
                    final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
                    if (RegionGroupFlag.isMember(set, group, localPlayer)) {
                        event.setRespawnLocation(spawnLoc);
                    }
                }
                else {
                    event.setRespawnLocation(spawnLoc);
                }
            }
        }
    }
    
    public void onItemHeldChange(final PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(player.getWorld());
        if (wcfg.removeInfiniteStacks && !this.plugin.hasPermission((CommandSender)player, "worldguard.override.infinite-stack")) {
            final int newSlot = event.getNewSlot();
            final ItemStack heldItem = player.getInventory().getItem(newSlot);
            if (heldItem.getAmount() < 0) {
                player.getInventory().setItem(newSlot, (ItemStack)null);
                player.sendMessage(ChatColor.RED + "Infinite stack removed.");
            }
        }
    }
    
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final Location location = player.getLocation();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(player.getWorld());
        if (wcfg.useRegions) {
            final Vector pt = BukkitUtil.toVector(location);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(player.getWorld());
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            if (!this.plugin.getGlobalRegionManager().hasBypass(player, player.getWorld()) && !set.allows(DefaultFlag.SLEEP)) {
                event.setCancelled(true);
                player.sendMessage("This bed doesn't belong to you!");
            }
        }
    }
    
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.useRegions && !this.plugin.getGlobalRegionManager().hasBypass(player, world)) {
            final Vector pt = BukkitUtil.toVector(player.getLocation());
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            final String[] parts = event.getMessage().split(" ");
            final Set<String> allowedCommands = set.getFlag(DefaultFlag.ALLOWED_CMDS);
            if (allowedCommands != null && !allowedCommands.contains(parts[0].toLowerCase())) {
                player.sendMessage(ChatColor.RED + parts[0].toLowerCase() + " is not allowed in this area.");
                event.setCancelled(true);
                return;
            }
            final Set<String> blockedCommands = set.getFlag(DefaultFlag.BLOCKED_CMDS);
            if (blockedCommands != null && blockedCommands.contains(parts[0].toLowerCase())) {
                player.sendMessage(ChatColor.RED + parts[0].toLowerCase() + " is blocked in this area.");
                event.setCancelled(true);
            }
        }
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
