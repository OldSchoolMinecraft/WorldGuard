package com.sk89q.worldguard.protection.managers;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.databases.*;
import java.io.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldguard.protection.*;
import java.util.*;
import com.sk89q.worldguard.*;

public abstract class RegionManager
{
    protected ProtectionDatabase loader;
    
    public RegionManager(final ProtectionDatabase loader) {
        this.loader = loader;
    }
    
    public void load() throws IOException {
        this.loader.load(this);
    }
    
    public void save() throws IOException {
        this.loader.save(this);
    }
    
    public abstract Map<String, ProtectedRegion> getRegions();
    
    public abstract void setRegions(final Map<String, ProtectedRegion> p0);
    
    public abstract void addRegion(final ProtectedRegion p0);
    
    public abstract boolean hasRegion(final String p0);
    
    public abstract ProtectedRegion getRegion(final String p0);
    
    public abstract void removeRegion(final String p0);
    
    public abstract ApplicableRegionSet getApplicableRegions(final Vector p0);
    
    @Deprecated
    public abstract ApplicableRegionSet getApplicableRegions(final ProtectedRegion p0);
    
    public abstract List<String> getApplicableRegionsIDs(final Vector p0);
    
    public abstract boolean overlapsUnownedRegion(final ProtectedRegion p0, final LocalPlayer p1);
    
    public abstract int size();
    
    public abstract int getRegionCountOfPlayer(final LocalPlayer p0);
}
