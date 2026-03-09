package com.sk89q.worldguard.bukkit;

import java.util.logging.*;

import com.sk89q.worldguard.LocalPlayer;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.block.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.*;
import org.bukkit.inventory.*;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.command.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.blacklist.events.*;
import org.bukkit.event.block.*;

public class WorldGuardBlockListener extends BlockListener
{
    private static final Logger logger;
    private WorldGuardPlugin plugin;
    
    public WorldGuardBlockListener(final WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerEvents() {
        final PluginManager pm = this.plugin.getServer().getPluginManager();
        this.registerEvent("BLOCK_DAMAGE", Event.Priority.High);
        this.registerEvent("BLOCK_BREAK", Event.Priority.High);
        this.registerEvent("BLOCK_FROMTO", Event.Priority.Normal);
        this.registerEvent("BLOCK_IGNITE", Event.Priority.High);
        this.registerEvent("BLOCK_PHYSICS", Event.Priority.Normal);
        this.registerEvent("BLOCK_PLACE", Event.Priority.High);
        this.registerEvent("BLOCK_BURN", Event.Priority.High);
        this.registerEvent("SIGN_CHANGE", Event.Priority.High);
        this.registerEvent("REDSTONE_CHANGE", Event.Priority.High);
        this.registerEvent("SNOW_FORM", Event.Priority.High);
        this.registerEvent("LEAVES_DECAY", Event.Priority.High);
        this.registerEvent("BLOCK_FORM", Event.Priority.High);
        this.registerEvent("BLOCK_SPREAD", Event.Priority.High);
        this.registerEvent("BLOCK_FADE", Event.Priority.High);
    }
    
    private void registerEvent(final String typeName, final Event.Priority priority) {
        try {
            final Event.Type type = Event.Type.valueOf(typeName);
            final PluginManager pm = this.plugin.getServer().getPluginManager();
            pm.registerEvent(type, (Listener)this, priority, (Plugin)this.plugin);
        }
        catch (IllegalArgumentException e) {
            WorldGuardBlockListener.logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }
    
    protected WorldConfiguration getWorldConfig(final World world) {
        return this.plugin.getGlobalStateManager().get(world);
    }
    
    protected WorldConfiguration getWorldConfig(final Player player) {
        return this.plugin.getGlobalStateManager().get(player.getWorld());
    }
    
    public void onBlockDamage(final BlockDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final Block blockDamaged = event.getBlock();
        if (blockDamaged.getType() == Material.CAKE_BLOCK && !this.plugin.getGlobalRegionManager().canBuild(player, blockDamaged)) {
            player.sendMessage(ChatColor.DARK_RED + "You're not invited to this tea party!");
            event.setCancelled(true);
        }
    }
    
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final WorldConfiguration wcfg = this.getWorldConfig(player);
        if (!wcfg.itemDurability) {
            final ItemStack held = player.getItemInHand();
            if (held.getTypeId() > 0 && !ItemType.usesDamageValue(held.getTypeId()) && !BlockType.usesData(held.getTypeId())) {
                held.setDurability((short)(-1));
                player.setItemInHand(held);
            }
        }
        if (!this.plugin.getGlobalRegionManager().canBuild(player, event.getBlock())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }
        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(new BlockBreakBlacklistEvent(this.plugin.wrapPlayer(player), (Vector)BukkitUtil.toVector(event.getBlock()), event.getBlock().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
            if (!wcfg.getBlacklist().check(new DestroyWithBlacklistEvent(this.plugin.wrapPlayer(player), (Vector)BukkitUtil.toVector(event.getBlock()), player.getItemInHand().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
        if (wcfg.isChestProtected(event.getBlock(), player)) {
            player.sendMessage(ChatColor.DARK_RED + "The chest is protected.");
            event.setCancelled(true);
        }
    }
    
    public void onBlockFromTo(final BlockFromToEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final World world = event.getBlock().getWorld();
        final Block blockFrom = event.getBlock();
        final Block blockTo = event.getToBlock();
        final boolean isWater = blockFrom.getTypeId() == 8 || blockFrom.getTypeId() == 9;
        final boolean isLava = blockFrom.getTypeId() == 10 || blockFrom.getTypeId() == 11;
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.simulateSponge && isWater) {
            final int ox = blockTo.getX();
            final int oy = blockTo.getY();
            final int oz = blockTo.getZ();
            for (int cx = -wcfg.spongeRadius; cx <= wcfg.spongeRadius; ++cx) {
                for (int cy = -wcfg.spongeRadius; cy <= wcfg.spongeRadius; ++cy) {
                    for (int cz = -wcfg.spongeRadius; cz <= wcfg.spongeRadius; ++cz) {
                        final Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getTypeId() == 19 && (!wcfg.redstoneSponges || !sponge.isBlockIndirectlyPowered())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
        if (wcfg.preventWaterDamage.size() > 0) {
            final int targetId = blockTo.getTypeId();
            if ((blockFrom.getTypeId() == 0 || isWater) && wcfg.preventWaterDamage.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }
        if (wcfg.allowedLavaSpreadOver.size() > 0 && isLava) {
            final int targetId = blockTo.getRelative(0, -1, 0).getTypeId();
            if (!wcfg.allowedLavaSpreadOver.contains(targetId)) {
                event.setCancelled(true);
                return;
            }
        }
        if (wcfg.highFreqFlags && isWater && !this.plugin.getGlobalRegionManager().allows(DefaultFlag.WATER_FLOW, blockFrom.getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.highFreqFlags && isLava && !this.plugin.getGlobalRegionManager().allows(DefaultFlag.LAVA_FLOW, blockFrom.getLocation())) {
            event.setCancelled(true);
        }
    }
    
    public void onBlockIgnite(final BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final BlockIgniteEvent.IgniteCause cause = event.getCause();
        final Block block = event.getBlock();
        final World world = block.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        final boolean isFireSpread = cause == BlockIgniteEvent.IgniteCause.SPREAD;
        if (wcfg.preventLightningFire && cause == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.preventLavaFire && cause == BlockIgniteEvent.IgniteCause.LAVA) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.disableFireSpread && isFireSpread) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.blockLighter && cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL && event.getPlayer() != null && !this.plugin.hasPermission((CommandSender)event.getPlayer(), "worldguard.override.lighter")) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.fireSpreadDisableToggle && isFireSpread) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.disableFireSpreadBlocks.size() > 0 && isFireSpread) {
            final int x = block.getX();
            final int y = block.getY();
            final int z = block.getZ();
            if (wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y - 1, z)) || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x + 1, y, z)) || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x - 1, y, z)) || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y, z - 1)) || wcfg.disableFireSpreadBlocks.contains(world.getBlockTypeIdAt(x, y, z + 1))) {
                event.setCancelled(true);
                return;
            }
        }
        if (wcfg.useRegions) {
            final Vector pt = (Vector)BukkitUtil.toVector(block);
            final Player player = event.getPlayer();
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(world);
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            if (player != null && !this.plugin.getGlobalRegionManager().hasBypass(player, world)) {
                final LocalPlayer localPlayer = this.plugin.wrapPlayer(player);
                if (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
                    if (!set.canBuild(localPlayer)) {
                        event.setCancelled(true);
                        return;
                    }
                    if (!set.allows(DefaultFlag.LIGHTER) && !this.plugin.hasPermission((CommandSender)player, "worldguard.override.lighter")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (wcfg.highFreqFlags && isFireSpread && !set.allows(DefaultFlag.FIRE_SPREAD)) {
                event.setCancelled(true);
                return;
            }
            if (wcfg.highFreqFlags && cause == BlockIgniteEvent.IgniteCause.LAVA && !set.allows(DefaultFlag.LAVA_FIRE)) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onBlockBurn(final BlockBurnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.disableFireSpread) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.fireSpreadDisableToggle) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.disableFireSpreadBlocks.size() > 0) {
            final Block block = event.getBlock();
            if (wcfg.disableFireSpreadBlocks.contains(block.getTypeId())) {
                event.setCancelled(true);
                return;
            }
        }
        if (wcfg.isChestProtected(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.useRegions) {
            final Block block = event.getBlock();
            final Vector pt = (Vector)BukkitUtil.toVector(block);
            final RegionManager mgr = this.plugin.getGlobalRegionManager().get(block.getWorld());
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            if (!set.allows(DefaultFlag.FIRE_SPREAD)) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onBlockPhysics(final BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        final int id = event.getChangedTypeId();
        if (id == 13 && wcfg.noPhysicsGravel) {
            event.setCancelled(true);
            return;
        }
        if (id == 12 && wcfg.noPhysicsSand) {
            event.setCancelled(true);
            return;
        }
        if (id == 90 && wcfg.allowPortalAnywhere) {
            event.setCancelled(true);
        }
    }
    
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Block blockPlaced = event.getBlock();
        final Player player = event.getPlayer();
        final World world = blockPlaced.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.useRegions && !this.plugin.getGlobalRegionManager().canBuild(player, blockPlaced.getLocation())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }
        if (wcfg.getBlacklist() != null && !wcfg.getBlacklist().check(new BlockPlaceBlacklistEvent(this.plugin.wrapPlayer(player), (Vector)BukkitUtil.toVector(blockPlaced), blockPlaced.getTypeId()), false, false)) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.signChestProtection && wcfg.getChestProtection().isChest(blockPlaced.getType()) && wcfg.isAdjacentChestProtected(event.getBlock(), player)) {
            player.sendMessage(ChatColor.DARK_RED + "This spot is for a chest that you don't have permission for.");
            event.setCancelled(true);
            return;
        }
        if (wcfg.simulateSponge && blockPlaced.getTypeId() == 19) {
            if (wcfg.redstoneSponges && blockPlaced.isBlockIndirectlyPowered()) {
                return;
            }
            final int ox = blockPlaced.getX();
            final int oy = blockPlaced.getY();
            final int oz = blockPlaced.getZ();
            SpongeUtil.clearSpongeWater(this.plugin, world, ox, oy, oz);
        }
    }
    
    public void onBlockRedstoneChange(final BlockRedstoneEvent event) {
        final Block blockTo = event.getBlock();
        final World world = blockTo.getWorld();
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        if (wcfg.simulateSponge && wcfg.redstoneSponges) {
            final int ox = blockTo.getX();
            final int oy = blockTo.getY();
            final int oz = blockTo.getZ();
            for (int cx = -1; cx <= 1; ++cx) {
                for (int cy = -1; cy <= 1; ++cy) {
                    for (int cz = -1; cz <= 1; ++cz) {
                        final Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                        if (sponge.getTypeId() == 19 && sponge.isBlockIndirectlyPowered()) {
                            SpongeUtil.clearSpongeWater(this.plugin, world, ox + cx, oy + cy, oz + cz);
                        }
                        else if (sponge.getTypeId() == 19 && !sponge.isBlockIndirectlyPowered()) {
                            SpongeUtil.addSpongeWater(this.plugin, world, ox + cx, oy + cy, oz + cz);
                        }
                    }
                }
            }
        }
    }
    
    public void onSignChange(final SignChangeEvent event) {
        final Player player = event.getPlayer();
        final WorldConfiguration wcfg = this.getWorldConfig(player);
        if (wcfg.signChestProtection) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                if (wcfg.isChestProtectedPlacement(event.getBlock(), player)) {
                    player.sendMessage(ChatColor.DARK_RED + "You do not own the adjacent chest.");
                    BukkitUtil.dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
                if (event.getBlock().getType() != Material.SIGN_POST) {
                    player.sendMessage(ChatColor.RED + "The [Lock] sign must be a sign post, not a wall sign.");
                    BukkitUtil.dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
                if (!event.getLine(1).equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "The first owner line must be your name.");
                    BukkitUtil.dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
                final Material below = event.getBlock().getRelative(0, -1, 0).getType();
                if (below == Material.TNT || below == Material.SAND || below == Material.GRAVEL || below == Material.SIGN_POST) {
                    player.sendMessage(ChatColor.RED + "That is not a safe block that you're putting this sign on.");
                    BukkitUtil.dropSign(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
                event.setLine(0, "[Lock]");
                player.sendMessage(ChatColor.YELLOW + "A chest or double chest above is now protected.");
            }
        }
        else if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
            player.sendMessage(ChatColor.RED + "WorldGuard's sign chest protection is disabled.");
            BukkitUtil.dropSign(event.getBlock());
            event.setCancelled(true);
            return;
        }
        if (!this.plugin.getGlobalRegionManager().canBuild(player, event.getBlock())) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
        }
    }
    
    /*public void onSnowForm(final SnowFormEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.disableSnowFormation) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.useRegions && !this.plugin.getGlobalRegionManager().allows(DefaultFlag.SNOW_FALL, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }*/
    
    public void onLeavesDecay(final LeavesDecayEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.disableLeafDecay) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.useRegions && !this.plugin.getGlobalRegionManager().allows(DefaultFlag.LEAF_DECAY, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
    
    public void onBlockForm(final BlockFormEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        final Material type = event.getNewState().getType();
        if (wcfg.disableIceFormation && type == Material.ICE) {
            event.setCancelled(true);
            return;
        }
        if (wcfg.disableSnowFormation && type == Material.SNOW) {
            event.setCancelled(true);
        }
    }
    
    public void onBlockSpread(final BlockSpreadEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        if (cfg.activityHaltToggle) {
            event.setCancelled(true);
            return;
        }
        final Material fromType = event.getSource().getType();
        if (wcfg.disableMushroomSpread && (fromType == Material.RED_MUSHROOM || fromType == Material.BROWN_MUSHROOM)) {
            event.setCancelled(true);
        }
    }
    
    public void onBlockFade(final BlockFadeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(event.getBlock().getWorld());
        final Material type = event.getBlock().getType();

        if (type.getId() == 79) // ice
        {
            if (wcfg.disableIceMelting && type == Material.ICE) {
                event.setCancelled(true);
                return;
            }

            // handle region-based melting flags
            if (wcfg.useRegions && (!this.plugin.getGlobalRegionManager().allows(DefaultFlag.ICE_MELT, event.getBlock().getLocation())))
            {
                event.setCancelled(true);
                return;
            }
        }

        if (type.getId() == 78) // snow
        {
            if (wcfg.disableSnowMelting && type == Material.SNOW) {
                event.setCancelled(true);
            }

            // handle region-based melting flags
            if (wcfg.useRegions && (!this.plugin.getGlobalRegionManager().allows(DefaultFlag.SNOW_MELT, event.getBlock().getLocation()))) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
