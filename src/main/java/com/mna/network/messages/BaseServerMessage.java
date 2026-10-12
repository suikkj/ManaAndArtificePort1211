/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 */
package com.mna.network.messages;

import com.mna.network.messages.IMAMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class BaseServerMessage
implements IMAMessage {
    protected boolean messageIsValid = false;

    @Override
    public final boolean isMessageValid() {
        return this.messageIsValid;
    }

    public abstract void Handle(ServerLevel var1, ServerPlayer var2);
}

