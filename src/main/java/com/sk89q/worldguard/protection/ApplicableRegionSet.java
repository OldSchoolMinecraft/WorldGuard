package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.protection.flags.*;
import java.util.*;

public class ApplicableRegionSet implements Iterable<ProtectedRegion>
{
    private Collection<ProtectedRegion> applicable;
    private ProtectedRegion globalRegion;
    
    public ApplicableRegionSet(final Collection<ProtectedRegion> applicable, final ProtectedRegion globalRegion) {
        this.applicable = applicable;
        this.globalRegion = globalRegion;
    }
    
    public boolean canBuild(final LocalPlayer player) {
        return this.internalGetState(DefaultFlag.BUILD, player, null, null);
    }
    
    public boolean canUse(final LocalPlayer player) {
        return !this.allows(DefaultFlag.USE, player) && !this.canBuild(player);
    }
    
    public boolean allows(final StateFlag flag) {
        if (flag == DefaultFlag.BUILD) {
            throw new IllegalArgumentException("Can't use build flag with allows()");
        }
        return this.internalGetState(flag, null, null, null);
    }
    
    public boolean allows(final StateFlag flag, final LocalPlayer player) {
        if (flag == DefaultFlag.BUILD) {
            throw new IllegalArgumentException("Can't use build flag with allows()");
        }
        return this.internalGetState(flag, null, flag.getGroupFlag(), player);
    }
    
    public boolean isOwnerOfAll(final LocalPlayer player) {
        for (final ProtectedRegion region : this.applicable) {
            if (!region.isOwner(player)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isMemberOfAll(final LocalPlayer player) {
        for (final ProtectedRegion region : this.applicable) {
            if (!region.isMember(player)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean internalGetState(final StateFlag flag, final LocalPlayer player, final RegionGroupFlag groupFlag, final LocalPlayer groupPlayer) {
        boolean found = false;
        boolean hasFlagDefined = false;
        boolean allowed = false;
        boolean def = flag.getDefault();
        if (this.globalRegion != null) {
            final StateFlag.State globalState = this.globalRegion.getFlag(flag);
            if (globalState != null) {
                if (player != null && this.globalRegion.hasMembersOrOwners()) {
                    def = (this.globalRegion.isMember(player) && globalState == StateFlag.State.ALLOW);
                }
                else {
                    def = (globalState == StateFlag.State.ALLOW);
                }
            }
            else if (player != null && this.globalRegion.hasMembersOrOwners()) {
                def = this.globalRegion.isMember(player);
            }
        }
        if (player == null) {
            allowed = def;
        }
        int lastPriority = Integer.MIN_VALUE;
        final Set<ProtectedRegion> needsClear = new HashSet<ProtectedRegion>();
        final Set<ProtectedRegion> hasCleared = new HashSet<ProtectedRegion>();
        for (final ProtectedRegion region : this.applicable) {
            if (hasFlagDefined && region.getPriority() < lastPriority) {
                break;
            }
            lastPriority = region.getPriority();
            if (player != null && region.getFlag(DefaultFlag.PASSTHROUGH) == StateFlag.State.ALLOW) {
                continue;
            }
            if (groupPlayer != null && groupFlag != null) {
                RegionGroupFlag.RegionGroup group = region.getFlag(groupFlag);
                if (group == null) {
                    group = groupFlag.getDefault();
                }
                if (!RegionGroupFlag.isMember(region, group, groupPlayer)) {
                    continue;
                }
            }
            final StateFlag.State v = region.getFlag(flag);
            if (v == StateFlag.State.DENY) {
                return false;
            }
            if (v == StateFlag.State.ALLOW) {
                allowed = true;
                found = true;
                hasFlagDefined = true;
            }
            else {
                if (player != null) {
                    hasFlagDefined = true;
                    if (!hasCleared.contains(region)) {
                        if (!region.isMember(player)) {
                            needsClear.add(region);
                        }
                        else {
                            this.clearParents(needsClear, hasCleared, region);
                        }
                    }
                }
                found = true;
            }
        }
        return found ? (allowed || (player != null && needsClear.size() == 0)) : def;
    }
    
    private void clearParents(final Set<ProtectedRegion> needsClear, final Set<ProtectedRegion> hasCleared, final ProtectedRegion region) {
        for (ProtectedRegion parent = region.getParent(); parent != null; parent = parent.getParent()) {
            if (!needsClear.remove(parent)) {
                hasCleared.add(parent);
            }
        }
    }
    
    public <T extends Flag<V>, V> V getFlag(final T flag) {
        if (flag instanceof StateFlag) {
            throw new IllegalArgumentException("Cannot use StateFlag with getFlag()");
        }
        int lastPriority = 0;
        boolean found = false;
        final Map<ProtectedRegion, V> needsClear = new HashMap<ProtectedRegion, V>();
        final Set<ProtectedRegion> hasCleared = new HashSet<ProtectedRegion>();
        for (final ProtectedRegion region : this.applicable) {
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            if (!hasCleared.contains(region)) {
                if (region.getFlag(flag) != null) {
                    this.clearParents(needsClear, hasCleared, region);
                    needsClear.put(region, region.getFlag(flag));
                    found = true;
                }
            }
            lastPriority = region.getPriority();
        }
        try {
            return needsClear.values().iterator().next();
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }
    
    private void clearParents(final Map<ProtectedRegion, ?> needsClear, final Set<ProtectedRegion> hasCleared, final ProtectedRegion region) {
        for (ProtectedRegion parent = region.getParent(); parent != null; parent = parent.getParent()) {
            if (needsClear.remove(parent) == null) {
                hasCleared.add(parent);
            }
        }
    }
    
    public int size() {
        return this.applicable.size();
    }
    
    public Iterator<ProtectedRegion> iterator() {
        return this.applicable.iterator();
    }
}
