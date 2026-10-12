/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.api.rituals;

import com.mna.api.recipes.IRitualRecipe;
import com.mna.api.rituals.RitualBlockPos;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IRitualContext {
    public Player getCaster();

    public IRitualRecipe getRecipe();

    public NonNullList<RitualBlockPos> getAllPositions();

    public NonNullList<RitualBlockPos> getIndexedPositions();

    public BlockPos getCenter();

    public List<ItemStack> getCollectedReagents();

    public List<ItemStack> getCollectedReagents(Predicate<ItemStack> var1);

    public List<ResourceLocation> getCollectedPatterns();

    public List<ResourceLocation> getCollectedPatterns(Predicate<ResourceLocation> var1);

    public Level getLevel();

    public void replaceReagents(ResourceLocation var1, NonNullList<ResourceLocation> var2);

    public void replaceReagents(ResourceLocation var1, ResourceLocation var2);

    public void replacePatterns(NonNullList<ResourceLocation> var1);

    public void appendPatterns(NonNullList<ResourceLocation> var1);
}

