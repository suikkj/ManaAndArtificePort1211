/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.mna.api.blocks;

import com.mna.api.blocks.FillerBlock;
import com.mna.api.blocks.interfaces.IOffsetPlace;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class BlockWithOffset
extends Block
implements IOffsetPlace {
    private BlockPos[] offsets;

    public BlockWithOffset(BlockBehaviour.Properties properties, BlockPos ... offsets) {
        super(properties);
        this.offsets = offsets;
    }

    public void onPlace(BlockState oldState, Level world, BlockPos pos, BlockState newState, boolean p_220082_5_) {
        for (BlockPos offset : this.offsets) {
            BlockPos offsetPos = pos.offset((Vec3i)offset);
            if (!world.isEmptyBlock(offsetPos)) continue;
            FillerBlock.setAtOffsetFrom((LevelAccessor)world, pos, offset);
        }
    }

    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean doDrops) {
        if (state.getBlock() != newState.getBlock()) {
            Block filler = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("mna", "filler_block"));
            for (BlockPos offset : this.offsets) {
                BlockPos offsetPos = pos.offset((Vec3i)offset);
                BlockState offsetState = world.getBlockState(offsetPos);
                if (offsetState.getBlock() != filler) continue;
                world.destroyBlock(offsetPos, false);
            }
        }
        super.onRemove(state, world, pos, newState, doDrops);
    }

    @Nullable
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        for (BlockPos offset : this.offsets) {
            BlockPos offsetPos = pos.offset((Vec3i)offset);
            if (context.getLevel().isEmptyBlock(offsetPos)) continue;
            return null;
        }
        return this.defaultBlockState();
    }

    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {
        Block filler = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("mna", "filler_block"));
        for (BlockPos offset : this.offsets) {
            BlockPos offsetPos = pos.offset((Vec3i)offset);
            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() != filler) continue;
            world.levelEvent(player, 2001, offsetPos, BlockWithOffset.getId((BlockState)state));
        }
        world.levelEvent(player, 2001, pos, BlockWithOffset.getId((BlockState)state));
    }

    @Override
    public BlockPlaceContext adjustPlacement(BlockPlaceContext attempted) {
        return attempted;
    }

    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        return Shapes.block();
    }
}


