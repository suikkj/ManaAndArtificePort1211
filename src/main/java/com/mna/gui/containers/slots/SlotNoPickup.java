/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.IItemHandlerModifiable
 */
package com.mna.gui.containers.slots;

import com.mna.gui.containers.slots.BaseSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class SlotNoPickup
extends BaseSlot {
    public SlotNoPickup(IItemHandlerModifiable craftResult, int index, int x, int y) {
        super((IItemHandler)craftResult, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    public ItemStack remove(int par1) {
        return ItemStack.EMPTY;
    }

    protected void onQuickCraft(ItemStack par1ItemStack, int par2) {
    }

    protected void checkTakeAchievements(ItemStack itemCrafted) {
    }

    public void onQuickCraft(ItemStack par1ItemStack, ItemStack par2ItemStack) {
    }

    public void onTake(Player par1EntityPlayer, ItemStack par2ItemStack) {
    }
}

