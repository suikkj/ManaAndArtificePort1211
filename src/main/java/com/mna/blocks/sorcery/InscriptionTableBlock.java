/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.WizardLabBlock;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.tools.BlockUtilities;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InscriptionTableBlock
extends WizardLabBlock
implements EntityBlock,
ICutoutBlock {
    public static final IntegerProperty CONTAINED_RESOURCES = IntegerProperty.create((String)"contained_resources", (int)0, (int)7);
    private static final VoxelShape OFFSET_SHAPE_NORTH = Shapes.or((VoxelShape)Block.box((double)0.0, (double)7.0, (double)6.0, (double)16.0, (double)8.0, (double)16.0), (VoxelShape[])new VoxelShape[]{Block.box((double)1.0, (double)0.0, (double)7.0, (double)2.0, (double)7.0, (double)15.0), Block.box((double)1.0, (double)0.0, (double)14.0, (double)15.0, (double)7.0, (double)15.0), Block.box((double)9.0, (double)0.0, (double)10.0, (double)14.0, (double)5.0, (double)15.0), Block.box((double)10.0, (double)0.0, (double)5.0, (double)12.0, (double)1.0, (double)7.0), Block.box((double)13.0, (double)0.0, (double)4.0, (double)15.0, (double)1.0, (double)6.0), Block.box((double)14.0, (double)0.0, (double)2.0, (double)15.0, (double)1.0, (double)3.0), Block.box((double)1.0, (double)0.0, (double)3.0, (double)3.0, (double)1.0, (double)5.0), Block.box((double)1.5, (double)1.0, (double)3.5, (double)2.5, (double)4.0, (double)4.5)});
    private static final VoxelShape OFFSET_SHAPE_NORTH_PAPER = Shapes.or((VoxelShape)OFFSET_SHAPE_NORTH, (VoxelShape)Block.box((double)2.0, (double)0.0, (double)9.0, (double)9.0, (double)3.0, (double)15.0));
    private static final VoxelShape OFFSET_SHAPE_EAST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_EAST_PAPER = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.EAST, OFFSET_SHAPE_NORTH_PAPER);
    private static final VoxelShape OFFSET_SHAPE_SOUTH = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_SOUTH_PAPER = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.SOUTH, OFFSET_SHAPE_NORTH_PAPER);
    private static final VoxelShape OFFSET_SHAPE_WEST = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH);
    private static final VoxelShape OFFSET_SHAPE_WEST_PAPER = BlockUtilities.rotateVoxelShape(Direction.NORTH, Direction.WEST, OFFSET_SHAPE_NORTH_PAPER);

    public InscriptionTableBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0f).randomTicks().noOcclusion().isSuffocating(BlockInit.NEVER), new BlockPos(0, 1, 0));
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)).setValue(CONTAINED_RESOURCES, Integer.valueOf(0)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{CONTAINED_RESOURCES});
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        Direction d1 = (Direction)stateIn.getValue((Property)HorizontalDirectionalBlock.FACING);
        float h1 = 0.875f;
        float h2 = 0.75f;
        float v1 = 1.255f;
        if (d1 == Direction.NORTH) {
            h2 = 1.0f - h2;
            h1 = 1.0f - h1;
        } else if (d1 == Direction.EAST) {
            h2 = 0.125f;
            h1 = 0.75f;
        } else if (d1 == Direction.WEST) {
            h2 = 0.875f;
            h1 = 0.25f;
        }
        for (int i = 0; i < 2; ++i) {
            worldIn.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setScale(0.01f).setMaxAge((int)(20.0 + Math.random() * 20.0)).setColor(30, 172, 255), (double)((float)pos.getX() + h1), (double)((float)pos.getY() + v1), (double)((float)pos.getZ() + h2), 0.0, 0.005, 0.0);
        }
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InscriptionTableTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.INSCRIPTION_TABLE.get() && !level.isClientSide() ? (lvl, pos, state1, be) -> InscriptionTableTile.ServerTick(lvl, pos, state1, (InscriptionTableTile)be) : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof InscriptionTableTile) {
            if (player.isCreative()) {
                ((InscriptionTableTile)te).setCreative(true);
            } else {
                ((InscriptionTableTile)te).setCreative(false);
            }
        }
        return super.use(state, worldIn, pos, player, handIn, p_225533_6_);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof InscriptionTableTile && ((InscriptionTableTile)te).isBuilding()) {
            return 15;
        }
        return 0;
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {
        for (int i = 0; i < 20; ++i) {
            world.addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_WOOD.defaultBlockState()), (double)pos.getX() + Math.random(), (double)pos.getY() + Math.random(), (double)pos.getZ() + Math.random(), 0.0, 0.05 * Math.random(), 0.0);
        }
    }

    @Override
    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity workbench = level.getBlockEntity(pos);
        if (workbench != null && workbench instanceof InscriptionTableTile) {
            return (InscriptionTableTile)workbench;
        }
        return null;
    }

    @Override
    protected Consumer<FriendlyByteBuf> getContainerBufferWriter(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity workbench = level.getBlockEntity(pos);
        if (workbench != null && workbench instanceof InscriptionTableTile) {
            return (InscriptionTableTile)workbench;
        }
        return null;
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        boolean hasPaper = ((Integer)parentState.getValue((Property)CONTAINED_RESOURCES) & 2) > 0;
        Direction facing = (Direction)parentState.getValue((Property)HorizontalDirectionalBlock.FACING);
        switch (facing) {
            case EAST: {
                return hasPaper ? OFFSET_SHAPE_EAST_PAPER : OFFSET_SHAPE_EAST;
            }
            case NORTH: {
                return hasPaper ? OFFSET_SHAPE_NORTH_PAPER : OFFSET_SHAPE_NORTH;
            }
            case SOUTH: {
                return hasPaper ? OFFSET_SHAPE_SOUTH_PAPER : OFFSET_SHAPE_SOUTH;
            }
            case WEST: {
                return hasPaper ? OFFSET_SHAPE_WEST_PAPER : OFFSET_SHAPE_WEST;
            }
        }
        return Shapes.block();
    }
}

