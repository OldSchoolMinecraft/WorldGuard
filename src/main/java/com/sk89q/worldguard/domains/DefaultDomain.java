package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.*;
import java.util.*;

public class DefaultDomain implements Domain
{
    private Set<String> groups;
    private Set<String> players;
    
    public DefaultDomain() {
        this.groups = new LinkedHashSet<String>();
        this.players = new HashSet<String>();
    }
    
    public void addPlayer(final String name) {
        this.players.add(name.toLowerCase());
    }
    
    public void addPlayer(final LocalPlayer player) {
        this.players.add(player.getName().toLowerCase());
    }
    
    public void removePlayer(final String name) {
        this.players.remove(name.toLowerCase());
    }
    
    public void removePlayer(final LocalPlayer player) {
        this.players.remove(player.getName().toLowerCase());
    }
    
    public void addGroup(final String name) {
        this.groups.add(name.toLowerCase());
    }
    
    public void removeGroup(final String name) {
        this.groups.remove(name.toLowerCase());
    }
    
    public Set<String> getGroups() {
        return this.groups;
    }
    
    public Set<String> getPlayers() {
        return this.players;
    }
    
    public boolean contains(final LocalPlayer player) {
        if (this.players.contains(player.getName().toLowerCase())) {
            return true;
        }
        for (final String group : this.groups) {
            if (player.hasGroup(group)) {
                return true;
            }
        }
        return false;
    }
    
    public int size() {
        return this.groups.size() + this.players.size();
    }
    
    public String toPlayersString() {
        final StringBuilder str = new StringBuilder();
        final Iterator<String> it = this.players.iterator();
        while (it.hasNext()) {
            str.append(it.next());
            if (it.hasNext()) {
                str.append(", ");
            }
        }
        return str.toString();
    }
    
    public String toGroupsString() {
        final StringBuilder str = new StringBuilder();
        final Iterator<String> it = this.groups.iterator();
        while (it.hasNext()) {
            str.append("*");
            str.append(it.next());
            if (it.hasNext()) {
                str.append(", ");
            }
        }
        return str.toString();
    }
    
    public String toUserFriendlyString() {
        final StringBuilder str = new StringBuilder();
        if (this.players.size() > 0) {
            str.append(this.toPlayersString());
        }
        if (this.groups.size() > 0) {
            if (str.length() > 0) {
                str.append("; ");
            }
            str.append(this.toGroupsString());
        }
        return str.toString();
    }
}
