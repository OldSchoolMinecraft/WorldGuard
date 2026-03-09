package com.sk89q.worldguard.protection.managers;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.protection.databases.*;
import com.sk89q.worldedit.*;
import java.util.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.protection.*;

public class FlatRegionManager extends RegionManager
{
    private Map<String, ProtectedRegion> regions;
    
    public FlatRegionManager(final ProtectionDatabase regionloader) {
        super(regionloader);
        this.regions = new TreeMap<String, ProtectedRegion>();
    }
    
    @Override
    public Map<String, ProtectedRegion> getRegions() {
        return this.regions;
    }
    
    @Override
    public void setRegions(final Map<String, ProtectedRegion> regions) {
        this.regions = new TreeMap<String, ProtectedRegion>(regions);
    }
    
    @Override
    public void addRegion(final ProtectedRegion region) {
        this.regions.put(region.getId().toLowerCase(), region);
    }
    
    @Override
    public void removeRegion(final String id) {
        final ProtectedRegion region = this.regions.get(id.toLowerCase());
        this.regions.remove(id.toLowerCase());
        if (region != null) {
            final List<String> removeRegions = new ArrayList<String>();
            for (final ProtectedRegion curRegion : this.regions.values()) {
                if (curRegion.getParent() == region) {
                    removeRegions.add(curRegion.getId().toLowerCase());
                }
            }
            for (final String remId : removeRegions) {
                this.removeRegion(remId);
            }
        }
    }
    
    @Override
    public boolean hasRegion(final String id) {
        return this.regions.containsKey(id.toLowerCase());
    }
    
    @Override
    public ProtectedRegion getRegion(final String id) {
        return this.regions.get(id.toLowerCase());
    }
    
    @Override
    public ApplicableRegionSet getApplicableRegions(final Vector pt) {
        final TreeSet<ProtectedRegion> appRegions = new TreeSet<ProtectedRegion>();
        for (final ProtectedRegion region : this.regions.values()) {
            if (region.contains(pt)) {
                appRegions.add(region);
                for (ProtectedRegion parent = region.getParent(); parent != null; parent = parent.getParent()) {
                    if (!appRegions.contains(parent)) {
                        appRegions.add(region);
                    }
                }
            }
        }
        return new ApplicableRegionSet(appRegions, this.regions.get("__global__"));
    }
    
    @Override
    public List<String> getApplicableRegionsIDs(final Vector pt) {
        final List<String> applicable = new ArrayList<String>();
        for (final Map.Entry<String, ProtectedRegion> entry : this.regions.entrySet()) {
            if (entry.getValue().contains(pt)) {
                applicable.add(entry.getKey());
            }
        }
        return applicable;
    }
    
    @Override
    public ApplicableRegionSet getApplicableRegions(final ProtectedRegion checkRegion) {
        final List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
        appRegions.addAll(this.regions.values());
        List<ProtectedRegion> intersectRegions;
        try {
            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
        }
        catch (Exception e) {
            intersectRegions = new ArrayList<ProtectedRegion>();
        }
        return new ApplicableRegionSet(intersectRegions, this.regions.get("__global__"));
    }
    
    @Override
    public boolean overlapsUnownedRegion(final ProtectedRegion checkRegion, final LocalPlayer player) {
        final List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
        for (final ProtectedRegion other : this.regions.values()) {
            if (other.getOwners().contains(player)) {
                continue;
            }
            appRegions.add(other);
        }
        List<ProtectedRegion> intersectRegions;
        try {
            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
        }
        catch (UnsupportedIntersectionException e) {
            intersectRegions = new ArrayList<ProtectedRegion>();
        }
        return intersectRegions.size() > 0;
    }
    
    @Override
    public int size() {
        return this.regions.size();
    }
    
    @Override
    public int getRegionCountOfPlayer(final LocalPlayer player) {
        int count = 0;
        for (final ProtectedRegion region : this.regions.values()) {
            if (region.getOwners().contains(player)) {
                ++count;
            }
        }
        return count;
    }
}
