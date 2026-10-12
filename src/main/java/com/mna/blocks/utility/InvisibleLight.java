/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.minecraft.world.ticks.TickPriority
 *  org.jetbrains.annotations.Nullable
 */
package com.mna.blocks.utility;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

public class InvisibleLight
extends WaterloggableBlock
implements IDontCreateBlockItem {
    public InvisibleLight() {
        super(BlockBehaviour.Properties.of().strength(0.0f).noOcclusion().lightLevel(state -> 15).replaceable().noCollission().noLootTable().air().isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BlockStateProperties.DELAY, Integer.valueOf(4)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{BlockStateProperties.DELAY});
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return true;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    @Nullable
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.OPEN;
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pRandom.nextInt(10) < 2) {
            for (int i = 0; i < 5; ++i) {
                pLevel.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), (double)((float)pPos.getX() + 0.25f) + Math.random() * 0.5, (double)((float)pPos.getY() + 0.25f) + Math.random() * 0.5, (double)((float)pPos.getZ() + 0.25f) + Math.random() * 0.5, (double)-0.005f + Math.random() * (double)0.01f, (double)-0.005f + Math.random() * (double)0.01f, (double)-0.005f + Math.random() * (double)0.01f);
            }
        }
    }

    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        int delay = (Integer)state.getValue((Property)BlockStateProperties.DELAY);
        if (delay > 1) {
            worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(BlockStateProperties.DELAY, Integer.valueOf(delay - 1)));
            worldIn.scheduleTick(new BlockPos((Vec3i)pos), (Block)this, 2400, TickPriority.EXTREMELY_LOW);
        } else {
            worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}

