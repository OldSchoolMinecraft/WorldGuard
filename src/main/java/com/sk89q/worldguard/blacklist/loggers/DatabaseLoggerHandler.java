package com.sk89q.worldguard.blacklist.loggers;

import com.sk89q.worldguard.*;
import com.sk89q.worldedit.*;
import java.util.logging.*;
import java.sql.*;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.*;

public class DatabaseLoggerHandler implements BlacklistLoggerHandler
{
    private static final Logger logger;
    private String dsn;
    private String user;
    private String pass;
    private String table;
    private String worldName;
    private Connection conn;
    
    public DatabaseLoggerHandler(final String dsn, final String user, final String pass, final String table, final String worldName) {
        this.dsn = dsn;
        this.user = user;
        this.pass = pass;
        this.table = table;
        this.worldName = worldName;
    }
    
    private Connection getConnection() throws SQLException {
        if (this.conn == null || this.conn.isClosed()) {
            this.conn = DriverManager.getConnection(this.dsn, this.user, this.pass);
        }
        return this.conn;
    }
    
    private void logEvent(final String event, final LocalPlayer player, final Vector pos, final int item, final String comment) {
        try {
            final Connection conn = this.getConnection();
            final PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + this.table + "(event, world, player, x, y, z, item, time, comment) VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, event);
            stmt.setString(2, this.worldName);
            stmt.setString(3, player.getName());
            stmt.setInt(4, pos.getBlockX());
            stmt.setInt(5, pos.getBlockY());
            stmt.setInt(6, pos.getBlockZ());
            stmt.setInt(7, item);
            stmt.setInt(8, (int)(System.currentTimeMillis() / 1000L));
            stmt.setString(9, comment);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            DatabaseLoggerHandler.logger.log(Level.SEVERE, "Failed to log blacklist event to database: " + e.getMessage());
        }
    }
    
    public void logEvent(final BlacklistEvent event, final String comment) {
        if (event instanceof BlockBreakBlacklistEvent) {
            final BlockBreakBlacklistEvent evt = (BlockBreakBlacklistEvent)event;
            this.logEvent("BREAK", evt.getPlayer(), evt.getPosition(), evt.getType(), comment);
        }
        else if (event instanceof BlockPlaceBlacklistEvent) {
            final BlockPlaceBlacklistEvent evt2 = (BlockPlaceBlacklistEvent)event;
            this.logEvent("PLACE", evt2.getPlayer(), evt2.getPosition(), evt2.getType(), comment);
        }
        else if (event instanceof BlockPlaceBlacklistEvent) {
            final BlockPlaceBlacklistEvent evt2 = (BlockPlaceBlacklistEvent)event;
            this.logEvent("INTERACT", evt2.getPlayer(), evt2.getPosition(), evt2.getType(), comment);
        }
        else if (event instanceof DestroyWithBlacklistEvent) {
            final DestroyWithBlacklistEvent evt3 = (DestroyWithBlacklistEvent)event;
            this.logEvent("DESTROY_WITH", evt3.getPlayer(), evt3.getPosition(), evt3.getType(), comment);
        }
        else if (event instanceof ItemAcquireBlacklistEvent) {
            final ItemAcquireBlacklistEvent evt4 = (ItemAcquireBlacklistEvent)event;
            this.logEvent("ACQUIRE", evt4.getPlayer(), evt4.getPlayer().getPosition(), evt4.getType(), comment);
        }
        else if (event instanceof ItemDropBlacklistEvent) {
            final ItemDropBlacklistEvent evt5 = (ItemDropBlacklistEvent)event;
            this.logEvent("DROP", evt5.getPlayer(), evt5.getPlayer().getPosition(), evt5.getType(), comment);
        }
        else if (event instanceof ItemUseBlacklistEvent) {
            final ItemUseBlacklistEvent evt6 = (ItemUseBlacklistEvent)event;
            this.logEvent("USE", evt6.getPlayer(), evt6.getPlayer().getPosition(), evt6.getType(), comment);
        }
        else {
            this.logEvent("UNKNOWN", event.getPlayer(), event.getPlayer().getPosition(), -1, comment);
        }
    }
    
    public void close() {
        try {
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
            }
        }
        catch (SQLException ex) {}
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
