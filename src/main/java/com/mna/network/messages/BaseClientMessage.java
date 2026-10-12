/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package com.mna.network.messages;

import com.mna.network.messages.IMAMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class BaseClientMessage
implements IMAMessage {
    protected boolean messageIsValid = false;

    @Override
    public final boolean isMessageValid() {
        return this.messageIsValid;
    }

    public abstract void Handle(Level var1, Player var2);
}

