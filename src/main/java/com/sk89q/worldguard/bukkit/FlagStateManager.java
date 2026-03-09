package com.sk89q.worldguard.bukkit;

import java.util.*;

import com.sk89q.worldedit.Vector;
import org.bukkit.entity.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.*;

public class FlagStateManager implements Runnable
{
    public static final int RUN_DELAY = 20;
    private WorldGuardPlugin plugin;
    private Map<String, PlayerFlagState> states;
    
    public FlagStateManager(final WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.states = new HashMap<String, PlayerFlagState>();
    }
    
    public void run() {
        final Player[] players = this.plugin.getServer().getOnlinePlayers();
        final ConfigurationManager config = this.plugin.getGlobalStateManager();
        for (final Player player : players) {
            final WorldConfiguration worldConfig = config.get(player.getWorld());
            if (worldConfig.useRegions) {
                PlayerFlagState state;
                synchronized (this) {
                    state = this.states.get(player.getName());
                    if (state == null) {
                        state = new PlayerFlagState();
                        this.states.put(player.getName(), state);
                    }
                }
                final Vector playerLocation = BukkitUtil.toVector(player.getLocation());
                final RegionManager regionManager = this.plugin.getGlobalRegionManager().get(player.getWorld());
                final ApplicableRegionSet applicable = regionManager.getApplicableRegions(playerLocation);
                this.processHeal(applicable, player, state);
            }
        }
    }
    
    private void processHeal(final ApplicableRegionSet applicable, final Player player, final PlayerFlagState state) {
        if (player.getHealth() <= 0) {
            return;
        }
        final long now = System.currentTimeMillis();
        final Integer healAmount = applicable.getFlag(DefaultFlag.HEAL_AMOUNT);
        final Integer healDelay = applicable.getFlag(DefaultFlag.HEAL_DELAY);
        if (healAmount == null || healDelay == null || healAmount == 0 || healDelay < 0) {
            return;
        }
        if (player.getHealth() >= 20 && healAmount > 0) {
            return;
        }
        if (healDelay <= 0 && healAmount > 0) {
            player.setHealth(20);
            state.lastHeal = now;
        }
        else if (now - state.lastHeal > healDelay * 1000) {
            player.setHealth(Math.min(20, Math.max(0, player.getHealth() + healAmount)));
            state.lastHeal = now;
        }
    }
    
    public synchronized void forget(final Player player) {
        this.states.remove(player.getName());
    }
    
    public synchronized PlayerFlagState getState(final Player player) {
        PlayerFlagState state = this.states.get(player.getName());
        if (state == null) {
            state = new PlayerFlagState();
            this.states.put(player.getName(), state);
        }
        return state;
    }
    
    public static class PlayerFlagState
    {
        public long lastHeal;
        public String lastGreeting;
        public String lastFarewell;
        public Boolean lastExitAllowed;
        public Boolean notifiedForLeave;
        public Boolean notifiedForEnter;
        public World lastWorld;
        public int lastBlockX;
        public int lastBlockY;
        public int lastBlockZ;
        
        public PlayerFlagState() {
            this.lastExitAllowed = null;
            this.notifiedForLeave = false;
            this.notifiedForEnter = false;
        }
    }
}
