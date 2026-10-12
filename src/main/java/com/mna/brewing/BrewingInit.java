 
package com.mna.brewing;

import com.mna.blocks.BlockInit;
import com.mna.brewing.ManaPotion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class BrewingInit {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, "mna");
    public static final DeferredHolder<Potion, ? extends Potion> MINOR_MANA_POTION = POTIONS.register("minor_mana_potion", () -> new ManaPotion(0));
    public static final DeferredHolder<Potion, ? extends Potion> LIGHT_MANA_POTION = POTIONS.register("light_mana_potion", () -> new ManaPotion(1));
    public static final DeferredHolder<Potion, ? extends Potion> MANA_POTION = POTIONS.register("mana_potion", () -> new ManaPotion(2));
    public static final DeferredHolder<Potion, ? extends Potion> GREATER_MANA_POTION = POTIONS.register("greater_mana_potion", () -> new ManaPotion(3));
    public static final DeferredHolder<Potion, ? extends Potion> SUPERIOR_MANA_POTION = POTIONS.register("superior_mana_potion", () -> new ManaPotion(4));

    @SubscribeEvent
    public static void initBrewingRecipes(FMLCommonSetupEvent event) {
        BrewingRecipeRegistry.addRecipe((IBrewingRecipe)new BrewingRecipe(Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD))}), Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)BlockInit.CERUBLOSSOM.get())}), new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(((Potion)MINOR_MANA_POTION.get())))));
        BrewingRecipeRegistry.addRecipe((IBrewingRecipe)new BrewingRecipe(Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD))}), Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)BlockInit.AUM.get())}), new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(((Potion)LIGHT_MANA_POTION.get())))));
        BrewingRecipeRegistry.addRecipe((IBrewingRecipe)new BrewingRecipe(Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD))}), Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)BlockInit.DESERT_NOVA.get())}), new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(((Potion)MANA_POTION.get())))));
        BrewingRecipeRegistry.addRecipe((IBrewingRecipe)new BrewingRecipe(Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD))}), Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)BlockInit.WAKEBLOOM.get())}), new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(((Potion)GREATER_MANA_POTION.get())))));
        BrewingRecipeRegistry.addRecipe((IBrewingRecipe)new BrewingRecipe(Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD))}), Ingredient.of((ItemStack[])new ItemStack[]{new ItemStack((ItemLike)BlockInit.TARMA_ROOT.get())}), new ItemStack((ItemLike)Items.POTION).set(DataComponents.POTION_CONTENTS, new PotionContents(((Potion)SUPERIOR_MANA_POTION.get())))));
    }
}


