/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.SeerStoneBlock;
import com.mna.blocks.tileentities.ElementalSentryTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElementalSentryBlock
extends WaterloggableBlock
implements ICutoutBlock,
EntityBlock {
    public static final BooleanProperty SHOOTING = BooleanProperty.create((String)"shooting");
    public static final BooleanProperty TARGET_OVERRIDE = BooleanProperty.create((String)"targeting_overridden");
    protected static final VoxelShape COLLISION_SHAPE = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)2.0, (double)16.0);
    private static final Direction[] offsets = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    private final Affinity shootAffinity;

    public ElementalSentryBlock(Affinity shootAffinity) {
        super(BlockBehaviour.Properties.of().noOcclusion().strength(6.0f, 30.0f).noCollission().isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(SHOOTING, Boolean.valueOf(false))).setValue(TARGET_OVERRIDE, Boolean.valueOf(false)));
        this.shootAffinity = shootAffinity;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof ElementalSentryTile && placer instanceof Player) {
            ElementalSentryTile teas = (ElementalSentryTile)te;
            teas.setOwner((Player)placer);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        for (Direction dir : offsets) {
            BlockState offsetState = context.getLevel().getBlockState(context.getClickedPos().offset(dir.getNormal()));
            if (!(offsetState.getBlock() instanceof SeerStoneBlock)) continue;
            state = (BlockState)state.setValue(TARGET_OVERRIDE, Boolean.valueOf(true));
            break;
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{SHOOTING});
        builder.add(new Property[]{TARGET_OVERRIDE});
    }

    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean broken) {
        if (world.isClientSide) {
            return;
        }
        boolean found = false;
        for (Direction dir : offsets) {
            BlockState offsetState = world.getBlockState(pos.offset(dir.getNormal()));
            if (!(offsetState.getBlock() instanceof SeerStoneBlock)) continue;
            found = true;
            break;
        }
        if (!found && ((Boolean)state.getValue((Property)TARGET_OVERRIDE)).booleanValue()) {
            world.setBlock(pos, (BlockState)state.setValue(TARGET_OVERRIDE, Boolean.valueOf(false)), 3);
        } else if (found && !((Boolean)state.getValue((Property)TARGET_OVERRIDE)).booleanValue()) {
            world.setBlock(pos, (BlockState)state.setValue(TARGET_OVERRIDE, Boolean.valueOf(true)), 3);
        }
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof ElementalSentryTile && ((ElementalSentryTile)te).hasTarget()) {
            return 15;
        }
        return 0;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElementalSentryTile(pos, state, this.shootAffinity);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.ELEMENTAL_SENTRY.get() ? (world1, pos, state1, te) -> ElementalSentryTile.Tick(world1, pos, state1, (ElementalSentryTile)te) : null;
    }
}

