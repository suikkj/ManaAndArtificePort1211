/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$ItemCraftedEvent
 *  net.neoforged.bus.api.Event
 */
package com.mna.gui.containers.slots;

import com.mna.gui.containers.block.ContainerMagiciansWorkbench;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.Event;

public class SlotMagiciansWorkbenchOutput
extends Slot {
    private final Container craftMatrix;
    private final Player thePlayer;
    private final ContainerMagiciansWorkbench workbench;
    private int amountCrafted;

    public SlotMagiciansWorkbenchOutput(Player player, Container craftMatrix, Container craftResult, ContainerMagiciansWorkbench workbench, int index, int x, int y) {
        super(craftResult, index, x, y);
        this.thePlayer = player;
        this.craftMatrix = craftMatrix;
        this.workbench = workbench;
    }

    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    public ItemStack remove(int par1) {
        if (this.hasItem()) {
            this.amountCrafted += Math.min(par1, this.getItem().getCount());
        }
        return super.remove(par1);
    }

    protected void onQuickCraft(ItemStack par1ItemStack, int par2) {
        this.amountCrafted += par2;
        this.checkTakeAchievements(par1ItemStack);
    }

    protected void checkTakeAchievements(ItemStack itemCrafted) {
        itemCrafted.onCraftedBy(this.thePlayer.level(), this.thePlayer, this.amountCrafted);
        this.amountCrafted = 0;
        ItemStack[] components = new ItemStack[this.craftMatrix.getContainerSize()];
        for (int i = 0; i < components.length; ++i) {
            components[i] = this.craftMatrix.getItem(i) != null ? this.craftMatrix.getItem(i).copy() : null;
        }
        Level world = this.workbench.getWorkbench().getLevel();
        if (!world.isClientSide) {
            this.workbench.getWorkbench().rememberRecipe(itemCrafted, components);
        }
    }

    public void onQuickCraft(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        int i = par2ItemStack.getCount() - par1ItemStack.getCount();
        if (i > 0) {
            this.onQuickCraft(par2ItemStack, i);
            PlayerEvent.ItemCraftedEvent event = new PlayerEvent.ItemCraftedEvent(this.thePlayer, par2ItemStack, this.craftMatrix);
            NeoForge.EVENT_BUS.post((Event)event);
        }
    }

    public void onTake(Player par1EntityPlayer, ItemStack par2ItemStack) {
        this.checkTakeAchievements(par2ItemStack);
        PlayerEvent.ItemCraftedEvent event = new PlayerEvent.ItemCraftedEvent(par1EntityPlayer, par2ItemStack, this.craftMatrix);
        NeoForge.EVENT_BUS.post((Event)event);
        this.doComponentDecrements(par1EntityPlayer);
    }

    private void doComponentDecrements(Player par1EntityPlayer) {
        for (int i = 0; i < this.craftMatrix.getContainerSize(); ++i) {
            ItemStack itemstack1 = this.craftMatrix.getItem(i);
            if (itemstack1 == null) continue;
            if (itemstack1.getCount() > 1 || !this.searchAndDecrement(par1EntityPlayer, itemstack1)) {
                this.doStandardDecrement(par1EntityPlayer, this.craftMatrix, itemstack1, i);
                continue;
            }
            this.workbench.slotsChanged(this.craftMatrix);
        }
    }

    private boolean searchAndDecrement(Player player, ItemStack itemstack1) {
        for (int n = ContainerMagiciansWorkbench.INVENTORY_STORAGE_START; n <= ContainerMagiciansWorkbench.INVENTORY_STORAGE_END; ++n) {
            ItemStack wbStack = this.workbench.getWorkbench().getItem(n);
            if (wbStack == null || !ItemStack.isSameItemSameComponents((ItemStack)itemstack1, (ItemStack)wbStack)) continue;
            this.doStandardDecrement(player, this.workbench.getWorkbench(), wbStack, n);
            return true;
        }
        return false;
    }

    private void doStandardDecrement(Player player, Container inventory, ItemStack itemstack1, int i) {
        if (itemstack1.getItem().hasCraftingRemainingItem(itemstack1)) {
            ItemStack remainder = itemstack1.getItem().getCraftingRemainingItem(itemstack1);
            if (remainder.isDamageableItem() && remainder.getDamageValue() > remainder.getMaxDamage()) {
                remainder = ItemStack.EMPTY;
            }
            inventory.removeItem(i, 1);
            if (!remainder.isEmpty()) {
                if (inventory.getItem(i).isEmpty()) {
                    inventory.setItem(i, remainder);
                } else if (!player.addItem(remainder)) {
                    player.drop(remainder, false);
                }
            }
        } else {
            inventory.removeItem(i, 1);
        }
    }
}


