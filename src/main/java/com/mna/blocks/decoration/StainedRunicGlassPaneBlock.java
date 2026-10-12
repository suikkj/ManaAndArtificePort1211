/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.StainedGlassPaneBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 */
package com.mna.blocks.decoration;

import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.blocks.BlockInit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class StainedRunicGlassPaneBlock
extends StainedGlassPaneBlock
implements ITranslucentBlock {
    public StainedRunicGlassPaneBlock(DyeColor pDyeColor) {
        super(pDyeColor, BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.3f).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, lel, pos, type) -> false).isRedstoneConductor(BlockInit.NEVER).isSuffocating(BlockInit.NEVER).isViewBlocking(BlockInit.NEVER).lightLevel(s -> 15));
    }
}

