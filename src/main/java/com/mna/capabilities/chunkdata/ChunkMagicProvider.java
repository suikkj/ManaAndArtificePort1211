 
package com.mna.capabilities.chunkdata;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.capabilities.chunkdata.ChunkMagic;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

public class ChunkMagicProvider {
    private static final String KEY_RESIDUAL_MAGIC = "residualMagic";
    private static final String KEY_ELDRIN_POSITIONS = "knownEldrinPositions";
    private static final String KEY_WARD_DATA = "wardData";
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<com.mna.api.capabilities.IChunkMagic>> MAGIC = com.mna.capabilities.CapabilitiesInit.CHUNK_MAGIC;
    private final IChunkMagic holder = new ChunkMagic();

    public Tag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        IChunkMagic resolved = this.holder;
        nbt.putFloat(KEY_RESIDUAL_MAGIC, resolved.getResidualMagic());
        ListTag positions = new ListTag();
        resolved.getKnownEldrinSuppliers().forEach(pos -> positions.add(LongTag.valueOf((long)pos)));
        nbt.put(KEY_ELDRIN_POSITIONS, (Tag)positions);
        ListTag wards = new ListTag();
        resolved.getWards().forEach((l, p) -> {
            CompoundTag wardTag = new CompoundTag();
            wardTag.putLong("l", l.longValue());
            wardTag.putUUID("u", (UUID)p.getFirst());
            if (p.getSecond() != null) {
                wardTag.putString("k", ((ResourceLocation)p.getSecond()).toString());
            }
            wards.add(wardTag);
        });
        nbt.put(KEY_WARD_DATA, (Tag)wards);
        return nbt;
    }

    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag) {
            IChunkMagic resolved = this.holder;
            CompoundTag cnbt = (CompoundTag)nbt;
            if (cnbt.contains(KEY_RESIDUAL_MAGIC)) {
                resolved.setResidualMagic(((CompoundTag)nbt).getFloat(KEY_RESIDUAL_MAGIC));
            }
            if (cnbt.contains(KEY_ELDRIN_POSITIONS, 9)) {
                ListTag positions = cnbt.getList(KEY_ELDRIN_POSITIONS, 4);
                positions.forEach(pos -> resolved.pushKnownEldrinSupplier(BlockPos.of((long)((LongTag)pos).getAsLong())));
            }
            if (cnbt.contains(KEY_WARD_DATA, 9)) {
                ListTag wards = cnbt.getList(KEY_WARD_DATA, 10);
                HashMap<Long, Pair<UUID, ResourceLocation>> wardData = new HashMap<Long, Pair<UUID, ResourceLocation>>();
                wards.forEach(tag -> {
                    CompoundTag cTag = (CompoundTag)tag;
                    if (cTag.contains("l", 4) && cTag.contains("u", 11)) {
                        if (cTag.contains("k", 8)) {
                            wardData.put(cTag.getLong("l"), new Pair(cTag.getUUID("u"), ResourceLocation.parse(cTag.getString("k"))));
                        } else {
                            wardData.put(cTag.getLong("l"), new Pair(cTag.getUUID("u"), null));
                        }
                    }
                });
                resolved.setWards(wardData);
            }
        } else {
            ManaAndArtifice.LOGGER.error("Chunk capability data NBT passed back not an instance of CompoundNBT - save data was NOT loaded!");
        }
    }
}


