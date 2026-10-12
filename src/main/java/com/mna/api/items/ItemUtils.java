/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.items;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
    private static final String KEY_CHARGES = "charges";
    private static final String KEY_MAX_CHARGES = "max_charges";

    public static void writeMaxCharges(ItemStack stack, int charges) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_MAX_CHARGES, charges);
    }

    public static void writeCharges(ItemStack stack, int charges) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_CHARGES, charges);
    }

    public static int getCharges(ItemStack stack) {
        return ItemUtils.hasCharges(stack) ? stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_CHARGES) : 0;
    }

    public static int getMaxCharges(ItemStack stack) {
        return ItemUtils.hasCharges(stack) ? stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_MAX_CHARGES) : 0;
    }

    public static boolean hasCharges(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_CHARGES);
    }
}

