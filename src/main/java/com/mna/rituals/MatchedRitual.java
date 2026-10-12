/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.NonNullList
 */
package com.mna.rituals;

import com.mna.api.rituals.RitualBlockPos;
import com.mna.recipes.rituals.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;

public class MatchedRitual {
    private NonNullList<RitualBlockPos> matchedPositions;
    private RitualRecipe ritual;
    private BlockPos center;
    private Direction rotation;

    public MatchedRitual(NonNullList<RitualBlockPos> positions, RitualRecipe ritual, BlockPos center, Direction rotation) {
        this.matchedPositions = positions;
        this.ritual = ritual;
        this.center = center;
        this.rotation = rotation;
    }

    public RitualRecipe getRitual() {
        return this.ritual;
    }

    public NonNullList<RitualBlockPos> getPositions() {
        return this.matchedPositions;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public Direction getRotation() {
        return this.rotation;
    }
}

