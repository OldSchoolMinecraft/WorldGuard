package com.sk89q.worldguard.bukkit;

import java.util.logging.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.event.world.*;
import org.bukkit.entity.*;

public class WorldGuardWorldListener extends WorldListener
{
    private static final Logger logger;
    private WorldGuardPlugin plugin;
    
    public WorldGuardWorldListener(final WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerEvents() {
        final PluginManager pm = this.plugin.getServer().getPluginManager();
        this.registerEvent("CHUNK_LOAD", Event.Priority.Normal);
    }
    
    private void registerEvent(final String typeName, final Event.Priority priority) {
        try {
            final Event.Type type = Event.Type.valueOf(typeName);
            final PluginManager pm = this.plugin.getServer().getPluginManager();
            pm.registerEvent(type, (Listener)this, priority, (Plugin)this.plugin);
        }
        catch (IllegalArgumentException e) {
            WorldGuardWorldListener.logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }
    
    public void onChunkLoad(final ChunkLoadEvent event) {
        final ConfigurationManager cfg = this.plugin.getGlobalStateManager();
        if (cfg.activityHaltToggle) {
            int removed = 0;
            for (final Entity entity : event.getChunk().getEntities()) {
                if (entity instanceof Item || (entity instanceof LivingEntity && !(entity instanceof Tameable) && !(entity instanceof Player))) {
                    entity.remove();
                    ++removed;
                }
            }
            if (removed > 50) {
                WorldGuardWorldListener.logger.info("WG Halt-Act: " + removed + " entities (>50) auto-removed from " + event.getChunk().toString());
            }
        }
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
