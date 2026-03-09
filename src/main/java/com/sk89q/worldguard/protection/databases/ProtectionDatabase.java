package com.sk89q.worldguard.protection.databases;

import java.io.*;
import com.sk89q.worldguard.protection.managers.*;
import java.util.*;
import com.sk89q.worldguard.protection.regions.*;

public interface ProtectionDatabase
{
    void load() throws IOException;
    
    void save() throws IOException;
    
    void load(final RegionManager p0) throws IOException;
    
    void save(final RegionManager p0) throws IOException;
    
    Map<String, ProtectedRegion> getRegions();
    
    void setRegions(final Map<String, ProtectedRegion> p0);
}
