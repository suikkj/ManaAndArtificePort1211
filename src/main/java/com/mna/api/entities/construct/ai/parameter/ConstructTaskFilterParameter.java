/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 */
package com.mna.api.entities.construct.ai.parameter;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructParameterTypes;
import com.mna.api.items.DynamicItemFilter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ConstructTaskFilterParameter
extends ConstructAITaskParameter {
    private DynamicItemFilter _filter = new DynamicItemFilter();

    public ConstructTaskFilterParameter(String id) {
        super(id, ConstructParameterTypes.FILTER);
    }

    public DynamicItemFilter getValue() {
        return this._filter;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        super.loadData(nbt);
        if (nbt.contains("filter")) {
            this._filter.loadFromTag(nbt.getCompound("filter"));
        }
    }

    @Override
    public CompoundTag saveData() {
        CompoundTag tag = super.saveData();
        tag.put("filter", (Tag)this._filter.getTag());
        return tag;
    }
}

