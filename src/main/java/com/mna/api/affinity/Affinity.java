/*
 * Decompiled with CFR 0.152.
 */
package com.mna.api.affinity;

public enum Affinity {
    ARCANE,
    EARTH,
    ENDER,
    FIRE,
    WATER,
    WIND,
    HELLFIRE,
    ICE,
    LIGHTNING,
    UNKNOWN,
    BLOOD;

    private static int[] COLOR_ARCANE;
    private static int[] COLOR_EARTH;
    private static int[] COLOR_ENDER;
    private static int[] COLOR_FIRE;
    private static int[] COLOR_LIGHTNING;
    private static int[] COLOR_WATER;
    private static int[] COLOR_ICE;
    private static int[] COLOR_WIND;
    private static int[] COLOR_HELLFIRE;
    private static int[] COLOR_BLOOD;
    private static int[] COLOR_UNKNOWN;
    private static int[] COLOR_ARCANE_SECONDARY;
    private static int[] COLOR_EARTH_SECONDARY;
    private static int[] COLOR_ENDER_SECONDARY;
    private static int[] COLOR_FIRE_SECONDARY;
    private static int[] COLOR_LIGHTNING_SECONDARY;
    private static int[] COLOR_WATER_SECONDARY;
    private static int[] COLOR_ICE_SECONDARY;
    private static int[] COLOR_WIND_SECONDARY;
    private static int[] COLOR_HELLFIRE_SECONDARY;
    private static int[] COLOR_BLOOD_SECONDARY;
    private static int[] COLOR_UNKNOWN_SECONDARY;

    public static Affinity[] CoreSix() {
        return new Affinity[]{ARCANE, ENDER, EARTH, FIRE, WATER, WIND};
    }

    public Affinity getOpposite() {
        switch (this) {
            case ARCANE: {
                return ENDER;
            }
            case EARTH: {
                return WIND;
            }
            case ENDER: {
                return ARCANE;
            }
            case FIRE: {
                return WATER;
            }
            case WATER: {
                return FIRE;
            }
            case WIND: {
                return EARTH;
            }
            case HELLFIRE: {
                return WATER;
            }
            case ICE: {
                return FIRE;
            }
            case LIGHTNING: {
                return WATER;
            }
            case BLOOD: {
                return ARCANE;
            }
        }
        return UNKNOWN;
    }

    public Affinity getAdjacentLeft() {
        switch (this) {
            case WIND: {
                return FIRE;
            }
            case FIRE: {
                return ENDER;
            }
            case ENDER: {
                return EARTH;
            }
            case EARTH: {
                return ARCANE;
            }
            case ARCANE: {
                return WATER;
            }
            case WATER: {
                return WIND;
            }
            case HELLFIRE: {
                return ENDER;
            }
            case ICE: {
                return WIND;
            }
            case LIGHTNING: {
                return ENDER;
            }
            case BLOOD: {
                return EARTH;
            }
        }
        return UNKNOWN;
    }

    public Affinity getAdjacentRight() {
        switch (this) {
            case WIND: {
                return WATER;
            }
            case WATER: {
                return ARCANE;
            }
            case ARCANE: {
                return EARTH;
            }
            case EARTH: {
                return ENDER;
            }
            case ENDER: {
                return FIRE;
            }
            case FIRE: {
                return WIND;
            }
            case HELLFIRE: {
                return WIND;
            }
            case ICE: {
                return ARCANE;
            }
            case LIGHTNING: {
                return WIND;
            }
            case BLOOD: {
                return FIRE;
            }
        }
        return UNKNOWN;
    }

    public int[] getColor() {
        switch (this) {
            case ARCANE: {
                return COLOR_ARCANE;
            }
            case EARTH: {
                return COLOR_EARTH;
            }
            case ENDER: {
                return COLOR_ENDER;
            }
            case FIRE: {
                return COLOR_FIRE;
            }
            case WATER: {
                return COLOR_WATER;
            }
            case WIND: {
                return COLOR_WIND;
            }
            case HELLFIRE: {
                return COLOR_HELLFIRE;
            }
            case ICE: {
                return COLOR_ICE;
            }
            case LIGHTNING: {
                return COLOR_LIGHTNING;
            }
            case BLOOD: {
                return COLOR_BLOOD;
            }
        }
        return COLOR_UNKNOWN;
    }

    public int[] getSecondaryColor() {
        switch (this) {
            case ARCANE: {
                return COLOR_ARCANE_SECONDARY;
            }
            case EARTH: {
                return COLOR_EARTH_SECONDARY;
            }
            case ENDER: {
                return COLOR_ENDER_SECONDARY;
            }
            case FIRE: {
                return COLOR_FIRE_SECONDARY;
            }
            case WATER: {
                return COLOR_WATER_SECONDARY;
            }
            case WIND: {
                return COLOR_WIND_SECONDARY;
            }
            case HELLFIRE: {
                return COLOR_HELLFIRE_SECONDARY;
            }
            case ICE: {
                return COLOR_ICE_SECONDARY;
            }
            case LIGHTNING: {
                return COLOR_LIGHTNING_SECONDARY;
            }
            case BLOOD: {
                return COLOR_BLOOD_SECONDARY;
            }
        }
        return COLOR_UNKNOWN_SECONDARY;
    }

    public Affinity getShiftAffinity() {
        switch (this) {
            case HELLFIRE: {
                return FIRE;
            }
            case ICE: {
                return WATER;
            }
            case LIGHTNING: {
                return FIRE;
            }
            case BLOOD: {
                return ENDER;
            }
        }
        return this;
    }

    static {
        COLOR_ARCANE = new int[]{95, 24, 125};
        COLOR_EARTH = new int[]{87, 54, 9};
        COLOR_ENDER = new int[]{49, 2, 51};
        COLOR_FIRE = new int[]{219, 106, 20};
        COLOR_LIGHTNING = new int[]{132, 163, 176};
        COLOR_WATER = new int[]{57, 76, 227};
        COLOR_ICE = new int[]{134, 213, 240};
        COLOR_WIND = new int[]{190, 204, 209};
        COLOR_HELLFIRE = new int[]{46, 123, 59};
        COLOR_BLOOD = new int[]{91, 15, 14};
        COLOR_UNKNOWN = new int[]{255, 255, 255};
        COLOR_ARCANE_SECONDARY = new int[]{77, 133, 207};
        COLOR_EARTH_SECONDARY = new int[]{87, 54, 9};
        COLOR_ENDER_SECONDARY = new int[]{49, 2, 51};
        COLOR_FIRE_SECONDARY = new int[]{219, 106, 20};
        COLOR_LIGHTNING_SECONDARY = new int[]{132, 163, 176};
        COLOR_WATER_SECONDARY = new int[]{57, 76, 227};
        COLOR_ICE_SECONDARY = new int[]{134, 213, 240};
        COLOR_WIND_SECONDARY = new int[]{190, 204, 209};
        COLOR_HELLFIRE_SECONDARY = new int[]{20, 79, 31};
        COLOR_BLOOD_SECONDARY = new int[]{137, 51, 51};
        COLOR_UNKNOWN_SECONDARY = new int[]{0, 0, 0};
    }
}

