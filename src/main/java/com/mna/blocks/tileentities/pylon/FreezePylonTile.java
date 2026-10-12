/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.SnowLayerBlock
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.FluidState
 */
package com.mna.blocks.tileentities.pylon;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.init.TileEntityInit;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public class FreezePylonTile
extends PylonTileBase {
    private static final ResourceLocation icon = RLoc.create("textures/gui/pylon/freeze.png");

    public FreezePylonTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FreezePylonTile(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType)TileEntityInit.FREEZE_PYLON.get(), pPos, pBlockState);
    }

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, FreezePylonTile tile) {
        if (tile.tick()) {
            int n = tile.powerUpTicks;
            Objects.requireNonNull(tile);
            if (n == 60) {
                tile.forAllBlocks(20, (cPos, cState) -> {
                    FluidState flState;
                    if (cState.is(Blocks.WATER) && (flState = level.getFluidState(cPos)) != null) {
                        if (flState.isSource()) {
                            level.setBlock(cPos, Blocks.ICE.defaultBlockState(), 3);
                        } else {
                            int amt = flState.getAmount();
                            BlockState snowState = Blocks.SNOW.defaultBlockState();
                            if ((snowState = (BlockState)snowState.setValue((Property)SnowLayerBlock.LAYERS, (Comparable)Integer.valueOf(Math.min(Math.max(amt, 1), 8)))).canSurvive((LevelReader)level, cPos)) {
                                level.setBlock(cPos, snowState, 3);
                            }
                        }
                    }
                });
            }
        }
    }
}

