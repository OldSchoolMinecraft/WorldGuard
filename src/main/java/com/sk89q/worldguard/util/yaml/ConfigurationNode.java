package com.sk89q.worldguard.util.yaml;

import java.util.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;

public class ConfigurationNode
{
    protected Map<String, Object> root;
    
    protected ConfigurationNode(final Map<String, Object> root) {
        this.root = root;
    }
    
    public void clear() {
        this.root.clear();
    }
    
    public Object getProperty(final String path) {
        if (path.contains(".")) {
            final String[] parts = path.split("\\.");
            Map<String, Object> node = this.root;
            for (int i = 0; i < parts.length; ++i) {
                final Object o = node.get(parts[i]);
                if (o == null) {
                    return null;
                }
                if (i == parts.length - 1) {
                    return o;
                }
                try {
                    node = (Map<String, Object>)o;
                }
                catch (ClassCastException e) {
                    return null;
                }
            }
            return null;
        }
        final Object val = this.root.get(path);
        if (val == null) {
            return null;
        }
        return val;
    }
    
    private Object prepareSerialization(final Object value) {
        if (value instanceof Vector) {
            final Map<String, Double> out = new HashMap<String, Double>();
            final Vector vec = (Vector)value;
            out.put("x", vec.getX());
            out.put("y", vec.getY());
            out.put("z", vec.getZ());
            return out;
        }
        return value;
    }
    
    public void setProperty(final String path, Object value) {
        value = this.prepareSerialization(value);
        if (!path.contains(".")) {
            this.root.put(path, value);
            return;
        }
        final String[] parts = path.split("\\.");
        Map<String, Object> node = this.root;
        for (int i = 0; i < parts.length; ++i) {
            Object o = node.get(parts[i]);
            if (i == parts.length - 1) {
                node.put(parts[i], value);
                return;
            }
            if (o == null || !(o instanceof Map)) {
                o = new HashMap();
                node.put(parts[i], o);
            }
            node = (Map<String, Object>)o;
        }
    }
    
    public ConfigurationNode addNode(final String path) {
        final Map<String, Object> map = new HashMap<String, Object>();
        final ConfigurationNode node = new ConfigurationNode(map);
        this.setProperty(path, map);
        return node;
    }
    
    public String getString(final String path) {
        final Object o = this.getProperty(path);
        if (o == null) {
            return null;
        }
        return o.toString();
    }
    
    public Vector getVector(final String path) {
        final ConfigurationNode o = this.getNode(path);
        if (o == null) {
            return null;
        }
        final Double x = o.getDouble("x");
        final Double y = o.getDouble("y");
        final Double z = o.getDouble("z");
        if (x == null || y == null || z == null) {
            return null;
        }
        return new Vector((double)x, (double)y, (double)z);
    }
    
    public Vector2D getVector2d(final String path) {
        final ConfigurationNode o = this.getNode(path);
        if (o == null) {
            return null;
        }
        final Double x = o.getDouble("x");
        final Double z = o.getDouble("z");
        if (x == null || z == null) {
            return null;
        }
        return new Vector2D((double)x, (double)z);
    }
    
    public Vector getVector(final String path, final Vector def) {
        final Vector v = this.getVector(path);
        if (v == null) {
            this.setProperty(path, def);
            return def;
        }
        return v;
    }
    
    public String getString(final String path, final String def) {
        final String o = this.getString(path);
        if (o == null) {
            this.setProperty(path, def);
            return def;
        }
        return o;
    }
    
    public Integer getInt(final String path) {
        final Integer o = castInt(this.getProperty(path));
        if (o == null) {
            return null;
        }
        return o;
    }
    
    public int getInt(final String path, final int def) {
        final Integer o = castInt(this.getProperty(path));
        if (o == null) {
            this.setProperty(path, def);
            return def;
        }
        return o;
    }
    
    public Double getDouble(final String path) {
        final Double o = castDouble(this.getProperty(path));
        if (o == null) {
            return null;
        }
        return o;
    }
    
    public double getDouble(final String path, final double def) {
        final Double o = castDouble(this.getProperty(path));
        if (o == null) {
            this.setProperty(path, def);
            return def;
        }
        return o;
    }
    
    public Boolean getBoolean(final String path) {
        final Boolean o = castBoolean(this.getProperty(path));
        if (o == null) {
            return null;
        }
        return o;
    }
    
    public boolean getBoolean(final String path, final boolean def) {
        final Boolean o = castBoolean(this.getProperty(path));
        if (o == null) {
            this.setProperty(path, def);
            return def;
        }
        return o;
    }
    
    public List<String> getKeys(final String path) {
        if (path == null) {
            return new ArrayList<String>(this.root.keySet());
        }
        final Object o = this.getProperty(path);
        if (o == null) {
            return null;
        }
        if (o instanceof Map) {
            return new ArrayList<String>(((Map)o).keySet());
        }
        return null;
    }
    
    public List<Object> getList(final String path) {
        final Object o = this.getProperty(path);
        if (o == null) {
            return null;
        }
        if (o instanceof List) {
            return (List<Object>)o;
        }
        return null;
    }
    
    public List<String> getStringList(final String path, final List<String> def) {
        final List<Object> raw = this.getList(path);
        if (raw == null) {
            return (def != null) ? def : new ArrayList<String>();
        }
        final List<String> list = new ArrayList<String>();
        for (final Object o : raw) {
            if (o == null) {
                continue;
            }
            list.add(o.toString());
        }
        return list;
    }
    
