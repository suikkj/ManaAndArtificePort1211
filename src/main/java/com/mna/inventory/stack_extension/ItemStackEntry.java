/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  org.apache.logging.log4j.Logger
 */
package com.mna.inventory.stack_extension;

import com.mna.ManaAndArtifice;
import com.mna.inventory.stack_extension.SlotInfoTable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

public class ItemStackEntry {
    public static Codec<ItemStackEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(ItemStack.CODEC.fieldOf("item").forGetter(o -> o.stack), Codec.LONG.fieldOf("count").forGetter(o -> o.count), SlotInfoTable.CODEC.optionalFieldOf("info").forGetter(o -> o.info == null || o.info.isEmpty() ? Optional.empty() : Optional.of(o.info))).apply(instance, (a, b, c) -> new ItemStackEntry((ItemStack)a, (long)b, c.orElse(new SlotInfoTable()))));
    public static ItemStackEntry EMPTY = new ItemStackEntry(ItemStack.EMPTY);
    private ItemStack stack;
    private long count;
    private SlotInfoTable info;

    public ItemStackEntry(ItemStack stack) {
        this.stack = stack;
        this.count = stack.getCount();
        this.info = null;
    }

    private ItemStackEntry(ItemStack stack, long count, SlotInfoTable info) {
        this.stack = stack;
        this.count = count;
        this.info = info;
    }

    public ItemStack getStackOriginal() {
        return this.stack;
    }

    public ItemStack getStackCopy() {
        ItemStack copy = this.stack.copy();
        copy.setCount((int)this.count);
        return copy;
    }

    public ItemStack extract(int count) {
        if ((long)count > this.count) {
            count = (int)this.count;
        }
        if (this.stack.getCount() == count) {
            this.count = 0L;
            ItemStack result = this.stack;
            this.stack = ItemStack.EMPTY;
            return result;
        }
        ItemStack copy = this.getStackCopy();
        copy.setCount(count);
        this.count -= (long)count;
        return copy;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
        this.stack.setCount((int)this.count);
    }

    public void grow(long count) {
        this.count += count;
        this.stack.setCount((int)this.count);
    }

    public SlotInfoTable getInfo() {
        return this.info;
    }

    public boolean isEmpty() {
        if (this.stack.isEmpty()) {
            return true;
        }
        return this.count == 0L && this.info.isEmpty();
    }

    public Tag serialize() {
        return (Tag)((DataResult)NbtOps.INSTANCE.withEncoder(CODEC).apply(this)).getOrThrow();
    }

    public static ItemStackEntry deserialize(Tag tag) {
        if (tag == null) {
            return EMPTY;
        }
        return (ItemStackEntry)CODEC.parse((DynamicOps)NbtOps.INSTANCE, tag).getOrThrow();
    }
}

