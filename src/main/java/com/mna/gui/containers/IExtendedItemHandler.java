/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraftforge.items.IItemHandlerModifiable
 */
package com.mna.gui.containers;

import com.mna.inventory.stack_extension.IInventoryListener;
import com.mna.inventory.stack_extension.MAInventoryData;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public interface IExtendedItemHandler
extends IItemHandlerModifiable {
    default public int getSlots() {
        return this.size();
    }

    public int size();

    public long getCountInSlot(int var1);

    public void enlarge(int var1);

    public CompoundTag serialize();

    public void deserialize(CompoundTag var1);

    default public void setInventoryData(MAInventoryData<?> data) {
    }

    @Nullable
    default public MAInventoryData<?> getInventoryData() {
        return null;
    }

    default public void markDirty() {
        MAInventoryData<?> data = this.getInventoryData();
        if (data != null) {
            data.setDirty();
        }
    }

    public void addListener(IInventoryListener var1);
}

