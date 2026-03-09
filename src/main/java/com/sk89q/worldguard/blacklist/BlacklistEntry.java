package com.sk89q.worldguard.blacklist;

import com.sk89q.worldguard.*;
import java.util.*;
import com.sk89q.worldguard.blacklist.events.*;
import org.bukkit.*;
import com.sk89q.worldedit.blocks.*;

public class BlacklistEntry
{
    private Blacklist blacklist;
    private Set<String> ignoreGroups;
    private Set<String> ignorePermissions;
    private String[] breakActions;
    private String[] destroyWithActions;
    private String[] placeActions;
    private String[] interactActions;
    private String[] useActions;
    private String[] dropActions;
    private String[] acquireActions;
    private String message;
    private String comment;
    
    public BlacklistEntry(final Blacklist blacklist) {
        this.blacklist = blacklist;
    }
    
    public String[] getIgnoreGroups() {
        return this.ignoreGroups.toArray(new String[this.ignoreGroups.size()]);
    }
    
    public String[] getIgnorePermissions() {
        return this.ignorePermissions.toArray(new String[this.ignorePermissions.size()]);
    }
    
    public void setIgnoreGroups(final String[] ignoreGroups) {
        final Set<String> ignoreGroupsSet = new HashSet<String>();
        for (final String group : ignoreGroups) {
            ignoreGroupsSet.add(group.toLowerCase());
        }
        this.ignoreGroups = ignoreGroupsSet;
    }
    
    public void setIgnorePermissions(final String[] ignorePermissions) {
        final Set<String> ignorePermissionsSet = new HashSet<String>();
        for (final String perm : ignorePermissions) {
            ignorePermissionsSet.add(perm);
        }
        this.ignorePermissions = ignorePermissionsSet;
    }
    
    public String[] getBreakActions() {
        return this.breakActions;
    }
    
    public void setBreakActions(final String[] actions) {
        this.breakActions = actions;
    }
    
    public String[] getDestroyWithActions() {
        return this.destroyWithActions;
    }
    
    public void setDestroyWithActions(final String[] actions) {
        this.destroyWithActions = actions;
    }
    
    public String[] getPlaceActions() {
        return this.placeActions;
    }
    
    public void setPlaceActions(final String[] actions) {
        this.placeActions = actions;
    }
    
    public String[] getInteractActions() {
        return this.interactActions;
    }
    
    public void setInteractActions(final String[] actions) {
        this.interactActions = actions;
    }
    
    public String[] getUseActions() {
        return this.useActions;
    }
    
    public void setUseActions(final String[] actions) {
        this.useActions = actions;
    }
    
    public String[] getDropActions() {
        return this.dropActions;
    }
    
    public void setDropActions(final String[] actions) {
        this.dropActions = actions;
    }
    
    public String[] getAcquireActions() {
        return this.acquireActions;
    }
    
    public void setAcquireActions(final String[] actions) {
        this.acquireActions = actions;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(final String message) {
        this.message = message;
    }
    
    public String getComment() {
        return this.comment;
    }
    
    public void setComment(final String comment) {
        this.comment = comment;
    }
    
    public boolean shouldIgnore(final LocalPlayer player) {
        if (this.ignoreGroups != null) {
            for (final String group : player.getGroups()) {
                if (this.ignoreGroups.contains(group.toLowerCase())) {
                    return true;
                }
            }
        }
        if (this.ignorePermissions != null) {
            for (final String perm : this.ignorePermissions) {
                if (player.hasPermission(perm)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private String[] getActions(final BlacklistEvent event) {
        if (event instanceof BlockBreakBlacklistEvent) {
            return this.breakActions;
        }
        if (event instanceof BlockPlaceBlacklistEvent) {
            return this.placeActions;
        }
        if (event instanceof BlockInteractBlacklistEvent) {
            return this.interactActions;
        }
        if (event instanceof DestroyWithBlacklistEvent) {
            return this.destroyWithActions;
        }
        if (event instanceof ItemAcquireBlacklistEvent) {
            return this.acquireActions;
        }
        if (event instanceof ItemDropBlacklistEvent) {
            return this.dropActions;
        }
        if (event instanceof ItemUseBlacklistEvent) {
            return this.useActions;
        }
        return null;
    }
    
    public boolean check(final boolean useAsWhitelist, final BlacklistEvent event, final boolean forceRepeat, final boolean silent) {
        final LocalPlayer player = event.getPlayer();
        if (this.shouldIgnore(player)) {
            return true;
        }
        final String name = player.getName();
        final long now = System.currentTimeMillis();
        boolean repeating = false;
        final BlacklistTrackedEvent tracked = this.blacklist.lastAffected.get(name);
        if (tracked != null) {
            if (tracked.matches(event, now)) {
                repeating = true;
            }
        }
        else {
            this.blacklist.lastAffected.put(name, new BlacklistTrackedEvent(event, now));
        }
        final String[] actions = this.getActions(event);
        boolean ret = !useAsWhitelist;
        if (actions == null) {
            return !useAsWhitelist;
        }
        for (final String action : actions) {
            if (action.equalsIgnoreCase("deny")) {
                if (silent) {
                    return false;
                }
                ret = false;
            }
            else if (action.equalsIgnoreCase("allow")) {
                if (silent) {
                    return true;
                }
                ret = true;
            }
            else if (action.equalsIgnoreCase("kick")) {
                if (!silent) {
                    if (this.message != null) {
                        player.kick(String.format(this.message, getFriendlyItemName(event.getType())));
                    }
                    else {
                        player.kick("You can't " + event.getDescription() + " " + getFriendlyItemName(event.getType()));
                    }
                }
            }
            else if (action.equalsIgnoreCase("ban")) {
                if (!silent) {
                    if (this.message != null) {
                        player.ban("Banned: " + String.format(this.message, getFriendlyItemName(event.getType())));
                    }
                    else {
                        player.ban("Banned: You can't " + event.getDescription() + " " + getFriendlyItemName(event.getType()));
                    }
                }
            }
            else if (!silent && (!repeating || forceRepeat)) {
                if (action.equalsIgnoreCase("notify")) {
                    this.blacklist.notify(event, this.comment);
                }
                else if (action.equalsIgnoreCase("log")) {
                    this.blacklist.getLogger().logEvent(event, this.comment);
                }
                else if (action.equalsIgnoreCase("tell")) {
                    if (this.message != null) {
                        player.printRaw(ChatColor.YELLOW + String.format(this.message, getFriendlyItemName(event.getType())) + ".");
                    }
                    else {
                        player.printRaw(ChatColor.YELLOW + "You're not allowed to " + event.getDescription() + " " + getFriendlyItemName(event.getType()) + ".");
                    }
                }
            }
        }
        return ret;
    }
    
    private static String getFriendlyItemName(final int id) {
        final ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        }
        return "#" + id + "";
    }
}
