/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 */
package com.mna.api.rituals;

import com.mna.api.rituals.IRitualReagent;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class RitualBlockPos {
    private int index;
    private int displayIndex;
    private BlockPos blockPos;
    private IRitualReagent requiredReagent;

    public RitualBlockPos(int index, int displayIndex, BlockPos pos, IRitualReagent requiredReagent) {
        this.index = index;
        this.displayIndex = displayIndex;
        this.blockPos = pos;
        this.requiredReagent = requiredReagent;
    }

    public int getIndex() {
        return this.index;
    }

    public int getDisplayIndex() {
        return this.displayIndex;
    }

    @Nullable
    public IRitualReagent getReagent() {
        return this.requiredReagent;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public boolean isPresent() {
        return this.requiredReagent != null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof RitualBlockPos) {
            RitualBlockPos other = (RitualBlockPos)obj;
            return other.index == this.index && other.displayIndex == this.displayIndex && other.blockPos.equals(this.blockPos);
        }
        if (obj instanceof BlockPos) {
            return ((BlockPos)obj).equals(this.getBlockPos());
        }
        return false;
    }
}

