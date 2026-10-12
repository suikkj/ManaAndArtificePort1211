/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantments
 */
package com.mna.items.artifice;

import com.mna.items.artifice.SpellModifierRing;
import java.util.ArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import com.mna.api.enchantments.MNAEnchantmentRegistry;

public class FortuneRing
extends SpellModifierRing {
    private final ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> allowedEnchantments = new ArrayList();

    public FortuneRing(Item.Properties properties, float maxMana) {
        super(properties, maxMana);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return this.getAllowedEnchantments().contains(com.mna.api.enchantments.MNAEnchantmentRegistry.keyOf(enchantment));
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    private ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> getAllowedEnchantments() {
        if (this.allowedEnchantments.size() == 0) {
            this.allowedEnchantments.add(Enchantments.FORTUNE);
        }
        return this.allowedEnchantments;
    }
}

