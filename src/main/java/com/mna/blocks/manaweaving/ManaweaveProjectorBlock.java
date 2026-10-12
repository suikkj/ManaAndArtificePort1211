/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
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
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.manaweaving;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.ManaweaveProjectorTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ManaweavingPatternPaper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ManaweaveProjectorBlock
extends WaterloggableBlock
implements ICutoutBlock,
EntityBlock {
    public static final IntegerProperty FILL_LEVEL = IntegerProperty.create((String)"fill_level", (int)0, (int)4);
    public static final BooleanProperty PROJECTING = BooleanProperty.create((String)"projecting");
    public static final int EMPTY = 0;
    public static final int QUARTER = 1;
    public static final int HALF = 2;
    public static final int THREE_QUARTERS = 3;
    public static final int FULL = 4;
    private static final VoxelShape FOOT = Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)2.0, (double)12.0);
    private static final VoxelShape PILLAR = Block.box((double)6.0, (double)2.0, (double)6.0, (double)10.0, (double)16.0, (double)10.0);
    private static final VoxelShape COMBINED_SHAPE = Shapes.or((VoxelShape)FOOT, (VoxelShape)PILLAR);

    public ManaweaveProjectorBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).noOcclusion().isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue((Property)FILL_LEVEL, (Comparable)Integer.valueOf(0))).setValue((Property)PROJECTING, (Comparable)Boolean.valueOf(false)));
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return COMBINED_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FILL_LEVEL, PROJECTING});
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaweaveProjectorTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.MANAWEAVE_PROJECTOR.get() ? (lvl, pos, state1, be) -> ManaweaveProjectorTile.Tick(lvl, pos, state1, (ManaweaveProjectorTile)be) : null;
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        if (!worldIn.isClientSide) {
            ItemStack stack = player.getItemInHand(handIn);
            BlockEntity te = worldIn.getBlockEntity(pos);
            if (te != null && te instanceof ManaweaveProjectorTile) {
                ManaweaveProjectorTile mWeave = (ManaweaveProjectorTile)te;
                if (stack.getItem() == ItemInit.RECIPE_SCRAP_MANAWEAVING_PATTERN.get()) {
                    ResourceLocation rLoc = ((ManaweavingPatternPaper)ItemInit.RECIPE_SCRAP_MANAWEAVING_PATTERN.get()).getRecipe(stack, worldIn);
                    mWeave.setPattern(rLoc, player);
                    worldIn.sendBlockUpdated(pos, state, state, 2);
                } else {
                    mWeave.trySpawnManaweaveEntity(player);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof ManaweaveProjectorTile) {
            ManaweaveProjectorTile mWeave = (ManaweaveProjectorTile)te;
            float pct = mWeave.getMana() / 100.0f;
            return (int)(pct * 15.0f);
        }
        return 0;
    }
}

