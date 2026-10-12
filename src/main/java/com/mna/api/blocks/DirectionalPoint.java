/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.mna.api.blocks;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class DirectionalPoint {
    private final Direction direction;
    private final BlockPos position;
    private final String originalBlockName;
    private final Block originalBlock;
    private final String originalBlockID;

    public DirectionalPoint(BlockPos position, Direction direction, @Nullable String originalBlockName, @Nullable String originalBlockID) {
        this.position = position;
        this.direction = direction;
        this.originalBlockName = originalBlockName;
        this.originalBlockID = originalBlockID;
        this.originalBlock = originalBlockID != null ? (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.parse(originalBlockID)) : null;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public boolean isValid() {
        return this.position != null && this.direction != null;
    }

    @Nullable
    public String getBlockName() {
        return this.originalBlockName;
    }

    @Nullable
    public Block getBlock() {
        return this.originalBlock;
    }

    @Nullable
    public String getBlockID() {
        return this.originalBlockID;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (this.direction != null) {
            tag.putInt("direction", this.direction.get3DDataValue());
        }
        if (this.position != null) {
            tag.put("position", (Tag)NbtUtils.writeBlockPos((BlockPos)this.position));
        }
        if (this.originalBlockName != null) {
            tag.putString("block", this.originalBlockName);
        }
        if (this.originalBlockID != null) {
            tag.putString("block_id", this.originalBlockID);
        }
        return tag;
    }

    public static DirectionalPoint of(CompoundTag tag) {
        if (tag.contains("position") && tag.contains("direction")) {
            BlockPos pos = NbtUtils.readBlockPos(tag, "position").orElse(null);
            Direction direction = Direction.from3DDataValue((int)tag.getInt("direction"));
            String name = tag.contains("block") ? tag.getString("block") : null;
            String blockID = tag.contains("block_id") ? tag.getString("block_id") : null;
            return new DirectionalPoint(pos, direction, name, blockID);
        }
        return null;
    }
}