    public List<Integer> getIntList(final String path, final List<Integer> def) {
        final List<Object> raw = this.getList(path);
        if (raw == null) {
            return (def != null) ? def : new ArrayList<Integer>();
        }
        final List<Integer> list = new ArrayList<Integer>();
        for (final Object o : raw) {
            final Integer i = castInt(o);
            if (i != null) {
                list.add(i);
            }
        }
        return list;
    }
    
    public List<Double> getDoubleList(final String path, final List<Double> def) {
        final List<Object> raw = this.getList(path);
        if (raw == null) {
            return (def != null) ? def : new ArrayList<Double>();
        }
        final List<Double> list = new ArrayList<Double>();
        for (final Object o : raw) {
            final Double i = castDouble(o);
            if (i != null) {
                list.add(i);
            }
        }
        return list;
    }
    
    public List<Boolean> getBooleanList(final String path, final List<Boolean> def) {
        final List<Object> raw = this.getList(path);
        if (raw == null) {
            return (def != null) ? def : new ArrayList<Boolean>();
        }
        final List<Boolean> list = new ArrayList<Boolean>();
        for (final Object o : raw) {
            final Boolean tetsu = castBoolean(o);
            if (tetsu != null) {
                list.add(tetsu);
            }
        }
        return list;
    }
    
    public List<Vector> getVectorList(final String path, final List<Vector> def) {
        final List<ConfigurationNode> raw = this.getNodeList(path, null);
        final List<Vector> list = new ArrayList<Vector>();
        for (final ConfigurationNode o : raw) {
            final Double x = o.getDouble("x");
            final Double y = o.getDouble("y");
            final Double z = o.getDouble("z");
            if (x != null && y != null) {
                if (z == null) {
                    continue;
                }
                list.add(new Vector((double)x, (double)y, (double)z));
            }
        }
        return list;
    }
    
    public List<Vector2D> getVector2dList(final String path, final List<Vector2D> def) {
        final List<ConfigurationNode> raw = this.getNodeList(path, null);
        final List<Vector2D> list = new ArrayList<Vector2D>();
        for (final ConfigurationNode o : raw) {
            final Double x = o.getDouble("x");
            final Double z = o.getDouble("z");
            if (x != null) {
                if (z == null) {
                    continue;
                }
                list.add(new Vector2D((double)x, (double)z));
            }
        }
        return list;
    }
    
    public List<BlockVector2D> getBlockVector2dList(final String path, final List<BlockVector2D> def) {
        final List<ConfigurationNode> raw = this.getNodeList(path, null);
        final List<BlockVector2D> list = new ArrayList<BlockVector2D>();
        for (final ConfigurationNode o : raw) {
            final Double x = o.getDouble("x");
            final Double z = o.getDouble("z");
            if (x != null) {
                if (z == null) {
                    continue;
                }
                list.add(new BlockVector2D((double)x, (double)z));
            }
        }
        return list;
    }
    
    public List<ConfigurationNode> getNodeList(final String path, final List<ConfigurationNode> def) {
        final List<Object> raw = this.getList(path);
        if (raw == null) {
            return (def != null) ? def : new ArrayList<ConfigurationNode>();
        }
        final List<ConfigurationNode> list = new ArrayList<ConfigurationNode>();
        for (final Object o : raw) {
            if (o instanceof Map) {
                list.add(new ConfigurationNode((Map<String, Object>)o));
            }
        }
        return list;
    }
    
    public ConfigurationNode getNode(final String path) {
        final Object raw = this.getProperty(path);
        if (raw instanceof Map) {
            return new ConfigurationNode((Map<String, Object>)raw);
        }
        return null;
    }
    
    public Map<String, ConfigurationNode> getNodes(final String path) {
        final Object o = this.getProperty(path);
        if (o == null) {
            return null;
        }
        if (o instanceof Map) {
            Map<String, Object> oMap = (Map<String, Object>) o;
            final Map<String, ConfigurationNode> nodes = new HashMap<>();
            for (final Map.Entry<String, Object> entry : oMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    nodes.put(entry.getKey(), new ConfigurationNode((Map<String, Object>) entry.getValue()));
                }
            }
            return nodes;
        }
        return null;
    }
    
    private static Integer castInt(final Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Byte) {
            return (int)(byte)o;
        }
        if (o instanceof Integer) {
            return (Integer)o;
        }
        if (o instanceof Double) {
            return (int)(double)o;
        }
        if (o instanceof Float) {
            return (int)(float)o;
        }
        if (o instanceof Long) {
            return (int)(long)o;
        }
        return null;
    }
    
    private static Double castDouble(final Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Float) {
            return (double)(float)o;
        }
        if (o instanceof Double) {
            return (Double)o;
        }
        if (o instanceof Byte) {
            return (double)(byte)o;
        }
        if (o instanceof Integer) {
            return (double)(int)o;
        }
        if (o instanceof Long) {
            return Double.valueOf((long)o);
        }
        return null;
    }
    
    private static Boolean castBoolean(final Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return (Boolean)o;
        }
        return null;
    }
    
    public void removeProperty(final String path) {
        if (!path.contains(".")) {
            this.root.remove(path);
            return;
        }
        final String[] parts = path.split("\\.");
        Map<String, Object> node = this.root;
        for (int i = 0; i < parts.length; ++i) {
            final Object o = node.get(parts[i]);
            if (i == parts.length - 1) {
                node.remove(parts[i]);
                return;
            }
            node = (Map<String, Object>)o;
        }
    }
}
