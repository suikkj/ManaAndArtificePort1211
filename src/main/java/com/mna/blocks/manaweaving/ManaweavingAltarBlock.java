 
package com.mna.blocks.manaweaving;

import com.mna.api.blocks.IManaweaveNotifiable;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.ManaweavingAltarTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ItemManaweaverWand;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ManaweavingAltarBlock
extends WaterloggableBlock
implements EntityBlock,
IManaweaveNotifiable {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape FOOT = Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)2.0, (double)12.0);
    private static final VoxelShape PILLAR = Block.box((double)7.0, (double)2.0, (double)7.0, (double)9.0, (double)12.0, (double)9.0);
    private static final VoxelShape TOP = Block.box((double)0.0, (double)12.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0);
    private static final VoxelShape COMBINED_SHAPE = Shapes.or((VoxelShape)FOOT, (VoxelShape[])new VoxelShape[]{PILLAR, TOP});

    public ManaweavingAltarBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).noOcclusion().isSuffocating(BlockInit.NEVER), false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
    }

    public VoxelShape getShape(BlockState p_48816_, BlockGetter p_48817_, BlockPos p_48818_, CollisionContext p_48819_) {
        return COMBINED_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        ItemStack activeStack = player.getMainHandItem();
        if (activeStack.getItem() == ItemInit.RECIPE_COPY_BOOK.get()) {
            return InteractionResult.PASS;
        }
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null) {
            return InteractionResult.FAIL;
        }
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof ManaweavingAltarTile) {
            ManaweavingAltarTile te = (ManaweavingAltarTile)tileEntity;
            if (activeStack.isEmpty() && te.reCraft(player)) {
                return InteractionResult.SUCCESS;
            }
            if (activeStack.getItem() instanceof ItemManaweaverWand) {
                te.popPattern(player);
            } else if (player.isCrouching() || activeStack.isEmpty()) {
                ItemStack stack = te.popItem(player);
                if (!stack.isEmpty() && !player.addItem(stack)) {
                    player.drop(stack, true);
                }
            } else if (!activeStack.isEmpty()) {
                ItemStack single = activeStack.copy();
                single.setCount(1);
                if (te.pushItem(player, single) && !player.isCreative()) {
                    activeStack.shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaweavingAltarTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.MANAWEAVING_ALTAR.get() ? (lvl, pos, state1, be) -> ManaweavingAltarTile.Tick(lvl, pos, state1, (ManaweavingAltarTile)be) : null;
    }

    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        this.dropInventory(worldIn, pos);
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private void dropInventory(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof ManaweavingAltarTile) {
            Containers.dropContents((Level)world, (BlockPos)pos, (Container)((Container)tileEntity));
        }
    }

    @Override
    public boolean notify(Level world, BlockPos pos, BlockState state, List<IManaweavePattern> patterns, @Nullable LivingEntity caster) {
        if (!world.isClientSide && caster instanceof Player) {
            Player player = (Player)caster;
            ManaweavingAltarTile te = (ManaweavingAltarTile)world.getBlockEntity(pos);
            if (te != null && te.getAddedPatterns().size() < 6) {
                te.pushPattern(patterns.get(0), player);
                return true;
            }
        }
        return false;
    }
}

