/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.StringRepresentable
 */
package com.mna.blocks;

import net.minecraft.util.StringRepresentable;

public enum TableShape implements StringRepresentable
{
    STANDALONE("standalone"),
    END("end"),
    STRAIGHT("straight"),
    CORNER("corner"),
    CORNER_INTERSECTION("corner_intersection"),
    CORNER_INTERIOR("corner_interior"),
    T("t"),
    T_INTERSECTION("t_intersection"),
    EDGE("edge"),
    EDGE_INTERIOR_RIGHT("edge_interior_right"),
    EDGE_INTERIOR_LEFT("edge_interior_left"),
    CENTER("center"),
    CROSS("cross"),
    CROSS_SINGLE_INTERCARDINAL("cross_single_intercardinal"),
    CROSS_DIAGONAL_INTERCARDINAL("cross_diagonal_intercardinal");

    private final String name;

    private TableShape(String pName) {
        this.name = pName;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}

