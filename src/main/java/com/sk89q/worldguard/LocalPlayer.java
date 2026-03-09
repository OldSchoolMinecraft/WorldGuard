package com.sk89q.worldguard;

import com.sk89q.worldedit.*;

public abstract class LocalPlayer
{
    public abstract String getName();
    
    public abstract boolean hasGroup(final String p0);
    
    public abstract Vector getPosition();
    
    public abstract void kick(final String p0);
    
    public abstract void ban(final String p0);
    
    public abstract void printRaw(final String p0);
    
    public abstract String[] getGroups();
    
    public abstract boolean hasPermission(final String p0);
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof LocalPlayer && ((LocalPlayer)obj).getName().equals(this.getName());
    }
    
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
}
