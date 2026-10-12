/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderSet
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.levelgen.structure.Structure
 */
package com.mna.rituals.effects;

import com.mna.items.ItemInit;
import com.mna.items.ritual.ThaumaturgicLink;
import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class WorldUtils {
    @Nullable
    public static BlockPos locateBiome(ServerLevel world, BlockPos center, ItemStack thaumLink) {
        ResourceLocation biomeLoc = ((ThaumaturgicLink)(ItemInit.THAUMATURGIC_LINK.get())).getLocationKey(thaumLink);
        if (biomeLoc == null) {
            return null;
        }
        return WorldUtils.locateBiome(world, center, biomeLoc);
    }

    @Nullable
    public static BlockPos locateBiome(ServerLevel world, BlockPos center, ResourceLocation biomeLoc) {
        Registry registry = world.registryAccess().registryOrThrow(Registries.BIOME);
        Biome biome = (Biome)registry.get(biomeLoc);
        if (biome == null) {
            return null;
        }
        Pair result = world.findClosestBiome3d(b -> b.isBound() && ((Biome)b.get()).equals(biome), center, 6400, 8, 64);
        if (result == null) {
            return null;
        }
        return (BlockPos)result.getFirst();
    }

    @Nullable
    public static BlockPos locateStructure(ServerLevel world, BlockPos center, ItemStack thaumLink) {
        ResourceLocation structureLoc = ((ThaumaturgicLink)(ItemInit.THAUMATURGIC_LINK.get())).getLocationKey(thaumLink);
        if (structureLoc == null) {
            return null;
        }
        return WorldUtils.locateStructure(world, center, structureLoc, false);
    }

    @Nullable
    public static BlockPos locateStructure(ServerLevel world, BlockPos center, ResourceLocation structureLoc, boolean ignoreExisting) {
        Registry registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure pStructure = (Structure)registry.get(structureLoc);
        int pStructureId = registry.getId(pStructure);
        try {
            HolderSet holderset = (HolderSet)registry.getHolder(pStructureId).map(p_214491_ -> HolderSet.direct((Holder[])new Holder[]{p_214491_})).get();
            Pair pair = world.getChunkSource().getGenerator().findNearestMapStructure(world, holderset, center, 100, ignoreExisting);
            if (pair != null) {
                return (BlockPos)pair.getFirst();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }
}

