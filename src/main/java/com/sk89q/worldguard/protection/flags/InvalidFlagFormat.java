package com.sk89q.worldguard.protection.flags;

public class InvalidFlagFormat extends Exception
{
    private static final long serialVersionUID = 8101615074524004172L;
    
    public InvalidFlagFormat(final String msg) {
        super(msg);
    }
}
