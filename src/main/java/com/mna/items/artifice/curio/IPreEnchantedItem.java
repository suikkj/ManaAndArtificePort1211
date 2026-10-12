/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.artifice.curio;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IPreEnchantedItem<T extends Item> {
    default public boolean hasEffect(ItemStack stack) {
        return true;
    }

    default public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}

