/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.nbt.TagParser
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package com.mna.recipes.multiblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mna.tools.BlockUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class MultiblockConfiguration {
    String identifier;
    HashMap<Long, Pair<Integer, MutableBoolean>> structureBlocks;
    private static final Pair<Integer, MutableBoolean> DEFAULT = new Pair(-1, new MutableBoolean(false));
    private Vec3i size;
    private boolean cachedMapped = false;
    private Rotation curRotation = Rotation.NONE;
    private boolean isValid = false;

    private MultiblockConfiguration() {
        this.structureBlocks = new HashMap();
    }

    public void rotate(Rotation rotation) {
        if (rotation == this.curRotation) {
            return;
        }
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Set<Long> getOffsets() {
        return this.structureBlocks.keySet();
    }

    public Vec3i getSize() {
        return this.size;
    }

    public boolean getIsValid() {
        return this.isValid;
    }

    public int countBlocks() {
        return this.structureBlocks.size();
    }

    public void resetMatchData() {
        this.structureBlocks.values().forEach(p -> ((MutableBoolean)p.getSecond()).setFalse());
        this.cachedMapped = false;
    }

    public Integer getBlockAt(BlockPos offset) {
        return (Integer)this.structureBlocks.getOrDefault(offset.asLong(), DEFAULT).getFirst();
    }

    public void setBlockAt(BlockPos offset, Integer blockStateIndex) {
        this.structureBlocks.put(offset.asLong(), (Pair<Integer, MutableBoolean>)new Pair(blockStateIndex, new MutableBoolean(false)));
        this.recalculateSize();
    }

    private void recalculateSize() {
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        int minZ = 0;
        int maxZ = 0;
        for (Long l : this.structureBlocks.keySet()) {
            BlockPos cur = BlockPos.of((long)l);
            if (cur.getX() < minX) {
                minX = cur.getX();
            } else if (cur.getX() > maxX) {
                maxX = cur.getX();
            }
            if (cur.getY() < minY) {
                minY = cur.getY();
            } else if (cur.getY() > maxY) {
                maxY = cur.getY();
            }
            if (cur.getZ() < minZ) {
                minZ = cur.getZ();
                continue;
            }
            if (cur.getZ() <= maxZ) continue;
            maxZ = cur.getZ();
        }
        this.size = new BlockPos(maxX - minX, maxY - minY, maxZ - minZ);
    }

    public void validate() {
        if (this.size.getX() == 0 || this.size.getY() == 0 || this.size.getZ() == 0) {
            this.isValid = false;
            return;
        }
        if (this.structureBlocks.size() == 0) {
            this.isValid = false;
            return;
        }
        this.isValid = true;
    }

    public void markMatch(BlockPos offset) {
        Pair<Integer, MutableBoolean> pair = this.structureBlocks.get(offset.asLong());
        ((MutableBoolean)pair.getSecond()).setTrue();
    }

    private boolean offsetMatched(Long offset) {
        return ((MutableBoolean)this.structureBlocks.getOrDefault(offset, DEFAULT).getSecond()).booleanValue();
    }

    public void computeMatch() {
        this.cachedMapped = this.structureBlocks.values().stream().allMatch(e -> ((MutableBoolean)e.getSecond()).isTrue());
    }

    public boolean matched() {
        return this.cachedMapped;
    }

    public Pair<Integer, Integer> matchCount(List<MultiblockConfiguration> variations) {
        int total = this.structureBlocks.size();
        int matched = (int)this.structureBlocks.entrySet().stream().filter(e -> ((MutableBoolean)((Pair)e.getValue()).getSecond()).isTrue() || variations.stream().anyMatch(v -> v.offsetMatched((Long)e.getKey()))).count();
        return new Pair(matched, total);
    }

    private static int updateBlockStateIndex(BlockState state, List<BlockState> blockIndex, boolean allowAir) {
        if (state.isAir() && !allowAir) {
            return -1;
        }
        int index = blockIndex.indexOf(state);
        if (index == -1) {
            blockIndex.add(state);
            index = blockIndex.size() - 1;
        }
        return index;
    }

    public static MultiblockConfiguration parseVariation(JsonObject variation, List<BlockState> blockIndex) {
        MultiblockConfiguration mbc = new MultiblockConfiguration();
        if (variation.has("id") && variation.has("data")) {
            mbc.identifier = variation.get("id").getAsString();
            mbc.isValid = true;
            JsonArray data = variation.getAsJsonArray("data");
            int[] min = new int[]{0, 0, 0};
            int[] max = new int[]{0, 0, 0};
            data.forEach(d -> {
                JsonObject elem = d.getAsJsonObject();
                CompoundTag nbt = null;
                try {
                    nbt = TagParser.parseTag((String)elem.toString());
                }
                catch (CommandSyntaxException ex) {
                    mbc.isValid = false;
                    return;
                }
                if (nbt.contains("offset", 10) && nbt.contains("state", 10)) {
                    BlockPos offset = com.mna.api.tools.MNANbt.readBlockPosLegacy((CompoundTag)nbt.get("offset"));
                    BlockState state = BlockUtils.readBlockState((CompoundTag)nbt.get("state"));
                    int index = MultiblockConfiguration.updateBlockStateIndex(state, blockIndex, true);
                    if (index == -1) {
                        mbc.isValid = false;
                        return;
                    }
                    if (offset.getX() < min[0]) {
                        min[0] = offset.getX();
                    }
                    if (offset.getY() < min[1]) {
                        min[1] = offset.getY();
                    }
                    if (offset.getZ() < min[2]) {
                        min[2] = offset.getZ();
                    }
                    if (offset.getX() > max[0]) {
                        max[0] = offset.getX();
                    }
                    if (offset.getY() > max[1]) {
                        max[1] = offset.getY();
                    }
                    if (offset.getZ() > max[2]) {
                        max[2] = offset.getZ();
                    }
                    mbc.structureBlocks.put(offset.asLong(), (Pair<Integer, MutableBoolean>)new Pair(index, new MutableBoolean(false)));
                } else {
                    mbc.isValid = false;
                }
            });
            mbc.size = new BlockPos(max[0] - min[0], max[1] - min[1], max[2] - min[2]);
        }
        return mbc;
    }

    public static MultiblockConfiguration loadStructure(StructureTemplateManager manager, ResourceLocation structureFile, List<BlockState> blockIndex) {
        MultiblockConfiguration mbc = new MultiblockConfiguration();
        manager.get(structureFile).ifPresent(template -> {
            mbc.size = template.getSize();
            com.mna.tools.MNAReflection.structurePalettes(template).forEach(p -> p.blocks().forEach(bi -> {
                int index = MultiblockConfiguration.updateBlockStateIndex(bi.state(), blockIndex, false);
                if (index == -1) {
                    return;
                }
                mbc.structureBlocks.put(bi.pos().asLong(), (Pair<Integer, MutableBoolean>)new Pair(index, new MutableBoolean(false)));
            }));
            mbc.isValid = true;
        });
        return mbc;
    }

    public static MultiblockConfiguration createDummyStructure() {
        MultiblockConfiguration mbc = new MultiblockConfiguration();
        mbc.size = new BlockPos(0, 0, 0);
        mbc.isValid = false;
        return mbc;
    }

    public CompoundTag serialize() {
        CompoundTag data = new CompoundTag();
        if (!this.isValid) {
            return data;
        }
        ListTag list = new ListTag();
        this.structureBlocks.forEach((l, i) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("offset", l.longValue());
            entry.putInt("index", ((Integer)i.getFirst()).intValue());
            list.add(entry);
        });
        data.put("list", (Tag)list);
        if (this.size != null) {
            data.put("size", (Tag)NbtUtils.writeBlockPos((BlockPos)new BlockPos(this.size)));
        }
        return data;
    }

    public static MultiblockConfiguration deserialize(CompoundTag nbt) {
        MultiblockConfiguration conf = new MultiblockConfiguration();
        conf.isValid = true;
        if (nbt.contains("list")) {
            ListTag list = nbt.getList("list", 10);
            list.forEach(e -> {
                CompoundTag entry = (CompoundTag)e;
                if (entry.contains("offset") && entry.contains("index")) {
                    long offset = entry.getLong("offset");
                    int index = entry.getInt("index");
                    conf.structureBlocks.put(offset, (Pair<Integer, MutableBoolean>)new Pair(index, new MutableBoolean(false)));
                }
            });
        } else {
            conf.isValid = false;
        }
        if (nbt.contains("size")) {
            conf.size = NbtUtils.readBlockPos(nbt, "size").orElse(null);
        } else {
            conf.isValid = false;
        }
        return conf;
    }

    public JsonObject computeRecipeDiff(MultiblockConfiguration other, ArrayList<BlockState> blockIndex) {
        HashMap<Long, Integer> changes = new HashMap<>();
        other.structureBlocks.keySet().stream().forEach(k -> {
            if (this.structureBlocks.containsKey(k)) {
                if (this.structureBlocks.get(k).getFirst() != other.structureBlocks.get(k).getFirst()) {
                    changes.put(k, (Integer)other.structureBlocks.get(k).getFirst());
                }
            } else {
                changes.put(k, (Integer)other.structureBlocks.get(k).getFirst());
            }
        });
        ListTag diffData = new ListTag();
        changes.entrySet().stream().forEach(e -> {
            BlockPos offset = BlockPos.of((long)((Long)e.getKey()));
            BlockState state = (BlockState)blockIndex.get((Integer)e.getValue());
            CompoundTag diffItem = new CompoundTag();
            diffItem.put("offset", (Tag)NbtUtils.writeBlockPos((BlockPos)offset));
            diffItem.put("state", (Tag)NbtUtils.writeBlockState((BlockState)state));
            diffData.add(diffItem);
        });
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "variation_id_here_must_be_unique");
        nbt.put("data", (Tag)diffData);
        return JsonParser.parseString((String)nbt.toString()).getAsJsonObject();
    }
}

