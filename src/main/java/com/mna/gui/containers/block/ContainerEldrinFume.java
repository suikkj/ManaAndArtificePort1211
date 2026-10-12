 
package com.mna.gui.containers.block;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.block.SimpleWizardLabContainer;
import com.mna.gui.containers.slots.NonStupidFurnaceFuelSlot;
import java.util.HashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class ContainerEldrinFume
extends SimpleWizardLabContainer<ContainerEldrinFume, EldrinFumeTile> {
    public ContainerEldrinFume(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super((MenuType<? extends SimpleWizardLabContainer<?, ?>>)((MenuType)ContainerInit.ELDRIN_FUME.get()), i, playerInventory, packetBuffer);
    }

    public ContainerEldrinFume(int id, Inventory playerInventory, EldrinFumeTile tile) {
        super((MenuType)ContainerInit.ELDRIN_FUME.get(), id, playerInventory, tile);
    }

    @Override
    protected int addInventorySlots() {
        InvWrapper wrapper = new InvWrapper((Container)this.tile);
        this.addSlot((Slot)new NonStupidFurnaceFuelSlot((IItemHandler)wrapper, 0, 80, 52));
        this.addSlot(new Slot((Container)this.tile, 1, 80, 14));
        return 2;
    }

    @Override
    protected int playerInventoryXStart() {
        return 8;
    }

    @Override
    protected int playerInventoryYStart() {
        return 76;
    }

    public float getMotePctRemaining() {
        return ((EldrinFumeTile)this.tile).getMotePctRemaining();
    }

    public float getFuelPctRemaining() {
        return ((EldrinFumeTile)this.tile).getFuelPctRemaining();
    }

    public Affinity[] getGeneratingAffinities() {
        return ((EldrinFumeTile)this.tile).getGeneratingAffinities();
    }

    public float getPowerPerSecond(Affinity which) {
        return ((EldrinFumeTile)this.tile).getPowerPerTick(which) * 20.0f;
    }

    public boolean isPowerFull(Level level) {
        Affinity[] generating;
        if (this.user == null) {
            return false;
        }
        boolean allFull = true;
        for (Affinity toGenerate : generating = this.getGeneratingAffinities()) {
            if (toGenerate == Affinity.UNKNOWN) continue;
            IWorldMagic worldMagic = (IWorldMagic)level.getData(WorldMagicProvider.MAGIC.get());
            if (worldMagic == null) {
                return false;
            }
            IWellspringNodeRegistry wellspringNetwork = worldMagic.getWellspringRegistry();
            if (wellspringNetwork == null) {
                return false;
            }
            HashMap<Affinity, Float> curAmt = wellspringNetwork.getNodeNetworkAmountFor(this.user.getUUID(), level);
            allFull &= curAmt.getOrDefault(generating, Float.valueOf(0.0f)).floatValue() >= 5000.0f;
        }
        return allFull;
    }
}

