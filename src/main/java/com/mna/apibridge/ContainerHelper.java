/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.items.IItemHandler
 *  net.minecraftforge.items.IItemHandlerModifiable
 */
package com.mna.apibridge;

import com.mna.api.gui.ContainerPylon;
import com.mna.api.gui.EldrinCapacitorPermissionsContainer;
import com.mna.api.gui.IContainerHelper;
import com.mna.api.items.DynamicItemFilter;
import com.mna.gui.containers.ContainerInit;
import com.mna.tools.InventoryUtilities;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class ContainerHelper
implements IContainerHelper {
    @Override
    public MenuType<EldrinCapacitorPermissionsContainer> GetEldrinPermissionsContainerType() {
        return (MenuType)ContainerInit.ELDRIN_PERMISSIONS.get();
    }

    @Override
    public MenuType<ContainerPylon> GetPylonContainerType() {
        return (MenuType)ContainerInit.PYLON.get();
    }

    @Override
    public boolean HasRoomFor(IItemHandlerModifiable inventory, ItemStack item) {
        return InventoryUtilities.hasRoomFor((IItemHandler)inventory, item);
    }

    @Override
    public boolean HasStackInInventory(DynamicItemFilter search, IItemHandler inventory) {
        return InventoryUtilities.hasStackInInventory(search, inventory);
    }

    @Override
    public boolean HasStackInInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory) {
        return InventoryUtilities.hasStackInInventory(search, ignoreDurability, compareNBT, inventory);
    }

    @Override
    public boolean HasStackInInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory, Direction face) {
        return InventoryUtilities.hasStackInInventory(search, ignoreDurability, compareNBT, inventory, face);
    }

    @Override
    public int CountItem(ItemStack search, IItemHandler inventory, Direction face, boolean compareNBT, boolean ignoreDurability) {
        return InventoryUtilities.countItem(search, inventory, face, compareNBT, ignoreDurability);
    }

    @Override
    public int CountItem(DynamicItemFilter filter, IItemHandler inventory, Direction face) {
        return InventoryUtilities.countItem(filter, inventory, face);
    }

    @Override
    public boolean MergeIntoInventory(IItemHandler handler, ItemStack toMerge) {
        return InventoryUtilities.mergeIntoInventory(handler, toMerge);
    }

    @Override
    public boolean MergeIntoInventory(IItemHandler handler, ItemStack toMerge, int quantity) {
        return InventoryUtilities.mergeIntoInventory(handler, toMerge, quantity);
    }

    @Override
    public ItemStack MergeToPlayerInvPrioritizeOffhand(Player player, ItemStack toMerge) {
        return InventoryUtilities.mergeToPlayerInvPrioritizeOffhand(player, toMerge);
    }

    @Override
    public ItemStack GetFirstItemFromContainer(List<Item> items, int maxCount, IItemHandler handler, Direction side) {
        return InventoryUtilities.getFirstItemFromContainer(items, maxCount, handler, side);
    }

    @Override
    public ItemStack GetFirstItemFromContainer(List<Item> items, int maxCount, IItemHandler handler, Direction side, boolean simulate) {
        return InventoryUtilities.getFirstItemFromContainer(items, maxCount, handler, side, simulate);
    }

    @Override
    public ItemStack GetFirstItemFromContainer(DynamicItemFilter filter, int maxCount, IItemHandler handler, Direction side) {
        return InventoryUtilities.getFirstItemFromContainer(filter, maxCount, handler, side);
    }

    @Override
    public ItemStack GetFirstItemFromContainer(DynamicItemFilter filter, int maxCount, IItemHandler handler, Direction side, boolean simulate) {
        return InventoryUtilities.getFirstItemFromContainer(filter, maxCount, handler, side, simulate);
    }

    @Override
    public ItemStack GetFirstItemFromContainer(DynamicItemFilter filter, int maxCount, IItemHandler handler, Direction side, boolean randomFilterItem, boolean simulate) {
        return InventoryUtilities.getFirstItemFromContainer(filter, maxCount, handler, side, randomFilterItem, simulate);
    }

    @Override
    public boolean RemoveItemFromInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory) {
        return InventoryUtilities.removeItemFromInventory(search, ignoreDurability, compareNBT, inventory);
    }

    @Override
    public boolean RemoveItemFromInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory, Direction face) {
        return InventoryUtilities.removeItemFromInventory(search, ignoreDurability, compareNBT, inventory, face);
    }

    @Override
    public boolean ConsumeAcrossInventories(ItemStack search, boolean ignoreDurability, boolean compareNBT, boolean simulate, List<Pair<IItemHandler, Direction>> inventories) {
        return InventoryUtilities.consumeAcrossInventories(search, ignoreDurability, compareNBT, simulate, inventories);
    }

    @Override
    public boolean ConsumeAcrossInventories(List<Item> search, int count, boolean ignoreDurability, boolean compareNBT, boolean simulate, List<Pair<IItemHandler, Direction>> inventories) {
        return InventoryUtilities.consumeAcrossInventories(search, count, ignoreDurability, compareNBT, simulate, inventories);
    }

    @Override
    public ItemStack RemoveSingleItemFromInventory(ResourceLocation search, Container inventory) {
        return InventoryUtilities.removeSingleItemFromInventory(search, inventory);
    }

    @Override
    public void RedirectCaptureOrDrop(Player player, Level world, List<ItemStack> drops, boolean allowRedirect) {
        InventoryUtilities.redirectCaptureOrDrop(player, world, drops, allowRedirect);
    }

    @Override
    public Pair<Boolean, Boolean> GetCaptureAndRedirect(Player player) {
        return InventoryUtilities.getCaptureAndRedirect(player);
    }

    @Override
    public void DropItemAt(ItemStack stack, Vec3 pos, Level world, boolean randomVelocity) {
        InventoryUtilities.DropItemAt(stack, pos, world, randomVelocity);
    }

    @Override
    public void DropItemAt(ItemStack stack, Vec3 pos, Level world, Vec3 velocity) {
        InventoryUtilities.DropItemAt(stack, pos, world, velocity);
    }
}

