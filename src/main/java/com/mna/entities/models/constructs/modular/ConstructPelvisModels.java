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

public class ConstructPelvisModels
extends ConstructModelCollection {
    public static final String ENDER = "ender";

    public ConstructPelvisModels(String materialIdentifier) {
        super(materialIdentifier);
        this.defineModel(ENDER, 4);
    }

    @Override
    protected ResourceLocation getRLoc(String part_type) {
        return RLoc.create("construct/" + this.materialIdentifier + "/pelvis_" + part_type);
    }

    @Override
    public ConstructSlot getSlot() {
        return ConstructSlot.LEGS;
    }
}

