/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.items;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public class DynamicItemFilter {
    private NonNullList<ItemStack> _whiteList = NonNullList.create();
    private NonNullList<ItemStack> _blackList = NonNullList.create();
    private boolean _whitelistDamageMatching;
    private boolean _whitelistTagMatching;
    private boolean _blacklistDamageMatching;
    private boolean _blacklistTagMatching;

    public CompoundTag getTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag whitelist = new CompoundTag();
        CompoundTag blacklist = new CompoundTag();
        MNAItemData.saveItems((CompoundTag)whitelist, this._whiteList);
        MNAItemData.saveItems((CompoundTag)blacklist, this._blackList);
        tag.put("whitelist", (Tag)whitelist);
        tag.put("blacklist", (Tag)blacklist);
        tag.putBoolean("whitelist_damage", this._whitelistDamageMatching);
        tag.putBoolean("whitelist_tag", this._whitelistTagMatching);
        tag.putBoolean("blacklist_damage", this._blacklistDamageMatching);
        tag.putBoolean("blacklist_tag", this._blacklistTagMatching);
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        this.clear();
        if (tag.contains("whitelist")) {
            this.loadItemsFromTag(tag.getCompound("whitelist"), this._whiteList);
        }
        if (tag.contains("blacklist")) {
            this.loadItemsFromTag(tag.getCompound("blacklist"), this._blackList);
        }
        if (tag.contains("whitelist_damage")) {
            this._whitelistDamageMatching = tag.getBoolean("whitelist_damage");
        }
        if (tag.contains("whitelist_tag")) {
            this._whitelistTagMatching = tag.getBoolean("whitelist_tag");
        }
        if (tag.contains("blacklist_damage")) {
            this._blacklistDamageMatching = tag.getBoolean("blacklist_damage");
        }
        if (tag.contains("blacklist_tag")) {
            this._blacklistTagMatching = tag.getBoolean("blacklist_tag");
        }
    }

    private void loadItemsFromTag(CompoundTag tag, NonNullList<ItemStack> list) {
        list.clear();
        ListTag listtag = tag.getList("Items", 10);
        for (int i = 0; i < listtag.size(); ++i) {
            list.add(ItemStack.EMPTY);
        }
        MNAItemData.loadItems((CompoundTag)tag, list);
    }

    public void setWhitelist(NonNullList<ItemStack> items, boolean matchDamage, boolean matchTag) {
        this._whiteList = NonNullList.createWithCapacity((int)24);
        this._whiteList.addAll(items);
        this._whitelistDamageMatching = matchDamage;
        this._whitelistTagMatching = matchTag;
    }

    public void setBlacklist(NonNullList<ItemStack> items, boolean matchDamage, boolean matchTag) {
        this._blackList = NonNullList.createWithCapacity((int)24);
        this._blackList.addAll(items);
        this._blacklistDamageMatching = matchDamage;
        this._blacklistTagMatching = matchTag;
    }

    public NonNullList<ItemStack> getWhiteList() {
        return this._whiteList;
    }

    public NonNullList<ItemStack> getBlackList() {
        return this._blackList;
    }

    public boolean isWhitelistEmpty() {
        for (int i = 0; i < this._whiteList.size(); ++i) {
            if (((ItemStack)this._whiteList.get(i)).isEmpty()) continue;
            return false;
        }
        return true;
    }

    public boolean isBlacklistEmpty() {
        for (int i = 0; i < this._blackList.size(); ++i) {
            if (((ItemStack)this._blackList.get(i)).isEmpty()) continue;
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return this.isWhitelistEmpty() && this.isBlacklistEmpty();
    }

    private boolean matchStack(ItemStack check, NonNullList<ItemStack> filterList, boolean matchDamage, boolean matchTags) {
        return filterList.stream().anyMatch(is -> {
            if (is.getItem() != check.getItem()) {
                return false;
            }
            if (matchDamage && check.getDamageValue() < is.getDamageValue()) {
                return false;
            }
            return !matchTags || ManaAndArtificeMod.getItemHelper().AreTagsEqual((ItemStack)is, check);
        });
    }

    public void copyFrom(DynamicItemFilter other) {
        if (other == null) {
            return;
        }
        this.setWhitelist(other.getWhiteList(), other._whitelistDamageMatching, other._whitelistTagMatching);
        this.setBlacklist(other.getBlackList(), other._blacklistDamageMatching, other._blacklistTagMatching);
    }

    public void clear() {
        this._whiteList = NonNullList.create();
        this._blackList = NonNullList.create();
    }

    public boolean matches(ItemStack stack) {
        return (this.isWhitelistEmpty() || this.matchStack(stack, this._whiteList, this._whitelistDamageMatching, this._whitelistTagMatching)) && !this.matchStack(stack, this._blackList, this._blacklistDamageMatching, this._blacklistTagMatching);
    }

    public boolean getWhitelistMatchDurability() {
        return this._whitelistDamageMatching;
    }

    public boolean getWhitelistMatchTag() {
        return this._whitelistTagMatching;
    }

    public boolean getBlacklistMatchTag() {
        return this._blacklistTagMatching;
    }

    public boolean getBlacklistMatchDurability() {
        return this._blacklistDamageMatching;
    }

    public int hashStack(ItemStack stack) {
        int itemHash = stack.getItem().hashCode();
        if (this.isWhitelistEmpty()) {
            return itemHash;
        }
        if (this._whitelistDamageMatching) {
            itemHash += stack.getDamageValue();
        }
        if (this._whitelistTagMatching && stack.has(DataComponents.CUSTOM_DATA)) {
            itemHash += stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().hashCode();
        }
        return itemHash;
    }
}

