package com.sk89q.worldguard.bukkit;

import java.util.logging.*;
import org.bukkit.*;
import com.sk89q.worldguard.protection.databases.*;
import com.sk89q.worldguard.protection.managers.*;
import java.io.*;

public class LegacyWorldGuardMigration
{
    protected static final Logger logger;
    
    public static void migrateBlacklist(final WorldGuardPlugin plugin) {
        final World mainWorld = plugin.getServer().getWorlds().get(0);
        final String mainWorldName = mainWorld.getName();
        final String newPath = "worlds/" + mainWorldName + "/blacklist.txt";
        final File oldFile = new File(plugin.getDataFolder(), "blacklist.txt");
        final File newFile = new File(plugin.getDataFolder(), newPath);
        if (!newFile.exists() && oldFile.exists()) {
            LegacyWorldGuardMigration.logger.warning("WorldGuard: WorldGuard will now update your blacklist from an older version of WorldGuard.");
            newFile.getParentFile().mkdirs();
            if (copyFile(oldFile, newFile)) {
                oldFile.renameTo(new File(plugin.getDataFolder(), "blacklist.txt.old"));
            }
            else {
                LegacyWorldGuardMigration.logger.warning("WorldGuard: blacklist.txt has been converted for the main world at " + newPath + "");
                LegacyWorldGuardMigration.logger.warning("WorldGuard: Your other worlds currently have no blacklist defined!");
            }
        }
    }
    
    public static void migrateRegions(final WorldGuardPlugin plugin) {
        try {
            final File oldDatabase = new File(plugin.getDataFolder(), "regions.txt");
            if (!oldDatabase.exists()) {
                return;
            }
            LegacyWorldGuardMigration.logger.info("WorldGuard: The regions database has changed in 4.x. Your old regions database will be converted to the new format and set as your primarily world's database.");
            final World w = plugin.getServer().getWorlds().get(0);
            final RegionManager mgr = plugin.getGlobalRegionManager().get(w);
            final CSVDatabase db = new CSVDatabase(oldDatabase);
            db.load();
            mgr.setRegions(db.getRegions());
            mgr.save();
            oldDatabase.renameTo(new File(plugin.getDataFolder(), "regions.txt.old"));
            LegacyWorldGuardMigration.logger.info("WorldGuard: Regions database converted!");
        }
        catch (FileNotFoundException e2) {}
        catch (IOException e) {
            LegacyWorldGuardMigration.logger.warning("WorldGuard: Failed to load regions: " + e.getMessage());
        }
    }
    
    private static boolean copyFile(final File from, final File to) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        }
        catch (FileNotFoundException ex) {}
        catch (IOException e) {}
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex2) {}
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex3) {}
            }
        }
        return false;
    }
    
    static {
        logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
