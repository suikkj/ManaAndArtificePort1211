 
package com.mna.recipes.progression;

import net.minecraft.core.HolderLookup;
import com.google.gson.JsonObject;
import com.mna.api.config.GeneralConfigValues;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeInit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ProgressionCondition
extends AMRecipeBase {
    private ResourceLocation advancementID;
    private String _desc;
    private Component _description;

    public ProgressionCondition(ResourceLocation id) {
        super(id);
    }

    @Override
    protected void parseExtraJson(JsonObject recipeJSON) {
        this.advancementID = ResourceLocation.parse(recipeJSON.get("advancement").getAsString());
        this._desc = recipeJSON.has("description") ? recipeJSON.get("description").getAsString() : this.advancementID.toString() + ".description";
    }

    public ResourceLocation getAdvancementID() {
        return this.advancementID;
    }

    public String getDescriptionID() {
        return this._desc;
    }

    public Component getDescription() {
        if (this._description == null) {
            this._description = Component.translatable((String)this._desc);
        }
        return this._description;
    }

    public void setAdvancementID(ResourceLocation advancementID) {
        this.advancementID = advancementID;
    }

    public void setDescriptionID(String id) {
        this._desc = id;
        this._description = null;
    }

    public ItemStack assemble(CraftingContainer pContainer, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }

    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        return false;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.PROGRESSION_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.PROGRESSION_TYPE.get();
    }

    public static Optional<ProgressionCondition> get(Level world, ResourceLocation rLoc) {
        return world.getRecipeManager().getAllRecipesFor(RecipeInit.PROGRESSION_TYPE.get()).stream().filter(r -> r.id().equals(rLoc)).map(r -> r.value()).findFirst();
    }

    public static List<ProgressionCondition> get(Level world, int tier) {
        return ProgressionCondition.get(world, tier, new ArrayList<ResourceLocation>());
    }

    public static List<ProgressionCondition> get(Level world, int tier, List<ResourceLocation> except) {
        return world.getRecipeManager().getAllRecipesFor(RecipeInit.PROGRESSION_TYPE.get()).stream().filter(r -> r.value().getTier() == tier && !except.contains(r.id())).map(r -> r.value()).collect(Collectors.toList());
    }

    public static int getCompletionRequirementForTier(Level world, int tier) {
        double pctRequired = GeneralConfigValues.TierUpPercentTasksComplete;
        List<ProgressionCondition> conditions = ProgressionCondition.get(world, tier);
        return (int)Math.max(Math.round((double)conditions.size() * pctRequired), 1L);
    }
}

