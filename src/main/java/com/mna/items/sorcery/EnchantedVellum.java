 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.gui.containers.providers.NamedSpellRecipe;
import com.mna.items.base.IItemWithGui;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EnchantedVellum
extends Item
implements IItemWithGui<EnchantedVellum>,
ICanContainSpell {
    public EnchantedVellum(Item.Properties properties) {
        super(properties);
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedSpellRecipe();
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack heldStack = playerIn.getItemInHand(handIn);
        if (heldStack.getItem() == this && this.openGuiIfModifierPressed(heldStack, playerIn, worldIn)) {
            return InteractionResultHolder.success(heldStack);
        }
        return InteractionResultHolder.fail(heldStack);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        if (player == null) {
            return;
        }
        ArrayList<Component> flavorText = this.getFlavorText(stack);
        if (flavorText != null) {
            tooltip.addAll(flavorText);
        }
        SpellRecipe recipe = SpellRecipe.fromNBT(this.getSpellCompound(stack, player));
        recipe.addItemTooltip(stack, worldIn.level(), tooltip, player);
        IItemWithGui.super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean requiresModifierKey() {
        return false;
    }

    @Nullable
    protected ArrayList<Component> getFlavorText(ItemStack stack) {
        return null;
    }

    public Component getName(ItemStack stack) {
        SpellRecipe recipe = SpellRecipe.fromNBT(this.getSpellCompound(stack, null));
        if (recipe.isValid() && recipe.getComponents().stream().noneMatch(c -> c.getPart() == Components.LMNOP)) {
            return this.getTranslatedDisplayName(recipe);
        }
        return super.getName(stack);
    }

    protected Component getTranslatedDisplayName(SpellRecipe recipe) {
        StringBuilder sb = new StringBuilder();
        recipe.iterateComponents(c -> {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(Component.translatable((String)((SpellEffect)c.getPart()).getRegistryName().toString()).getString());
        });
        return Component.translatable((String)"item.mna.spell.recipe_display_name", (Object[])new Object[]{Component.translatable((String)((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).getRegistryName().toString()).getString(), sb.toString()});
    }

    public boolean shouldOverrideMultiplayerNbt() {
        return true;
    }

    public CompoundTag getSpellCompound(ItemStack stack, @Nullable Player player) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
}


