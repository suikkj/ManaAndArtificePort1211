/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.recipes;

import com.mna.api.tools.RLoc;
import com.mna.recipes.arcanefurnace.ArcaneFurnaceRecipe;
import com.mna.recipes.arcanefurnace.ArcaneFurnaceRecipeSerializer;
import com.mna.recipes.crush.CrushingRecipe;
import com.mna.recipes.crush.CrushingRecipeSerializer;
import com.mna.recipes.eldrin.EldrinAltarRecipe;
import com.mna.recipes.eldrin.EldrinAltarRecipeSerializer;
import com.mna.recipes.eldrin.FumeFilterRecipe;
import com.mna.recipes.eldrin.FumeFilterRecipeSerializer;
import com.mna.recipes.manaweaving.ManaweaveCacheEffect;
import com.mna.recipes.manaweaving.ManaweaveCacheEffectSerializer;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternSerializer;
import com.mna.recipes.manaweaving.ManaweavingRecipe;
import com.mna.recipes.manaweaving.ManaweavingRecipeSerializer;
import com.mna.recipes.manaweaving.TransmutationRecipe;
import com.mna.recipes.manaweaving.TransmutationRecipeSerializer;
import com.mna.recipes.multiblock.MultiblockDefinition;
import com.mna.recipes.multiblock.MultiblockRecipeSerializer;
import com.mna.recipes.progression.ProgressionCondition;
import com.mna.recipes.progression.ProgressionConditionSerializer;
import com.mna.recipes.rituals.RitualRecipe;
import com.mna.recipes.rituals.RitualRecipeSerializer;
import com.mna.recipes.runeforging.RuneforgingRecipe;
import com.mna.recipes.runeforging.RuneforgingRecipeSerializer;
import com.mna.recipes.runeforging.RunescribingRecipe;
import com.mna.recipes.runeforging.RunescribingRecipeSerializer;
import com.mna.recipes.spells.ComponentRecipe;
import com.mna.recipes.spells.ComponentRecipeSerializer;
import com.mna.recipes.spells.ModifierRecipe;
import com.mna.recipes.spells.ModifierRecipeSerializer;
import com.mna.recipes.spells.ShapeRecipe;
import com.mna.recipes.spells.ShapeRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class RecipeInit {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "mna");
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "mna");

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> DeferredHolder<RecipeType<T>, RecipeType<T>> type(String id) {
        return (DeferredHolder<RecipeType<T>, RecipeType<T>>)(DeferredHolder)TYPES.register(id, () -> RecipeType.simple(RLoc.create(id)));
    }

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> DeferredHolder<RecipeSerializer<T>, RecipeSerializer<T>> serializer(String id, java.util.function.Supplier<? extends RecipeSerializer<T>> supplier) {
        return (DeferredHolder<RecipeSerializer<T>, RecipeSerializer<T>>)(DeferredHolder)SERIALIZERS.register(id, supplier);
    }
    public static DeferredHolder<RecipeType<RitualRecipe>, ? extends RecipeType<RitualRecipe>> RITUAL_TYPE = type("ritual-type");
    public static DeferredHolder<RecipeType<ManaweavingPattern>, ? extends RecipeType<ManaweavingPattern>> MANAWEAVING_PATTERN_TYPE = type("manaweaving-pattern-type");
    public static DeferredHolder<RecipeType<ManaweavingRecipe>, ? extends RecipeType<ManaweavingRecipe>> MANAWEAVING_RECIPE_TYPE = type("manaweaving-recipe-type");
    public static DeferredHolder<RecipeType<RunescribingRecipe>, ? extends RecipeType<RunescribingRecipe>> RUNESCRIBING_TYPE = type("runescribing-type");
    public static DeferredHolder<RecipeType<RuneforgingRecipe>, ? extends RecipeType<RuneforgingRecipe>> RUNEFORGING_TYPE = type("runeforging-type");
    public static DeferredHolder<RecipeType<CrushingRecipe>, ? extends RecipeType<CrushingRecipe>> CRUSHING_TYPE = type("crushing-type");
    public static DeferredHolder<RecipeType<ArcaneFurnaceRecipe>, ? extends RecipeType<ArcaneFurnaceRecipe>> ARCANE_FURNACE_TYPE = type("arcane-furnace-type");
    public static DeferredHolder<RecipeType<MultiblockDefinition>, ? extends RecipeType<MultiblockDefinition>> MULTIBLOCK_TYPE = type("multiblock-recipe-type");
    public static DeferredHolder<RecipeType<EldrinAltarRecipe>, ? extends RecipeType<EldrinAltarRecipe>> ELDRIN_ALTAR_TYPE = type("eldrin-altar-recipe-type");
    public static DeferredHolder<RecipeType<ManaweaveCacheEffect>, ? extends RecipeType<ManaweaveCacheEffect>> MANAWEAVE_CACHE_EFFECT_TYPE = type("manaweave-cache-effect-recipe-type");
    public static DeferredHolder<RecipeType<TransmutationRecipe>, ? extends RecipeType<TransmutationRecipe>> TRANSMUTATION_TYPE = type("transmutation-recipe-type");
    public static DeferredHolder<RecipeType<ProgressionCondition>, ? extends RecipeType<ProgressionCondition>> PROGRESSION_TYPE = type("progression-condition-type");
    public static DeferredHolder<RecipeType<FumeFilterRecipe>, ? extends RecipeType<FumeFilterRecipe>> FUME_FILTER_TYPE = type("fume-filter-recipe-type");
    public static DeferredHolder<RecipeType<ShapeRecipe>, ? extends RecipeType<ShapeRecipe>> SHAPE_TYPE = type("shape-recipe-type");
    public static DeferredHolder<RecipeType<ComponentRecipe>, ? extends RecipeType<ComponentRecipe>> COMPONENT_TYPE = type("component-recipe-type");
    public static DeferredHolder<RecipeType<ModifierRecipe>, ? extends RecipeType<ModifierRecipe>> MODIFIER_TYPE = type("modifier-recipe-type");
    public static final DeferredHolder<RecipeSerializer<RitualRecipe>, ? extends RecipeSerializer<RitualRecipe>> RITUAL_SERIALIZER = serializer("ritual", RitualRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ManaweavingPattern>, ? extends RecipeSerializer<ManaweavingPattern>> MANAWEAVING_SERIALIZER = serializer("manaweaving-pattern", ManaweavingPatternSerializer::new);
    public static final DeferredHolder<RecipeSerializer<RunescribingRecipe>, ? extends RecipeSerializer<RunescribingRecipe>> RUNESCRIBING_SERIALIZER = serializer("runescribing", RunescribingRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<RuneforgingRecipe>, ? extends RecipeSerializer<RuneforgingRecipe>> RUNEFORGING_SERIALIZER = serializer("runeforging", RuneforgingRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<CrushingRecipe>, ? extends RecipeSerializer<CrushingRecipe>> CRUSHING_SERIALIZER = serializer("crushing", CrushingRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ManaweavingRecipe>, ? extends RecipeSerializer<ManaweavingRecipe>> MANAWEAVING_RECIPE_SERIALIZER = serializer("manaweaving-recipe", ManaweavingRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ShapeRecipe>, ? extends RecipeSerializer<ShapeRecipe>> SHAPE_RECIPE_SERIALIZER = serializer("shape", ShapeRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ComponentRecipe>, ? extends RecipeSerializer<ComponentRecipe>> COMPONENT_RECIPE_SERIALIZER = serializer("component", ComponentRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ModifierRecipe>, ? extends RecipeSerializer<ModifierRecipe>> MODIFIER_RECIPE_SERIALIZER = serializer("modifier", ModifierRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ArcaneFurnaceRecipe>, ? extends RecipeSerializer<ArcaneFurnaceRecipe>> ARCANE_FURNACE_RECIPE_SERIALIZER = serializer("arcane-furnace", ArcaneFurnaceRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<MultiblockDefinition>, ? extends RecipeSerializer<MultiblockDefinition>> MULTIBLOCK_RECIPE_SERIALIZER = serializer("multiblock", MultiblockRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<EldrinAltarRecipe>, ? extends RecipeSerializer<EldrinAltarRecipe>> ELDRIN_ALTAR_RECIPE_SERIALIZER = serializer("eldrin-altar", EldrinAltarRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ManaweaveCacheEffect>, ? extends RecipeSerializer<ManaweaveCacheEffect>> MANAWEAVE_CACHE_EFFECT_SERIALIZER = serializer("manaweave-cache-effect", ManaweaveCacheEffectSerializer::new);
    public static final DeferredHolder<RecipeSerializer<TransmutationRecipe>, ? extends RecipeSerializer<TransmutationRecipe>> TRANSMUTATION_SERIALIZER = serializer("transmutation", TransmutationRecipeSerializer::new);
    public static final DeferredHolder<RecipeSerializer<ProgressionCondition>, ? extends RecipeSerializer<ProgressionCondition>> PROGRESSION_SERIALIZER = serializer("progression-condition", ProgressionConditionSerializer::new);
    public static final DeferredHolder<RecipeSerializer<FumeFilterRecipe>, ? extends RecipeSerializer<FumeFilterRecipe>> ELDRIN_FUME_RECIPE_SERIALIZER = serializer("eldrin-fume", FumeFilterRecipeSerializer::new);
}


