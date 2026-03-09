package com.sk89q.worldguard.blacklist.loggers;

import java.util.logging.*;
import com.sk89q.worldguard.blacklist.events.*;
import com.sk89q.worldedit.blocks.*;

public class ConsoleLoggerHandler implements BlacklistLoggerHandler
{
    private static final Logger logger;
    private String worldName;
    
    public ConsoleLoggerHandler(final String worldName) {
        this.worldName = worldName;
    }
    
    public void logEvent(final BlacklistEvent event, final String comment) {
        if (event instanceof BlockBreakBlacklistEvent) {
            final BlockBreakBlacklistEvent evt = (BlockBreakBlacklistEvent)event;
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " tried to break " + getFriendlyItemName(evt.getType()) + ((comment != null) ? (" (" + comment + ")") : ""));
        }
        else if (event instanceof BlockPlaceBlacklistEvent) {
            final BlockPlaceBlacklistEvent evt2 = (BlockPlaceBlacklistEvent)event;
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " tried to place " + getFriendlyItemName(evt2.getType()) + ((comment != null) ? (" (" + comment + ")") : ""));
        }
        else if (event instanceof BlockInteractBlacklistEvent) {
            final BlockInteractBlacklistEvent evt3 = (BlockInteractBlacklistEvent)event;
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " tried to interact with " + getFriendlyItemName(evt3.getType()) + ((comment != null) ? (" (" + comment + ")") : ""));
        }
        else if (event instanceof DestroyWithBlacklistEvent) {
            final DestroyWithBlacklistEvent evt4 = (DestroyWithBlacklistEvent)event;
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " tried to destroy with " + getFriendlyItemName(evt4.getType()) + ((comment != null) ? (" (" + comment + ")") : ""));
        }
        else if (event instanceof ItemAcquireBlacklistEvent) {
            final ItemAcquireBlacklistEvent evt5 = (ItemAcquireBlacklistEvent)event;
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " tried to acquire " + getFriendlyItemName(evt5.getType()) + ((comment != null) ? (" (" + comment + ")") : ""));
        }
        else if (event instanceof ItemDropBlacklistEvent) {
            final ItemDropBlacklistEvent evt6 = (ItemDropBlacklistEvent)event;
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " tried to drop " + getFriendlyItemName(evt6.getType()) + ((comment != null) ? (" (" + comment + ")") : ""));
        }
        else if (event instanceof ItemUseBlacklistEvent) {
            final ItemUseBlacklistEvent evt7 = (ItemUseBlacklistEvent)event;
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " tried to use " + getFriendlyItemName(evt7.getType()) + ((comment != null) ? (" (" + comment + ")") : ""));
        }
        else {
            ConsoleLoggerHandler.logger.log(Level.INFO, "WorldGuard: [" + this.worldName + "] " + event.getPlayer().getName() + " caught unknown event: " + event.getClass().getCanonicalName());
        }
    }
    
    private static String getFriendlyItemName(final int id) {
        final ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        }
        return "#" + id + "";
    }
    
    public void close() {
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
