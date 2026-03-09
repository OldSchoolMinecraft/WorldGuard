package com.sk89q.worldguard.blacklist.events;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;

public class ItemUseBlacklistEvent extends ItemBlacklistEvent
{
    public ItemUseBlacklistEvent(final LocalPlayer player, final Vector pos, final int type) {
        super(player, pos, type);
    }
    
    @Override
    public String getDescription() {
        return "use";
    }
}
