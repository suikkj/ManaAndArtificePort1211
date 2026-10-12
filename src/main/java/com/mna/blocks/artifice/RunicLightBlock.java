/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.ManaAndArtifice;
import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.tileentities.RunicLightTile;
import com.mna.tools.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RunicLightBlock
extends WaterloggableBlockWithOffset
implements EntityBlock {
    private static final VoxelShape SHAPE_MAIN = Shapes.or((VoxelShape)Block.box((double)4.5, (double)0.0, (double)4.5, (double)11.5, (double)3.0, (double)11.5), (VoxelShape)Block.box((double)6.5, (double)3.0, (double)6.5, (double)9.5, (double)16.0, (double)9.5));
    private static final VoxelShape SHAPE_OFFSET = Shapes.or((VoxelShape)Block.box((double)4.5, (double)3.0, (double)4.5, (double)11.5, (double)6.0, (double)11.5), (VoxelShape[])new VoxelShape[]{Block.box((double)6.5, (double)0.0, (double)6.5, (double)9.5, (double)3.0, (double)9.5), Block.box((double)5.5, (double)6.0, (double)5.5, (double)10.5, (double)12.0, (double)10.5), Block.box((double)4.5, (double)12.0, (double)4.5, (double)11.5, (double)14.0, (double)11.5), Block.box((double)5.5, (double)14.0, (double)5.5, (double)10.5, (double)15.0, (double)10.5), Block.box((double)7.5, (double)15.0, (double)7.5, (double)8.5, (double)16.0, (double)8.5)});

    public RunicLightBlock(BlockBehaviour.Properties properties) {
        super(properties, false, new BlockPos(0, 1, 0));
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue((Property)HorizontalDirectionalBlock.FACING, (Comparable)Direction.NORTH)).setValue((Property)BlockStateProperties.LIT, (Comparable)Boolean.valueOf(true)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(new Property[]{HorizontalDirectionalBlock.FACING});
        pBuilder.add(new Property[]{BlockStateProperties.LIT});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        return (BlockState)((BlockState)super.getStateForPlacement(context).setValue((Property)HorizontalDirectionalBlock.FACING, (Comparable)direction)).setValue((Property)BlockStateProperties.LIT, (Comparable)Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())));
    }

    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (((Boolean)stateIn.getValue((Property)BlockStateProperties.LIT)).booleanValue()) {
            return;
        }
        float pPartialTick = ManaAndArtifice.instance.proxy.getPartialTick();
        double sinA = Math.sin(((float)worldIn.getGameTime() + pPartialTick) / 10.0f);
        double cosZ = Math.cos(((float)worldIn.getGameTime() + pPartialTick) / 10.0f);
        double sinB = Math.sin(((float)worldIn.getGameTime() + pPartialTick) / 20.0f);
        double offsetY = 0.075 * sinA;
        double offsetX = 0.035 * sinB;
        double offsetZ = 0.035 * cosZ;
        worldIn.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_STATIONARY.get())).setMaxAge(100), (double)pos.getX() + 0.5 + offsetX, (double)pos.getY() + 1.575 + offsetY, (double)pos.getZ() + 0.5 + offsetZ, (double)MathUtils.RandomBetween(-0.02f, 0.02f), (double)MathUtils.RandomBetween(0.0f, 0.02f), (double)MathUtils.RandomBetween(-0.02f, 0.02f));
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RunicLightTile(pPos, pState).setRuneword("light");
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        boolean flag;
        if (!pLevel.isClientSide && (flag = ((Boolean)pState.getValue((Property)BlockStateProperties.LIT)).booleanValue()) != pLevel.hasNeighborSignal(pPos)) {
            if (flag) {
                pLevel.scheduleTick(pPos, (Block)this, 4);
            } else {
                pLevel.setBlock(pPos, (BlockState)pState.cycle((Property)BlockStateProperties.LIT), 2);
            }
        }
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (((Boolean)pState.getValue((Property)BlockStateProperties.LIT)).booleanValue() && !pLevel.hasNeighborSignal(pPos)) {
            pLevel.setBlock(pPos, (BlockState)pState.cycle((Property)BlockStateProperties.LIT), 2);
        }
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState state, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        return SHAPE_OFFSET;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_MAIN;
    }
}

