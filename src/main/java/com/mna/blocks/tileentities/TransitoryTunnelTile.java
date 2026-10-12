/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.commands.arguments.blocks.BlockStateParser
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.tileentities;

import net.minecraft.core.HolderLookup;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TransitoryTunnelTile
extends BlockEntity {
    private int duration = 20;
    private int age;
    private BlockState previousBlockState = Blocks.AIR.defaultBlockState();

    public TransitoryTunnelTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.TRANSITORY_TUNNEL.get(), pos, state);
    }

    public void setDurationAndPreviousState(int duration, BlockState previous) {
        this.duration = duration;
        this.previousBlockState = previous;
    }

    public static void ServerTick(Level level, BlockPos pos, BlockState state, TransitoryTunnelTile tile) {
        ++tile.age;
        if (tile.age > tile.duration) {
            level.setBlock(pos, tile.previousBlockState, 2);
        }
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("duration", this.duration);
        nbt.putInt("age", this.age);
        nbt.putString("blockState", BlockStateParser.serialize((BlockState)this.previousBlockState));
        compound.put("tunnelData", (Tag)nbt);
    }

    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        if (compound.contains("tunnelData")) {
            CompoundTag nbt = compound.getCompound("tunnelData");
            if (nbt.contains("duration")) {
                this.duration = nbt.getInt("duration");
            }
            if (nbt.contains("age")) {
                this.age = nbt.getInt("age");
            }
            if (nbt.contains("blockState")) {
                try {
                    this.previousBlockState = BlockStateParser.parseForBlock((HolderLookup)this.level.holderLookup(Registries.BLOCK), (String)nbt.getString("blockState"), (boolean)true).blockState();
                }
                catch (CommandSyntaxException e) {
                    this.previousBlockState = Blocks.AIR.defaultBlockState();
                    e.printStackTrace();
                }
            }
            if (this.previousBlockState == null) {
                this.previousBlockState = Blocks.AIR.defaultBlockState();
            }
        }
    }
}

