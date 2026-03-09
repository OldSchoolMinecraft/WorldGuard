package com.sk89q.worldguard.blacklist.loggers;

import com.sk89q.worldguard.blacklist.events.*;

public interface BlacklistLoggerHandler
{
    void logEvent(final BlacklistEvent p0, final String p1);
    
    void close();
}
