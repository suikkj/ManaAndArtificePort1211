/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.inventory;

import com.mna.api.capabilities.IPlayerCantrip;
import com.mna.api.capabilities.IPlayerCantrips;
import com.mna.cantrips.CantripRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryCantrips
implements Container {
    private List<IPlayerCantrip> cantripsWithInventory = new ArrayList<IPlayerCantrip>();

    public InventoryCantrips(IPlayerCantrips cantripData) {
        cantripData.getCantrips().forEach(c -> CantripRegistry.INSTANCE.getCantrip(c.getCantripID()).ifPresent(rc -> {
            if (!rc.isStackLocked()) {
                this.cantripsWithInventory.add((IPlayerCantrip)c);
            }
        }));
    }

    public void setItem(int index, ItemStack stack) {
        if (index < 0 || index >= this.cantripsWithInventory.size()) {
            return;
        }
        this.cantripsWithInventory.get(index).setStack(stack);
    }

    public ItemStack getItem(int index) {
        if (index < 0 || index >= this.cantripsWithInventory.size()) {
            return ItemStack.EMPTY;
        }
        return this.cantripsWithInventory.get(index).getStack();
    }

    public int getMaxStackSize() {
        return 1;
    }

    public void clearContent() {
        this.cantripsWithInventory.forEach(c -> c.setStack(ItemStack.EMPTY));
    }

    public int getContainerSize() {
        return this.cantripsWithInventory.size();
    }

    public boolean isEmpty() {
        return this.cantripsWithInventory.stream().allMatch(c -> c.getStack().isEmpty());
    }

    public ItemStack removeItem(int index, int count) {
        return this.removeItemNoUpdate(index);
    }

    public ItemStack removeItemNoUpdate(int index) {
        if (index < 0 || index >= this.cantripsWithInventory.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.cantripsWithInventory.get(index).getStack();
        this.cantripsWithInventory.get(index).setStack(ItemStack.EMPTY);
        return stack;
    }

    public void setChanged() {
    }

    public boolean stillValid(Player player) {
        return true;
    }
}

