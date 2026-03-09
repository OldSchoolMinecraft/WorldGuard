package com.sk89q.worldguard.bukkit;

import org.bukkit.*;

public class SpongeUtil
{
    public static void clearSpongeWater(final WorldGuardPlugin plugin, final World world, final int ox, final int oy, final int oz) {
        final ConfigurationManager cfg = plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        for (int cx = -wcfg.spongeRadius; cx <= wcfg.spongeRadius; ++cx) {
            for (int cy = -wcfg.spongeRadius; cy <= wcfg.spongeRadius; ++cy) {
                for (int cz = -wcfg.spongeRadius; cz <= wcfg.spongeRadius; ++cz) {
                    if (BukkitUtil.isBlockWater(world, ox + cx, oy + cy, oz + cz)) {
                        world.getBlockAt(ox + cx, oy + cy, oz + cz).setTypeId(0);
                    }
                }
            }
        }
    }
    
    public static void addSpongeWater(final WorldGuardPlugin plugin, final World world, final int ox, final int oy, final int oz) {
        final ConfigurationManager cfg = plugin.getGlobalStateManager();
        final WorldConfiguration wcfg = cfg.get(world);
        int cx = ox - wcfg.spongeRadius - 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; ++cy) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; ++cz) {
                if (BukkitUtil.isBlockWater(world, cx, cy, cz)) {
                    BukkitUtil.setBlockToWater(world, cx + 1, cy, cz);
                }
            }
        }
        cx = ox + wcfg.spongeRadius + 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; ++cy) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; ++cz) {
                if (BukkitUtil.isBlockWater(world, cx, cy, cz)) {
                    BukkitUtil.setBlockToWater(world, cx - 1, cy, cz);
                }
            }
        }
        int cy = oy - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; ++cx) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; ++cz) {
                if (BukkitUtil.isBlockWater(world, cx, cy, cz)) {
                    BukkitUtil.setBlockToWater(world, cx, cy + 1, cz);
                }
            }
        }
        cy = oy + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; ++cx) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; ++cz) {
                if (BukkitUtil.isBlockWater(world, cx, cy, cz)) {
                    BukkitUtil.setBlockToWater(world, cx, cy - 1, cz);
                }
            }
        }
        int cz = oz - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; ++cx) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; ++cy) {
                if (BukkitUtil.isBlockWater(world, cx, cy, cz)) {
                    BukkitUtil.setBlockToWater(world, cx, cy, cz + 1);
                }
            }
        }
        cz = oz + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; ++cx) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; ++cy) {
                if (BukkitUtil.isBlockWater(world, cx, cy, cz)) {
                    BukkitUtil.setBlockToWater(world, cx, cy, cz - 1);
                }
            }
        }
    }
}
