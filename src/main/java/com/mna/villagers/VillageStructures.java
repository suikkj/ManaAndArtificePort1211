/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement
 *  net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
 *  net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool$Projection
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList
 *  net.minecraftforge.event.server.ServerAboutToStartEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.mna.villagers;

import com.mna.api.tools.RLoc;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna")
public class VillageStructures {
    private static Registry<StructureTemplatePool> templatePoolRegistry;
    private static Registry<StructureProcessorList> processorListRegistry;
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY;

    @SubscribeEvent
    public static void addNewVillageBuilding(ServerAboutToStartEvent event) {
        templatePoolRegistry = (Registry)event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        processorListRegistry = (Registry)event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        VillageStructures.registerDesertVillageStructure(RLoc.create("village/desert/spellmonger"), 4);
        VillageStructures.registerDesertVillageStructure(RLoc.create("village/desert/manaweaver"), 2);
        VillageStructures.registerDesertVillageStructure(RLoc.create("village/desert/runeforge_and_runescribe"), 2);
        VillageStructures.registerDesertVillageStructure(RLoc.create("village/desert/runescribe"), 2);
        VillageStructures.registerPlainsVillageStructure(RLoc.create("village/plains/spellmonger"), 4);
        VillageStructures.registerPlainsVillageStructure(RLoc.create("village/plains/manaweaver"), 2);
        VillageStructures.registerPlainsVillageStructure(RLoc.create("village/plains/runeforge_and_runescribe"), 2);
        VillageStructures.registerPlainsVillageStructure(RLoc.create("village/plains/runescribe"), 2);
        VillageStructures.registerSavannaVillageStructure(RLoc.create("village/savanna/spellmonger"), 4);
        VillageStructures.registerSavannaVillageStructure(RLoc.create("village/savanna/manaweaver"), 2);
        VillageStructures.registerSavannaVillageStructure(RLoc.create("village/savanna/runeforge_and_runescribe"), 2);
        VillageStructures.registerSavannaVillageStructure(RLoc.create("village/savanna/runescribe"), 2);
        VillageStructures.registerSnowyVillageStructure(RLoc.create("village/snowy/spellmonger"), 4);
        VillageStructures.registerSnowyVillageStructure(RLoc.create("village/snowy/manaweaver"), 2);
        VillageStructures.registerSnowyVillageStructure(RLoc.create("village/snowy/runeforge_and_runescribe"), 2);
        VillageStructures.registerSnowyVillageStructure(RLoc.create("village/snowy/runescribe"), 2);
        VillageStructures.registerTaigaVillageStructure(RLoc.create("village/taiga/spellmonger"), 4);
        VillageStructures.registerTaigaVillageStructure(RLoc.create("village/taiga/manaweaver"), 2);
        VillageStructures.registerTaigaVillageStructure(RLoc.create("village/taiga/runeforge_and_runescribe"), 2);
        VillageStructures.registerTaigaVillageStructure(RLoc.create("village/taiga/runescribe"), 2);
    }

    private static void registerTaigaVillageStructure(ResourceLocation pieceNBTLocation, int weight) {
        VillageStructures.addBuildingToPool(ResourceLocation.parse("village/taiga/houses"), pieceNBTLocation, weight);
    }

    private static void registerDesertVillageStructure(ResourceLocation pieceNBTLocation, int weight) {
        VillageStructures.addBuildingToPool(ResourceLocation.parse("village/desert/houses"), pieceNBTLocation, weight);
    }

    private static void registerSavannaVillageStructure(ResourceLocation pieceNBTLocation, int weight) {
        VillageStructures.addBuildingToPool(ResourceLocation.parse("village/savanna/houses"), pieceNBTLocation, weight);
    }

    private static void registerSnowyVillageStructure(ResourceLocation pieceNBTLocation, int weight) {
        VillageStructures.addBuildingToPool(ResourceLocation.parse("village/snowy/houses"), pieceNBTLocation, weight);
    }

    private static void registerPlainsVillageStructure(ResourceLocation pieceNBTLocation, int weight) {
        VillageStructures.addBuildingToPool(ResourceLocation.parse("village/plains/houses"), pieceNBTLocation, weight);
    }

    private static void addBuildingToPool(ResourceLocation poolRL, ResourceLocation nbtPieceRL, int weight) {
        Holder.Reference emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        StructureTemplatePool pool = (StructureTemplatePool)templatePoolRegistry.get(poolRL);
        if (pool == null) {
            return;
        }
        SinglePoolElement piece = (SinglePoolElement)SinglePoolElement.legacy((String)nbtPieceRL.toString(), (Holder)emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
        addToPool(pool, piece, weight);
    }

    static {
        EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create((ResourceKey)Registries.PROCESSOR_LIST, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("minecraft", "empty"));
    }

    /** [PORT 1.21.1] StructureTemplatePool.templates/rawTemplates viraram private. */
    @SuppressWarnings("unchecked")
    private static void addToPool(StructureTemplatePool pool, SinglePoolElement piece, int weight) {
        try {
            java.lang.reflect.Field f = StructureTemplatePool.class.getDeclaredField("templates");
            f.setAccessible(true);
            java.util.List<StructurePoolElement> templates = (java.util.List<StructurePoolElement>)f.get(pool);
            for (int i = 0; i < weight; ++i) {
                templates.add(piece);
            }
            java.lang.reflect.Field f2 = StructureTemplatePool.class.getDeclaredField("rawTemplates");
            f2.setAccessible(true);
            java.util.List<Pair<StructurePoolElement, Integer>> raw = (java.util.List<Pair<StructurePoolElement, Integer>>)f2.get(pool);
            raw.add(new Pair(piece, weight));
        } catch (Throwable t) {
        }
    }
}

