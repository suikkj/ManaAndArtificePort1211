/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
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
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.artifice;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.BlockWithOffset;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.RefractionLensTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RefractionLensBlock
extends BlockWithOffset
implements ICutoutBlock,
EntityBlock {
    private final Affinity affinity;
    public static final BooleanProperty INVALID = BooleanProperty.create((String)"invalid");
    public static final VoxelShape SHAPE = Block.box((double)0.0, (double)6.0, (double)0.0, (double)16.0, (double)10.0, (double)16.0);

    public RefractionLensBlock(Affinity affinity) {
        super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).noOcclusion().strength(3.0f).isViewBlocking(BlockInit.NEVER), new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(-1, 0, 1), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1), new BlockPos(1, 0, -1), new BlockPos(1, 0, 0), new BlockPos(1, 0, 1));
        this.affinity = affinity;
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)INVALID, (Comparable)Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{INVALID});
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefractionLensTile(this.affinity, pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.REFRACTION_LENS.get() ? (lvl, pos, state1, be) -> RefractionLensTile.Tick(lvl, pos, state1, (RefractionLensTile)be) : null;
    }

    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity living, ItemStack stack) {
        if (living instanceof Player) {
            RefractionLensTile tile = (RefractionLensTile)world.getBlockEntity(pos);
            tile.setPlacedBy((Player)living);
        }
    }

    public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean doDrops) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null && tile instanceof RefractionLensTile) {
            ((RefractionLensTile)tile).onBlockBroken();
        }
        super.onRemove(state, world, pos, newState, doDrops);
    }

    public VoxelShape getVisualShape(BlockState p_230322_1_, BlockGetter p_230322_2_, BlockPos p_230322_3_, CollisionContext p_230322_4_) {
        return Shapes.empty();
    }

    public InteractionResult use(BlockState p_225533_1_, Level p_225533_2_, BlockPos p_225533_3_, Player p_225533_4_, InteractionHand p_225533_5_, BlockHitResult p_225533_6_) {
        BlockEntity te;
        if (p_225533_2_.isClientSide && (te = p_225533_2_.getBlockEntity(p_225533_3_)) instanceof RefractionLensTile) {
            ((RefractionLensTile)te).setGhostMultiblock();
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockPlaceContext adjustPlacement(BlockPlaceContext attempted) {
        switch (attempted.getClickedFace()) {
            case EAST: 
            case NORTH: 
            case SOUTH: 
            case WEST: {
                BlockPos newPos = attempted.getClickedPos().offset(attempted.getClickedFace().getNormal());
                BlockHitResult bhr = new BlockHitResult(Vec3.atCenterOf((Vec3i)newPos), attempted.getClickedFace(), newPos, true);
                return new BlockPlaceContext(attempted.getPlayer(), attempted.getHand(), attempted.getItemInHand(), bhr);
            }
        }
        return attempted;
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        return SHAPE;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}

