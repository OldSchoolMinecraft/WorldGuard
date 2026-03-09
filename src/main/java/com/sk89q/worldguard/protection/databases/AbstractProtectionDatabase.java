package com.sk89q.worldguard.protection.databases;

import com.sk89q.worldguard.protection.managers.*;
import java.io.*;

public abstract class AbstractProtectionDatabase implements ProtectionDatabase
{
    public void load(final RegionManager manager) throws IOException {
        this.load();
        manager.setRegions(this.getRegions());
    }
    
    public void save(final RegionManager manager) throws IOException {
        this.setRegions(manager.getRegions());
        this.save();
    }
}
