/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.MapColor
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.blocks.tileentities.TransitoryTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;

public class TransitoryTileBlock
extends Block
implements ITranslucentBlock,
IDontCreateBlockItem,
EntityBlock {
    public static final IntegerProperty DURATION = IntegerProperty.create((String)"duration", (int)0, (int)30);

    public TransitoryTileBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(1.0f).noOcclusion().isViewBlocking((state, reader, pos) -> false));
    }

    public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{DURATION});
    }

    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (worldIn instanceof ServerLevel) {
            this.checkNeighbors((LevelReader)worldIn, pos);
        }
    }

    public void setPlacedBy(Level p_180633_1_, BlockPos p_180633_2_, BlockState p_180633_3_, LivingEntity p_180633_4_, ItemStack p_180633_5_) {
        p_180633_1_.updateNeighborsAt(p_180633_2_, (Block)this);
    }

    private void checkNeighbors(LevelReader worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof TransitoryTile) {
            for (Direction d : Direction.values()) {
                BlockPos directionOffset = pos.offset(d.getNormal());
                BlockState directionState = worldIn.getBlockState(directionOffset);
                if (directionState.getBlock() != this) continue;
                ((TransitoryTile)te).setNeighborOnSide(d);
                BlockEntity neighbor = worldIn.getBlockEntity(directionOffset);
                if (neighbor == null || !(neighbor instanceof TransitoryTile)) continue;
                ((TransitoryTile)neighbor).setNeighborOnSide(d.getOpposite());
            }
        }
    }

    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        BlockEntity te;
        if (!world.isClientSide() && (te = world.getBlockEntity(pos)) != null && te instanceof TransitoryTile) {
            BlockState neighborState = world.getBlockState(neighbor);
            if (neighborState.getBlock() == this) {
                ((TransitoryTile)te).setNeighborOnSide(this.directionBetweenPositions(pos, neighbor));
            } else {
                ((TransitoryTile)te).clearNeighborOnSide(this.directionBetweenPositions(pos, neighbor));
            }
        }
    }

    @Nullable
    private Direction directionBetweenPositions(BlockPos a, BlockPos b) {
        if (a.above().equals(b)) {
            return Direction.UP;
        }
        if (a.below().equals(b)) {
            return Direction.DOWN;
        }
        if (a.east().equals(b)) {
            return Direction.EAST;
        }
        if (a.west().equals(b)) {
            return Direction.WEST;
        }
        if (a.north().equals(b)) {
            return Direction.NORTH;
        }
        if (a.south().equals(b)) {
            return Direction.SOUTH;
        }
        return null;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransitoryTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.TRANSITORY_TILE.get() ? (lvl, pos, state1, be) -> TransitoryTile.Tick(lvl, pos, state1, (TransitoryTile)be) : null;
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public void setColor(Level world, BlockPos pos, int color) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null && te instanceof TransitoryTile) {
            TransitoryTile tile = (TransitoryTile)te;
            tile.setColor(color);
        }
    }

    public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
        return false;
    }
}

