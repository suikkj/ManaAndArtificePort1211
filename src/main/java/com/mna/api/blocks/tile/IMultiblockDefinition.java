/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.api.blocks.tile;

import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public interface IMultiblockDefinition
extends Recipe<CraftingContainer> {
    public boolean match(Level var1, BlockPos var2);

    public boolean match(Level var1, BlockPos var2, Rotation var3, boolean var4);

    public Pair<Integer, Integer> matchWithCount(Level var1, BlockPos var2, Rotation var3, boolean var4);

    public HashMap<BlockPos, BlockState> getMissingBlocks(Level var1, BlockPos var2, Rotation var3, boolean var4);

    public List<String> getMatchedVariations();

    public boolean spawn(ServerLevel var1, BlockPos var2);

    public boolean spawn(ServerLevel var1, BlockPos var2, Rotation var3, boolean var4);

    public int getBlockCount();

    public boolean isSymmetrical();

    public ResourceLocation getStructurePath();

    public void setStructurePath(ResourceLocation var1);

    public Vec3i getSize();

    public List<BlockPos> getPositions(ResourceLocation var1, @Nullable Predicate<BlockState> var2);

    public List<BlockPos> getPositions(List<ResourceLocation> var1, @Nullable Predicate<BlockState> var2);

    public HashMap<ResourceLocation, Integer> getSpecialBlockMatchersByBlock();

    public HashMap<Long, Integer> getSpecialBlockMatchersByOffset();

    public void setSpecialBlockMatchersByBlock(HashMap<ResourceLocation, Integer> var1);

    public void setSpecialBlockMatchersByOffset(HashMap<Long, Integer> var1);

    public ResourceLocation getId();
}

