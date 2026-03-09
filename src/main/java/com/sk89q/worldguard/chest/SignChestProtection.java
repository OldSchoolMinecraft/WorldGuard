package com.sk89q.worldguard.chest;

import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.block.*;

public class SignChestProtection implements ChestProtection
{
    public boolean isProtected(final Block block, final Player player) {
        if (this.isChest(block.getType())) {
            final Block below = block.getRelative(0, -1, 0);
            return this.isProtectedSignAround(below, player);
        }
        if (block.getType() == Material.SIGN_POST) {
            return this.isProtectedSignAndChestBinary(block, player);
        }
        final Block above = block.getRelative(0, 1, 0);
        final Boolean res = this.isProtectedSign(above, player);
        return res != null && res;
    }
    
    public boolean isProtectedPlacement(final Block block, final Player player) {
        return this.isProtectedSignAround(block, player);
    }
    
    private boolean isProtectedSignAround(final Block searchBlock, final Player player) {
        Block side = searchBlock;
        Boolean res = this.isProtectedSign(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(-1, 0, 0);
        res = this.isProtectedSignAndChest(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(1, 0, 0);
        res = this.isProtectedSignAndChest(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(0, 0, -1);
        res = this.isProtectedSignAndChest(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(0, 0, 1);
        res = this.isProtectedSignAndChest(side, player);
        return res != null && res && res;
    }
    
    private Boolean isProtectedSign(final Sign sign, final Player player) {
        if (!sign.getLine(0).equalsIgnoreCase("[Lock]")) {
            return null;
        }
        if (player == null) {
            return true;
        }
        final String name = player.getName();
        if (name.equalsIgnoreCase(sign.getLine(1).trim()) || name.equalsIgnoreCase(sign.getLine(2).trim()) || name.equalsIgnoreCase(sign.getLine(3).trim())) {
            return false;
        }
        return true;
    }
    
    private Boolean isProtectedSign(final Block block, final Player player) {
        final BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) {
            return null;
        }
        return this.isProtectedSign((Sign)state, player);
    }
    
    private Boolean isProtectedSignAndChest(final Block block, final Player player) {
        if (!this.isChest(block.getRelative(0, 1, 0).getType())) {
            return null;
        }
        return this.isProtectedSign(block, player);
    }
    
    private boolean isProtectedSignAndChestBinary(final Block block, final Player player) {
        final Boolean res = this.isProtectedSignAndChest(block, player);
        return res != null && res;
    }
    
    public boolean isAdjacentChestProtected(final Block searchBlock, final Player player) {
        Block side = searchBlock;
        Boolean res = this.isProtected(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(-1, 0, 0);
        res = this.isProtected(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(1, 0, 0);
        res = this.isProtected(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(0, 0, -1);
        res = this.isProtected(side, player);
        if (res != null && res) {
            return res;
        }
        side = searchBlock.getRelative(0, 0, 1);
        res = this.isProtected(side, player);
        return res != null && res && res;
    }
    
    public boolean isChest(final Material material) {
        return material == Material.CHEST || material == Material.DISPENSER || material == Material.FURNACE || material == Material.BURNING_FURNACE;
    }
}
