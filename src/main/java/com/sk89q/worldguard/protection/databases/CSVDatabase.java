package com.sk89q.worldguard.protection.databases;

import java.util.logging.*;
import au.com.bytecode.opencsv.*;
import java.io.*;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.util.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldedit.*;
import java.util.*;
import com.sk89q.worldguard.domains.*;
import java.util.regex.*;
import com.sk89q.worldguard.protection.flags.*;

public class CSVDatabase extends AbstractProtectionDatabase
{
    private static Logger logger;
    private File file;
    private Map<String, ProtectedRegion> regions;
    
    public CSVDatabase(final File file) {
        this.file = file;
    }
    
    public void save() throws IOException {
        throw new UnsupportedOperationException("CSV format is no longer implemented");
    }
    
    public void load() throws IOException {
        final Map<String, ProtectedRegion> regions = new HashMap<String, ProtectedRegion>();
        final Map<ProtectedRegion, String> parentSets = new LinkedHashMap<ProtectedRegion, String>();
        final CSVReader reader = new CSVReader(new FileReader(this.file));
        try {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 2) {
                    CSVDatabase.logger.warning("Invalid region definition: " + line);
                }
                else {
                    final String id = line[0].toLowerCase().replace(".", "");
                    final String type = line[1];
                    final ArrayReader<String> entries = new ArrayReader<String>(line);
                    if (type.equalsIgnoreCase("cuboid")) {
                        if (line.length < 8) {
                            CSVDatabase.logger.warning("Invalid region definition: " + line);
                        }
                        else {
                            final Vector pt1 = new Vector(Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4]));
                            final Vector pt2 = new Vector(Integer.parseInt(line[5]), Integer.parseInt(line[6]), Integer.parseInt(line[7]));
                            final BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                            final BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                            final int priority = (entries.get(8) == null) ? 0 : Integer.parseInt(entries.get(8));
                            final String ownersData = entries.get(9);
                            final String flagsData = entries.get(10);
                            final ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
                            region.setPriority(priority);
                            this.parseFlags(region, flagsData);
                            region.setOwners(this.parseDomains(ownersData));
                            regions.put(id, region);
                        }
                    }
                    else {
                        if (!type.equalsIgnoreCase("cuboid.2")) {
                            continue;
                        }
                        final Vector pt1 = new Vector(Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4]));
                        final Vector pt2 = new Vector(Integer.parseInt(line[5]), Integer.parseInt(line[6]), Integer.parseInt(line[7]));
                        final BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                        final BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                        final int priority = (entries.get(8) == null) ? 0 : Integer.parseInt(entries.get(8));
                        final String parentId = entries.get(9);
                        final String ownersData2 = entries.get(10);
                        final String membersData = entries.get(11);
                        final String flagsData2 = entries.get(12);
                        final ProtectedRegion region2 = new ProtectedCuboidRegion(id, min, max);
                        region2.setPriority(priority);
                        this.parseFlags(region2, flagsData2);
                        region2.setOwners(this.parseDomains(ownersData2));
                        region2.setMembers(this.parseDomains(membersData));
                        regions.put(id, region2);
                        if (parentId.length() <= 0) {
                            continue;
                        }
                        parentSets.put(region2, parentId);
                    }
                }
            }
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException ex) {}
        }
        for (final Map.Entry<ProtectedRegion, String> entry : parentSets.entrySet()) {
            final ProtectedRegion parent = regions.get(entry.getValue());
            if (parent != null) {
                try {
                    entry.getKey().setParent(parent);
                }
                catch (ProtectedRegion.CircularInheritanceException e) {
                    CSVDatabase.logger.warning("Circular inheritance detect with '" + entry.getValue() + "' detected as a parent");
                }
            }
            else {
                CSVDatabase.logger.warning("Unknown region parent: " + entry.getValue());
            }
        }
        this.regions = regions;
    }
    
    private DefaultDomain parseDomains(final String data) {
        if (data == null) {
            return new DefaultDomain();
        }
        final DefaultDomain domain = new DefaultDomain();
        final Pattern pattern = Pattern.compile("^([A-Za-z]):(.*)$");
        final String[] arr$;
        final String[] parts = arr$ = data.split(",");
        for (final String part : arr$) {
            if (part.trim().length() != 0) {
                final Matcher matcher = pattern.matcher(part);
                if (!matcher.matches()) {
                    CSVDatabase.logger.warning("Invalid owner specification: " + part);
                }
                else {
                    final String type = matcher.group(1);
                    final String id = matcher.group(2);
                    if (type.equals("u")) {
                        domain.addPlayer(id);
                    }
                    else if (type.equals("g")) {
                        domain.addGroup(id);
                    }
                    else {
                        CSVDatabase.logger.warning("Unknown owner specification: " + type);
                    }
                }
            }
        }
        return domain;
    }
    
    private void parseFlags(final ProtectedRegion region, final String data) {
        if (data == null) {
            return;
        }
        StateFlag.State curState = StateFlag.State.ALLOW;
        for (int i = 0; i < data.length(); ++i) {
            final char k = data.charAt(i);
            if (k == '+') {
                curState = StateFlag.State.ALLOW;
            }
            else if (k == '-') {
                curState = StateFlag.State.DENY;
            }
            else if (k == '_') {
                if (i == data.length() - 1) {
                    CSVDatabase.logger.warning("_ read ahead fail");
                    break;
                }
                final String flagStr = "_" + data.charAt(i + 1);
                ++i;
                CSVDatabase.logger.warning("_? custom flags are no longer supported");
            }
            else {
                final String flagStr = String.valueOf(k);
                final StateFlag flag = DefaultFlag.getLegacyFlag(flagStr);
                if (flag != null) {
                    region.setFlag(flag, curState);
                }
                else {
                    CSVDatabase.logger.warning("Legacy flag '" + flagStr + "' is unsupported");
                }
            }
        }
    }
    
    protected String nullEmptyString(final String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return null;
        }
        return str;
    }
    
    public Map<String, ProtectedRegion> getRegions() {
        return this.regions;
    }
    
    public void setRegions(final Map<String, ProtectedRegion> regions) {
        this.regions = regions;
    }
    
    static {
        CSVDatabase.logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
