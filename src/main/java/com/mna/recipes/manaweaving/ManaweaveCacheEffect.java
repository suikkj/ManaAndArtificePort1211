 
package com.mna.recipes.manaweaving;

import net.minecraft.core.HolderLookup;
import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeInit;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ManaweaveCacheEffect
extends AMRecipeBase {
    private ResourceLocation effect;
    private int magnitude;
    private int durationMin;
    private int durationMax;

    public ManaweaveCacheEffect(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public void parseExtraJson(JsonObject recipeJSON) {
        if (recipeJSON.has("effect")) {
            this.effect = ResourceLocation.parse(recipeJSON.get("effect").getAsString());
        }
        this.magnitude = recipeJSON.has("magnitude") ? recipeJSON.get("magnitude").getAsInt() : 1;
        if (recipeJSON.has("duration_min")) {
            this.durationMin = recipeJSON.get("duration_min").getAsInt() * 20;
        }
        if (recipeJSON.has("duration_max")) {
            this.durationMax = recipeJSON.get("duration_max").getAsInt() * 20;
        }
        if (this.durationMin < 0) {
            this.durationMin = 0;
        }
        if (this.durationMax < 0) {
            this.durationMax = 0;
        }
        if (this.durationMax < this.durationMin) {
            int temp = this.durationMin;
            this.durationMin = this.durationMax;
            this.durationMax = temp;
        }
    }

    @Override
    public void runValidation() {
        super.runValidation();
        if (this.durationMin == 0) {
            ManaAndArtifice.LOGGER.error("Manaweave cache effect recipe has a duration minimum of 0, this may cause problems! [" + this.getId().toString() + "]");
        }
        if (this.durationMax == 0) {
            ManaAndArtifice.LOGGER.error("Manaweave cache effect recipe has a duration maximum of 0, this may cause problems! [" + this.getId().toString() + "]");
        }
    }

    public boolean matches(CraftingContainer inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public ResourceLocation getEffect() {
        return this.effect;
    }

    public void setEffect(ResourceLocation effect) {
        this.effect = effect;
    }

    public int getDurationMin() {
        return this.durationMin;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public int getDurationMax() {
        return this.durationMax;
    }

    public void setDurationMax(int durationMax) {
        this.durationMax = durationMax;
    }

    public int getMagnitude() {
        return this.magnitude;
    }

    public void setMagnitude(int magnitude) {
        this.magnitude = magnitude;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.MANAWEAVE_CACHE_EFFECT_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.MANAWEAVE_CACHE_EFFECT_TYPE.get();
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return this.getResultItem();
    }

    public ItemStack assemble(CraftingContainer p_44001_, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return false;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }
}

