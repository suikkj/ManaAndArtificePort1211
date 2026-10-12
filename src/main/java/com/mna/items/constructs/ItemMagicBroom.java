/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.items.constructs;

import com.mna.api.items.TieredItem;
import com.mna.blocks.tileentities.ManaweavingAltarTile;
import com.mna.entities.constructs.MagicBroom;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class ItemMagicBroom
extends TieredItem {
    private int broomType;

    public ItemMagicBroom(int broomType) {
        super(new Item.Properties().stacksTo(1).setNoRepair());
        this.broomType = broomType;
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockEntity te;
        Level world = context.getLevel();
        if (!world.isClientSide && (te = world.getBlockEntity(context.getClickedPos())) instanceof Container && !(te instanceof ManaweavingAltarTile)) {
            MagicBroom broom = new MagicBroom(world);
            Vec3 tePos = new Vec3((double)(te.getBlockPos().getX() + context.getClickedFace().getStepX()) + 0.5, (double)(te.getBlockPos().getY() + context.getClickedFace().getStepY()), (double)(te.getBlockPos().getZ() + context.getClickedFace().getStepZ()) + 0.5);
            broom.setPos(tePos.x, tePos.y, tePos.z);
            broom.setContainerPosition(te.getBlockPos());
            broom.setBroomType(this.broomType);
            world.addFreshEntity((Entity)broom);
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }
        return super.onItemUseFirst(stack, context);
    }
}

