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
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.mna.api.blocks;

import com.mna.api.blocks.FillerBlock;
import com.mna.api.blocks.WaterloggableBlock;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class WaterloggableBlockWithOffset
extends WaterloggableBlock
implements IOffsetPlace {
    private final BlockPos[] offsets;

    public WaterloggableBlockWithOffset(BlockBehaviour.Properties properties, boolean startWaterlogged, BlockPos ... offsets) {
        super(properties, startWaterlogged);
        this.offsets = offsets;
    }

    public void onPlace(BlockState oldState, Level world, BlockPos pos, BlockState newState, boolean p_220082_5_) {
        for (BlockPos offset : this.getOffsets(oldState)) {
            BlockPos offsetPos = pos.offset((Vec3i)offset);
            if (!world.isEmptyBlock(offsetPos)) continue;
            FillerBlock.setAtOffsetFrom((LevelAccessor)world, pos, offset);
        }
    }

    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        Block filler = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("mna", "filler_block"));
        if (state.getBlock() != newState.getBlock()) {
            for (BlockPos offset : this.getOffsets(state)) {
                BlockPos offsetPos = pos.offset((Vec3i)offset);
                BlockState offsetState = world.getBlockState(offsetPos);
                if (offsetState.getBlock() != filler) continue;
                world.removeBlock(offsetPos, false);
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        for (BlockPos offset : this.getOffsets(context.getLevel().getBlockState(pos))) {
            FluidState flState;
            BlockPos offsetPos = pos.offset((Vec3i)offset);
            if (context.getLevel().isEmptyBlock(offsetPos) || (flState = context.getLevel().getFluidState(offsetPos)) == null || flState.isEmpty() || flState.getType() != Fluids.WATER && flState.getType() != Fluids.FLOWING_WATER) continue;
            return (BlockState)this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
        }
        return this.defaultBlockState();
    }

    @Nullable
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {
        Block filler = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("mna", "filler_block"));
        for (BlockPos offset : this.getOffsets(state)) {
            BlockPos offsetPos = pos.offset((Vec3i)offset);
            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() != filler) continue;
            world.levelEvent(player, 2001, offsetPos, WaterloggableBlockWithOffset.getId((BlockState)state));
        }
        world.levelEvent(player, 2001, pos, WaterloggableBlockWithOffset.getId((BlockState)state));
    }

    protected BlockPos[] getOffsets(BlockState state) {
        return this.offsets;
    }

    @Override
    public BlockPlaceContext adjustPlacement(BlockPlaceContext attempted) {
        return attempted;
    }

    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        return Shapes.block();
    }
}


