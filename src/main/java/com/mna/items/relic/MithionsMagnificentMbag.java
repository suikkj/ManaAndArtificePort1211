/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 */
package com.mna.items.relic;

import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class MithionsMagnificentMbag
extends PractitionersPouch {
    @Override
    public int getPatchLevel(ItemStack stack, PractitionersPouchPatches patch) {
        return 9999;
    }

    public Rarity getRarity(ItemStack p_77613_1_) {
        return Rarity.EPIC;
    }

    @Override
    public PractitionersPouchPatches[] getAppliedPatches(ItemStack stack) {
        return PractitionersPouchPatches.values();
    }
}

