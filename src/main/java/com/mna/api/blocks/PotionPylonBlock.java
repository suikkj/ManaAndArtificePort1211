/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.api.blocks;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.blocks.PylonBlock;
import com.mna.api.blocks.tile.PotionEffectPylonTile;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PotionPylonBlock
extends PylonBlock
implements EntityBlock {
    private Supplier<MobEffect> effect;
    private int amplifier = 0;
    private int duration = 100;
    private int reapplicationThreshold = 40;

    public PotionPylonBlock(BlockBehaviour.Properties pProperties, Supplier<MobEffect> applicationEffect, String runeword) {
        super(pProperties, runeword, new BlockPos(0, 1, 0), new BlockPos(0, 2, 0));
        this.effect = applicationEffect;
    }

    public PotionPylonBlock setAmplifier(int amplifier) {
        this.amplifier = amplifier;
        return this;
    }

    public PotionPylonBlock setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public PotionPylonBlock setReapplicationThreshold(int threshold) {
        this.reapplicationThreshold = threshold;
        return this;
    }

    public MobEffect getEffect() {
        return this.effect.get();
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getReapplicationThreshold() {
        return this.reapplicationThreshold;
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        PotionEffectPylonTile tile = new PotionEffectPylonTile(pPos, pState);
        tile.setRuneword(this.runeword);
        return tile;
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pBlockEntityType == ManaAndArtificeMod.getTileHelper().getPotionPylonTileType() ? (lvl, pos, state1, be) -> PotionEffectPylonTile.Tick(lvl, pos, state1, (PotionEffectPylonTile)be) : null;
    }
}

