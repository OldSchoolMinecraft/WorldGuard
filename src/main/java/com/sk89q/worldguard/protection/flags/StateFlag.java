package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.command.*;

public class StateFlag extends Flag<StateFlag.State>
{
    private boolean def;
    private RegionGroupFlag groupFlag;
    
    public StateFlag(final String name, final char legacyCode, final boolean def) {
        super(name, legacyCode);
        this.def = def;
    }
    
    public StateFlag(final String name, final boolean def) {
        super(name);
        this.def = def;
    }
    
    public boolean getDefault() {
        return this.def;
    }
    
    public RegionGroupFlag getGroupFlag() {
        return this.groupFlag;
    }
    
    public void setGroupFlag(final RegionGroupFlag groupFlag) {
        this.groupFlag = groupFlag;
    }
    
    @Override
    public State parseInput(final WorldGuardPlugin plugin, final CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();
        if (input.equalsIgnoreCase("allow")) {
            return State.ALLOW;
        }
        if (input.equalsIgnoreCase("deny")) {
            return State.DENY;
        }
        if (input.equalsIgnoreCase("none")) {
            return null;
        }
        throw new InvalidFlagFormat("Not none/allow/deny: " + input);
    }
    
    @Override
    public State unmarshal(final Object o) {
        final String str = o.toString();
        if (str.equalsIgnoreCase("allow")) {
            return State.ALLOW;
        }
        if (str.equalsIgnoreCase("deny")) {
            return State.DENY;
        }
        return null;
    }
    
    @Override
    public Object marshal(final State o) {
        if (o == State.ALLOW) {
            return "allow";
        }
        if (o == State.DENY) {
            return "deny";
        }
        return null;
    }
    
    public enum State
    {
        ALLOW, 
        DENY;
    }
}
