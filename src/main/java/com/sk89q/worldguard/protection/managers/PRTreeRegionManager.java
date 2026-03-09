//package com.sk89q.worldguard.protection.managers;
//
//import org.khelekore.prtree.*;
//import com.sk89q.worldguard.protection.databases.*;
//import com.sk89q.worldguard.protection.regions.*;
//import java.io.*;
//import com.sk89q.worldedit.*;
//import com.sk89q.worldguard.protection.*;
//import java.util.*;
//import com.sk89q.worldguard.*;
//
//public class PRTreeRegionManager extends RegionManager
//{
//    private static final int BRANCH_FACTOR = 30;
//    private Map<String, ProtectedRegion> regions;
//    private MBRConverter<ProtectedRegion> converter;
//    private PRTree<ProtectedRegion> tree;
//
//    public PRTreeRegionManager(final ProtectionDatabase regionloader) throws IOException {
//        super(regionloader);
//        this.converter = (MBRConverter<ProtectedRegion>)new ProtectedRegionMBRConverter();
//        this.regions = new TreeMap<String, ProtectedRegion>();
//        this.tree = (PRTree<ProtectedRegion>)new PRTree((MBRConverter)this.converter, 30);
//        this.load();
//    }
//
//    @Override
//    public Map<String, ProtectedRegion> getRegions() {
//        return this.regions;
//    }
//
//    @Override
//    public void setRegions(final Map<String, ProtectedRegion> regions) {
//        this.regions = new TreeMap<String, ProtectedRegion>(regions);
//        (this.tree = (PRTree<ProtectedRegion>)new PRTree((MBRConverter)this.converter, 30)).load((Collection)regions.values());
//    }
//
//    @Override
//    public void addRegion(final ProtectedRegion region) {
//        this.regions.put(region.getId().toLowerCase(), region);
//        (this.tree = (PRTree<ProtectedRegion>)new PRTree((MBRConverter)this.converter, 30)).load((Collection)this.regions.values());
//    }
//
//    @Override
//    public boolean hasRegion(final String id) {
//        return this.regions.containsKey(id.toLowerCase());
//    }
//
//    @Override
//    public ProtectedRegion getRegion(final String id) {
//        return this.regions.get(id.toLowerCase());
//    }
//
//    @Override
//    public void removeRegion(final String id) {
//        final ProtectedRegion region = this.regions.get(id.toLowerCase());
//        this.regions.remove(id.toLowerCase());
//        if (region != null) {
//            final List<String> removeRegions = new ArrayList<String>();
//            for (final ProtectedRegion curRegion : this.regions.values()) {
//                if (curRegion.getParent() == region) {
//                    removeRegions.add(curRegion.getId().toLowerCase());
//                }
//            }
//            for (final String remId : removeRegions) {
//                this.removeRegion(remId);
//            }
//        }
//        (this.tree = (PRTree<ProtectedRegion>)new PRTree((MBRConverter)this.converter, 30)).load((Collection)this.regions.values());
//    }
//
//    @Override
//    public ApplicableRegionSet getApplicableRegions(final Vector pt) {
//        final List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
//        final int x = pt.getBlockX();
//        final int z = pt.getBlockZ();
//        for (final ProtectedRegion region : this.tree.find((double)x, (double)z, (double)x, (double)z)) {
//            if (region.contains(pt)) {
//                appRegions.add(region);
//            }
//        }
//        Collections.sort(appRegions);
//        return new ApplicableRegionSet(appRegions, this.regions.get("__global__"));
//    }
//
//    @Override
//    public ApplicableRegionSet getApplicableRegions(final ProtectedRegion checkRegion) {
//        final List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
//        appRegions.addAll(this.regions.values());
//        List<ProtectedRegion> intersectRegions;
//        try {
//            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
//        }
//        catch (Exception e) {
//            intersectRegions = new ArrayList<ProtectedRegion>();
//        }
//        return new ApplicableRegionSet(intersectRegions, this.regions.get("__global__"));
//    }
//
//    @Override
//    public List<String> getApplicableRegionsIDs(final Vector pt) {
//        final List<String> applicable = new ArrayList<String>();
//        final int x = pt.getBlockX();
//        final int z = pt.getBlockZ();
//        for (final ProtectedRegion region : this.tree.find((double)x, (double)z, (double)x, (double)z)) {
//            if (region.contains(pt)) {
//                applicable.add(region.getId());
//            }
//        }
//        return applicable;
//    }
//
//    @Override
//    public boolean overlapsUnownedRegion(final ProtectedRegion checkRegion, final LocalPlayer player) {
//        final List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
//        for (final ProtectedRegion other : this.regions.values()) {
//            if (other.getOwners().contains(player)) {
//                continue;
//            }
//            appRegions.add(other);
//        }
//        List<ProtectedRegion> intersectRegions;
//        try {
//            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
//        }
//        catch (Exception e) {
//            intersectRegions = new ArrayList<ProtectedRegion>();
//        }
//        return intersectRegions.size() > 0;
//    }
//
//    @Override
//    public int size() {
//        return this.regions.size();
//    }
//
//    @Override
//    public int getRegionCountOfPlayer(final LocalPlayer player) {
//        int count = 0;
//        for (final Map.Entry<String, ProtectedRegion> entry : this.regions.entrySet()) {
//            if (entry.getValue().getOwners().contains(player)) {
//                ++count;
//            }
//        }
//        return count;
//    }
//}
