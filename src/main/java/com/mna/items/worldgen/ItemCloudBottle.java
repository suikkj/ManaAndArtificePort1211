/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.CriteriaTriggers
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.LevelAccessor
 *  net.neoforged.neoforge.common.util.BlockSnapshot
 *  net.neoforged.neoforge.event.ForgeEventFactory
 */
package com.mna.items.worldgen;

import com.mna.blocks.BlockInit;
import com.mna.blocks.utility.CloudBlock;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.common.util.BlockSnapshot;

public class ItemCloudBottle
extends Item {
    public ItemCloudBottle() {
        super(new Item.Properties().stacksTo(16).setNoRepair());
    }

    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        if (face == Direction.UP) {
            pos = pos.above();
            int count = 0;
            while (context.getLevel().getBlockState(pos).isAir() && count++ < 5) {
                pos = pos.above();
            }
            pos = pos.below();
        } else {
            pos = pos.offset(face.getNormal());
        }
        if (context.getLevel().getBlockState(pos).isAir()) {
            BlockSnapshot blocksnapshot = BlockSnapshot.create((ResourceKey)context.getLevel().dimension(), (LevelAccessor)context.getLevel(), (BlockPos)pos);
            context.getLevel().setBlock(pos, ((CloudBlock)BlockInit.STORM_CLOUD.get()).defaultBlockState(), 11);
            if (true) {
                blocksnapshot.restore(true);
                return InteractionResult.FAIL;
            }
            if (context.getPlayer() instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)context.getPlayer(), pos, context.getItemInHand());
            }
            if (!context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            context.getLevel().playSound(context.getPlayer(), pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}


