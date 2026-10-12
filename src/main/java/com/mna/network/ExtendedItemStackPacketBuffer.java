/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufInputStream
 *  io.netty.buffer.ByteBufOutputStream
 *  io.netty.handler.codec.EncoderException
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.network;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ExtendedItemStackPacketBuffer
extends FriendlyByteBuf {
    public ExtendedItemStackPacketBuffer(ByteBuf buffer) {
        super(buffer);
    }

    public void writeExtendedItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            this.writeInt(-1);
        } else {
            this.writeInt(Item.getId((Item)stack.getItem()));
            this.writeInt(stack.getCount());
            CompoundTag nbttagcompound = null;
            if (com.mna.api.tools.MNAItemData.tag(stack) != null) {
                nbttagcompound = com.mna.api.tools.MNAItemData.tag(stack);
            }
            this.writeNbt(nbttagcompound);
        }
    }

    public ItemStack readExtendedItemStack() {
        int itemID = this.readInt();
        if (itemID < 0) {
            return ItemStack.EMPTY;
        }
        int count = this.readInt();
        ItemStack stack = new ItemStack((ItemLike)Item.byId((int)itemID), count);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(this.readNbt()));
        return stack;
    }

    public CompoundTag readNBT() {
        int i = this.readerIndex();
        byte b0 = this.readByte();
        if (b0 == 0) {
            return null;
        }
        this.readerIndex(i);
        try {
            return NbtIo.read((DataInput)new ByteBufInputStream(this.copy()), (NbtAccounter)new NbtAccounter(0x200000L));
        }
        catch (IOException ioexception) {
            throw new EncoderException((Throwable)ioexception);
        }
    }

    public void writeNBT(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            this.writeByte(0);
        } else {
            try {
                NbtIo.write((CompoundTag)nbt, (DataOutput)new ByteBufOutputStream((ByteBuf)this));
            }
            catch (IOException ioexception) {
                throw new EncoderException((Throwable)ioexception);
            }
        }
    }

    public <T, C extends Collection<T>> C readExtendedCollection(IntFunction<C> p_178372_, Function<ExtendedItemStackPacketBuffer, T> p_178373_) {
        int i = this.readVarInt();
        Collection c = (Collection)p_178372_.apply(i);
        for (int j = 0; j < i; ++j) {
            c.add(p_178373_.apply(this));
        }
        return (C)c;
    }

    public <T> void writeExtendedCollection(Collection<T> p_178353_, BiConsumer<ExtendedItemStackPacketBuffer, T> p_178354_) {
        this.writeVarInt(p_178353_.size());
        for (T t : p_178353_) {
            p_178354_.accept(this, (ExtendedItemStackPacketBuffer)(t));
        }
    }
}

