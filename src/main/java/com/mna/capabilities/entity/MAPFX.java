/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package com.mna.capabilities.entity;

import com.mna.network.ClientMessageDispatcher;
import com.mna.network.ServerMessageDispatcher;
import net.minecraft.world.entity.LivingEntity;

public class MAPFX {
    private long flags;
    private boolean requestedSync = false;
    private boolean needsSync = false;

    public long getFlags() {
        return this.flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public boolean getFlag(LivingEntity e, Flag f) {
        long mask = 1 << f.ordinal();
        return (this.flags & mask) != 0L;
    }

    public void setFlag(LivingEntity e, Flag f, boolean value) {
        long mask = 1 << f.ordinal();
        this.flags = value ? (this.flags |= mask) : (this.flags &= mask ^ 0xFFFFFFFFFFFFFFFFL);
        this.needsSync = true;
    }

    public void sync(LivingEntity entity) {
        if (this.needsSync && !entity.level().isClientSide()) {
            this.needsSync = false;
            ServerMessageDispatcher.sendMAPFXMessage(entity);
        }
    }

    public void requestSync(LivingEntity entity) {
        if (!this.requestedSync) {
            this.requestedSync = ClientMessageDispatcher.sendMAPFXSyncRequestMessage(entity);
        }
    }

    public static enum Flag {
        CANCEL_RENDER,
        MIST_FORM,
        LIVING_BOMB,
        FIRE_SHIELD,
        MANA_SHIELD,
        BRIARTHORN_BARRIER,
        CIRCLE_OF_POWER,
        WIND_WALL,
        AURA_OF_FROST,
        SOAKED,
        SOUL_TRAP,
        SHACKLES_OF_PAIN,
        CURSE_OF_AGONY,
        TRUE_INVISIBILITY;

    }
}

