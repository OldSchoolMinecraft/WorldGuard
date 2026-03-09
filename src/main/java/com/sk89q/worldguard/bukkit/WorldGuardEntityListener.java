package com.sk89q.worldguard.bukkit;

import java.util.logging.*;

import com.sk89q.worldedit.Vector;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.block.*;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.inventory.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.*;
import org.bukkit.*;
import java.util.*;
import com.sk89q.worldguard.blacklist.events.*;
import org.bukkit.entity.*;
import org.bukkit.event.painting.*;
import org.bukkit.event.entity.*;

public class WorldGuardEntityListener extends EntityListener
{
    private static final Logger logger;
    private WorldGuardPlugin plugin;
    
    public WorldGuardEntityListener(final WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerEvents() {
        final PluginManager pm = this.plugin.getServer().getPluginManager();
        this.registerEvent("ENTITY_DAMAGE", Event.Priority.High);
        this.registerEvent("ENTITY_COMBUST", Event.Priority.High);
        this.registerEvent("ENTITY_EXPLODE", Event.Priority.High);
        this.registerEvent("CREATURE_SPAWN", Event.Priority.High);
        this.registerEvent("ENTITY_INTERACT", Event.Priority.High);
        this.registerEvent("CREEPER_POWER", Event.Priority.High);
        this.registerEvent("PIG_ZAP", Event.Priority.High);
        this.registerEvent("PAINTING_BREAK", Event.Priority.High);
        this.registerEvent("PAINTING_PLACE", Event.Priority.High);
        this.registerEvent("ENTITY_REGAIN_HEALTH", Event.Priority.High);
    }
    
    private void registerEvent(final String typeName, final Event.Priority priority) {
        try {
            final Event.Type type = Event.Type.valueOf(typeName);
            final PluginManager pm = this.plugin.getServer().getPluginManager();
            pm.registerEvent(type, (Listener)this, priority, (Plugin)this.plugin);
        }
        catch (IllegalArgumentException e) {
            WorldGuardEntityListener.logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }
    
    public void onEntityInteract(final EntityInteractEvent event) {
        final Entity entity = event.getEntity();
        final Block block = event.getBlock();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(entity.getWorld());
        if (block.getType() == Material.SOIL && entity instanceof Creature && wcfg.disableCreatureCropTrampling) {
            event.setCancelled(true);
        }
    }
    
    public void onEntityDamageByBlock(final EntityDamageByBlockEvent event) {
        final Entity defender = event.getEntity();
        final EntityDamageEvent.DamageCause type = event.getCause();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(defender.getWorld());
        if (defender instanceof Wolf && ((Wolf)defender).isTamed()) {
            if (wcfg.antiWolfDumbness && type != EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
            }
        }
        else if (defender instanceof Player) {
            final Player player = (Player)defender;
            if (cfg.hasGodMode(player)) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableLavaDamage && type == EntityDamageEvent.DamageCause.LAVA) {
                event.setCancelled(true);
                if (cfg.hasGodMode(player)) {
                    player.setFireTicks(0);
                }
                return;
            }
            if (wcfg.disableContactDamage && type == EntityDamageEvent.DamageCause.CONTACT) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.teleportOnVoid && type == EntityDamageEvent.DamageCause.VOID) {
                BukkitUtil.findFreePosition(player);
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableVoidDamage && type == EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableExplosionDamage && event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        final Entity attacker = event.getDamager();
        final Entity defender = event.getEntity();
        if (attacker instanceof Player) {
            final Player player = (Player)attacker;
            final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
            final WorldConfiguration wcfg = cfg.get(player.getWorld());
            final ItemStack held = player.getInventory().getItemInHand();
            if (held != null && wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new ItemUseBlacklistEvent(this.plugin.wrapPlayer(player), BukkitUtil.toVector(player.getLocation()), held.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
        if (defender instanceof Player) {
            final Player player = (Player)defender;
            final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
            final WorldConfiguration wcfg = cfg.get(player.getWorld());
            if (cfg.hasGodMode(player)) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableLightningDamage && event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableExplosionDamage && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
                return;
            }
            if (attacker != null && attacker instanceof Player && wcfg.useRegions) {
                final Vector pt = BukkitUtil.toVector(defender.getLocation());
                final RegionManager mgr = this.plugin.getGlobalRegionManager().get(player.getWorld());
                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.PVP)) {
                    ((Player)attacker).sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
                    event.setCancelled(true);
                    return;
                }
            }
            if (attacker != null && attacker instanceof LivingEntity && !(attacker instanceof Player)) {
                if (attacker instanceof Creeper && wcfg.blockCreeperExplosions) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions) {
                    final Vector pt = BukkitUtil.toVector(defender.getLocation());
                    final RegionManager mgr = this.plugin.getGlobalRegionManager().get(player.getWorld());
                    final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
                    if (!set.allows(DefaultFlag.MOB_DAMAGE)) {
                        event.setCancelled(true);
                        return;
                    }
                    if (attacker instanceof Creeper && !set.allows(DefaultFlag.CREEPER_EXPLOSION)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    private void onEntityDamageByProjectile(final EntityDamageByProjectileEvent event) {
        final Entity defender = event.getEntity();
        final Entity attacker = event.getDamager();
        if (defender instanceof Player) {
            final Player player = (Player)defender;
            final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
            final WorldConfiguration wcfg = cfg.get(player.getWorld());
            if (cfg.hasGodMode(player)) {
                event.setCancelled(true);
                return;
            }
            if (attacker != null && attacker instanceof Player && wcfg.useRegions) {
                final Vector pt = BukkitUtil.toVector(defender.getLocation());
                final RegionManager mgr = this.plugin.getGlobalRegionManager().get(player.getWorld());
                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.PVP)) {
                    ((Player)attacker).sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
                    event.setCancelled(true);
                    return;
                }
            }
            if (attacker != null && attacker instanceof Skeleton) {
                if (wcfg.disableMobDamage) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions) {
                    final Vector pt = BukkitUtil.toVector(defender.getLocation());
                    final RegionManager mgr = this.plugin.getGlobalRegionManager().get(player.getWorld());
                    if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.MOB_DAMAGE)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event instanceof EntityDamageByProjectileEvent) {
            this.onEntityDamageByProjectile((EntityDamageByProjectileEvent)event);
            return;
        }
        if (event instanceof EntityDamageByEntityEvent) {
            this.onEntityDamageByEntity((EntityDamageByEntityEvent)event);
            return;
        }
        if (event instanceof EntityDamageByBlockEvent) {
            this.onEntityDamageByBlock((EntityDamageByBlockEvent)event);
            return;
        }
        final Entity defender = event.getEntity();
        final EntityDamageEvent.DamageCause type = event.getCause();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(defender.getWorld());
        if (defender instanceof Wolf && ((Wolf)defender).isTamed()) {
            if (wcfg.antiWolfDumbness) {
                event.setCancelled(true);
            }
        }
        else if (defender instanceof Player) {
            final Player player = (Player)defender;
            if (cfg.hasGodMode(player)) {
                event.setCancelled(true);
                player.setFireTicks(0);
                return;
            }
            if (type == EntityDamageEvent.DamageCause.DROWNING && cfg.hasAmphibiousMode(player)) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }
            if (type == EntityDamageEvent.DamageCause.DROWNING && wcfg.pumpkinScuba && (player.getInventory().getHelmet().getType() == Material.PUMPKIN || player.getInventory().getHelmet().getType() == Material.JACK_O_LANTERN)) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableFallDamage && type == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableFireDamage && (type == EntityDamageEvent.DamageCause.FIRE || type == EntityDamageEvent.DamageCause.FIRE_TICK)) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableDrowningDamage && type == EntityDamageEvent.DamageCause.DROWNING) {
                player.setRemainingAir(player.getMaximumAir());
                event.setCancelled(true);
                return;
            }
            if (wcfg.teleportOnSuffocation && type == EntityDamageEvent.DamageCause.SUFFOCATION) {
                BukkitUtil.findFreePosition(player);
                event.setCancelled(true);
                return;
            }
            if (wcfg.disableSuffocationDamage && type == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onEntityCombust(final EntityCombustEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Entity entity = event.getEntity();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        if (entity instanceof Player) {
            final Player player = (Player)entity;
            if (cfg.hasGodMode(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final Location l = event.getLocation();
        final World world = l.getWorld();
        final WorldConfiguration wcfg = cfg.get(world);
        final Entity ent = event.getEntity();
        if (ent instanceof LivingEntity) {
            if (wcfg.blockCreeperBlockDamage) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.blockCreeperExplosions) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions) {
                final Vector pt = BukkitUtil.toVector(l);
                final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.CREEPER_EXPLOSION)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        else if (ent instanceof Fireball) {
            if (wcfg.useRegions) {
                final Vector pt = BukkitUtil.toVector(l);
                final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.GHAST_FIREBALL)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        else if (ent instanceof TNTPrimed) {
            if (cfg.activityHaltToggle) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.blockTNT) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.useRegions) {
                final Vector pt = BukkitUtil.toVector(l);
                final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
                if (!mgr.getApplicableRegions(pt).allows(DefaultFlag.TNT)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (wcfg.signChestProtection) {
            for (final Block block : event.blockList()) {
                if (wcfg.isChestProtected(block)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (wcfg.useRegions) {
            final RegionManager mgr2 = this.plugin.getGlobalRegionManager().get(world);
            for (final Block block2 : event.blockList()) {
                final Vector pt2 = (Vector)BukkitUtil.toVector(block2);
                if (!mgr2.getApplicableRegions(pt2).allows(DefaultFlag.TNT)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        final WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());
        final CreatureType creaType = event.getCreatureType();
        if (wcfg.blockCreatureSpawn.contains(creaType)) {
            event.setCancelled(true);
            return;
        }
        final Location eventLoc = event.getLocation();
        if (wcfg.useRegions) {
            final Vector pt = BukkitUtil.toVector(eventLoc);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(eventLoc.getWorld());
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            if (!set.allows(DefaultFlag.MOB_SPAWNING)) {
                event.setCancelled(true);
                return;
            }
            final Set<CreatureType> blockTypes = set.getFlag(DefaultFlag.DENY_SPAWN);
            if (blockTypes != null && blockTypes.contains(creaType)) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onPigZap(final PigZapEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());
        if (wcfg.disablePigZap) {
            event.setCancelled(true);
        }
    }
    
    public void onCreeperPower(final CreeperPowerEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getEntity().getWorld());
        if (wcfg.disableCreeperPower) {
            event.setCancelled(true);
        }
    }
    
    public void onPaintingBreak(final PaintingBreakEvent breakEvent) {
        if (breakEvent.isCancelled()) {
            return;
        }
        if (!(breakEvent instanceof PaintingBreakByEntityEvent)) {
            return;
        }
        final PaintingBreakByEntityEvent event = (PaintingBreakByEntityEvent)breakEvent;
        if (!(event.getRemover() instanceof Player)) {
            return;
        }
        final Painting painting = event.getPainting();
        final Player player = (Player)event.getRemover();
        final World world = painting.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new BlockBreakBlacklistEvent(this.plugin.wrapPlayer(player), BukkitUtil.toVector(player.getLocation()), 321), false, false)) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.useRegions && !this.plugin.getGlobalRegionManager().canBuild(player, painting.getLocation())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
        }
    }
    
    public void onPaintingPlace(final PaintingPlaceEvent event) {
        final Block placedOn = event.getBlock();
        final Player player = event.getPlayer();
        final World world = placedOn.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new ItemUseBlacklistEvent(this.plugin.wrapPlayer(player), BukkitUtil.toVector(player.getLocation()), 321), false, false)) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.useRegions && !this.plugin.getGlobalRegionManager().canBuild(player, placedOn.getLocation())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
        }
    }
    
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        final Entity ent = event.getEntity();
        final World world = ent.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.disableHealthRegain) {
            event.setCancelled(true);
        }
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
