/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.FenceBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.EntityCollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.entities.construct.IConstruct;
import com.mna.blocks.sorcery.SpectralCraftingTableBlock;
import com.mna.blocks.tileentities.SimpleSpectralTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SpectralFenceBlock
extends FenceBlock
implements EntityBlock,
ITranslucentBlock {
    public SpectralFenceBlock(BlockBehaviour.Properties blockProps) {
        super(blockProps);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(SpectralCraftingTableBlock.PERMANENT, Boolean.valueOf(false))).setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{SpectralCraftingTableBlock.PERMANENT});
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SimpleSpectralTile(pPos, pState);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (((Boolean)state.getValue((Property)SpectralCraftingTableBlock.PERMANENT)).booleanValue()) {
            return null;
        }
        return type == TileEntityInit.SIMPLE_SPECTRAL_TILE.get() && !level.isClientSide() ? (lvl, pos, state1, be) -> SimpleSpectralTile.ServerTick(lvl, pos, state1, (SimpleSpectralTile)be) : null;
    }

    private boolean collidesWith(Entity entity) {
        if (entity instanceof Player || !(entity instanceof LivingEntity)) {
            return false;
        }
        return !(entity instanceof IConstruct);
    }

    public boolean collisionExtendsVertically(BlockState state, BlockGetter level, BlockPos pos, Entity collidingEntity) {
        if (!this.collidesWith(collidingEntity)) {
            return false;
        }
        return super.collisionExtendsVertically(state, level, pos, collidingEntity);
    }

    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        EntityCollisionContext ecc;
        if (pContext instanceof EntityCollisionContext && !this.collidesWith((ecc = (EntityCollisionContext)pContext).getEntity())) {
            return Shapes.empty();
        }
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Nullable
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        if (mob != null && mob instanceof IConstruct) {
            return PathType.OPEN;
        }
        return null;
    }

    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 8;
    }
}

