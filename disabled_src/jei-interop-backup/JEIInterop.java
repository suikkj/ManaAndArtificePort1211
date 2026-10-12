/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mezz.jei.api.IModPlugin
 *  mezz.jei.api.JeiPlugin
 *  mezz.jei.api.constants.RecipeTypes
 *  mezz.jei.api.gui.handlers.IGuiContainerHandler
 *  mezz.jei.api.helpers.IGuiHelper
 *  mezz.jei.api.ingredients.IIngredientHelper
 *  mezz.jei.api.ingredients.IIngredientRenderer
 *  mezz.jei.api.ingredients.IIngredientType
 *  mezz.jei.api.recipe.RecipeType
 *  mezz.jei.api.recipe.category.IRecipeCategory
 *  mezz.jei.api.recipe.transfer.IRecipeTransferInfo
 *  mezz.jei.api.registration.IGuiHandlerRegistration
 *  mezz.jei.api.registration.IModIngredientRegistration
 *  mezz.jei.api.registration.IRecipeCatalystRegistration
 *  mezz.jei.api.registration.IRecipeCategoryRegistration
 *  mezz.jei.api.registration.IRecipeRegistration
 *  mezz.jei.api.registration.IRecipeTransferRegistration
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 */
package com.mna.interop.jei;

import com.mna.blocks.BlockInit;
import com.mna.gui.base.GuiJEIDisable;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.block.ContainerMagiciansWorkbench;
import com.mna.gui.containers.block.ContainerSpectralCraftingTable;
import com.mna.interop.jei.MARecipeTypes;
import com.mna.interop.jei.categories.ArcaneFurnaceRecipeCategory;
import com.mna.interop.jei.categories.CrushingRecipeCategory;
import com.mna.interop.jei.categories.EldrinAltarRecipeCategory;
import com.mna.interop.jei.categories.EldrinFumeRecipeCategory;
import com.mna.interop.jei.categories.ManaweavingAltarRecipeCategory;
import com.mna.interop.jei.categories.RitualRecipeCategory;
import com.mna.interop.jei.categories.RunescribingRecipeCategory;
import com.mna.interop.jei.categories.RunicAnvilRecipeCategory;
import com.mna.interop.jei.categories.TransmutationRecipeCategory;
import com.mna.interop.jei.ingredients.ManaweavePatternIngredient;
import com.mna.interop.jei.ingredients.ManaweavePatternIngredientRenderer;
import com.mna.interop.jei.ingredients.ManaweavePatternIngredientType;
import com.mna.interop.jei.ingredients.ManaweavingPatternIngredientHelper;
import com.mna.items.ItemInit;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class JEIInterop
implements IModPlugin {
    private EldrinAltarRecipeCategory eldrinAltarRecipeCategory;
    private RitualRecipeCategory ritualRecipeCategory;
    private ManaweavingAltarRecipeCategory manaweavingRecipeCategory;
    private ArcaneFurnaceRecipeCategory arcaneFurnaceRecipeCategory;
    private RunicAnvilRecipeCategory runicAnvilRecipeCategory;
    private RunescribingRecipeCategory runescribingRecipeCategory;
    private CrushingRecipeCategory crushingRecipeCategory;
    private TransmutationRecipeCategory transmutationRecipeCategory;
    private EldrinFumeRecipeCategory fumeRecipeCategory;
    private Minecraft minecraft = Minecraft.getInstance();
    public static final ManaweavePatternIngredientRenderer manaweaveIngredientRenderer = new ManaweavePatternIngredientRenderer();
    public static final ManaweavePatternIngredientType MANAWEAVE_PATTERN = new ManaweavePatternIngredientType();

    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("mna", "jei-integration");
    }

    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(GuiJEIDisable.class, new IGuiContainerHandler<GuiJEIDisable<?>>(){

            public List<Rect2i> getGuiExtraAreas(GuiJEIDisable<?> containerScreen) {
                return containerScreen.getSideWindowBounds();
            }
        });
    }

    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        this.ritualRecipeCategory = new RitualRecipeCategory(guiHelper);
        this.manaweavingRecipeCategory = new ManaweavingAltarRecipeCategory(guiHelper);
        this.arcaneFurnaceRecipeCategory = new ArcaneFurnaceRecipeCategory(guiHelper);
        this.runicAnvilRecipeCategory = new RunicAnvilRecipeCategory(guiHelper);
        this.runescribingRecipeCategory = new RunescribingRecipeCategory(guiHelper);
        this.eldrinAltarRecipeCategory = new EldrinAltarRecipeCategory(guiHelper);
        this.crushingRecipeCategory = new CrushingRecipeCategory(guiHelper);
        this.transmutationRecipeCategory = new TransmutationRecipeCategory(guiHelper);
        this.fumeRecipeCategory = new EldrinFumeRecipeCategory(guiHelper);
        registration.addRecipeCategories(new IRecipeCategory[]{this.ritualRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.manaweavingRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.arcaneFurnaceRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.runicAnvilRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.runescribingRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.eldrinAltarRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.crushingRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.transmutationRecipeCategory});
        registration.addRecipeCategories(new IRecipeCategory[]{this.fumeRecipeCategory});
    }

    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register((IIngredientType)MANAWEAVE_PATTERN, (Collection)ManaweavingPatternHelper.getAllPatterns((Level)this.minecraft.level).stream().map(p -> new ManaweavePatternIngredient(p.getId())).collect(Collectors.toList()), (IIngredientHelper)new ManaweavingPatternIngredientHelper(), (IIngredientRenderer)manaweaveIngredientRenderer);
    }

    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel world = this.minecraft.level;
        registration.addRecipes(MARecipeTypes.RITUAL, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.RITUAL_TYPE.get()));
        registration.addRecipes(MARecipeTypes.ELDRIN_ALTAR, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.ELDRIN_ALTAR_TYPE.get()));
        registration.addRecipes(MARecipeTypes.MANAWEAVING, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.MANAWEAVING_RECIPE_TYPE.get()));
        registration.addRecipes(MARecipeTypes.ARCANE_FURNACE, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.ARCANE_FURNACE_TYPE.get()));
        registration.addRecipes(MARecipeTypes.RUNEFORGING, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.RUNEFORGING_TYPE.get()));
        registration.addRecipes(MARecipeTypes.RUNESCRIBING, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.RUNESCRIBING_TYPE.get()));
        registration.addRecipes(MARecipeTypes.CRUSHING, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.CRUSHING_TYPE.get()));
        registration.addRecipes(MARecipeTypes.TRANSMUTATION, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.TRANSMUTATION_TYPE.get()));
        registration.addRecipes(MARecipeTypes.FUME, world.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)RecipeInit.FUME_FILTER_TYPE.get()));
    }

    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ContainerSpectralCraftingTable.class, (MenuType)ContainerInit.SPECTRAL_CRAFTING_TABLE.get(), RecipeTypes.CRAFTING, 1, 9, 10, 36);
        registration.addRecipeTransferHandler((IRecipeTransferInfo)new IRecipeTransferInfo<ContainerMagiciansWorkbench, CraftingRecipe>(){

            public Class<ContainerMagiciansWorkbench> getContainerClass() {
                return ContainerMagiciansWorkbench.class;
            }

            public boolean canHandle(ContainerMagiciansWorkbench container, CraftingRecipe recipe) {
                return true;
            }

            public List<Slot> getRecipeSlots(ContainerMagiciansWorkbench container, CraftingRecipe recipe) {
                ArrayList<Slot> slots = new ArrayList<Slot>();
                if (container.craftingGridIsEmpty(false)) {
                    for (int i = ContainerMagiciansWorkbench.FIRST_CRAFT_GRID_START; i < ContainerMagiciansWorkbench.FIRST_OUTPUT_SLOT; ++i) {
                        Slot slot = container.getSlot(i);
                        slots.add(slot);
                    }
                } else {
                    for (int i = ContainerMagiciansWorkbench.SECOND_CRAFT_GRID_START; i < ContainerMagiciansWorkbench.SECOND_OUTPUT_SLOT; ++i) {
                        Slot slot = container.getSlot(i);
                        slots.add(slot);
                    }
                }
                return slots;
            }

            public List<Slot> getInventorySlots(ContainerMagiciansWorkbench container, CraftingRecipe recipe) {
                Slot slot;
                int i;
                ArrayList<Slot> slots = new ArrayList<Slot>();
                for (i = ContainerMagiciansWorkbench.INVENTORY_STORAGE_START; i <= ContainerMagiciansWorkbench.INVENTORY_STORAGE_END; ++i) {
                    slot = container.getSlot(i);
                    slots.add(slot);
                }
                for (i = ContainerMagiciansWorkbench.PLAYER_INVENTORY_START; i <= ContainerMagiciansWorkbench.PLAYER_ACTION_BAR_END; ++i) {
                    slot = container.getSlot(i);
                    slots.add(slot);
                }
                return slots;
            }

            public Optional<MenuType<ContainerMagiciansWorkbench>> getMenuType() {
                return Optional.of((MenuType)ContainerInit.MAGICIANS_WORKBENCH.get());
            }

            public RecipeType<CraftingRecipe> getRecipeType() {
                return RecipeTypes.CRAFTING;
            }
        });
    }

    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.RUNEFORGE.get()), new RecipeType[]{MARecipeTypes.ARCANE_FURNACE, RecipeTypes.CRAFTING, RecipeTypes.SMELTING});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.RUNESCRIBING_TABLE.get()), new RecipeType[]{MARecipeTypes.RUNESCRIBING});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.RUNIC_ANVIL.get()), new RecipeType[]{MARecipeTypes.RUNEFORGING});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.MANAWEAVING_ALTAR.get()), new RecipeType[]{MARecipeTypes.MANAWEAVING});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.INSCRIPTION_TABLE.get()), new RecipeType[]{MARecipeTypes.SHAPE, MARecipeTypes.COMPONENT, MARecipeTypes.MODIFIER});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.ELDRIN_ALTAR.get()), new RecipeType[]{MARecipeTypes.ELDRIN_ALTAR});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.CONSTRUCT_WORKBENCH.get()), new RecipeType[]{MARecipeTypes.CRUSHING});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)Blocks.GRINDSTONE), new RecipeType[]{MARecipeTypes.CRUSHING});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)ItemInit.WIZARD_CHALK.get()), new RecipeType[]{MARecipeTypes.RITUAL});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)ItemInit.PRACTITIONERS_POUCH.get()), new RecipeType[]{MARecipeTypes.RITUAL});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND.get()), new RecipeType[]{MARecipeTypes.TRANSMUTATION});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND_ADVANCED.get()), new RecipeType[]{MARecipeTypes.TRANSMUTATION});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)ItemInit.MANAWEAVER_WAND_IMPROVISED.get()), new RecipeType[]{MARecipeTypes.TRANSMUTATION});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.MAGICIANS_WORKBENCH.get()), new RecipeType[]{RecipeTypes.CRAFTING});
        registration.addRecipeCatalyst(new ItemStack((ItemLike)BlockInit.ELDRIN_FUME.get()), new RecipeType[]{MARecipeTypes.FUME});
    }
}

