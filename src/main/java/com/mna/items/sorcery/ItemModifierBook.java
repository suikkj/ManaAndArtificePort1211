 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.Registries;
import com.mna.api.items.TieredItem;
import com.mna.api.spells.parts.Modifier;
import com.mna.items.base.IRadialMenuItem;
import com.mna.items.renderers.books.AlterationBookRenderer;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.spells.ModifierRecipe;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


public class ItemModifierBook
extends TieredItem
implements IRadialMenuItem {
    public static final String NBT_MODIFIER_ID = "key_modifier";

    public ItemModifierBook() {
        super(new Item.Properties());
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new AlterationBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ItemModifierBook.getModifier(stack).ifPresent(m -> tooltip.add((Component)Component.translatable((String)m.getRegistryName().toString()).withStyle(ChatFormatting.AQUA)));
        IRadialMenuItem.super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Nullable
    public static final Optional<Modifier> getModifier(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_MODIFIER_ID)) {
            return Optional.empty();
        }
        ResourceLocation rLoc = ResourceLocation.parse(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(NBT_MODIFIER_ID));
        return Optional.ofNullable((Modifier)Registries.Modifier.getValue(rLoc));
    }

    public static final Optional<ModifierRecipe> getRecipe(Modifier modifier, Level world) {
        return world.getRecipeManager().getAllRecipesFor((RecipeType)RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.getComponent() == modifier).findFirst();
    }

    public static final void setModifier(ItemStack stack, ResourceLocation modifierRloc) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(NBT_MODIFIER_ID, modifierRloc.toString());
    }
}


