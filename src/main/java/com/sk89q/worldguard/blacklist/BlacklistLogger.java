package com.sk89q.worldguard.blacklist;

import com.sk89q.worldguard.blacklist.loggers.*;
import com.sk89q.worldguard.blacklist.events.*;
import java.util.*;

public class BlacklistLogger implements BlacklistLoggerHandler
{
    private Set<BlacklistLoggerHandler> handlers;
    
    public BlacklistLogger() {
        this.handlers = new HashSet<BlacklistLoggerHandler>();
    }
    
    public void addHandler(final BlacklistLoggerHandler handler) {
        this.handlers.add(handler);
    }
    
    public void removeHandler(final BlacklistLoggerHandler handler) {
        this.handlers.remove(handler);
    }
    
    public void clearHandlers() {
        this.handlers.clear();
    }
    
    public void logEvent(final BlacklistEvent event, final String comment) {
        for (final BlacklistLoggerHandler handler : this.handlers) {
            handler.logEvent(event, comment);
        }
    }
    
    public void close() {
        for (final BlacklistLoggerHandler handler : this.handlers) {
            handler.close();
        }
    }
}
