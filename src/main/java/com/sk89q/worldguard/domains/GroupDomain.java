package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.*;
import java.util.*;

public class GroupDomain implements Domain
{
    private Set<String> groups;
    
    public GroupDomain() {
        this.groups = new LinkedHashSet<String>();
    }
    
    public GroupDomain(final String[] groups) {
        this.groups = new LinkedHashSet<String>(Arrays.asList(groups));
    }
    
    public void addGroup(final String name) {
        this.groups.add(name);
    }
    
    public boolean contains(final LocalPlayer player) {
        for (final String group : this.groups) {
            if (player.hasGroup(group)) {
                return true;
            }
        }
        return false;
    }
    
    public int size() {
        return this.groups.size();
    }
}
