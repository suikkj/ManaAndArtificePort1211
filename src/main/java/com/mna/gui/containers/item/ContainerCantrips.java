 
package com.mna.gui.containers.item;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.gui.SingleItemSlot;
import com.mna.cantrips.CantripRegistry;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.ContainerInit;
import com.mna.items.manaweaving.ItemManaweaverWand;
import java.util.HashMap;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.mutable.MutableInt;

public class ContainerCantrips
extends AbstractContainerMenu {
    private Container inventory;
    private int NUM_SLOTS = 0;
    private HashMap<ResourceLocation, SingleItemSlot> slots_per_cantrip = new HashMap();

    public ContainerCantrips(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, ((IPlayerMagic)playerInventory.player.getData(PlayerMagicProvider.MAGIC.get())).getCantripData().getAsInventory());
    }

    public ContainerCantrips(int i, Inventory playerInv, Container grimoireInv) {
        super((MenuType)ContainerInit.CANTRIPS.get(), i);
        this.inventory = grimoireInv;
        this.initializeSlots(playerInv);
    }

    protected void initializeSlots(Inventory playerInv) {
        MutableInt cantrip_index = new MutableInt(0);
        CantripRegistry.INSTANCE.getCantrips(playerInv.player).stream().forEach(c -> {
            if (!c.isStackLocked()) {
                int x = 9;
                int y = 33;
                SingleItemSlot slot = new SingleItemSlot((IItemHandler)new InvWrapper(this.inventory), this.NUM_SLOTS, x, y, c.getValidDynamicItem());
                slot.setActive(false);
                this.addSlot((Slot)slot);
                this.slots_per_cantrip.put(c.getId(), slot);
                ++this.NUM_SLOTS;
            }
            cantrip_index.increment();
        });
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot((Container)playerInv, j1 + l * 9 + 9, 25 + j1 * 18, 174 + l * 18));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot((Container)playerInv, i1, 25 + i1 * 18, 232));
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void enableSlotsForCantrip(ResourceLocation cantrip) {
        this.slots_per_cantrip.values().forEach(al -> al.setActive(false));
        if (this.cantripHasSlot(cantrip)) {
            this.slots_per_cantrip.get(cantrip).setActive(true);
        }
    }

    public boolean cantripHasSlot(ResourceLocation cantrip) {
        return this.slots_per_cantrip.containsKey(cantrip);
    }

    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        try {
            if (this.getSlot(slotId).getItem().getItem() instanceof ItemManaweaverWand) {
                return;
            }
        }
        catch (Exception exception) {
             
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public boolean stillValid(@Nonnull Player player) {
        return true;
    }

    @Nonnull
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.NUM_SLOTS ? !this.moveItemStackTo(itemstack1, this.NUM_SLOTS, this.slots.size(), true) : !this.moveItemStackTo(itemstack1, 0, this.NUM_SLOTS, false)) {
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


