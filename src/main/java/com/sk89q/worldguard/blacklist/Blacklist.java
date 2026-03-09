package com.sk89q.worldguard.blacklist;

import com.sk89q.worldguard.blacklist.events.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.bukkit.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldedit.blocks.*;

public abstract class Blacklist
{
    private static final Logger logger;
    private Map<Integer, List<BlacklistEntry>> blacklist;
    private BlacklistLogger blacklistLogger;
    private BlacklistEvent lastEvent;
    Map<String, BlacklistTrackedEvent> lastAffected;
    private boolean useAsWhitelist;
    
    public Blacklist(final Boolean useAsWhitelist) {
        this.blacklist = new HashMap<Integer, List<BlacklistEntry>>();
        this.blacklistLogger = new BlacklistLogger();
        this.lastAffected = new HashMap<String, BlacklistTrackedEvent>();
        this.useAsWhitelist = useAsWhitelist;
    }
    
    public boolean isEmpty() {
        return this.blacklist.isEmpty();
    }
    
    public List<BlacklistEntry> getEntries(final int id) {
        return this.blacklist.get(id);
    }
    
    public int getItemCount() {
        return this.blacklist.size();
    }
    
    public boolean isWhitelist() {
        return this.useAsWhitelist;
    }
    
    public BlacklistLogger getLogger() {
        return this.blacklistLogger;
    }
    
    public boolean check(final BlacklistEvent event, final boolean forceRepeat, final boolean silent) {
        final List<BlacklistEntry> entries = this.getEntries(event.getType());
        if (entries == null) {
            return true;
        }
        boolean ret = true;
        for (final BlacklistEntry entry : entries) {
            if (!entry.check(this.useAsWhitelist, event, forceRepeat, silent)) {
                ret = false;
            }
        }
        return ret;
    }
    
    public void load(final File file) throws IOException {
        FileReader input = null;
        final Map<Integer, List<BlacklistEntry>> blacklist = new HashMap<Integer, List<BlacklistEntry>>();
        try {
            input = new FileReader(file);
            final BufferedReader buff = new BufferedReader(input);
            List<BlacklistEntry> currentEntries = null;
            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (line.charAt(0) == ';') {
                    continue;
                }
                if (line.charAt(0) == '#') {
                    continue;
                }
                if (line.matches("^\\[.*\\]$")) {
                    final String[] items = line.substring(1, line.length() - 1).split(",");
                    currentEntries = new ArrayList<BlacklistEntry>();
                    for (final String item : items) {
                        int id = 0;
                        try {
                            id = Integer.parseInt(item.trim());
                        }
                        catch (NumberFormatException e) {
                            id = getItemID(item.trim());
                            if (id == 0) {
                                Blacklist.logger.log(Level.WARNING, "WorldGuard: Unknown block name: " + item);
                                break;
                            }
                        }
                        final BlacklistEntry entry = new BlacklistEntry(this);
                        if (blacklist.containsKey(id)) {
                            blacklist.get(id).add(entry);
                        }
                        else {
                            final List<BlacklistEntry> entries = new ArrayList<BlacklistEntry>();
                            entries.add(entry);
                            blacklist.put(id, entries);
                        }
                        currentEntries.add(entry);
                    }
                }
                else if (currentEntries != null) {
                    final String[] parts = line.split("=");
                    if (parts.length == 1) {
                        Blacklist.logger.log(Level.WARNING, "Found option with no value " + file.getName() + " for '" + line + "'");
                    }
                    else {
                        boolean unknownOption = false;
                        for (final BlacklistEntry entry2 : currentEntries) {
                            if (parts[0].equalsIgnoreCase("ignore-groups")) {
                                entry2.setIgnoreGroups(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("ignore-perms")) {
                                entry2.setIgnorePermissions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("on-break")) {
                                entry2.setBreakActions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("on-destroy-with")) {
                                entry2.setDestroyWithActions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("on-place")) {
                                entry2.setPlaceActions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("on-interact")) {
                                entry2.setInteractActions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("on-use")) {
                                entry2.setUseActions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("on-drop")) {
                                entry2.setDropActions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("on-acquire")) {
                                entry2.setAcquireActions(parts[1].split(","));
                            }
                            else if (parts[0].equalsIgnoreCase("message")) {
                                entry2.setMessage(parts[1].trim());
                            }
                            else if (parts[0].equalsIgnoreCase("comment")) {
                                entry2.setComment(parts[1].trim());
                            }
                            else {
                                unknownOption = true;
                            }
                        }
                        if (!unknownOption) {
                            continue;
                        }
                        Blacklist.logger.log(Level.WARNING, "Unknown option '" + parts[0] + "' in " + file.getName() + " for '" + line + "'");
                    }
                }
                else {
                    Blacklist.logger.log(Level.WARNING, "Found option with no heading " + file.getName() + " for '" + line + "'");
                }
            }
            this.blacklist = blacklist;
        }
        finally {
            try {
                if (input != null) {
                    input.close();
                }
            }
            catch (IOException ex) {}
        }
    }
    
    public BlacklistEvent getLastEvent() {
        return this.lastEvent;
    }
    
    public void notify(final BlacklistEvent event, final String comment) {
        this.lastEvent = event;
        this.broadcastNotification(ChatColor.GRAY + "WG: " + ChatColor.LIGHT_PURPLE + event.getPlayer().getName() + ChatColor.GOLD + " (" + event.getDescription() + ") " + ChatColor.WHITE + getFriendlyItemName(event.getType()) + ((comment != null) ? (" (" + comment + ")") : "") + ".");
    }
    
    public abstract void broadcastNotification(final String p0);
    
    public void forgetPlayer(final LocalPlayer player) {
        this.lastAffected.remove(player.getName());
    }
    
    public void forgetAllPlayers() {
        this.lastAffected.clear();
    }
    
    private static int getItemID(final String name) {
        final ItemType type = ItemType.lookup(name);
        if (type != null) {
            return type.getID();
        }
        return -1;
    }
    
    private static String getFriendlyItemName(final int id) {
        final ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        }
        return "#" + id + "";
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
