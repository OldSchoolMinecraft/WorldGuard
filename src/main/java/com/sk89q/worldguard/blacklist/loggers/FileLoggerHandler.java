package com.sk89q.worldguard.blacklist.loggers;

import java.text.*;
import java.util.regex.*;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.*;
import java.io.*;
import java.util.logging.*;
import java.util.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.*;
import com.sk89q.worldedit.blocks.*;

public class FileLoggerHandler implements BlacklistLoggerHandler
{
    private static final Logger logger;
    private static Pattern pattern;
    private static SimpleDateFormat dateFormat;
    private int cacheSize;
    private String pathPattern;
    private String worldName;
    private TreeMap<String, FileLoggerWriter> writers;
    
    public FileLoggerHandler(final String pathPattern, final String worldName) {
        this.cacheSize = 10;
        this.writers = new TreeMap<String, FileLoggerWriter>();
        this.pathPattern = pathPattern;
        this.worldName = worldName;
    }
    
    public FileLoggerHandler(final String pathPattern, final int cacheSize, final String worldName) {
        this.cacheSize = 10;
        this.writers = new TreeMap<String, FileLoggerWriter>();
        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size cannot be less than 1");
        }
        this.pathPattern = pathPattern;
        this.cacheSize = cacheSize;
        this.worldName = worldName;
    }
    
    private String buildPath(final String playerName) {
        final GregorianCalendar calendar = new GregorianCalendar();
        final Matcher m = FileLoggerHandler.pattern.matcher(this.pathPattern);
        final StringBuffer buffer = new StringBuffer();
        while (m.find()) {
            final String group = m.group();
            String rep = "?";
            if (group.matches("%%")) {
                rep = "%";
            }
            else if (group.matches("%u")) {
                rep = playerName.toLowerCase().replaceAll("[^A-Za-z0-9_]", "_");
                if (rep.length() > 32) {
                    rep = rep.substring(0, 32);
                }
            }
            else if (group.matches("%w")) {
                rep = this.worldName.toLowerCase().replaceAll("[^A-Za-z0-9_]", "_");
                if (rep.length() > 32) {
                    rep = rep.substring(0, 32);
                }
            }
            else if (group.matches("%Y")) {
                rep = String.valueOf(calendar.get(1));
            }
            else if (group.matches("%m")) {
                rep = String.format("%02d", calendar.get(2));
            }
            else if (group.matches("%d")) {
                rep = String.format("%02d", calendar.get(5));
            }
            else if (group.matches("%W")) {
                rep = String.format("%02d", calendar.get(3));
            }
            else if (group.matches("%H")) {
                rep = String.format("%02d", calendar.get(11));
            }
            else if (group.matches("%h")) {
                rep = String.format("%02d", calendar.get(10));
            }
            else if (group.matches("%i")) {
                rep = String.format("%02d", calendar.get(12));
            }
            else if (group.matches("%s")) {
                rep = String.format("%02d", calendar.get(13));
            }
            m.appendReplacement(buffer, rep);
        }
        m.appendTail(buffer);
        return buffer.toString();
    }
    
    private void log(final LocalPlayer player, final String message, final String comment) {
        final String path = this.buildPath(player.getName());
        try {
            final String date = FileLoggerHandler.dateFormat.format(new Date());
            final String line = "[" + date + "] " + player.getName() + ": " + message + ((comment != null) ? (" (" + comment + ")") : "") + "\r\n";
            FileLoggerWriter writer = this.writers.get(path);
            if (writer != null) {
                try {
                    final BufferedWriter out = writer.getWriter();
                    out.write(line);
                    out.flush();
                    writer.updateLastUse();
                    return;
                }
                catch (IOException ex) {}
            }
            final File file = new File(path);
            final File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            final FileWriter stream = new FileWriter(path, true);
            final BufferedWriter out2 = new BufferedWriter(stream);
            out2.write(line);
            out2.flush();
            writer = new FileLoggerWriter(path, out2);
            this.writers.put(path, writer);
            if (this.writers.size() > this.cacheSize) {
                final Iterator<Map.Entry<String, FileLoggerWriter>> it = this.writers.entrySet().iterator();
                while (it.hasNext()) {
                    final Map.Entry<String, FileLoggerWriter> entry = it.next();
                    try {
                        entry.getValue().getWriter().close();
                    }
                    catch (IOException ex2) {}
                    it.remove();
                    if (this.writers.size() <= this.cacheSize) {
                        break;
                    }
                }
            }
        }
        catch (IOException e) {
            FileLoggerHandler.logger.log(Level.WARNING, "Failed to log blacklist event to '" + path + "': " + e.getMessage());
        }
    }
    
    private String getCoordinates(final Vector pos) {
        return "@" + pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ();
    }
    
    private void logEvent(final BlacklistEvent event, final String text, final int id, final Vector pos, final String comment) {
        this.log(event.getPlayer(), "Tried to " + text + " " + getFriendlyItemName(id) + " " + this.getCoordinates(pos), comment);
    }
    
    public void logEvent(final BlacklistEvent event, final String comment) {
        if (event instanceof BlockBreakBlacklistEvent) {
            final BlockBreakBlacklistEvent evt = (BlockBreakBlacklistEvent)event;
            this.logEvent(event, "break", evt.getType(), evt.getPosition(), comment);
        }
        else if (event instanceof BlockPlaceBlacklistEvent) {
            final BlockPlaceBlacklistEvent evt2 = (BlockPlaceBlacklistEvent)event;
            this.logEvent(event, "place", evt2.getType(), evt2.getPosition(), comment);
        }
        else if (event instanceof BlockInteractBlacklistEvent) {
            final BlockInteractBlacklistEvent evt3 = (BlockInteractBlacklistEvent)event;
            this.logEvent(event, "interact with", evt3.getType(), evt3.getPosition(), comment);
        }
        else if (event instanceof DestroyWithBlacklistEvent) {
            final DestroyWithBlacklistEvent evt4 = (DestroyWithBlacklistEvent)event;
            this.logEvent(event, "destroy with", evt4.getType(), evt4.getPosition(), comment);
        }
        else if (event instanceof ItemAcquireBlacklistEvent) {
            final ItemAcquireBlacklistEvent evt5 = (ItemAcquireBlacklistEvent)event;
            this.logEvent(event, "acquire", evt5.getType(), evt5.getPosition(), comment);
        }
        else if (event instanceof ItemDropBlacklistEvent) {
            final ItemDropBlacklistEvent evt6 = (ItemDropBlacklistEvent)event;
            this.logEvent(event, "drop", evt6.getType(), evt6.getPosition(), comment);
        }
        else if (event instanceof ItemUseBlacklistEvent) {
            final ItemUseBlacklistEvent evt7 = (ItemUseBlacklistEvent)event;
            this.logEvent(event, "use", evt7.getType(), evt7.getPosition(), comment);
        }
        else {
            this.log(event.getPlayer(), "Unknown event: " + event.getClass().getCanonicalName(), comment);
        }
    }
    
    private static String getFriendlyItemName(final int id) {
        final ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        }
        return "#" + id + "";
    }
    
    public void close() {
        for (final Map.Entry<String, FileLoggerWriter> entry : this.writers.entrySet()) {
            try {
                entry.getValue().getWriter().close();
            }
            catch (IOException ex) {}
        }
        this.writers.clear();
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
        FileLoggerHandler.pattern = Pattern.compile("%.");
        FileLoggerHandler.dateFormat = new SimpleDateFormat("yyyyy-MM-dd HH:mm:ss");
    }
}
