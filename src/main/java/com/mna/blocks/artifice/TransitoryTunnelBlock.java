/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.blocks.artifice;

import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.TransitoryTunnelTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TransitoryTunnelBlock
extends Block
implements IDontCreateBlockItem,
ITranslucentBlock,
EntityBlock {
    public static final IntegerProperty FACE_VISIBILITY_MASK = IntegerProperty.create((String)"face_visibility", (int)0, (int)63);

    public TransitoryTunnelBlock() {
        super(BlockBehaviour.Properties.of().replaceable().noCollission().strength(-1.0f, 3600000.0f).noOcclusion().noLootTable());
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransitoryTunnelTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (lvl, pos, state1, be) -> TransitoryTunnelTile.ServerTick(lvl, pos, state1, (TransitoryTunnelTile)be);
    }

    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        int check_mask = TransitoryTunnelBlock.getMaskForDirection(side);
        int state_mask = (Integer)state.getValue((Property)FACE_VISIBILITY_MASK);
        return (state_mask & check_mask) != 0;
    }

    @OnlyIn(value=Dist.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0f;
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACE_VISIBILITY_MASK});
    }

    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        int mask = TransitoryTunnelBlock.getStateBasedOnSurroundings(worldIn, pos);
        if ((Integer)state.getValue((Property)FACE_VISIBILITY_MASK) == mask) {
            return;
        }
        worldIn.setBlock(pos, (BlockState)state.setValue(FACE_VISIBILITY_MASK, Integer.valueOf(mask)), 3);
    }

    public static int getStateBasedOnSurroundings(Level world, BlockPos pos) {
        int mask = 0;
        for (Direction d : Direction.values()) {
            mask |= TransitoryTunnelBlock.calculateDirectionalMask(world, pos, d);
        }
        return mask;
    }

    private static int calculateDirectionalMask(Level world, BlockPos pos, Direction direction) {
        BlockState offsetState = world.getBlockState(pos.offset(direction.getNormal()));
        if (offsetState.getBlock() != BlockInit.TRANSITORY_TUNNEL.get()) {
            return 0;
        }
        return TransitoryTunnelBlock.getMaskForDirection(direction);
    }

    private static int getMaskForDirection(Direction direction) {
        switch (direction) {
            default: {
                return 1;
            }
            case SOUTH: {
                return 2;
            }
            case EAST: {
                return 4;
            }
            case WEST: {
                return 8;
            }
            case UP: {
                return 16;
            }
            case DOWN: 
        }
        return 32;
    }

    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (worldIn.isClientSide && rand.nextFloat() < 0.2f) {
            worldIn.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), (double)((float)pos.getX() + rand.nextFloat()), (double)((float)pos.getY() + rand.nextFloat()), (double)((float)pos.getZ() + rand.nextFloat()), 0.0, (double)0.1f, 0.0);
        }
    }
}


