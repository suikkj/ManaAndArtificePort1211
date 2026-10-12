 
package com.mna.blocks.artifice;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.LodestarTile;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LodestarBlock
extends WaterloggableBlock
implements EntityBlock {
    public static final IntegerProperty SURFACE_TYPE = IntegerProperty.create((String)"surface", (int)1, (int)3);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LOW_TIER = BooleanProperty.create((String)"low_tier");
    private static final VoxelShape SHAPE_WE = Shapes.or((VoxelShape)Block.box((double)4.0, (double)0.0, (double)3.0, (double)12.0, (double)6.5, (double)13.0), (VoxelShape)Block.box((double)2.0, (double)6.5, (double)1.0, (double)14.0, (double)14.0, (double)15.0));
    private static final VoxelShape SHAPE_NS = Shapes.or((VoxelShape)Block.box((double)3.0, (double)0.0, (double)4.0, (double)13.0, (double)6.5, (double)12.0), (VoxelShape)Block.box((double)1.0, (double)6.5, (double)2.0, (double)15.0, (double)14.0, (double)14.0));
    private final boolean low_tier;

    public LodestarBlock(boolean low_tier) {
        super(BlockBehaviour.Properties.of().noOcclusion().strength(5.0f, 1000.0f).isSuffocating(BlockInit.NEVER), false);
        this.low_tier = low_tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{SURFACE_TYPE, FACING, LOW_TIER});
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (((Boolean)state.getValue((Property)LOW_TIER)).booleanValue()) {
            Direction facing = (Direction)state.getValue((Property)FACING);
            if (facing == Direction.EAST || facing == Direction.WEST) {
                return SHAPE_WE;
            }
            return SHAPE_NS;
        }
        return Block.box((double)2.0, (double)0.0, (double)2.0, (double)14.0, (double)18.0, (double)14.0);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face;
        int surfaceType;
        if (this.low_tier) {
            return (BlockState)((BlockState)((BlockState)super.getStateForPlacement(context).setValue((Property)FACING, (Comparable)context.getHorizontalDirection().getOpposite())).setValue((Property)SURFACE_TYPE, (Comparable)Integer.valueOf(3))).setValue((Property)LOW_TIER, (Comparable)Boolean.valueOf(this.low_tier));
        }
        if (context.getClickedFace() == Direction.UP) {
            surfaceType = 1;
            face = context.getHorizontalDirection().getOpposite();
        } else if (context.getClickedFace() == Direction.DOWN) {
            surfaceType = 2;
            face = context.getHorizontalDirection().getOpposite();
        } else {
            surfaceType = 3;
            face = context.getClickedFace();
        }
        return (BlockState)((BlockState)((BlockState)super.getStateForPlacement(context).setValue((Property)FACING, (Comparable)face)).setValue((Property)SURFACE_TYPE, (Comparable)Integer.valueOf(surfaceType))).setValue((Property)LOW_TIER, (Comparable)Boolean.valueOf(this.low_tier));
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        LodestarTile lodestar;
        if (!worldIn.isClientSide && (lodestar = (LodestarTile)worldIn.getBlockEntity(pos)) != null) {
            player.openMenu(lodestar, (Consumer)lodestar);
        }
        return InteractionResult.sidedSuccess((boolean)worldIn.isClientSide);
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return (Boolean)state.getValue((Property)LOW_TIER) != false ? 0 : 10;
    }

    public RenderShape getRenderShape(BlockState state) {
        return this.low_tier ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LodestarTile(pos, state);
    }
}


