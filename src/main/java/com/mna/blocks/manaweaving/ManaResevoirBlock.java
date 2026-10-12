/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
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
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.manaweaving;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.ManaResevoirTile;
import com.mna.blocks.tileentities.ManaweaveProjectorTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ManaResevoirBlock
extends WaterloggableBlock
implements ICutoutBlock,
EntityBlock {
    public static final IntegerProperty FILL_LEVEL = IntegerProperty.create((String)"fill_level", (int)0, (int)4);
    public static final int EMPTY = 0;
    public static final int QUARTER = 1;
    public static final int HALF = 2;
    public static final int THREE_QUARTERS = 3;
    public static final int FULL = 4;

    public ManaResevoirBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0f).noOcclusion().isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(FILL_LEVEL, Integer.valueOf(0)));
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Block.box((double)6.0, (double)0.0, (double)6.0, (double)10.0, (double)8.0, (double)10.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FILL_LEVEL});
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaResevoirTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.MANA_RESEVOIR.get() ? (lvl, pos, state1, be) -> ManaResevoirTile.Tick(lvl, pos, state1, (ManaResevoirTile)be) : null;
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof ManaweaveProjectorTile) {
            float pct = ((ManaweaveProjectorTile)te).getMana() / 100.0f;
            return (int)(pct * 15.0f);
        }
        return 0;
    }
}

