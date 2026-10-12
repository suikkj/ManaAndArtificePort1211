 
package com.mna.capabilities.playerdata.magic;

import com.mna.api.cantrips.ICantrip;
import com.mna.api.capabilities.IPlayerCantrip;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.mna.api.tools.MNAItemData;

public class PlayerCantrip
implements IPlayerCantrip {
    private ResourceLocation cantripID;
    private ItemStack stack;
    private boolean itemLocked = false;
    private ArrayList<ResourceLocation> combination;

    public PlayerCantrip(ICantrip cantrip) {
        this.cantripID = cantrip.getId();
        this.combination = new ArrayList();
        this.stack = cantrip.getDefaultStack();
        this.itemLocked = cantrip.isStackLocked();
        cantrip.getDefaultCombination().forEach(r -> this.combination.add((ResourceLocation)r));
    }

    @Override
    public ResourceLocation getCantripID() {
        return this.cantripID;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        if (!this.stack.isEmpty()) {
            CompoundTag item = new CompoundTag();
            MNAItemData.save(this.stack, item);
            nbt.put("item", (Tag)item);
        }
        nbt.putInt("count", this.combination.size());
        for (int i = 0; i < this.combination.size(); ++i) {
            nbt.putString("" + i, this.combination.get(i).toString());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        if (nbt.contains("item") && !this.itemLocked) {
            this.stack = ItemStack.parseOptional(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(), (CompoundTag)nbt.getCompound("item"));
        }
        this.combination.clear();
        if (nbt.contains("count")) {
            int count = nbt.getInt("count");
            for (int i = 0; i < count; ++i) {
                ResourceLocation rLoc = ResourceLocation.parse(nbt.getString("" + i));
                this.combination.add(rLoc);
            }
        }
    }

    @Override
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ResourceLocation getPattern(int index) {
        if (index < 0 || index >= this.combination.size()) {
            return null;
        }
        return this.combination.get(index);
    }

    @Override
    public void setPatterns(List<ResourceLocation> manaweavingPatterns) {
        this.combination.clear();
        this.combination.addAll(manaweavingPatterns);
    }

    @Override
    public boolean matches(List<ManaweavingPattern> patterns) {
        if (patterns.size() != this.combination.size()) {
            return false;
        }
        for (int i = 0; i < this.combination.size(); ++i) {
            if (patterns.get(i).getId().equals(this.combination.get(i))) continue;
            return false;
        }
        return true;
    }
}

