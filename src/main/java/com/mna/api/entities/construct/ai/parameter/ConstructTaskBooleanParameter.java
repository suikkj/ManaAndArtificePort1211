/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package com.mna.api.entities.construct.ai.parameter;

import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructParameterTypes;
import net.minecraft.nbt.CompoundTag;

public class ConstructTaskBooleanParameter
extends ConstructAITaskParameter {
    private boolean value;

    public ConstructTaskBooleanParameter(String id) {
        this(id, false);
    }

    public ConstructTaskBooleanParameter(String id, boolean defaultValue) {
        super(id, ConstructParameterTypes.BOOLEAN);
        this.value = defaultValue;
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        super.loadData(nbt);
        if (nbt.contains("value")) {
            this.value = nbt.getBoolean("value");
        }
    }

    @Override
    public CompoundTag saveData() {
        CompoundTag tag = super.saveData();
        tag.putBoolean("value", this.value);
        return tag;
    }
}

