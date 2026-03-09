package com.sk89q.worldguard.util.yaml;

public class ConfigurationException extends Exception
{
    private static final long serialVersionUID = -2442886939908724203L;
    
    public ConfigurationException() {
    }
    
    public ConfigurationException(final String msg) {
        super(msg);
    }
}
