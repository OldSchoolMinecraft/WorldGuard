package com.sk89q.worldguard.protection.regions;

import java.util.regex.*;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.*;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldedit.*;
import java.util.*;
import com.sk89q.worldguard.protection.*;

public abstract class ProtectedRegion implements Comparable<ProtectedRegion>
{
    private static final Pattern idPattern;
    private String id;
    private int priority;
    private ProtectedRegion parent;
    private DefaultDomain owners;
    private DefaultDomain members;
    private Map<Flag<?>, Object> flags;
    
    public ProtectedRegion(final String id) {
        this.priority = 0;
        this.owners = new DefaultDomain();
        this.members = new DefaultDomain();
        this.flags = new HashMap<Flag<?>, Object>();
        this.id = id;
    }
    
    public String getId() {
        return this.id;
    }
    
    public abstract BlockVector getMinimumPoint();
    
    public abstract BlockVector getMaximumPoint();
    
    public int getPriority() {
        return this.priority;
    }
    
    public void setPriority(final int priority) {
        this.priority = priority;
    }
    
    public ProtectedRegion getParent() {
        return this.parent;
    }
    
    public void setParent(final ProtectedRegion parent) throws CircularInheritanceException {
        if (parent == null) {
            this.parent = null;
            return;
        }
        if (parent == this) {
            throw new CircularInheritanceException();
        }
        for (ProtectedRegion p = parent.getParent(); p != null; p = p.getParent()) {
            if (p == this) {
                throw new CircularInheritanceException();
            }
        }
        this.parent = parent;
    }
    
    public DefaultDomain getOwners() {
        return this.owners;
    }
    
    public void setOwners(final DefaultDomain owners) {
        this.owners = owners;
    }
    
    public DefaultDomain getMembers() {
        return this.members;
    }
    
    public void setMembers(final DefaultDomain members) {
        this.members = members;
    }
    
    public boolean hasMembersOrOwners() {
        return this.owners.size() > 0 || this.members.size() > 0;
    }
    
    public boolean isOwner(final LocalPlayer player) {
        if (this.owners.contains(player)) {
            return true;
        }
        for (ProtectedRegion curParent = this.getParent(); curParent != null; curParent = curParent.getParent()) {
            if (curParent.getOwners().contains(player)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isMember(final LocalPlayer player) {
        if (this.owners.contains(player) || this.members.contains(player)) {
            return true;
        }
        for (ProtectedRegion curParent = this.getParent(); curParent != null; curParent = curParent.getParent()) {
            if (curParent.getOwners().contains(player) || curParent.getMembers().contains(player)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isMemberOnly(final LocalPlayer player) {
        if (this.members.contains(player)) {
            return true;
        }
        for (ProtectedRegion curParent = this.getParent(); curParent != null; curParent = curParent.getParent()) {
            if (curParent.getMembers().contains(player)) {
                return true;
            }
        }
        return false;
    }
    
    public <T extends Flag<V>, V> V getFlag(final T flag) {
        final Object obj = this.flags.get(flag);
        if (obj != null) {
            final V val = (V)obj;
            return val;
        }
        return null;
    }
    
    public <T extends Flag<V>, V> void setFlag(final T flag, final V val) {
        if (val == null) {
            this.flags.remove(flag);
        }
        else {
            this.flags.put(flag, val);
        }
    }
    
    public Map<Flag<?>, Object> getFlags() {
        return this.flags;
    }
    
    public void setFlags(final Map<Flag<?>, Object> flags) {
        this.flags = flags;
    }
    
    public abstract int volume();
    
    public abstract boolean contains(final Vector p0);
    
    public int compareTo(final ProtectedRegion other) {
        if (this.id.equals(other.id)) {
            return 0;
        }
        if (this.priority == other.priority) {
            return 1;
        }
        if (this.priority > other.priority) {
            return -1;
        }
        return 1;
    }
    
    public abstract String getTypeName();
    
    public abstract List<ProtectedRegion> getIntersectingRegions(final List<ProtectedRegion> p0) throws UnsupportedIntersectionException;
    
    public static boolean isValidId(final String id) {
        return ProtectedRegion.idPattern.matcher(id).matches();
    }
    
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ProtectedRegion)) {
            return false;
        }
        final ProtectedRegion other = (ProtectedRegion)obj;
        return other.getId().equals(this.getId());
    }
    
    static {
        idPattern = Pattern.compile("^[A-Za-z0-9_,'\\-\\+/]{1,}$");
    }
    
    public static class CircularInheritanceException extends Exception
    {
        private static final long serialVersionUID = 7479613488496776022L;
    }
}
