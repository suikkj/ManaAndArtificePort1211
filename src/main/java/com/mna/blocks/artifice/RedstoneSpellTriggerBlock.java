/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 */
package com.mna.blocks.artifice;

import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;

public class RedstoneSpellTriggerBlock
extends Block
implements ISpellInteractibleBlock<RedstoneSpellTriggerBlock> {
    public static final IntegerProperty OUTPUT_POWER = IntegerProperty.create((String)"output_power", (int)0, (int)15);

    public RedstoneSpellTriggerBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f));
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(OUTPUT_POWER, Integer.valueOf(0)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{OUTPUT_POWER});
    }

    @Override
    public boolean onHitBySpell(Level world, BlockPos pos, ISpellDefinition spell) {
        world.setBlockAndUpdate(pos, (BlockState)this.defaultBlockState().setValue(OUTPUT_POWER, Integer.valueOf(15)));
        world.updateNeighborsAt(pos, (Block)this);
        if (!world.isClientSide) {
            world.scheduleTick(new BlockPos((Vec3i)pos), (Block)this, 10);
        }
        return true;
    }

    public boolean isSignalSource(BlockState state) {
        return true;
    }

    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        int power = (Integer)state.getValue((Property)OUTPUT_POWER);
        if (power > 0) {
            worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(OUTPUT_POWER, Integer.valueOf(power - 1)));
            worldIn.updateNeighborsAt(pos, (Block)this);
            worldIn.scheduleTick(new BlockPos((Vec3i)pos), (Block)this, 10);
        }
    }

    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getSignal(blockAccess, pos, side);
    }

    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return (Integer)blockState.getValue((Property)OUTPUT_POWER);
    }

    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return true;
    }
}

