package com.mna.api.tools;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Compat 1.21.1: {@code DyeableLeatherItem} foi REMOVIDO. A coloracao de itens
 * passou a ser o data component {@code DYED_COLOR}.
 *
 * Esta classe preserva a API antiga {@code hasColor/getColor/setColor/clearColor}
 * lendo/escrevendo o componente.
 */
public interface MNADyeableItem {
    /** Item com este mixin deve implementar esta interface e declarar {@code implements MNADyeableItem, Item}. */

    static boolean hasColor(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    static int getColor(ItemStack stack) {
        DyedItemColor c = stack.get(DataComponents.DYED_COLOR);
        return c == null ? -1 : c.rgb();
    }

    static void setColor(ItemStack stack, int rgb) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb, true));
    }

    static void clearColor(ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
    }

    /** Reservado: itens que implementam devem expor isto. */
    default boolean isDyeable() {
        return true;
    }

    /** Reservado: itens que implementam devem expor isto. */
    default Item asItem() {
        return (Item) this;
    }
}
