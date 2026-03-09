package com.sk89q.worldguard.protection.databases;

import java.util.logging.*;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.util.yaml.*;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldedit.*;
import java.io.*;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.domains.*;
import java.util.*;

public class YAMLDatabase extends AbstractProtectionDatabase
{
    private static Logger logger;
    private Configuration config;
    private Map<String, ProtectedRegion> regions;
    
    public YAMLDatabase(final File file) {
        this.config = new Configuration(file);
    }
    
    public void load() throws IOException {
        this.config.load();
        final Map<String, ConfigurationNode> regionData = this.config.getNodes("regions");
        if (regionData == null) {
            this.regions = new HashMap<String, ProtectedRegion>();
            return;
        }
        final Map<String, ProtectedRegion> regions = new HashMap<String, ProtectedRegion>();
        final Map<ProtectedRegion, String> parentSets = new LinkedHashMap<ProtectedRegion, String>();
        for (final Map.Entry<String, ConfigurationNode> entry : regionData.entrySet()) {
            final String id = entry.getKey().toLowerCase().replace(".", "");
            final ConfigurationNode node = entry.getValue();
            final String type = node.getString("type");
            try {
                if (type == null) {
                    YAMLDatabase.logger.warning("Undefined region type for region '" + id + '\"');
                }
                else {
                    ProtectedRegion region;
                    if (type.equals("cuboid")) {
                        final Vector pt1 = this.checkNonNull(node.getVector("min"));
                        final Vector pt2 = this.checkNonNull(node.getVector("max"));
                        final BlockVector min = Vector.getMinimum(pt1, pt2).toBlockVector();
                        final BlockVector max = Vector.getMaximum(pt1, pt2).toBlockVector();
                        region = new ProtectedCuboidRegion(id, min, max);
                    }
                    else if (type.equals("poly2d")) {
                        final Integer minY = this.checkNonNull(node.getInt("min-y"));
                        final Integer maxY = this.checkNonNull(node.getInt("max-y"));
                        final List<BlockVector2D> points = node.getBlockVector2dList("points", null);
                        region = new ProtectedPolygonalRegion(id, points, minY, maxY);
                    }
                    else {
                        if (!type.equals("global")) {
                            YAMLDatabase.logger.warning("Unknown region type for region '" + id + '\"');
                            continue;
                        }
                        region = new GlobalProtectedRegion(id);
                    }
                    final Integer priority = this.checkNonNull(node.getInt("priority"));
                    region.setPriority(priority);
                    this.setFlags(region, node.getNode("flags"));
                    region.setOwners(this.parseDomain(node.getNode("owners")));
                    region.setMembers(this.parseDomain(node.getNode("members")));
                    regions.put(id, region);
                    final String parentId = node.getString("parent");
                    if (parentId == null) {
                        continue;
                    }
                    parentSets.put(region, parentId);
                }
            }
            catch (NullPointerException e) {
                YAMLDatabase.logger.warning("Missing data for region '" + id + '\"');
            }
        }
        for (final Map.Entry<ProtectedRegion, String> entry2 : parentSets.entrySet()) {
            final ProtectedRegion parent = regions.get(entry2.getValue());
            if (parent != null) {
                try {
                    entry2.getKey().setParent(parent);
                }
                catch (ProtectedRegion.CircularInheritanceException e2) {
                    YAMLDatabase.logger.warning("Circular inheritance detect with '" + entry2.getValue() + "' detected as a parent");
                }
            }
            else {
                YAMLDatabase.logger.warning("Unknown region parent: " + entry2.getValue());
            }
        }
        this.regions = regions;
    }
    
    private <V> V checkNonNull(final V val) throws NullPointerException {
        if (val == null) {
            throw new NullPointerException();
        }
        return val;
    }
    
    private void setFlags(final ProtectedRegion region, final ConfigurationNode flagsData) {
        if (flagsData == null) {
            return;
        }
        for (final Flag<?> flag : DefaultFlag.getFlags()) {
            final Object o = flagsData.getProperty(flag.getName());
            if (o != null) {
                this.setFlag(region, flag, o);
            }
        }
    }
    
    private <T> void setFlag(final ProtectedRegion region, final Flag<T> flag, final Object rawValue) {
        final T val = flag.unmarshal(rawValue);
        if (val == null) {
            YAMLDatabase.logger.warning("Failed to parse flag '" + flag.getName() + "' with value '" + rawValue.toString() + "'");
            return;
        }
        region.setFlag(flag, val);
    }
    
