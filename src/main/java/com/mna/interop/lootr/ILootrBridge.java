/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.interop.lootr;

import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public interface ILootrBridge {
    public long getLootSeed();

    public UUID getTileId();

    public ResourceLocation getLootTable();

    public void setLootrOpened(boolean var1);

    public Set<UUID> getOpeners();

    public void updatePacketViaState();
}

