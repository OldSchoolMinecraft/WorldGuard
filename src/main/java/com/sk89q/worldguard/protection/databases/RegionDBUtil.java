package com.sk89q.worldguard.protection.databases;

import com.sk89q.worldguard.domains.*;
import java.util.regex.*;

public class RegionDBUtil
{
    private static Pattern groupPattern;
    
    private RegionDBUtil() {
    }
    
    public static void addToDomain(final DefaultDomain domain, final String[] split, final int startIndex) {
        for (int i = startIndex; i < split.length; ++i) {
            final String s = split[i];
            final Matcher m = RegionDBUtil.groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            }
            else {
                domain.addPlayer(s);
            }
        }
    }
    
    public static void removeFromDomain(final DefaultDomain domain, final String[] split, final int startIndex) {
        for (int i = startIndex; i < split.length; ++i) {
            final String s = split[i];
            final Matcher m = RegionDBUtil.groupPattern.matcher(s);
            if (m.matches()) {
                domain.removeGroup(m.group(1));
            }
            else {
                domain.removePlayer(s);
            }
        }
    }
    
    public static DefaultDomain parseDomainString(final String[] split, final int startIndex) {
        final DefaultDomain domain = new DefaultDomain();
        for (int i = startIndex; i < split.length; ++i) {
            final String s = split[i];
            final Matcher m = RegionDBUtil.groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            }
            else {
                domain.addPlayer(s);
            }
        }
        return domain;
    }
    
    static {
        RegionDBUtil.groupPattern = Pattern.compile("^[gG]:(.+)$");
    }
}
