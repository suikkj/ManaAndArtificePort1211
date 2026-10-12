/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 */
package com.mna.api.capabilities;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface IRitualTeleportLocation {
    public boolean matches(List<ResourceLocation> var1);

    public boolean matches(BlockPos var1);

    public ImmutableList<ResourceLocation> getReagents();

    public Direction getDirection();

    public BlockPos getPos();

    public ResourceKey<Level> getWorldType();

    public void tryCorrectWorldKey(ResourceKey<Level> var1);
}

