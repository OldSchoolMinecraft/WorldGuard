package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.*;
import java.util.*;

public class DomainCollection implements Domain
{
    private Set<Domain> domains;
    
    public DomainCollection() {
        this.domains = new LinkedHashSet<Domain>();
    }
    
    public void add(final Domain domain) {
        this.domains.add(domain);
    }
    
    public void remove(final Domain domain) {
        this.domains.remove(domain);
    }
    
    public int size() {
        return this.domains.size();
    }
    
    public boolean contains(final LocalPlayer player) {
        for (final Domain domain : this.domains) {
            if (domain.contains(player)) {
                return true;
            }
        }
        return false;
    }
}
