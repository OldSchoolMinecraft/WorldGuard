package com.sk89q.worldguard.protection.regions;

import com.sk89q.worldedit.*;
import java.util.*;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.*;

public class GlobalProtectedRegion extends ProtectedRegion
{
    public GlobalProtectedRegion(final String id) {
        super(id);
    }
    
    @Override
    public BlockVector getMinimumPoint() {
        return new BlockVector(0, 0, 0);
    }
    
    @Override
    public BlockVector getMaximumPoint() {
        return new BlockVector(0, 0, 0);
    }
    
    @Override
    public int volume() {
        return 0;
    }
    
    @Override
    public boolean contains(final Vector pt) {
        return false;
    }
    
    @Override
    public String getTypeName() {
        return "global";
    }
    
    @Override
    public List<ProtectedRegion> getIntersectingRegions(final List<ProtectedRegion> regions) throws UnsupportedIntersectionException {
        return new ArrayList<ProtectedRegion>();
    }
}
