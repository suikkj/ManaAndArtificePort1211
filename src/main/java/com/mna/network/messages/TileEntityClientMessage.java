/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 */
package com.mna.network.messages;

import com.mna.network.messages.BaseClientMessage;
import net.minecraft.core.BlockPos;

public abstract class TileEntityClientMessage
extends BaseClientMessage {
    protected BlockPos pos;

    protected TileEntityClientMessage(BlockPos pos) {
        this.pos = pos;
    }

    public final BlockPos getPosition() {
        return this.pos;
    }
}

