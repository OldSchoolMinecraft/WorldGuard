package com.sk89q.worldguard.protection.regions;

import java.util.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.*;

public class ProtectedCuboidRegion extends ProtectedRegion
{
    private BlockVector min;
    private BlockVector max;
    
    public ProtectedCuboidRegion(final String id, final BlockVector min, final BlockVector max) {
        super(id);
        this.min = min;
        this.max = max;
    }
    
    @Override
    public BlockVector getMinimumPoint() {
        return this.min;
    }
    
    public void setMinimumPoint(final BlockVector pt) {
        this.min = pt;
    }
    
    @Override
    public BlockVector getMaximumPoint() {
        return this.max;
    }
    
    public void setMaximumPoint(final BlockVector pt) {
        this.max = pt;
    }
    
    @Override
    public boolean contains(final Vector pt) {
        final int x = pt.getBlockX();
        final int y = pt.getBlockY();
        final int z = pt.getBlockZ();
        return x >= this.min.getBlockX() && x <= this.max.getBlockX() && y >= this.min.getBlockY() && y <= this.max.getBlockY() && z >= this.min.getBlockZ() && z <= this.max.getBlockZ();
    }
    
    @Override
    public List<ProtectedRegion> getIntersectingRegions(final List<ProtectedRegion> regions) throws UnsupportedIntersectionException {
        final int numRegions = regions.size();
        final List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();
        for (int i = 0; i < numRegions; ++i) {
            final ProtectedRegion region = regions.get(i);
            final BlockVector rMinPoint = region.getMinimumPoint();
            final BlockVector rMaxPoint = region.getMaximumPoint();
            if (rMinPoint.getBlockX() >= this.min.getBlockX() || rMaxPoint.getBlockX() >= this.min.getBlockX()) {
                if (rMinPoint.getBlockX() > this.max.getBlockX() && rMaxPoint.getBlockX() > this.max.getBlockX() && ((rMinPoint.getBlockY() < this.min.getBlockY() && rMaxPoint.getBlockY() < this.min.getBlockY()) || (rMinPoint.getBlockY() > this.max.getBlockY() && rMaxPoint.getBlockY() > this.max.getBlockY()))) {
                    if (rMinPoint.getBlockZ() < this.min.getBlockZ() && rMaxPoint.getBlockZ() < this.min.getBlockZ()) {
                        continue;
                    }
                    if (rMinPoint.getBlockZ() > this.max.getBlockZ() && rMaxPoint.getBlockZ() > this.max.getBlockZ()) {
                        continue;
                    }
                }
                if (region.contains(new Vector(this.min.getBlockX(), this.min.getBlockY(), this.min.getBlockZ())) || region.contains(new Vector(this.min.getBlockX(), this.min.getBlockY(), this.max.getBlockZ())) || region.contains(new Vector(this.min.getBlockX(), this.max.getBlockY(), this.max.getBlockZ())) || region.contains(new Vector(this.min.getBlockX(), this.max.getBlockY(), this.min.getBlockZ())) || region.contains(new Vector(this.max.getBlockX(), this.max.getBlockY(), this.max.getBlockZ())) || region.contains(new Vector(this.max.getBlockX(), this.max.getBlockY(), this.min.getBlockZ())) || region.contains(new Vector(this.max.getBlockX(), this.min.getBlockY(), this.min.getBlockZ())) || region.contains(new Vector(this.max.getBlockX(), this.min.getBlockY(), this.max.getBlockZ()))) {
                    intersectingRegions.add(regions.get(i));
                }
                else {
                    if (region instanceof ProtectedPolygonalRegion) {
                        for (int i2 = 0; i2 < ((ProtectedPolygonalRegion)region).getPoints().size(); ++i2) {
                            final BlockVector2D pt2Dr = ((ProtectedPolygonalRegion)region).getPoints().get(i2);
                            final int minYr = ((ProtectedPolygonalRegion)region).minY;
                            final int maxYr = ((ProtectedPolygonalRegion)region).maxY;
                            final Vector ptr = new Vector(pt2Dr.getBlockX(), minYr, pt2Dr.getBlockZ());
                            final Vector ptr2 = new Vector(pt2Dr.getBlockX(), maxYr, pt2Dr.getBlockZ());
                            if (this.contains(ptr) || this.contains(ptr2)) {
                                intersectingRegions.add(regions.get(i));
                            }
                        }
                    }
                    else {
                        if (!(region instanceof ProtectedCuboidRegion)) {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }
                        final BlockVector ptcMin = region.getMinimumPoint();
                        final BlockVector ptcMax = region.getMaximumPoint();
                        if (this.contains(new Vector(ptcMin.getBlockX(), ptcMin.getBlockY(), ptcMin.getBlockZ())) || this.contains(new Vector(ptcMin.getBlockX(), ptcMin.getBlockY(), ptcMax.getBlockZ())) || this.contains(new Vector(ptcMin.getBlockX(), ptcMax.getBlockY(), ptcMax.getBlockZ())) || this.contains(new Vector(ptcMin.getBlockX(), ptcMax.getBlockY(), ptcMin.getBlockZ())) || this.contains(new Vector(ptcMax.getBlockX(), ptcMax.getBlockY(), ptcMax.getBlockZ())) || this.contains(new Vector(ptcMax.getBlockX(), ptcMax.getBlockY(), ptcMin.getBlockZ())) || this.contains(new Vector(ptcMax.getBlockX(), ptcMin.getBlockY(), ptcMin.getBlockZ())) || this.contains(new Vector(ptcMax.getBlockX(), ptcMin.getBlockY(), ptcMax.getBlockZ()))) {
                            intersectingRegions.add(regions.get(i));
                            continue;
                        }
                    }
                    boolean regionIsIntersecting = false;
                    final List<BlockVector2D> points = new ArrayList<BlockVector2D>();
                    points.add(new BlockVector2D(this.min.getBlockX(), this.min.getBlockZ()));
                    points.add(new BlockVector2D(this.min.getBlockX(), this.max.getBlockZ()));
                    points.add(new BlockVector2D(this.max.getBlockX(), this.max.getBlockZ()));
                    points.add(new BlockVector2D(this.max.getBlockX(), this.min.getBlockZ()));
                    for (int i2 = 0; i2 < points.size(); ++i2) {
                        boolean checkNextPoint = false;
                        final BlockVector2D currPoint = points.get(i2);
                        BlockVector2D nextPoint;
                        if (i2 == points.size() - 1) {
                            nextPoint = points.get(0);
                        }
                        else {
                            nextPoint = points.get(i2 + 1);
                        }
                        int currX = currPoint.getBlockX();
                        int currZ = currPoint.getBlockZ();
                        while (!checkNextPoint) {
                            for (int i3 = this.min.getBlockY(); i3 <= this.max.getBlockY(); ++i3) {
                                if (region.contains(new Vector(currX, i3, currZ))) {
                                    intersectingRegions.add(regions.get(i));
                                    regionIsIntersecting = true;
                                    break;
                                }
                            }
                            if (currX == nextPoint.getBlockX() || currZ == nextPoint.getBlockZ() || regionIsIntersecting) {
                                checkNextPoint = true;
                            }
                            if (nextPoint.getBlockX() > currPoint.getBlockX()) {
                                ++currX;
                            }
                            else {
                                --currX;
                            }
                            if (nextPoint.getBlockZ() > currPoint.getBlockZ()) {
                                ++currZ;
                            }
                            else {
                                --currZ;
                            }
                        }
                        if (regionIsIntersecting) {
                            break;
                        }
                    }
                }
            }
        }
        return intersectingRegions;
    }
    
    @Override
    public String getTypeName() {
        return "cuboid";
    }
    
    @Override
    public int volume() {
        final int xLength = this.max.getBlockX() - this.min.getBlockX() + 1;
        final int yLength = this.max.getBlockY() - this.min.getBlockY() + 1;
        final int zLength = this.max.getBlockZ() - this.min.getBlockZ() + 1;
        final int volume = xLength * yLength * zLength;
        return volume;
    }
}
