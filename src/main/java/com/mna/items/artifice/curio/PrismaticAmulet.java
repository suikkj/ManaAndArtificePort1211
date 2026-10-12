/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.items.artifice.curio;

import com.mna.api.items.ChargeableItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PrismaticAmulet
extends ChargeableItem {
    public PrismaticAmulet(float maxMana) {
        super(new Item.Properties().stacksTo(1).setNoRepair(), maxMana);
    }

    @Override
    protected float manaPerRechargeTick() {
        return 10.0f;
    }

    @Override
    protected boolean tickEffect(ItemStack stack, Player player, Level world, int slot, float mana, boolean selected) {
        return false;
    }
}

