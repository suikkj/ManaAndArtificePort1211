/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.tileentities;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.blocks.tileentities.init.TileEntityInit;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RunicLightTile
extends BlockEntity {
    private int[] runeword;
    private boolean isTorch;

    public RunicLightTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public RunicLightTile(BlockPos pPos, BlockState pBlockState) {
        this((BlockEntityType)TileEntityInit.RUNIC_LIGHTS.get(), pPos, pBlockState);
    }

    public RunicLightTile setRuneword(String runeword) {
        runeword = runeword.toLowerCase();
        ArrayList<Integer> runes = new ArrayList<Integer>();
        for (int i = 0; i < runeword.length(); ++i) {
            Integer val = PylonTileBase.lookup.get(Character.valueOf(runeword.charAt(i)));
            if (val != null) {
                runes.add(val);
                continue;
            }
            runes.add(-1);
        }
        int count = 0;
        this.runeword = new int[runes.size()];
        Iterator iterator = runes.iterator();
        while (iterator.hasNext()) {
            int i = (Integer)iterator.next();
            this.runeword[count++] = i;
        }
        return this;
    }

    public RunicLightTile setTorch() {
        this.isTorch = true;
        return this;
    }

    public int[] getRuneword() {
        return this.runeword;
    }

    public boolean isTorch() {
        return this.isTorch;
    }
}

