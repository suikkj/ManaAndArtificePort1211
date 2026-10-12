/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.gui.containers.item;

import com.mna.api.tools.MATags;
import com.mna.gui.containers.ContainerInit;
import com.mna.items.ItemInit;
import com.mna.spells.crafting.SpellRecipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ContainerSpellRecipe
extends AbstractContainerMenu {
    private List<List<ItemStack>> _cachedSpellRecipeReagents = new ArrayList<List<ItemStack>>();

    protected ContainerSpellRecipe(MenuType<?> type, int id) {
        super(type, id);
    }

    public ContainerSpellRecipe(int i, Inventory playerInv, FriendlyByteBuf buffer) {
        super((MenuType)ContainerInit.SPELL_RECIPE_LIST.get(), i);
        if (playerInv.player.getMainHandItem().getItem() == ItemInit.ENCHANTED_VELLUM.get()) {
            this.pullRecipeReagentsFromStack(playerInv.player.getMainHandItem());
        } else if (playerInv.player.getOffhandItem().getItem() == ItemInit.ENCHANTED_VELLUM.get()) {
            this.pullRecipeReagentsFromStack(playerInv.player.getOffhandItem());
        }
    }

    private void pullRecipeReagentsFromStack(ItemStack stack) {
        HashMap<ResourceLocation, Integer> reagentIDsMerged = new HashMap<ResourceLocation, Integer>();
        SpellRecipe.getShapeReagents(stack).stream().forEach(r -> {
            if (reagentIDsMerged.containsKey(r)) {
                reagentIDsMerged.put((ResourceLocation)r, (Integer)reagentIDsMerged.get(r) + 1);
            } else {
                reagentIDsMerged.put((ResourceLocation)r, 1);
            }
        });
        SpellRecipe.getComponentReagents(stack).stream().forEach(r -> {
            if (reagentIDsMerged.containsKey(r)) {
                reagentIDsMerged.put((ResourceLocation)r, (Integer)reagentIDsMerged.get(r) + 1);
            } else {
                reagentIDsMerged.put((ResourceLocation)r, 1);
            }
        });
        for (int i = 0; i < 3; ++i) {
            SpellRecipe.getModifierReagents(stack, i).stream().forEach(r -> {
                if (reagentIDsMerged.containsKey(r)) {
                    reagentIDsMerged.put((ResourceLocation)r, (Integer)reagentIDsMerged.get(r) + 1);
                } else {
                    reagentIDsMerged.put((ResourceLocation)r, 1);
                }
            });
        }
        this._cachedSpellRecipeReagents.addAll((Collection<List<ItemStack>>)this.resolveReagents(reagentIDsMerged));
    }

    private NonNullList<List<ItemStack>> resolveReagents(HashMap<ResourceLocation, Integer> reagentIDs) {
        NonNullList resolved = NonNullList.create();
        for (ResourceLocation rLoc : reagentIDs.keySet()) {
            resolved.add(MATags.smartLookupItem(rLoc).stream().map(i -> new ItemStack((ItemLike)i, ((Integer)reagentIDs.get(rLoc)).intValue())).collect(Collectors.toList()));
        }
        return resolved;
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    public List<List<ItemStack>> getReagents() {
        return this._cachedSpellRecipeReagents;
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}

