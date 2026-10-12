/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.mna.api.spells.targeting;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public final class SpellContext {
    private Level level;
    private Entity targetEntity;
    private ISpellDefinition recipe;
    private HashMap<ResourceLocation, ArrayList<Entity>> affectedEntities;
    private HashMap<ResourceLocation, ArrayList<BlockPos>> affectedBlocks;
    private List<Item> missingOptionalReagents;
    private CompoundTag meta;

    public SpellContext(Level level, ISpellDefinition recipe) {
        this(level, recipe, null);
    }

    public SpellContext(Level level, ISpellDefinition recipe, Entity targetEntity) {
        this.level = level;
        this.recipe = recipe;
        this.targetEntity = targetEntity;
        this.affectedEntities = new HashMap();
        this.affectedBlocks = new HashMap();
        this.missingOptionalReagents = Arrays.asList(new Item[0]);
    }

    public boolean isClientSide() {
        return this.level.isClientSide;
    }

    @Nullable
    public ServerLevel getServerLevel() {
        return this.level instanceof ServerLevel ? (ServerLevel)this.level : null;
    }

    public Level getLevel() {
        return this.level;
    }

    public ISpellDefinition getSpell() {
        return this.recipe;
    }

    @Nullable
    public Entity getSpawnedTargetEntity() {
        return this.targetEntity;
    }

    public final void setMissingReagents(List<Item> missingOptionals) {
        this.missingOptionalReagents = missingOptionals;
    }

    public final boolean isReagentMissing(Item item) {
        return this.missingOptionalReagents.contains(item);
    }

    public final boolean isReagentMissing(ResourceLocation rLoc) {
        return this.missingOptionalReagents.contains(BuiltInRegistries.ITEM.get(rLoc));
    }

    public final void addAffectedEntity(SpellEffect component, Entity entity) {
        if (!this.affectedEntities.containsKey(component.getRegistryName())) {
            this.affectedEntities.put(component.getRegistryName(), new ArrayList());
        }
        this.affectedEntities.get(component.getRegistryName()).add(entity);
    }

    public final boolean hasEntityBeenAffected(SpellEffect component, Entity entity) {
        if (!this.affectedEntities.containsKey(component.getRegistryName())) {
            return false;
        }
        return this.affectedEntities.get(component.getRegistryName()).contains(entity);
    }

    public final int countAffectedEntities(SpellEffect component) {
        if (!this.affectedEntities.containsKey(component.getRegistryName())) {
            return 0;
        }
        return this.affectedEntities.get(component.getRegistryName()).size();
    }

    public final void addAffectedBlock(SpellEffect component, BlockPos pos) {
        if (!this.affectedBlocks.containsKey(component.getRegistryName())) {
            this.affectedBlocks.put(component.getRegistryName(), new ArrayList());
        }
        this.affectedBlocks.get(component.getRegistryName()).add(pos);
    }

    public final boolean hasBlockBeenAffected(SpellEffect component, BlockPos pos) {
        if (!this.affectedBlocks.containsKey(component.getRegistryName())) {
            return false;
        }
        return this.affectedBlocks.get(component.getRegistryName()).contains(pos);
    }

    public final int countAffectedBlocks(SpellEffect component) {
        if (!this.affectedBlocks.containsKey(component.getRegistryName())) {
            return 0;
        }
        return this.affectedBlocks.get(component.getRegistryName()).size();
    }

    public final CompoundTag getMeta() {
        if (this.meta == null) {
            this.meta = new CompoundTag();
        }
        return this.meta;
    }
}


