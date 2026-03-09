package com.sk89q.worldguard.protection.flags;

import org.bukkit.entity.*;

public final class DefaultFlag
{
    public static final StateFlag PASSTHROUGH;
    public static final StateFlag BUILD;
    public static final StateFlag PVP;
    public static final StateFlag MOB_DAMAGE;
    public static final StateFlag MOB_SPAWNING;
    public static final StateFlag CREEPER_EXPLOSION;
    public static final StateFlag GHAST_FIREBALL;
    public static final StateFlag SLEEP;
    public static final StateFlag TNT;
    public static final StateFlag LIGHTER;
    public static final StateFlag FIRE_SPREAD;
    public static final StateFlag LAVA_FIRE;
    public static final StateFlag LIGHTNING;
    public static final StateFlag CHEST_ACCESS;
    public static final StateFlag WATER_FLOW;
    public static final StateFlag LAVA_FLOW;
    public static final StateFlag USE;
    public static final StateFlag PLACE_VEHICLE;
    public static final StateFlag SNOW_FALL;
    public static final StateFlag LEAF_DECAY;
    public static final StateFlag ENTRY;
    public static final RegionGroupFlag ENTRY_PERM;
    public static final StateFlag EXIT;
    public static final RegionGroupFlag EXIT_PERM;
    public static final StringFlag GREET_MESSAGE;
    public static final StringFlag FAREWELL_MESSAGE;
    public static final BooleanFlag NOTIFY_ENTER;
    public static final BooleanFlag NOTIFY_LEAVE;
    public static final SetFlag<CreatureType> DENY_SPAWN;
    public static final IntegerFlag HEAL_DELAY;
    public static final IntegerFlag HEAL_AMOUNT;
    public static final VectorFlag TELE_LOC;
    public static final RegionGroupFlag TELE_PERM;
    public static final VectorFlag SPAWN_LOC;
    public static final RegionGroupFlag SPAWN_PERM;
    public static final BooleanFlag BUYABLE;
    public static final DoubleFlag PRICE;
    public static final SetFlag<String> BLOCKED_CMDS;
    public static final SetFlag<String> ALLOWED_CMDS;
    public static final Flag<?>[] flagsList;
    
    private DefaultFlag() {
    }
    
    public static Flag<?>[] getFlags() {
        return DefaultFlag.flagsList;
    }
    
    public static StateFlag getLegacyFlag(final String flagString) {
        for (final Flag<?> flag : DefaultFlag.flagsList) {
            if (flag instanceof StateFlag && flagString.equals(String.valueOf(flag.getLegacyCode()))) {
                return (StateFlag)flag;
            }
        }
        return null;
    }
    
    static {
        PASSTHROUGH = new StateFlag("passthrough", 'z', false);
        BUILD = new StateFlag("build", 'b', true);
        PVP = new StateFlag("pvp", 'p', true);
        MOB_DAMAGE = new StateFlag("mob-damage", 'm', true);
        MOB_SPAWNING = new StateFlag("mob-spawning", true);
        CREEPER_EXPLOSION = new StateFlag("creeper-explosion", 'c', true);
        GHAST_FIREBALL = new StateFlag("ghast-fireball", true);
        SLEEP = new StateFlag("sleep", true);
        TNT = new StateFlag("tnt", 't', true);
        LIGHTER = new StateFlag("lighter", 'l', true);
        FIRE_SPREAD = new StateFlag("fire-spread", 'f', true);
        LAVA_FIRE = new StateFlag("lava-fire", 'F', true);
        LIGHTNING = new StateFlag("lightning", true);
        CHEST_ACCESS = new StateFlag("chest-access", 'C', false);
        WATER_FLOW = new StateFlag("water-flow", true);
        LAVA_FLOW = new StateFlag("lava-flow", true);
        USE = new StateFlag("use", true);
        PLACE_VEHICLE = new StateFlag("vehicle-place", false);
        SNOW_FALL = new StateFlag("snow-fall", true);
        LEAF_DECAY = new StateFlag("leaf-decay", true);
        ENTRY = new StateFlag("entry", true);
        ENTRY_PERM = new RegionGroupFlag("entry-group", RegionGroupFlag.RegionGroup.NON_MEMBERS);
        EXIT = new StateFlag("exit", true);
        EXIT_PERM = new RegionGroupFlag("exit-group", RegionGroupFlag.RegionGroup.NON_MEMBERS);
        GREET_MESSAGE = new StringFlag("greeting");
        FAREWELL_MESSAGE = new StringFlag("farewell");
        NOTIFY_ENTER = new BooleanFlag("notify-enter");
        NOTIFY_LEAVE = new BooleanFlag("notify-leave");
        DENY_SPAWN = new SetFlag<CreatureType>("deny-spawn", new CreatureTypeFlag(null));
        HEAL_DELAY = new IntegerFlag("heal-delay");
        HEAL_AMOUNT = new IntegerFlag("heal-amount");
        TELE_LOC = new VectorFlag("teleport");
        TELE_PERM = new RegionGroupFlag("teleport-group", RegionGroupFlag.RegionGroup.MEMBERS);
        SPAWN_LOC = new VectorFlag("spawn");
        SPAWN_PERM = new RegionGroupFlag("spawn-group", RegionGroupFlag.RegionGroup.MEMBERS);
        BUYABLE = new BooleanFlag("buyable");
        PRICE = new DoubleFlag("price");
        BLOCKED_CMDS = new SetFlag<String>("blocked-cmds", new CommandStringFlag(null));
        ALLOWED_CMDS = new SetFlag<String>("allowed-cmds", new CommandStringFlag(null));
        flagsList = new Flag[] { DefaultFlag.PASSTHROUGH, DefaultFlag.BUILD, DefaultFlag.PVP, DefaultFlag.MOB_DAMAGE, DefaultFlag.MOB_SPAWNING, DefaultFlag.CREEPER_EXPLOSION, DefaultFlag.SLEEP, DefaultFlag.TNT, DefaultFlag.LIGHTER, DefaultFlag.FIRE_SPREAD, DefaultFlag.LAVA_FIRE, DefaultFlag.CHEST_ACCESS, DefaultFlag.WATER_FLOW, DefaultFlag.LAVA_FLOW, DefaultFlag.USE, DefaultFlag.PLACE_VEHICLE, DefaultFlag.GREET_MESSAGE, DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.NOTIFY_ENTER, DefaultFlag.NOTIFY_LEAVE, DefaultFlag.DENY_SPAWN, DefaultFlag.HEAL_DELAY, DefaultFlag.HEAL_AMOUNT, DefaultFlag.TELE_LOC, DefaultFlag.TELE_PERM, DefaultFlag.SPAWN_LOC, DefaultFlag.SPAWN_PERM, DefaultFlag.BUYABLE, DefaultFlag.PRICE, DefaultFlag.SNOW_FALL, DefaultFlag.LEAF_DECAY, DefaultFlag.GHAST_FIREBALL, DefaultFlag.BLOCKED_CMDS, DefaultFlag.ALLOWED_CMDS, DefaultFlag.ENTRY, DefaultFlag.ENTRY_PERM, DefaultFlag.EXIT, DefaultFlag.EXIT_PERM };
        DefaultFlag.ENTRY.setGroupFlag(DefaultFlag.ENTRY_PERM);
        DefaultFlag.EXIT.setGroupFlag(DefaultFlag.EXIT_PERM);
    }
}
