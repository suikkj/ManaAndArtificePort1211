/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.capabilities;

import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IChunkMagic {
    public static final int WARD_RESULT_DENIED = 0;
    public static final int WARD_RESULT_ADDED = 1;
    public static final int WARD_RESULT_REMOVED = 2;

    public void addResidualMagic(float var1);

    public void removeResidualMagic(float var1);

    public float getResidualMagic();

    public void setResidualMagic(float var1);

    public void pushKnownEldrinSupplier(BlockPos var1);

    public void popKnownEldrinSupplier(BlockPos var1);

    public List<Long> getKnownEldrinSuppliers();

    public boolean blockInteractionFor(BlockPos var1, @Nullable Player var2);

    public boolean wardBlock(BlockPos var1, Player var2, ItemStack var3);

    public boolean removeBlockWard(BlockPos var1, Player var2);

    public boolean isWarded(long var1);

    public void removeBlockWard(BlockPos var1);

    public HashMap<Long, Pair<UUID, ResourceLocation>> getWards();

    public void setWards(HashMap<Long, Pair<UUID, ResourceLocation>> var1);
}

