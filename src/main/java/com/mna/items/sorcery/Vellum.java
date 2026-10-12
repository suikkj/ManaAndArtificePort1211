/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.TieredItem;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.items.ItemInit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class Vellum
extends TieredItem
implements ICanContainSpell {
    public Vellum() {
        super(new Item.Properties());
    }

    @Override
    public ItemStack setSpell(ItemStack stack, ISpellDefinition spell) {
        if (stack.getItem() != this) {
            return stack;
        }
        ItemStack output = new ItemStack((ItemLike)ItemInit.SPELL.get());
        spell.writeToNBT(output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        return output;
    }
}

