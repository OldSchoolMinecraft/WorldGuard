package com.sk89q.worldguard;

import java.util.logging.*;

public class TickSyncDelayLoggerFilter implements Filter
{
    public boolean isLoggable(final LogRecord record) {
        return record.getLevel() != Level.WARNING || !record.getMessage().equals("Can't keep up! Did the system time change, or is the server overloaded?");
    }
}
