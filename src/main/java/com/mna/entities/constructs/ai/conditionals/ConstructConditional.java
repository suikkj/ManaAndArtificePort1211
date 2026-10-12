/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.entities.constructs.ai.conditionals;

import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public abstract class ConstructConditional<V extends ConstructAITask<?>>
extends ConstructAITask<V> {
    public ConstructConditional(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    protected abstract boolean evaluate();

    @Override
    public void start() {
        super.start();
        this.exitCode = this.evaluate() ? 0 : 1;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    protected CompoundTag writeInternal(CompoundTag nbt) {
        return new CompoundTag();
    }
}

