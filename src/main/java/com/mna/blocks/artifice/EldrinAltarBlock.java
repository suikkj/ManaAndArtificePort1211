/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 */
package com.mna.blocks.artifice;

import com.mna.blocks.runeforging.PedestalBlock;
import com.mna.blocks.tileentities.EldrinAltarTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EldrinAltarBlock
extends PedestalBlock
implements EntityBlock {
    public EldrinAltarBlock() {
        super(false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EldrinAltarTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.ELDRIN_ALTAR_TILE.get() ? (lvl, pos, state1, be) -> EldrinAltarTile.Tick(lvl, pos, state1, (EldrinAltarTile)be) : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        ItemStack activeStack = player.getItemInHand(handIn);
        if (activeStack.getItem() == ItemInit.RECIPE_COPY_BOOK.get()) {
            return InteractionResult.PASS;
        }
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof EldrinAltarTile) {
            EldrinAltarTile tile = (EldrinAltarTile)te;
            if (tile.getStage() != EldrinAltarTile.Stage.IDLE) {
                return InteractionResult.FAIL;
            }
            if (!tile.structureMatched()) {
                if (worldIn.isClientSide) {
                    tile.setGhostMultiblock();
                }
                return InteractionResult.SUCCESS;
            }
            if (worldIn.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (activeStack.isEmpty() && tile.reCraft(player)) {
                return InteractionResult.SUCCESS;
            }
            if (!tile.isEmpty()) {
                if (activeStack.isEmpty() && tile.startCrafting(player)) {
                    return InteractionResult.SUCCESS;
                }
                ItemStack stack = tile.removeItem(0, 1);
                if (!player.addItem(stack)) {
                    player.drop(stack, true);
                }
            } else if (!activeStack.isEmpty() && tile.isEmpty()) {
                ItemStack single = activeStack.copy();
                single.setCount(1);
                tile.setItem(0, single);
                if (!player.isCreative()) {
                    activeStack.shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}

