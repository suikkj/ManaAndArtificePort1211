/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.entities.models.constructs.modular;

import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.tools.RLoc;
import com.mna.entities.models.constructs.modular.ConstructModelCollection;
import net.minecraft.resources.ResourceLocation;

public class ConstructLegModels
extends ConstructModelCollection {
    public static final String BASIC = "basic";
    public static final String ENDER_FRONT = "ender_front";
    public static final String ENDER_BACK = "ender_back";
    public static final String REINFORCED = "reinforced";
    public static final String ROCKET = "rocket";
    private final boolean isLeft;

    public ConstructLegModels(boolean isLeft, String materialIdentifier) {
        super(materialIdentifier);
        this.isLeft = isLeft;
        this.defineModel(BASIC, 1);
        this.defineModel(ENDER_FRONT, 4);
        this.defineModel(ENDER_BACK, 4);
        this.defineModel(REINFORCED, 2);
        this.defineModel(ROCKET, 8);
    }

    @Override
    protected ResourceLocation getRLoc(String part_type) {
        return RLoc.create("construct/" + this.materialIdentifier + "/leg_" + part_type + "_" + (this.isLeft ? (char)'l' : 'r'));
    }

    @Override
    public ConstructSlot getSlot() {
        return ConstructSlot.LEGS;
    }
}

