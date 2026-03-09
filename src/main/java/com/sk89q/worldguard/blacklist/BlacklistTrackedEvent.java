package com.sk89q.worldguard.blacklist;

import com.sk89q.worldguard.blacklist.events.*;

public class BlacklistTrackedEvent
{
    private BlacklistEvent event;
    private long time;
    
    public BlacklistTrackedEvent(final BlacklistEvent event, final long time) {
        this.event = event;
        this.time = time;
    }
    
    public boolean matches(final BlacklistEvent other, final long now) {
        return other.getType() == this.event.getType() && this.time > now - 3000L && other.getClass() == this.event.getClass();
    }
}
