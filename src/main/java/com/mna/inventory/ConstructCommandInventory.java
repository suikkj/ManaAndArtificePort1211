/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import com.mna.api.tools.MNAItemData;

public class ConstructCommandInventory
extends SimpleContainer {
    public ConstructCommandInventory(int size) {
        super(size);
    }

    public void fromTag(ListTag list) {
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag nbt = (CompoundTag)list.get(i);
            if (!nbt.contains("item") || !nbt.contains("index")) continue;
            ItemStack itemstack = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)nbt.getCompound("item"));
            int index = nbt.getInt("index");
            this.setItem(index, itemstack);
        }
    }

    public ListTag createTag() {
        ListTag listtag = new ListTag();
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);
            if (itemstack.isEmpty()) continue;
            CompoundTag item = MNAItemData.save(itemstack, new CompoundTag());
            CompoundTag tag = new CompoundTag();
            tag.put("item", (Tag)item);
            tag.putInt("index", i);
            listtag.add(tag);
        }
        return listtag;
    }
}

