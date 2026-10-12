/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.items;

import com.mna.entities.EntityInit;
import com.mna.entities.WandClone;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WandOfCloning
extends Item {
    public WandOfCloning() {
        super(new Item.Properties().stacksTo(1));
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {
            playerIn.onEquippedItemBroken(stack, handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            stack.shrink(1);
            double dist = 3.0;
            double vOffset = 1.0;
            Vec3 spawnPos = playerIn.position().add(0.0, vOffset, 0.0).add(playerIn.getLookAngle().normalize().scale(dist));
            WandClone ewc = new WandClone((EntityType<? extends WandClone>)((EntityType)EntityInit.WAND_CLONE.get()), worldIn);
            ewc.absMoveTo(spawnPos.x, spawnPos.y, spawnPos.z, playerIn.yHeadRot, playerIn.getXRot());
            worldIn.addFreshEntity((Entity)ewc);
        }
        return InteractionResultHolder.success(stack);
    }
}

