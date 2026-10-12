/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufInputStream
 *  io.netty.buffer.ByteBufOutputStream
 *  io.netty.buffer.PooledByteBufAllocator
 *  io.netty.handler.codec.EncoderException
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 */
package com.mna.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.EncoderException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class NBTUtilities {
    public static CompoundTag fromJSON(JsonObject object) {
        CompoundTag output = new CompoundTag();
        for (Map.Entry elem : object.entrySet()) {
            if (((JsonElement)elem.getValue()).isJsonPrimitive()) {
                JsonPrimitive primitive = ((JsonElement)elem.getValue()).getAsJsonPrimitive();
                if (primitive.isBoolean()) {
                    output.putBoolean((String)elem.getKey(), primitive.getAsBoolean());
                    continue;
                }
                if (primitive.isNumber()) {
                    Number val = primitive.getAsNumber();
                    if (val.floatValue() % 1.0f == 0.0f) {
                        output.putInt((String)elem.getKey(), val.intValue());
                        continue;
                    }
                    output.putFloat((String)elem.getKey(), val.floatValue());
                    continue;
                }
                if (!primitive.isString()) continue;
                output.putString((String)elem.getKey(), primitive.getAsString());
                continue;
            }
            if (!((JsonElement)elem.getValue()).isJsonObject()) continue;
            output.put((String)elem.getKey(), (Tag)NBTUtilities.fromJSON(((JsonElement)elem.getValue()).getAsJsonObject()));
        }
        return output;
    }

    public static ListTag writeVec3i(Vec3i vec) {
        ListTag tag = new ListTag();
        tag.add(IntTag.valueOf((int)vec.getX()));
        tag.add(IntTag.valueOf((int)vec.getY()));
        tag.add(IntTag.valueOf((int)vec.getZ()));
        return tag;
    }

    public static byte[] toBytes(CompoundTag tag) {
        PooledByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;
        ByteBuf buf = alloc.buffer(tag.sizeInBytes());
        try {
            NbtIo.write((CompoundTag)tag, (DataOutput)new ByteBufOutputStream(buf));
        }
        catch (IOException ioexception) {
            throw new EncoderException((Throwable)ioexception);
        }
        buf.readerIndex(0);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return bytes;
    }

    public static CompoundTag fromBytes(byte[] data) {
        PooledByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;
        ByteBuf buf = alloc.buffer(data.length);
        buf.writeBytes(data);
        CompoundTag output = new CompoundTag();
        try {
            output = NbtIo.read((DataInput)new ByteBufInputStream(buf), (NbtAccounter)NbtAccounter.UNLIMITED);
        }
        catch (IOException ioexception) {
            throw new EncoderException((Throwable)ioexception);
        }
        buf.release();
        return output;
    }
}

