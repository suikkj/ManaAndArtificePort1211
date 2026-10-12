 
package com.mna.gui.containers.item;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.gui.SingleItemSlot;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.slots.BaseSlot;
import com.mna.items.ItemInit;
import com.mna.items.base.IBagItem;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import com.mna.api.tools.MNACap;

public class ContainerGrimoire
extends AbstractContainerMenu {
    private IItemHandler inventory;
    private int bagHash;
    private ItemStack bagStack;

    public ContainerGrimoire(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, ((IPlayerMagic)playerInventory.player.getData(PlayerMagicProvider.MAGIC.get())).getGrimoireInventory());
    }

    public ContainerGrimoire(int i, Inventory playerInv, SimpleContainer grimoireInv) {
        super((MenuType)ContainerInit.GRIMOIRE.get(), i);
        this.inventory = new InvWrapper((Container)grimoireInv);
        this.bagStack = playerInv.getItem(playerInv.selected);
        this.bagHash = this.bagStack.hashCode();
        this.initializeSlots(playerInv);
    }

    protected void initializeSlots(Inventory playerInv) {
        for (int k = 0; k < this.slotsPerRow(); ++k) {
            for (int j = 0; j < this.numRows(); ++j) {
                if (k % this.slotsPerRow() == 0) {
                    this.addSlot((Slot)this.slot(this.inventory, k + j * this.slotsPerRow(), 18 + k * 18, 7 + j * 18));
                    continue;
                }
                this.addSlot((Slot)this.slot(this.inventory, k + j * this.slotsPerRow(), 107 + k * 26, 7 + j * 18));
            }
        }
        int i = (this.numRows() - 4) * 18;
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot((Container)playerInv, j1 + l * 9 + 9, 48 + j1 * 18, 102 + l * 18 + i));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot((Container)playerInv, i1, 48 + i1 * 18, 160 + i));
        }
    }

    public BaseSlot slot(IItemHandler inv, int index, int x, int y) {
        return new SingleItemSlot(inv, index, x, y, (Item)ItemInit.SPELL.get());
    }

    protected int slotsPerRow() {
        return 2;
    }

    protected int numRows() {
        return 8;
    }

    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        try {
            if (this.getSlot(slotId).getItem().hashCode() == this.bagHash) {
                return;
            }
        }
        catch (Exception exception) {
             
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (!playerIn.level().isClientSide()) {
            MNACap.get(playerIn, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                m.setSyncGrimoire();
                m.forceSync();
            });
        }
    }

    public boolean stillValid(@Nonnull Player player) {
        ItemStack held = player.getMainHandItem();
        return held == this.bagStack && !this.bagStack.isEmpty() && held.hashCode() == this.bagHash && held.getItem() instanceof IBagItem;
    }

    @Nonnull
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.numRows() * this.slotsPerRow() ? !this.moveItemStackTo(itemstack1, this.numRows() * this.slotsPerRow(), this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, this.numRows() * this.slotsPerRow(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}