    private DefaultDomain parseDomain(final ConfigurationNode node) {
        if (node == null) {
            return new DefaultDomain();
        }
        final DefaultDomain domain = new DefaultDomain();
        for (final String name : node.getStringList("players", null)) {
            domain.addPlayer(name);
        }
        for (final String name : node.getStringList("groups", null)) {
            domain.addGroup(name);
        }
        return domain;
    }
    
    public void save() throws IOException {
        this.config.clear();
        for (final Map.Entry<String, ProtectedRegion> entry : this.regions.entrySet()) {
            final ProtectedRegion region = entry.getValue();
            final ConfigurationNode node = this.config.addNode("regions." + entry.getKey());
            if (region instanceof ProtectedCuboidRegion) {
                final ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion)region;
                node.setProperty("type", "cuboid");
                node.setProperty("min", cuboid.getMinimumPoint());
                node.setProperty("max", cuboid.getMaximumPoint());
            }
            else if (region instanceof ProtectedPolygonalRegion) {
                final ProtectedPolygonalRegion poly = (ProtectedPolygonalRegion)region;
                node.setProperty("type", "poly2d");
                node.setProperty("min-y", poly.getMinimumPoint().getBlockY());
                node.setProperty("max-y", poly.getMaximumPoint().getBlockY());
                final List<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
                for (final BlockVector2D point : poly.getPoints()) {
                    final Map<String, Object> data = new HashMap<String, Object>();
                    data.put("x", point.getBlockX());
                    data.put("z", point.getBlockZ());
                    points.add(data);
                }
                node.setProperty("points", points);
            }
            else if (region instanceof GlobalProtectedRegion) {
                node.setProperty("type", "global");
            }
            else {
                node.setProperty("type", region.getClass().getCanonicalName());
            }
            node.setProperty("priority", region.getPriority());
            node.setProperty("flags", this.getFlagData(region));
            node.setProperty("owners", this.getDomainData(region.getOwners()));
            node.setProperty("members", this.getDomainData(region.getMembers()));
            final ProtectedRegion parent = region.getParent();
            if (parent != null) {
                node.setProperty("parent", parent.getId());
            }
        }
        this.config.setHeader("#\r\n# WorldGuard regions file\r\n#\r\n# WARNING: THIS FILE IS AUTOMATICALLY GENERATED. If you modify this file by\r\n# hand, be aware that A SINGLE MISTYPED CHARACTER CAN CORRUPT THE FILE. If\r\n# WorldGuard is unable to parse the file, your regions will FAIL TO LOAD and\r\n# the contents of this file will reset. Please use a YAML validator such as\r\n# http://yaml-online-parser.appspot.com (for smaller files).\r\n#\r\n# REMEMBER TO KEEP PERIODICAL BACKUPS.\r\n#");
        this.config.save();
    }
    
    private Map<String, Object> getFlagData(final ProtectedRegion region) {
        final Map<String, Object> flagData = new HashMap<String, Object>();
        for (final Map.Entry<Flag<?>, Object> entry : region.getFlags().entrySet()) {
            final Flag<?> flag = entry.getKey();
            this.addMarshalledFlag(flagData, flag, entry.getValue());
        }
        return flagData;
    }
    
    private <V> void addMarshalledFlag(final Map<String, Object> flagData, final Flag<V> flag, final Object val) {
        if (val == null) {
            return;
        }
        flagData.put(flag.getName(), flag.marshal((V)val));
    }
    
    private Map<String, Object> getDomainData(final DefaultDomain domain) {
        final Map<String, Object> domainData = new HashMap<String, Object>();
        this.setDomainData(domainData, "players", domain.getPlayers());
        this.setDomainData(domainData, "groups", domain.getGroups());
        return domainData;
    }
    
    private void setDomainData(final Map<String, Object> domainData, final String key, final Set<String> domain) {
        if (domain.size() == 0) {
            return;
        }
        final List<String> list = new ArrayList<String>();
        for (final String str : domain) {
            list.add(str);
        }
        domainData.put(key, list);
    }
    
    public Map<String, ProtectedRegion> getRegions() {
        return this.regions;
    }
    
    public void setRegions(final Map<String, ProtectedRegion> regions) {
        this.regions = regions;
    }
    
    static {
        YAMLDatabase.logger = Logger.getLogger("Minecraft.WorldGuard");
    }
}
