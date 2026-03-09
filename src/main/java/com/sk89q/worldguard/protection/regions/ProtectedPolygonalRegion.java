package com.sk89q.worldguard.protection.regions;

import com.sk89q.worldedit.*;
import java.util.*;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.*;

public class ProtectedPolygonalRegion extends ProtectedRegion
{
    protected List<BlockVector2D> points;
    protected int minY;
    protected int maxY;
    private BlockVector min;
    private BlockVector max;
    
    public ProtectedPolygonalRegion(final String id, final List<BlockVector2D> points, final int minY, final int maxY) {
        super(id);
        this.points = points;
        this.minY = minY;
        this.maxY = maxY;
        int minX = points.get(0).getBlockX();
        int minZ = points.get(0).getBlockZ();
        int maxX = points.get(0).getBlockX();
        int maxZ = points.get(0).getBlockZ();
        for (final BlockVector2D v : points) {
            final int x = v.getBlockX();
            final int z = v.getBlockZ();
            if (x < minX) {
                minX = x;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
        this.min = new BlockVector(minX, minY, minZ);
        this.max = new BlockVector(maxX, maxY, maxZ);
    }
    
    public List<BlockVector2D> getPoints() {
        return this.points;
    }
    
    @Override
    public BlockVector getMinimumPoint() {
        return this.min;
    }
    
    @Override
    public BlockVector getMaximumPoint() {
        return this.max;
    }
    
    @Override
    public boolean contains(final Vector pt) {
        final int targetX = pt.getBlockX();
        final int targetY = pt.getBlockY();
        final int targetZ = pt.getBlockZ();
        if (targetY < this.minY || targetY > this.maxY) {
            return false;
        }
        if (targetX < this.min.getBlockX() || targetX > this.max.getBlockX() || targetZ < this.min.getBlockZ() || targetZ > this.max.getBlockZ()) {
            return false;
        }
        boolean inside = false;
        final int npoints = this.points.size();
        int xOld = this.points.get(npoints - 1).getBlockX();
        int zOld = this.points.get(npoints - 1).getBlockZ();
        for (int i = 0; i < npoints; ++i) {
            final int xNew = this.points.get(i).getBlockX();
            final int zNew = this.points.get(i).getBlockZ();
            if (xNew == targetX && zNew == targetZ) {
                return true;
            }
            int x1;
            int x2;
            int z1;
            int z2;
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            }
            else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if (xNew < targetX == targetX <= xOld && (targetZ - (long)z1) * (x2 - x1) <= (z2 - (long)z1) * (targetX - x1)) {
                inside = !inside;
            }
            xOld = xNew;
            zOld = zNew;
        }
        return inside;
    }
    
    @Override
    public List<ProtectedRegion> getIntersectingRegions(final List<ProtectedRegion> regions) throws UnsupportedIntersectionException {
        final int numRegions = regions.size();
        final int numPoints = this.points.size();
        final List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();
        for (int i = 0; i < numRegions; ++i) {
            final ProtectedRegion region = regions.get(i);
            final BlockVector rMinPoint = region.getMinimumPoint();
            final BlockVector rMaxPoint = region.getMaximumPoint();
            if ((rMinPoint.getBlockX() < this.min.getBlockX() && rMaxPoint.getBlockX() < this.min.getBlockX()) || (rMinPoint.getBlockX() > this.max.getBlockX() && rMaxPoint.getBlockX() > this.max.getBlockX() && ((rMinPoint.getBlockY() < this.min.getBlockY() && rMaxPoint.getBlockY() < this.min.getBlockY()) || (rMinPoint.getBlockY() > this.max.getBlockY() && rMaxPoint.getBlockY() > this.max.getBlockY())) && ((rMinPoint.getBlockZ() < this.min.getBlockZ() && rMaxPoint.getBlockZ() < this.min.getBlockZ()) || (rMinPoint.getBlockZ() > this.max.getBlockZ() && rMaxPoint.getBlockZ() > this.max.getBlockZ())))) {
                intersectingRegions.add(regions.get(i));
            }
            else {
                int i2 = 0;
                while (i < numPoints) {
                    final Vector pt = new Vector(this.points.get(i2).getBlockX(), this.minY, this.points.get(i2).getBlockZ());
                    final Vector pt2 = new Vector(this.points.get(i2).getBlockX(), this.maxY, this.points.get(i2).getBlockZ());
                    if (region.contains(pt) || region.contains(pt2)) {
                        intersectingRegions.add(regions.get(i));
                    }
                    ++i;
                }
                if (region instanceof ProtectedPolygonalRegion) {
                    i2 = 0;
                    while (i < ((ProtectedPolygonalRegion)region).getPoints().size()) {
                        final BlockVector2D pt2Dr = ((ProtectedPolygonalRegion)region).getPoints().get(i2);
                        final int minYr = ((ProtectedPolygonalRegion)region).minY;
                        final int maxYr = ((ProtectedPolygonalRegion)region).maxY;
                        final Vector ptr = new Vector(pt2Dr.getBlockX(), minYr, pt2Dr.getBlockZ());
                        final Vector ptr2 = new Vector(pt2Dr.getBlockX(), maxYr, pt2Dr.getBlockZ());
                        if (this.contains(ptr) || this.contains(ptr2)) {
                            intersectingRegions.add(regions.get(i));
                        }
                        ++i;
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
                for (i2 = 0; i2 < numPoints; ++i2) {
                    boolean checkNextPoint = false;
                    final BlockVector2D currPoint = this.points.get(i2);
                    BlockVector2D nextPoint;
                    if (i2 == numPoints - 1) {
                        nextPoint = this.points.get(0);
                    }
                    else {
                        nextPoint = this.points.get(i2 + 1);
                    }
                    int currX = currPoint.getBlockX();
                    int currZ = currPoint.getBlockZ();
                    while (!checkNextPoint) {
                        for (int i3 = this.minY; i3 <= this.maxY; ++i3) {
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
        return intersectingRegions;
    }
    
    @Override
    public String getTypeName() {
        return "polygon";
    }
    
    @Override
    public int volume() {
        int volume = 0;
        final int numPoints = this.points.size();
        if (numPoints < 3) {
            return 0;
        }
        double area = 0.0;
        for (int i = 0; i <= numPoints - 1; ++i) {
            final int xa = this.points.get(i).getBlockX();
            int z1;
            if (this.points.get(i + 1) == null) {
                z1 = this.points.get(0).getBlockZ();
            }
            else {
                z1 = this.points.get(i + 1).getBlockZ();
            }
            int z2;
            if (this.points.get(i - 1) == null) {
                z2 = this.points.get(numPoints - 1).getBlockZ();
            }
            else {
                z2 = this.points.get(i - 1).getBlockZ();
            }
            area += xa * (z1 - z2);
        }
        final int xa = this.points.get(0).getBlockX();
        area += xa * (this.points.get(1).getBlockZ() - this.points.get(numPoints - 1).getBlockZ());
        volume = (Math.abs(this.maxY - this.minY) + 1) * (int)Math.ceil(Math.abs(area) / 2.0);
        return volume;
    }
}
