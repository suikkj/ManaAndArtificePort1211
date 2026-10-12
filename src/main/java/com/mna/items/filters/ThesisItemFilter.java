/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.filters;

import com.mna.api.spells.ICanContainSpell;
import com.mna.items.filters.ItemFilter;
import com.mna.items.sorcery.ItemTornJournalPage;
import net.minecraft.world.item.ItemStack;

public class ThesisItemFilter
extends ItemFilter {
    @Override
    public boolean IsValidItem(ItemStack stack) {
        return stack.getItem() instanceof ItemTornJournalPage || stack.getItem() instanceof ICanContainSpell;
    }
}

