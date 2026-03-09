package com.sk89q.worldguard.domains;

import java.util.*;
import com.sk89q.worldguard.*;

public class PlayerDomain implements Domain
{
    private Set<String> players;
    
    public PlayerDomain() {
        this.players = new HashSet<String>();
    }
    
    public PlayerDomain(final String[] players) {
        this.players = new HashSet<String>();
        for (final String name : players) {
            this.players.add(name.toLowerCase());
        }
    }
    
    public void addPlayer(final String name) {
        this.players.add(name.toLowerCase());
    }
    
    public boolean contains(final LocalPlayer player) {
        return this.players.contains(player.getName().toLowerCase());
    }
    
    public int size() {
        return this.players.size();
    }
}
