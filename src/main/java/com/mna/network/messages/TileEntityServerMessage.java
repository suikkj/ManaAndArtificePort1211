/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 */
package com.mna.network.messages;

import com.mna.network.messages.BaseServerMessage;
import net.minecraft.core.BlockPos;

public abstract class TileEntityServerMessage
extends BaseServerMessage {
    protected BlockPos pos;

    protected TileEntityServerMessage(BlockPos pos) {
        this.pos = pos;
    }

    public final BlockPos getPosition() {
        return this.pos;
    }
}

