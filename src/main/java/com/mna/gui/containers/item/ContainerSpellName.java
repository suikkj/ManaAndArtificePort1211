/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.gui.containers.item;

import net.minecraft.core.component.DataComponents;
import com.mna.gui.containers.ContainerInit;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.BookOfRote;
import com.mna.items.sorcery.ItemSpell;
import com.mna.items.sorcery.MagicStaff;
import com.mna.network.ClientMessageDispatcher;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ContainerSpellName
extends AbstractContainerMenu {
    public ItemStack heldItem;
    public InteractionHand hand;

    public ContainerSpellName(@Nullable MenuType<?> type, int id, Inventory playerInv, InteractionHand hand) {
        super(type, id);
        this.heldItem = playerInv.player.getItemInHand(hand);
        this.hand = hand;
    }

    public ContainerSpellName(int i, Inventory playerInv, FriendlyByteBuf buffer) {
        this((MenuType)ContainerInit.SPELL_CUSTOMIZATION.get(), i, playerInv, playerInv.player.getMainHandItem().getItem() == ItemInit.SPELL.get() || playerInv.player.getMainHandItem().getItem() instanceof BookOfRote || playerInv.player.getMainHandItem().getItem() instanceof MagicStaff ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    public String getName() {
        return this.heldItem.getHoverName().getString();
    }

    public int getIconIndex() {
        return ItemSpell.getCustomIcon(this.heldItem);
    }

    public void setName(String name) {
        this.heldItem.set(DataComponents.ITEM_NAME, (Component)Component.literal((String)name));
    }

    public void setIconIndex(int index) {
        ItemSpell.setCustomIcon(this.heldItem, index);
    }

    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (playerIn.level().isClientSide()) {
            ClientMessageDispatcher.sendSpellCustomizationValues(this.getName(), this.getIconIndex(), this.hand);
        }
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}

