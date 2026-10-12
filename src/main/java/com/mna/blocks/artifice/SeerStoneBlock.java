 
package com.mna.blocks.artifice;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.ElementalSentryBlock;
import com.mna.blocks.tileentities.ElementalSentryTile;
import com.mna.blocks.tileentities.SeerStoneTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeerStoneBlock
extends WaterloggableBlock
implements EntityBlock {
    public static final BooleanProperty HAS_TARGET = BooleanProperty.create((String)"has_target");
    protected static final VoxelShape COLLISION_SHAPE = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)2.0, (double)16.0);

    public SeerStoneBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).noOcclusion().isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(HAS_TARGET, Boolean.valueOf(false)));
    }

    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof SeerStoneTile && placer instanceof Player) {
            SeerStoneTile teas = (SeerStoneTile)te;
            teas.setOwner((Player)placer);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{HAS_TARGET});
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeerStoneTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.SEER_STONE.get() ? (world1, pos, state1, te) -> SeerStoneTile.Tick(world1, pos, state1, (SeerStoneTile)te) : null;
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof SeerStoneTile) {
            player.openMenu((MenuProvider)((SeerStoneTile)te), (Consumer)((SeerStoneTile)te));
        }
        return InteractionResult.SUCCESS;
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter getter, BlockPos pos, PathComputationType type) {
        return false;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        BlockState neighborState = world.getBlockState(neighbor);
        if (neighborState.getBlock() instanceof ElementalSentryBlock) {
            BlockEntity me = world.getBlockEntity(pos);
            BlockEntity they = world.getBlockEntity(neighbor);
            if (me != null && me instanceof SeerStoneTile && they != null && they instanceof ElementalSentryTile) {
                ((SeerStoneTile)me).copyTargetTo((ElementalSentryTile)they);
            }
        }
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        if (((Boolean)blockState.getValue((Property)HAS_TARGET)).booleanValue()) {
            return 15;
        }
        return 0;
    }

    public boolean isSignalSource(BlockState state) {
        return true;
    }

    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return (Boolean)blockState.getValue((Property)HAS_TARGET) != false ? 15 : 0;
    }

    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return (Boolean)blockState.getValue((Property)HAS_TARGET) != false ? 15 : 0;
    }

    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return direction != Direction.UP;
    }
}


