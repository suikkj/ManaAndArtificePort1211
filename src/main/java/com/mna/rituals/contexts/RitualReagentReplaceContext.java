/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.rituals.contexts;

import com.mna.ManaAndArtifice;
import com.mna.api.recipes.IRitualRecipe;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.entities.rituals.Ritual;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RitualReagentReplaceContext
implements IRitualContext {
    private Player caster;
    private Ritual ritual;
    private NonNullList<RitualBlockPos> positions;

    public RitualReagentReplaceContext(Player caster, Ritual ritual, NonNullList<RitualBlockPos> positions) {
        this.caster = caster;
        this.ritual = ritual;
        this.positions = positions;
    }

    @Override
    public Player getCaster() {
        return this.caster;
    }

    @Override
    public IRitualRecipe getRecipe() {
        return this.ritual.getCurrentRitual();
    }

    @Override
    public NonNullList<RitualBlockPos> getAllPositions() {
        return this.ritual.getRitualData((byte)0);
    }

    @Override
    public NonNullList<RitualBlockPos> getIndexedPositions() {
        return this.ritual.getRitualData((byte)1);
    }

    @Override
    public BlockPos getCenter() {
        return this.ritual.blockPosition();
    }

    @Override
    public List<ItemStack> getCollectedReagents() {
        ManaAndArtifice.LOGGER.warn("RitualContext getCollectedReagents called during replace reagents stage.");
        return new ArrayList<ItemStack>();
    }

    @Override
    public List<ItemStack> getCollectedReagents(Predicate<ItemStack> filter) {
        ManaAndArtifice.LOGGER.warn("RitualContext getCollectedReagents called during replace reagents stage.");
        return new ArrayList<ItemStack>();
    }

    @Override
    public List<ResourceLocation> getCollectedPatterns() {
        ManaAndArtifice.LOGGER.warn("RitualContext getCollectedReagents called during replace reagents stage.");
        return new ArrayList<ResourceLocation>();
    }

    @Override
    public List<ResourceLocation> getCollectedPatterns(Predicate<ResourceLocation> filter) {
        ManaAndArtifice.LOGGER.warn("RitualContext getCollectedReagents called during replace reagents stage.");
        return new ArrayList<ResourceLocation>();
    }

    public ServerLevel getLevel() {
        return (ServerLevel)this.ritual.level();
    }

    @Override
    public void replaceReagents(ResourceLocation key, NonNullList<ResourceLocation> replacements) {
        if (this.positions.size() == 0 || replacements.size() == 0) {
            return;
        }
        int replaceIndex = 0;
        for (RitualBlockPos reagent : this.positions) {
            if (reagent == null || !reagent.isPresent() || !reagent.getReagent().isDynamic() || reagent.getReagent().getResourceLocation().compareTo(key) != 0) continue;
            reagent.getReagent().setResourceLocation((ResourceLocation)replacements.get(replaceIndex));
            if (++replaceIndex < replacements.size()) continue;
            return;
        }
    }

    @Override
    public void replaceReagents(ResourceLocation key, ResourceLocation replacement) {
        if (this.positions.size() == 0) {
            return;
        }
        for (RitualBlockPos reagent : this.positions) {
            if (reagent == null || !reagent.isPresent() || !reagent.getReagent().isDynamic() || reagent.getReagent().getResourceLocation().compareTo(key) != 0) continue;
            reagent.getReagent().setResourceLocation(replacement);
        }
    }

    @Override
    public void replacePatterns(NonNullList<ResourceLocation> replacements) {
        this.ritual.getRequiredPatterns().clear();
        this.ritual.getRequiredPatterns().addAll(replacements);
    }

    @Override
    public void appendPatterns(NonNullList<ResourceLocation> append) {
        this.ritual.getRequiredPatterns().addAll(append);
    }
}

