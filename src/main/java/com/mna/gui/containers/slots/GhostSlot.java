/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.SlotItemHandler
 */
package com.mna.gui.containers.slots;

import com.mna.gui.containers.IExtendedItemHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GhostSlot
extends SlotItemHandler {
    public GhostSlot(IExtendedItemHandler p_40223_, int p_40224_, int p_40225_, int p_40226_) {
        super((IItemHandler)p_40223_, p_40224_, p_40225_, p_40226_);
    }

    public boolean mayPickup(Player p_40228_) {
        this.set(ItemStack.EMPTY);
        return false;
    }

    public boolean mayPlace(ItemStack stack) {
        this.set(stack.copy());
        return false;
    }

    public ItemStack remove(int par1) {
        this.set(ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

    protected void onQuickCraft(ItemStack par1ItemStack, int par2) {
    }

    protected void checkTakeAchievements(ItemStack itemCrafted) {
    }

    public void onQuickCraft(ItemStack par1ItemStack, ItemStack par2ItemStack) {
    }

    public void onTake(Player par1EntityPlayer, ItemStack par2ItemStack) {
        this.set(ItemStack.EMPTY);
    }
}

