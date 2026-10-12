/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.Level
 */
package com.mna.api.recipes;

import com.mna.api.recipes.IMARecipe;
import com.mna.api.rituals.IRitualReagent;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public interface IRitualRecipe
extends IMARecipe {
    public int[][] getPattern();

    public IRitualReagent[][] getReagents();

    public String[] getManaweavePatterns();

    public boolean isValid();

    public int countRunes();

    public int getLowerBound();

    @Nullable
    public Direction getMatchedDirection(Level var1, BlockPos var2);

    @Nullable
    public Direction getMatchedDirection(List<BlockPos> var1);

    @Nullable
    public Direction getMatchedDirection(List<BlockPos> var1, BlockPos var2);

    public boolean hasCommand();

    public String getCommand();
}

