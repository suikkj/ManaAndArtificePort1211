/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public interface IAnimPacketSync<T extends Entity> {
    public CompoundTag getPacketData();

    public void handlePacketData(CompoundTag var1);
}

