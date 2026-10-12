 
package com.mna.gui.containers.item;

import com.mna.gui.containers.ContainerInit;
import com.mna.items.artifice.EnderDisk;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.ServerMessageDispatcher;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerEnderDisc
extends AbstractContainerMenu {
    public ItemStack heldItem;
    private ArrayList<String> _dimensions;
    private ArrayList<EnderDiscPattern> _patterns;
    private byte[] dirty;
    private int _curIndex = 0;
    private Level level;

    private ContainerEnderDisc(@Nullable MenuType<?> type, int id, Inventory playerInv, ItemStack heldItem) {
        super(type, id);
        this.heldItem = heldItem;
        this._patterns = new ArrayList();
        this._dimensions = new ArrayList();
        this._curIndex = MathUtils.clamp(EnderDisk.getIndex(this.heldItem), 0, 8);
        this.dirty = new byte[8];
        for (int i = 0; i < this.dirty.length; ++i) {
            this.dirty[i] = 0;
            this._patterns.add(EnderDiscPattern.fromStack(this.heldItem, i));
        }
        this.level = playerInv.player.level();
        MinecraftServer server = this.level.getServer();
        this._dimensions.add("mna:dimension_current");
        if (server != null) {
            server.getAllLevels().forEach(sl -> this._dimensions.add(sl.dimension().location().toString()));
        }
    }

    public ContainerEnderDisc(int i, Inventory playerInv, FriendlyByteBuf buffer) {
        this((MenuType)ContainerInit.ENDER_DISC.get(), i, playerInv, playerInv.getItem(playerInv.selected));
    }

    public boolean stillValid(Player playerIn) {
        return true;
    }

    public void changePatternIndex(int newIndex) {
        if (newIndex >= 0 && newIndex < this._patterns.size()) {
            this._curIndex = newIndex;
        }
    }

    public ArrayList<ResourceLocation> getCurPattern() {
        return this._patterns.get((int)this._curIndex)._pattern;
    }

    public ResourceLocation getCurDimension() {
        return this._patterns.get((int)this._curIndex)._dimensionID;
    }

    public int getCurPatternIndex() {
        return this._curIndex;
    }

    public String getCurPatternName() {
        return this._patterns.get((int)this._curIndex)._name;
    }

    public void setPattern(ResourceLocation pattern, int index) {
        if (index < 0 || index >= 8) {
            return;
        }
        this._patterns.get((int)this._curIndex)._pattern.set(index, pattern);
        this.dirty[this._curIndex] = 1;
    }

    public void setDimension(ResourceLocation dimension) {
        if (dimension == null) {
            dimension = ResourceLocation.parse("");
        }
        this._patterns.get((int)this._curIndex)._dimensionID = dimension;
    }

    public void setName(String name) {
        this._patterns.get((int)this._curIndex)._name = name;
        this.dirty[this._curIndex] = 1;
    }

    public boolean clickMenuButton(Player pPlayer, int pId) {
        String currentDim = this.getCurDimension().toString();
        int foundIndex = -1;
        for (int idx = 0; idx < this._dimensions.size(); ++idx) {
            if (!this._dimensions.get(idx).equals(currentDim)) continue;
            foundIndex = idx;
            break;
        }
        int nextIdx = (foundIndex + 1) % this._dimensions.size();
        ResourceLocation nextDim = ResourceLocation.parse(this._dimensions.get(nextIdx));
        this.setDimension(nextDim);
        ServerMessageDispatcher.sendEnderDiscGuiDimensionCycle((ServerPlayer)pPlayer, nextDim);
        return true;
    }

    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (playerIn.level().isClientSide()) {
            for (int i = 0; i < this.dirty.length; ++i) {
                if (this.dirty[i] != 1) continue;
                ClientMessageDispatcher.sendEnderDiscPatternChange(this._patterns.get((int)i)._pattern, this._patterns.get((int)i)._dimensionID, i, this._patterns.get((int)i)._name);
            }
            ClientMessageDispatcher.sendEnderDiscIndexChange(this._curIndex, false);
        }
    }

    public String getPatternName(int index) {
        if (index < 0 || index >= this._patterns.size()) {
            return "";
        }
        return this._patterns.get((int)index)._name;
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    static class EnderDiscPattern {
        ArrayList<ResourceLocation> _pattern;
        ResourceLocation _dimensionID = ResourceLocation.parse("mna:dimension_current");
        String _name = "";

        EnderDiscPattern() {
        }

        static EnderDiscPattern fromStack(ItemStack stack, int index) {
            EnderDiscPattern pattern = new EnderDiscPattern();
            pattern._pattern = EnderDisk.getPattern(stack, index);
            pattern._name = EnderDisk.getPatternName(stack, index).getString();
            pattern._dimensionID = EnderDisk.getDimension(stack, index);
            return pattern;
        }
    }
}

