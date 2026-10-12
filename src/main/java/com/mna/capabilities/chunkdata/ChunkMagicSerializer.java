package com.mna.capabilities.chunkdata;

import net.minecraft.core.HolderLookup;
import com.mna.api.capabilities.IChunkMagic;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class ChunkMagicSerializer implements IAttachmentSerializer<Tag, ChunkMagic> {
    @Override
    public ChunkMagic read(IAttachmentHolder holder, Tag nbt, HolderLookup.Provider provider) {
        ChunkMagic resolved = new ChunkMagic();
        if (nbt instanceof CompoundTag) {
            CompoundTag cnbt = (CompoundTag) nbt;
            if (cnbt.contains("residualMagic")) {
                resolved.setResidualMagic(cnbt.getFloat("residualMagic"));
            }
            if (cnbt.contains("knownEldrinPositions", 9)) {
                ListTag positions = cnbt.getList("knownEldrinPositions", 4);
                positions.forEach(pos -> resolved.pushKnownEldrinSupplier(BlockPos.of(((LongTag) pos).getAsLong())));
            }
            if (cnbt.contains("wardData", 9)) {
                ListTag wards = cnbt.getList("wardData", 10);
                HashMap<Long, Pair<UUID, ResourceLocation>> wardData = new HashMap<>();
                wards.forEach(tag -> {
                    CompoundTag cTag = (CompoundTag) tag;
                    if (cTag.contains("l", 4) && cTag.contains("u", 11)) {
                        if (cTag.contains("k", 8)) {
                            wardData.put(cTag.getLong("l"), new Pair<>(cTag.getUUID("u"), ResourceLocation.parse(cTag.getString("k"))));
                        } else {
                            wardData.put(cTag.getLong("l"), new Pair<>(cTag.getUUID("u"), null));
                        }
                    }
                });
                resolved.getWards().putAll(wardData);
            }
        }
        return resolved;
    }

    @Override
    public Tag write(ChunkMagic resolved, HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("residualMagic", resolved.getResidualMagic());
        ListTag positions = new ListTag();
        resolved.getKnownEldrinSuppliers().forEach(pos -> positions.add(LongTag.valueOf(pos)));
        nbt.put("knownEldrinPositions", positions);
        ListTag wards = new ListTag();
        resolved.getWards().forEach((l, p) -> {
            CompoundTag wardTag = new CompoundTag();
            wardTag.putLong("l", l);
            wardTag.putUUID("u", p.getFirst());
            if (p.getSecond() != null) {
                wardTag.putString("k", p.getSecond().toString());
            }
            wards.add(wardTag);
        });
        nbt.put("wardData", wards);
        return nbt;
    }
}
