/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.common.util.FakePlayer
 *  net.neoforged.neoforge.common.util.FakePlayerFactory
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.FacingBlock;
import com.mna.blocks.tileentities.ImpaleSpikeTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class ImpaleBlock
extends FacingBlock
implements ITranslucentBlock,
IDontCreateBlockItem,
EntityBlock {
    private static final VoxelShape SHAPE_UP = Shapes.or((VoxelShape)Block.box((double)2.0, (double)0.0, (double)2.0, (double)14.0, (double)3.0, (double)14.0), (VoxelShape[])new VoxelShape[]{Block.box((double)4.0, (double)3.0, (double)4.0, (double)12.0, (double)6.0, (double)12.0), Block.box((double)6.0, (double)6.0, (double)6.0, (double)10.0, (double)12.0, (double)10.0), Block.box((double)7.0, (double)12.0, (double)7.0, (double)9.0, (double)18.0, (double)9.0)});
    private static final VoxelShape SHAPE_EAST = Shapes.or((VoxelShape)Block.box((double)0.0, (double)2.0, (double)2.0, (double)3.0, (double)14.0, (double)14.0), (VoxelShape[])new VoxelShape[]{Block.box((double)3.0, (double)4.0, (double)4.0, (double)6.0, (double)12.0, (double)12.0), Block.box((double)6.0, (double)6.0, (double)6.0, (double)12.0, (double)10.0, (double)10.0), Block.box((double)12.0, (double)7.0, (double)7.0, (double)18.0, (double)9.0, (double)9.0)});
    private static final VoxelShape SHAPE_SOUTH = Shapes.or((VoxelShape)Block.box((double)2.0, (double)2.0, (double)0.0, (double)14.0, (double)14.0, (double)3.0), (VoxelShape[])new VoxelShape[]{Block.box((double)4.0, (double)4.0, (double)3.0, (double)12.0, (double)12.0, (double)6.0), Block.box((double)6.0, (double)6.0, (double)6.0, (double)10.0, (double)10.0, (double)12.0), Block.box((double)7.0, (double)7.0, (double)12.0, (double)9.0, (double)9.0, (double)18.0)});
    private static final VoxelShape SHAPE_NORTH = Shapes.or((VoxelShape)Block.box((double)2.0, (double)2.0, (double)13.0, (double)14.0, (double)14.0, (double)16.0), (VoxelShape[])new VoxelShape[]{Block.box((double)4.0, (double)4.0, (double)10.0, (double)12.0, (double)12.0, (double)13.0), Block.box((double)6.0, (double)6.0, (double)4.0, (double)10.0, (double)10.0, (double)10.0), Block.box((double)7.0, (double)7.0, (double)-2.0, (double)9.0, (double)9.0, (double)4.0)});
    private static final VoxelShape SHAPE_WEST = Shapes.or((VoxelShape)Block.box((double)13.0, (double)2.0, (double)2.0, (double)16.0, (double)14.0, (double)14.0), (VoxelShape[])new VoxelShape[]{Block.box((double)10.0, (double)4.0, (double)4.0, (double)13.0, (double)12.0, (double)12.0), Block.box((double)4.0, (double)6.0, (double)6.0, (double)10.0, (double)10.0, (double)10.0), Block.box((double)-2.0, (double)7.0, (double)7.0, (double)4.0, (double)9.0, (double)9.0)});
    private static final VoxelShape SHAPE_DOWN = Shapes.or((VoxelShape)Block.box((double)2.0, (double)13.0, (double)2.0, (double)14.0, (double)16.0, (double)14.0), (VoxelShape[])new VoxelShape[]{Block.box((double)4.0, (double)10.0, (double)4.0, (double)12.0, (double)13.0, (double)12.0), Block.box((double)6.0, (double)4.0, (double)6.0, (double)10.0, (double)10.0, (double)10.0), Block.box((double)7.0, (double)-2.0, (double)7.0, (double)9.0, (double)4.0, (double)9.0)});

    public ImpaleBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).instabreak().noLootTable().noOcclusion());
    }

    protected boolean isAir(BlockState state) {
        return false;
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pRandom.nextBoolean()) {
            return;
        }
        Vec3 center = Vec3.atLowerCornerOf((Vec3i)pPos).add(pRandom.nextDouble(), pRandom.nextDouble(), pRandom.nextDouble());
        if (pRandom.nextBoolean()) {
            pLevel.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())).setGravity(0.01f), center.x, center.y, center.z, 0.0, 0.0, 0.0);
        } else {
            pLevel.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setGravity(0.01f), center.x, center.y, center.z, 0.0, 0.0, 0.0);
        }
    }

    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        int face = (Integer)p_60555_.getValue((Property)FacingBlock.SURFACE_TYPE);
        switch (face) {
            case 2: {
                return SHAPE_DOWN;
            }
            case 3: {
                Direction dir = (Direction)p_60555_.getValue((Property)FacingBlock.FACING);
                switch (dir) {
                    case SOUTH: {
                        return SHAPE_SOUTH;
                    }
                    case WEST: {
                        return SHAPE_WEST;
                    }
                    case EAST: {
                        return SHAPE_EAST;
                    }
                }
                return SHAPE_NORTH;
            }
        }
        return SHAPE_UP;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if (context.getLevel() instanceof ServerLevel) {
            FakePlayer fp = FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)context.getLevel()));
            if (!Block.isFaceFull((VoxelShape)blockstate.getBlock().getCollisionShape(blockstate, (BlockGetter)context.getLevel(), context.getClickedPos(), CollisionContext.of((Entity)fp)), (Direction)context.getClickedFace())) {
                return null;
            }
        }
        Direction[] random_values = new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
        BlockState state = super.getStateForPlacement(context);
        if ((Integer)state.getValue((Property)FacingBlock.SURFACE_TYPE) != 3) {
            state = (BlockState)state.setValue(FACING, random_values[(int)(Math.random() * (double)random_values.length)]);
        }
        return state;
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
    }

    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ImpaleSpikeTile(pPos, pState);
    }

    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance * 2.0f, 3.0f, entity.damageSources().fall());
    }
}


