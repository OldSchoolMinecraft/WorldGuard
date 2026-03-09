package com.sk89q.worldguard.bukkit;

import org.bukkit.command.*;
import java.util.logging.*;
import org.bukkit.*;

public class LoggerToChatHandler extends Handler
{
    private CommandSender player;
    
    public LoggerToChatHandler(final CommandSender player) {
        this.player = player;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    public void publish(final LogRecord record) {
        this.player.sendMessage(ChatColor.GRAY + record.getLevel().getName() + ": " + ChatColor.WHITE + record.getMessage());
    }
}
