/*
 * Decompiled with CFR 0.152.
 */
package com.mna.guide.recipe.init;

import com.mna.ManaAndArtifice;
import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.guide.recipe.Recipe3x3;
import com.mna.guide.recipe.RecipeArcaneFurnace;
import com.mna.guide.recipe.RecipeCrushing;
import com.mna.guide.recipe.RecipeEldrinAltar;
import com.mna.guide.recipe.RecipeEntity;
import com.mna.guide.recipe.RecipeFumeFilter;
import com.mna.guide.recipe.RecipeManaweavePattern;
import com.mna.guide.recipe.RecipeManaweaving;
import com.mna.guide.recipe.RecipeMaterialsList;
import com.mna.guide.recipe.RecipeRitual;
import com.mna.guide.recipe.RecipeRunescribing;
import com.mna.guide.recipe.RecipeRunesmith;
import com.mna.guide.recipe.RecipeSpellPart;
import com.mna.guide.recipe.RecipeTransmute;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class RecipeRenderers {
    public static final String CRAFTING = "crafting";
    public static final String MANAWEAVING_PATTERN = "manaweaving_pattern";
    public static final String MANAWEAVING_ALTAR = "manaweaving_altar";
    public static final String RITUAL = "ritual";
    public static final String RUNESMITHING = "runesmithing";
    public static final String ARCANE_FURNACE = "arcane_furnace";
    public static final String CRUSHING = "crushing";
    public static final String RUNESCRIBING = "runescribing";
    public static final String SPELL = "spell_part";
    public static final String ELDRIN_ALTAR = "eldrin_altar";
    public static final String MULTIBLOCK = "multiblock";
    public static final String ENTITY = "entity";
    public static final String TRANSMUTATION = "transmutation";
    public static final String FUME_FILTER = "fume_filter";
    private static final HashMap<String, Class<? extends RecipeRendererBase>> _registry = new HashMap();

    public static void registerRecipeRenderer(String identifier, Class<? extends RecipeRendererBase> clazz) {
        if (_registry.containsKey(identifier)) {
            ManaAndArtifice.LOGGER.warn("Registering new renderer for " + identifier + ", this will override what is already there.");
        }
        _registry.put(identifier, clazz);
    }

    public static RecipeRendererBase instantiate(String _type, int x, int y) {
        Class clazz = _registry.getOrDefault(_type, null);
        if (clazz == null) {
            return null;
        }
        try {
            Constructor ctor = clazz.getDeclaredConstructor(Integer.TYPE, Integer.TYPE);
            if (ctor != null) {
                return (RecipeRendererBase)(ctor.newInstance(x, y));
            }
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        catch (SecurityException e) {
            return null;
        }
        catch (InstantiationException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        catch (InvocationTargetException e) {
            return null;
        }
        return null;
    }

    static {
        RecipeRenderers.registerRecipeRenderer(CRAFTING, Recipe3x3.class);
        RecipeRenderers.registerRecipeRenderer(MANAWEAVING_PATTERN, RecipeManaweavePattern.class);
        RecipeRenderers.registerRecipeRenderer(MANAWEAVING_ALTAR, RecipeManaweaving.class);
        RecipeRenderers.registerRecipeRenderer(RITUAL, RecipeRitual.class);
        RecipeRenderers.registerRecipeRenderer(RUNESMITHING, RecipeRunesmith.class);
        RecipeRenderers.registerRecipeRenderer(ARCANE_FURNACE, RecipeArcaneFurnace.class);
        RecipeRenderers.registerRecipeRenderer(CRUSHING, RecipeCrushing.class);
        RecipeRenderers.registerRecipeRenderer(RUNESCRIBING, RecipeRunescribing.class);
        RecipeRenderers.registerRecipeRenderer(SPELL, RecipeSpellPart.class);
        RecipeRenderers.registerRecipeRenderer(ELDRIN_ALTAR, RecipeEldrinAltar.class);
        RecipeRenderers.registerRecipeRenderer(MULTIBLOCK, RecipeMaterialsList.class);
        RecipeRenderers.registerRecipeRenderer(ENTITY, RecipeEntity.class);
        RecipeRenderers.registerRecipeRenderer(TRANSMUTATION, RecipeTransmute.class);
        RecipeRenderers.registerRecipeRenderer(FUME_FILTER, RecipeFumeFilter.class);
    }
}

