 
package com.mna.blocks.artifice;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.BlockWithOffset;
import com.mna.api.blocks.tile.EldrinCapacitorTile;
import com.mna.api.blocks.tile.IEldrinCapacitorTile;
import com.mna.api.gui.EldrinCapacitorPermissionsContainer;
import com.mna.blocks.tileentities.EldrinConduitTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import com.mna.api.tools.MNACap;

public class EldrinConduitBlock
extends BlockWithOffset
implements EntityBlock {
    private final Affinity affinity;
    private final boolean isLesser;
    protected static final VoxelShape LESSER_SHAPE = Block.box((double)6.0, (double)0.0, (double)6.0, (double)10.0, (double)16.0, (double)10.0);
    protected static final VoxelShape LESSER_SHAPE_OFFSET = Block.box((double)6.0, (double)0.0, (double)6.0, (double)10.0, (double)8.0, (double)10.0);

    public EldrinConduitBlock(Affinity affinity, boolean isLesser) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).noOcclusion(), new BlockPos(0, 1, 0));
        this.affinity = affinity;
        this.isLesser = isLesser;
    }

    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EldrinConduitTile(this.affinity, this.isLesser ? 5.0f : 250.0f, this.isLesser, pos, state);
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Consumer<FriendlyByteBuf> dataWriter = this.getContainerBufferWriter(state, level, pos, player, hand, hitResult);
        MenuProvider provider = this.getProvider(state, level, pos, player, hand, hitResult);
        if (provider == null || dataWriter == null) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(provider, dataWriter);
        return InteractionResult.SUCCESS;
    }

    protected MenuProvider getProvider(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !(be instanceof EldrinCapacitorTile)) {
            return null;
        }
        return new SimpleMenuProvider((id, playerInv, user) -> new EldrinCapacitorPermissionsContainer(id, playerInv, (EldrinCapacitorTile)be), (Component)Component.empty());
    }

    protected Consumer<FriendlyByteBuf> getContainerBufferWriter(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity workbench = level.getBlockEntity(pos);
        if (workbench != null && workbench instanceof EldrinCapacitorTile) {
            return (EldrinCapacitorTile)workbench;
        }
        return null;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.ELDRIN_CONDUIT_TILE.get() ? (lvl, pos, state1, be) -> EldrinConduitTile.Tick(lvl, pos, state1, (EldrinConduitTile)be) : null;
    }

    public RenderShape getRenderShape(BlockState p_149645_1_) {
        return this.isLesser ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @Nullable
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return this.isLesser ? LESSER_SHAPE : Shapes.or((VoxelShape)Block.box((double)4.0, (double)4.0, (double)4.0, (double)12.0, (double)10.0, (double)12.0), (VoxelShape)Block.box((double)2.0, (double)10.0, (double)2.0, (double)14.0, (double)15.0, (double)14.0));
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            MNACap.get(worldIn.getChunkAt(pos), ChunkMagicProvider.MAGIC).ifPresent(magic -> magic.popKnownEldrinSupplier(pos));
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer instanceof Player) {
            Player player = (Player)pPlacer;
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof IEldrinCapacitorTile) {
                IEldrinCapacitorTile capTile = (IEldrinCapacitorTile)be;
                capTile.setPlacedBy(player);
                pLevel.getChunkAt(pPos).getCapability(ManaAndArtificeMod.getChunkMagicCapability()).ifPresent(magic -> magic.pushKnownEldrinSupplier(pPos));
            }
        }
    }

    @Override
    public VoxelShape getOffsetShape(BlockPos delta, BlockState parentState, BlockPos parentPos, BlockGetter pLevel, CollisionContext pContext) {
        if (this.isLesser) {
            return LESSER_SHAPE_OFFSET;
        }
        return Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)4.0, (double)12.0);
    }
}


