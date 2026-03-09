package com.sk89q.worldguard.blacklist.loggers;

import java.io.*;

public class FileLoggerWriter implements Comparable<FileLoggerWriter>
{
    public String path;
    private BufferedWriter writer;
    private long lastUse;
    
    public FileLoggerWriter(final String path, final BufferedWriter writer) {
        this.path = path;
        this.writer = writer;
        this.lastUse = System.currentTimeMillis();
    }
    
    public String getPath() {
        return this.path;
    }
    
    public BufferedWriter getWriter() {
        return this.writer;
    }
    
    public long getLastUse() {
        return this.lastUse;
    }
    
    public void updateLastUse() {
        this.lastUse = System.currentTimeMillis();
    }
    
    public int compareTo(final FileLoggerWriter other) {
        if (this.lastUse > other.lastUse) {
            return 1;
        }
        if (this.lastUse < other.lastUse) {
            return -1;
        }
        return 0;
    }
}
