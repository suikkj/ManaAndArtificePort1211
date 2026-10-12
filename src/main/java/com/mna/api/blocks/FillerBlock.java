/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  org.jetbrains.annotations.Nullable
 */
package com.mna.api.blocks;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.blocks.BlockWithOffset;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.blocks.tile.IOffsetTile;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;

public class FillerBlock
extends WaterloggableBlock
implements IDontCreateBlockItem,
EntityBlock {
    public FillerBlock() {
        super(BlockBehaviour.Properties.of().pushReaction(PushReaction.BLOCK).noOcclusion().strength(1.5f).isSuffocating(BlockInit.NEVER), false);
    }

    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        BlockPos offsetPos = this.getOffsetPos((BlockGetter)world, pos);
        if (!world.isEmptyBlock(offsetPos) && !offsetPos.equals(pos)) {
            world.removeBlock(offsetPos, true);
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockPos offsetPos = this.getOffsetPos((BlockGetter)world, pos);
        if (!(world.isEmptyBlock(offsetPos) || offsetPos.equals(pos) || newState.getBlock().equals(Blocks.BARRIER))) {
            world.destroyBlock(offsetPos, true);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.BLOCK;
    }

    @Nullable
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.BLOCKED;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTrace) {
        BlockPos offsetPos = this.getOffsetPos((BlockGetter)world, pos);
        if (!world.isEmptyBlock(offsetPos) && !offsetPos.equals(pos)) {
            BlockHitResult brtr = new BlockHitResult(rayTrace.getLocation(), rayTrace.getDirection(), offsetPos, rayTrace.isInside());
            return world.getBlockState(offsetPos).getBlock().use(world.getBlockState(offsetPos), world, offsetPos, player, hand, brtr);
        }
        return InteractionResult.CONSUME;
    }

    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        BlockPos offsetPos = this.getOffsetPos((BlockGetter)level, pos);
        if (!level.isEmptyBlock(offsetPos) && !offsetPos.equals(pos)) {
            level.getBlockState(offsetPos).onNeighborChange(level, offsetPos, neighbor);
        }
        super.onNeighborChange(state, level, offsetPos, neighbor);
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        BlockPos offsetPos = this.getOffsetPos((BlockGetter)pLevel, pPos);
        if (!pLevel.isEmptyBlock(offsetPos) && !offsetPos.equals(pPos)) {
            pLevel.getBlockState(offsetPos).handleNeighborChanged(pLevel, offsetPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    public boolean skipRendering(BlockState p_200122_1_, BlockState p_200122_2_, Direction p_200122_3_) {
        return true;
    }

    private BlockPos getOffsetPos(BlockGetter world, BlockPos origin) {
        IOffsetTile tile = (IOffsetTile)world.getBlockEntity(origin);
        if (tile == null) {
            return origin;
        }
        BlockPos offset = tile.getOffset();
        return origin.offset((Vec3i)offset);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ManaAndArtificeMod.getTileHelper().createOffsetTile(pos, state);
    }

    public static void setAtOffsetFrom(LevelAccessor world, BlockPos pos, BlockPos offset) {
        Block filler = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("mna", "filler_block"));
        BlockPos offsetPos = pos.offset((Vec3i)offset);
        world.setBlock(offsetPos, filler.defaultBlockState(), 3);
        ((IOffsetTile)world.getBlockEntity(offsetPos)).setOffset(new BlockPos(offset.getX() * -1, offset.getY() * -1, offset.getZ() * -1));
    }

    public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    public RenderShape getRenderShape(BlockState p_149645_1_) {
        return RenderShape.INVISIBLE;
    }

    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {
        BlockPos offsetPos = this.getOffsetPos((BlockGetter)world, pos);
        BlockState offsetState = world.getBlockState(offsetPos);
        world.levelEvent(player, 2001, pos, FillerBlock.getId((BlockState)offsetState));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockPos offsetPos = this.getOffsetPos(pLevel, pPos);
        if (!offsetPos.equals(pPos)) {
            BlockState offsetState = pLevel.getBlockState(offsetPos);
            Block block = offsetState.getBlock();
            if (block instanceof BlockWithOffset) {
                BlockWithOffset bwo = (BlockWithOffset)block;
                return bwo.getOffsetShape(pPos.subtract((Vec3i)offsetPos), offsetState, offsetPos, pLevel, pContext);
            }
            block = offsetState.getBlock();
            if (block instanceof WaterloggableBlockWithOffset) {
                WaterloggableBlockWithOffset bwo = (WaterloggableBlockWithOffset)block;
                return bwo.getOffsetShape(pPos.subtract((Vec3i)offsetPos), offsetState, offsetPos, pLevel, pContext);
            }
        }
        return Shapes.block();
    }
}


