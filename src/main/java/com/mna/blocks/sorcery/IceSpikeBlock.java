/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
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
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.common.util.FakePlayer
 *  net.neoforged.neoforge.common.util.FakePlayerFactory
 *  org.jetbrains.annotations.Nullable
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.FacingBlock;
import com.mna.blocks.tileentities.IceSpikeTile;
import com.mna.entities.boss.Odin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

public class IceSpikeBlock
extends FacingBlock
implements ITranslucentBlock,
IDontCreateBlockItem,
EntityBlock {
    private static final VoxelShape SHAPE_UP = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)1.0, (double)16.0);
    private static final VoxelShape SHAPE_EAST = Block.box((double)0.0, (double)0.0, (double)0.0, (double)1.0, (double)16.0, (double)16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)16.0, (double)1.0);
    private static final VoxelShape SHAPE_NORTH = Block.box((double)0.0, (double)0.0, (double)15.0, (double)16.0, (double)16.0, (double)16.0);
    private static final VoxelShape SHAPE_WEST = Block.box((double)15.0, (double)0.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0);
    private static final VoxelShape SHAPE_DOWN = Block.box((double)0.0, (double)15.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0);

    public IceSpikeBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.ICE).instabreak().noCollission().noLootTable().noOcclusion());
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
            pLevel.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setGravity(0.01f), center.x, center.y, center.z, 0.0, 0.0, 0.0);
        } else {
            pLevel.addParticle((ParticleOptions)ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, 0.0, 0.0, 0.0);
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
            if (!Block.isFaceFull((VoxelShape)blockstate.getCollisionShape((BlockGetter)context.getLevel(), context.getClickedPos(), CollisionContext.of((Entity)fp)), (Direction)context.getClickedFace())) {
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

    @Nullable
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        if (mob instanceof Odin) {
            return PathType.OPEN;
        }
        return PathType.DANGER_POWDER_SNOW;
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (pEntity instanceof Odin) {
            return;
        }
        if (pEntity instanceof LivingEntity && (pEntity.xOld != pEntity.getX() || pEntity.zOld != pEntity.getZ())) {
            LivingEntity living = (LivingEntity)pEntity;
            living.makeStuckInBlock(pState, new Vec3((double)0.8f, 0.75, (double)0.8f));
            if (!pLevel.isClientSide() && !living.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
            living.setTicksFrozen(living.getTicksFrozen() + 1);
            double d0 = Math.abs(pEntity.getX() - pEntity.xOld);
            double d1 = Math.abs(pEntity.getZ() - pEntity.zOld);
            if (d0 >= (double)0.003f || d1 >= (double)0.003f) {
                pEntity.hurt(pEntity.damageSources().freeze(), 1.0f);
            }
        }
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
        return new IceSpikeTile(pPos, pState);
    }
}


