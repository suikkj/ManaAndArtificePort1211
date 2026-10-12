/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 */
package com.mna.inventory.stack_extension;

import com.mna.inventory.stack_extension.SlotInfoBase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public class SlotInfoTable {
    public static Codec<SlotInfoTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(CompoundTag.CODEC.listOf().fieldOf("info").forGetter(o -> o.info.values().stream().map(SlotInfoBase::getTag).collect(Collectors.toList()))).apply(instance, SlotInfoTable::new));
    private final Map<String, SlotInfoBase<?>> info = new HashMap();

    public SlotInfoTable() {
    }

    public SlotInfoTable(List<CompoundTag> info) {
        this();
        info.stream().map(SlotInfoBase::fromNBT).forEach(this::add);
    }

    public boolean isEmpty() {
        return this.info.isEmpty();
    }

    @Nullable
    public SlotInfoBase<?> getByKey(String key) {
        return this.info.get(key);
    }

    public void add(SlotInfoBase<?> value) {
        this.info.put(value.getKey(), value);
    }
}

