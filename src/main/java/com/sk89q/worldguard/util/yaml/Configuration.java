package com.sk89q.worldguard.util.yaml;

import java.util.*;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.representer.*;
import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.reader.*;
import java.io.*;

public class Configuration extends ConfigurationNode
{
    private Yaml yaml;
    private File file;
    private String header;
    
    public Configuration(final File file) {
        super(new HashMap<String, Object>());
        this.header = null;
        final DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
        this.yaml = new Yaml((BaseConstructor)new SafeConstructor(), new Representer(), options);
        this.file = file;
    }
    
    public void load() throws IOException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(this.file);
            this.read(this.yaml.load((Reader)new UnicodeReader((InputStream)stream)));
        }
        catch (ConfigurationException e) {
            this.root = new HashMap<String, Object>();
        }
        finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            }
            catch (IOException ex) {}
        }
    }
    
    public void setHeader(final String... headerLines) {
        final StringBuilder header = new StringBuilder();
        for (final String line : headerLines) {
            if (header.length() > 0) {
                header.append("\r\n");
            }
            header.append(line);
        }
        this.setHeader(header.toString());
    }
    
    public void setHeader(final String header) {
        this.header = header;
    }
    
    public String getHeader() {
        return this.header;
    }
    
    public boolean save() {
        FileOutputStream stream = null;
        final File parent = this.file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try {
            stream = new FileOutputStream(this.file);
            final OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            if (this.header != null) {
                writer.append((CharSequence)this.header);
                writer.append((CharSequence)"\r\n");
            }
            this.yaml.dump((Object)this.root, (Writer)writer);
            return true;
        }
        catch (IOException e) {}
        finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            }
            catch (IOException ex) {}
        }
        return false;
    }
    
    private void read(final Object input) throws ConfigurationException {
        try {
            if (null == input) {
                this.root = new HashMap<String, Object>();
            }
            else {
                this.root = (Map<String, Object>)input;
            }
        }
        catch (ClassCastException e) {
            throw new ConfigurationException("Root document must be an key-value structure");
        }
    }
    
    public static ConfigurationNode getEmptyNode() {
        return new ConfigurationNode(new HashMap<String, Object>());
    }
}
