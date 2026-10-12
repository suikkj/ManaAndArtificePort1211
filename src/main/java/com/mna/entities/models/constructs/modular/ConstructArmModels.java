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

public class ConstructArmModels
extends ConstructModelCollection {
    private final boolean isLeft;
    public static final String AXE = "axe";
    public static final String BLADE = "blade";
    public static final String BLASTER = "blaster";
    public static final String GRABBER = "grabber";
    public static final String HAMMER = "hammer";
    public static final String NOZZLE = "nozzle";
    public static final String CASTER = "caster";
    public static final String SHIELD = "shield";
    public static final String FISHER = "fisher";

    public ConstructArmModels(boolean isLeft, String materialIdentifier) {
        super(materialIdentifier);
        this.isLeft = isLeft;
        this.defineModel(AXE, 8);
        this.defineModel(BLADE, 4);
        this.defineModel(BLASTER, 16);
        this.defineModel(GRABBER, 1);
        this.defineModel(HAMMER, 2);
        this.defineModel(NOZZLE, 64);
        this.defineModel(CASTER, 128);
        this.defineModel(SHIELD, 32);
        this.defineModel(FISHER, 256);
    }

    @Override
    protected ResourceLocation getRLoc(String part_type) {
        return RLoc.create("construct/" + this.materialIdentifier + "/arm_" + part_type + "_" + (this.isLeft ? (char)'l' : 'r'));
    }

    @Override
    public ConstructSlot getSlot() {
        return this.isLeft ? ConstructSlot.LEFT_ARM : ConstructSlot.RIGHT_ARM;
    }
}

