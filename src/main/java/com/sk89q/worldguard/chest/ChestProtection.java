package com.sk89q.worldguard.chest;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.*;

public interface ChestProtection
{
    boolean isProtected(final Block p0, final Player p1);
    
    boolean isProtectedPlacement(final Block p0, final Player p1);
    
    boolean isAdjacentChestProtected(final Block p0, final Player p1);
    
    boolean isChest(final Material p0);
}
