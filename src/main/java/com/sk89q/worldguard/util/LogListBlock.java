package com.sk89q.worldguard.util;

import java.util.*;

public class LogListBlock
{
    private LinkedHashMap<String, Object> items;
    private int maxKeyLength;
    
    public LogListBlock() {
        this.items = new LinkedHashMap<String, Object>();
        this.maxKeyLength = 0;
    }
    
    private void updateKey(final String key) {
        if (key.length() > this.maxKeyLength) {
            this.maxKeyLength = key.length();
        }
    }
    
    public LogListBlock put(final String key, final String value) {
        this.updateKey(key);
        this.items.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final LogListBlock value) {
        this.updateKey(key);
        this.items.put(key, value);
        return this;
    }
    
    public LogListBlock put(final String key, final Object value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final String value, final Object... args) {
        this.put(key, String.format(value, args));
        return this;
    }
    
    public LogListBlock put(final String key, final int value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final byte value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final double value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final float value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final short value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final long value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock put(final String key, final boolean value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public LogListBlock putChild(final String key) {
        this.updateKey(key);
        final LogListBlock block = new LogListBlock();
        this.items.put(key, block);
        return block;
    }
    
    private String padKey(final String key, final int len) {
        return String.format("%-" + len + "s", key);
    }
    
    protected String getOutput(final String prefix) {
        final StringBuilder out = new StringBuilder();
        for (final Map.Entry<String, Object> entry : this.items.entrySet()) {
            final Object val = entry.getValue();
            if (val instanceof LogListBlock) {
                out.append(prefix);
                out.append(this.padKey(entry.getKey(), this.maxKeyLength));
                out.append(":\r\n");
                out.append(((LogListBlock)val).getOutput(prefix + "    "));
            }
            else {
                out.append(prefix);
                out.append(this.padKey(entry.getKey(), this.maxKeyLength));
                out.append(": ");
                out.append(val.toString());
                out.append("\r\n");
            }
        }
        return out.toString();
    }
    
    @Override
    public String toString() {
        return this.getOutput("");
    }
}
