/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.apibridge;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.IItemHelper;
import com.mna.items.sorcery.Grimoire;
import com.mna.items.sorcery.MagicStaff;
import com.mna.items.sorcery.SpellBook;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemHelper
implements IItemHelper {
    @Override
    public Item CreateStaff(Item.Properties properties, float attackDamage, float attackSpeed) {
        return new MagicStaff(properties, attackDamage, attackSpeed);
    }

    @Override
    public Item CreateGrimoire(Item.Properties properties, @Nullable ResourceLocation faction, @Nullable ResourceLocation openModel, @Nullable ResourceLocation closedModel, boolean renderBookInFirstPerson) {
        return new Grimoire(properties, faction, openModel, closedModel, renderBookInFirstPerson);
    }

    @Override
    public Item CreateSpellBook(Item.Properties properties, @Nullable ResourceLocation openModel, @Nullable ResourceLocation closedModel, boolean renderBookInFirstPerson) {
        return new SpellBook(properties, openModel, closedModel, renderBookInFirstPerson);
    }

    @Override
    public boolean AreTagsEqual(ItemStack a, ItemStack b) {
        CompoundTag ta = a.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag tb = b.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if ((ta == null || ta.isEmpty()) && (tb == null || tb.isEmpty())) {
            return true;
        }
        return Objects.equals(ta, tb);
    }
}

