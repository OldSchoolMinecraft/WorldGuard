package com.sk89q.worldguard.blacklist.events;


import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;

public abstract class BlacklistEvent
{
    private Vector pos;
    private int type;
    private LocalPlayer player;
    
    public BlacklistEvent(final LocalPlayer player, final Vector pos, final int type) {
        this.player = player;
        this.pos = pos;
        this.type = type;
    }
    
    public LocalPlayer getPlayer() {
        return this.player;
    }
    
    public Vector getPosition() {
        return this.pos;
    }
    
    public int getType() {
        return this.type;
    }
    
    public abstract String getDescription();
}
