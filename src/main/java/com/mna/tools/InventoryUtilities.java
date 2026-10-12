 
package com.mna.tools;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.items.DynamicItemFilter;
import com.mna.api.items.IPositionalItem;
import com.mna.api.tools.MATags;
import com.mna.inventory.InventoryRitualKit;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import com.mna.items.runes.ItemRuneMarking;
import com.mna.items.sorcery.EntityCrystal;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import com.mna.api.tools.MNACaps;

public class InventoryUtilities {
    public static boolean hasRoomFor(IItemHandler inventory, ItemStack item) {
        ItemStack workingCopy = item.copy();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            if (!inventory.isItemValid(i, workingCopy)) continue;
            ItemStack remaining = inventory.insertItem(i, workingCopy, true);
            if (remaining.isEmpty()) {
                return true;
            }
            workingCopy.setCount(remaining.getCount());
        }
        return false;
    }

    public static boolean hasRoomForAny(IItemHandler inventory, ItemStack item) {
        ItemStack workingCopy = item.copy();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack remaining;
            if (!inventory.isItemValid(i, workingCopy) || (remaining = inventory.insertItem(i, workingCopy, true)).getCount() == workingCopy.getCount()) continue;
            return true;
        }
        return false;
    }

    public static boolean mergeIntoInventory(IItemHandler handler, ItemStack toMerge) {
        return InventoryUtilities.mergeIntoInventory(handler, toMerge, toMerge.getCount());
    }

    public static boolean mergeIntoInventory(IItemHandler handler, ItemStack toMerge, int quantity) {
        return InventoryUtilities.mergeIntoInventory(handler, toMerge, quantity, false);
    }

    public static boolean mergeIntoInventory(IItemHandler handler, ItemStack toMerge, int quantity, boolean simulate) {
        if (quantity > toMerge.getCount()) {
            quantity = toMerge.getCount();
        }
        int emptySlot = -1;
        ItemStack insertStack = toMerge.copy();
        for (int i = 0; i < handler.getSlots(); ++i) {
            if (!handler.isItemValid(i, insertStack)) continue;
            ItemStack inventoryStack = handler.getStackInSlot(i);
            if (inventoryStack.isEmpty()) {
                if (emptySlot != -1) continue;
                emptySlot = i;
                continue;
            }
            if (!ItemStack.isSameItemSameComponents((ItemStack)inventoryStack, (ItemStack)insertStack) || inventoryStack.getCount() >= handler.getSlotLimit(i)) continue;
            int spaceAvailable = handler.getSlotLimit(i) - inventoryStack.getCount();
            ItemStack split = insertStack.split(Math.min(quantity, spaceAvailable));
            ItemStack remainder = handler.insertItem(i, split, simulate);
            int amountInserted = split.getCount() - remainder.getCount();
            quantity -= amountInserted;
            if (!simulate) {
                toMerge.setCount(toMerge.getCount() - amountInserted);
            }
            if (quantity <= 0) {
                return true;
            }
            if (remainder.getCount() <= 0) continue;
            insertStack = remainder;
        }
        if (quantity > 0 && emptySlot > -1) {
            ItemStack insert = ItemStack.EMPTY;
            int maxStackSize = handler.getSlotLimit(emptySlot);
            if (insertStack.getCount() > maxStackSize) {
                insert = insertStack.split(maxStackSize);
                insert = handler.insertItem(emptySlot, insert, false);
                insertStack.setCount(insertStack.getCount() + insert.getCount());
            } else {
                insertStack = handler.insertItem(emptySlot, insertStack, false);
            }
            if (!simulate) {
                toMerge.setCount(insertStack.getCount());
            }
            return insertStack.getCount() == 0;
        }
        return false;
    }

    public static ItemStack mergeToPlayerInvPrioritizeOffhand(Player player, ItemStack toMerge) {
        if (player == null) {
            return toMerge;
        }
        ItemStack summonerOffhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (summonerOffhand.isEmpty()) {
            player.setItemSlot(EquipmentSlot.OFFHAND, toMerge);
            return ItemStack.EMPTY;
        }
        if (ItemStack.isSameItemSameComponents((ItemStack)toMerge, (ItemStack)summonerOffhand) && summonerOffhand.getCount() < summonerOffhand.getMaxStackSize()) {
            int toAdd = Math.min(summonerOffhand.getMaxStackSize() - summonerOffhand.getCount(), toMerge.getCount());
            summonerOffhand.setCount(summonerOffhand.getCount() + toAdd);
            toMerge.setCount(toMerge.getCount() - toAdd);
            if (toMerge.getCount() <= 0) {
                return ItemStack.EMPTY;
            }
        }
        if (player.addItem(toMerge)) {
            return ItemStack.EMPTY;
        }
        return toMerge;
    }

    public static ItemStack getFirstItemFromContainer(List<Item> items, int maxCount, IItemHandler handler, Direction side) {
        return InventoryUtilities.getFirstItemFromContainer(items, maxCount, handler, side, false);
    }

    public static ItemStack getFirstItemFromContainer(List<Item> items, int maxCount, IItemHandler handler, Direction side, boolean simulate) {
        for (int i = 0; i < handler.getSlots(); ++i) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty() || items.size() != 0 && !items.contains(stack.getItem())) continue;
            int count = Math.min(stack.getCount(), maxCount);
            return handler.extractItem(i, count, simulate);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getFirstItemFromContainer(DynamicItemFilter filter, int maxCount, IItemHandler handler, Direction side) {
        return InventoryUtilities.getFirstItemFromContainer(filter, maxCount, handler, side, false);
    }

    public static ItemStack getFirstItemFromContainer(DynamicItemFilter filter, int maxCount, IItemHandler handler, Direction side, boolean simulate) {
        return InventoryUtilities.getFirstItemFromContainer(filter, maxCount, handler, side, false, simulate);
    }

    public static ItemStack getFirstItemFromContainer(DynamicItemFilter filter, int maxCount, IItemHandler handler, Direction side, boolean randomFilterItem, boolean simulate) {
        ArrayList<Integer> slots = new ArrayList<Integer>();
        for (int i = 0; i < handler.getSlots(); ++i) {
            if (handler.getStackInSlot(i).isEmpty()) continue;
            slots.add(i);
        }
        if (randomFilterItem) {
            Collections.shuffle(slots);
        }
        for (Integer i : slots) {
            ItemStack stack = handler.getStackInSlot(i.intValue());
            if (stack.isEmpty() || !filter.matches(stack)) continue;
            int count = Math.min(stack.getCount(), maxCount);
            if (simulate) {
                ItemStack extractedSimulated = handler.extractItem(i.intValue(), count, true);
                if (extractedSimulated.isEmpty()) continue;
                return extractedSimulated;
            }
            if (handler.extractItem(i.intValue(), count, true).isEmpty()) continue;
            return handler.extractItem(i.intValue(), count, false);
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasStackInInventory(DynamicItemFilter search, IItemHandler inventory) {
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !search.matches(stack)) continue;
            return true;
        }
        return false;
    }

    public static boolean hasStackInInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory) {
        int countToFind = search.getCount();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            boolean equal;
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            boolean bl = ignoreDurability ? ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) : (equal = ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) && stack.getDamageValue() == search.getDamageValue());
            if (compareNBT) {
                equal &= ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, search);
            }
            if (!equal || (countToFind -= stack.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    public static boolean removeItemFromInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory) {
        int countToRemove = search.getCount();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            boolean equal;
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            boolean bl = ignoreDurability ? ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) : (equal = ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) && stack.getDamageValue() == search.getDamageValue());
            if (compareNBT) {
                equal = search.getItem() == ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get() ? (equal &= EntityCrystal.getEntityType(search) == EntityCrystal.getEntityType(stack)) : (equal &= ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, search));
            }
            if (!equal) continue;
            int removeQty = Math.min(countToRemove, stack.getCount());
            stack.shrink(removeQty);
            if ((countToRemove -= removeQty) > 0) continue;
            return true;
        }
        return false;
    }

    public static boolean hasStackInInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory, Direction face) {
        int countToFind = search.getCount();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            boolean equal;
            ItemStack stack = inventory.extractItem(i, countToFind, true);
            if (stack.isEmpty()) continue;
            boolean bl = ignoreDurability ? ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) : (equal = ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) && stack.getDamageValue() == search.getDamageValue());
            if (compareNBT) {
                equal = search.getItem() == ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get() ? (equal &= EntityCrystal.getEntityType(search) == EntityCrystal.getEntityType(stack)) : (equal &= ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, search));
            }
            if (!equal || (countToFind -= stack.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    public static boolean removeItemFromInventory(ItemStack search, boolean ignoreDurability, boolean compareNBT, IItemHandler inventory, Direction face) {
        int countToRemove = search.getCount();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack extracted;
            boolean equal;
            ItemStack stack = inventory.extractItem(i, countToRemove, true);
            if (stack.isEmpty()) continue;
            boolean bl = ignoreDurability ? ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) : (equal = ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) && stack.getDamageValue() == search.getDamageValue());
            if (compareNBT) {
                equal &= ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, search);
            }
            if (!equal || (extracted = inventory.extractItem(i, countToRemove, false)).isEmpty() || (countToRemove -= extracted.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    public static boolean consumeAcrossInventories(ItemStack search, boolean ignoreDurability, boolean compareNBT, boolean simulate, List<Pair<IItemHandler, Direction>> inventories) {
        int count = search.getCount();
        for (int idx = 0; idx < inventories.size(); ++idx) {
            IItemHandler inventory = (IItemHandler)inventories.get(idx).getFirst();
            if (inventory == null) continue;
            boolean invConsumed = false;
            for (int i = 0; i < inventory.getSlots(); ++i) {
                boolean equal;
                ItemStack stack = inventory.extractItem(i, count, true);
                if (stack.isEmpty()) continue;
                boolean bl = ignoreDurability ? ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) : (equal = ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) && stack.getDamageValue() == search.getDamageValue());
                if (compareNBT) {
                    boolean tagMatch = ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, search);
                    if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() == null && search.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() != null && search.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().isEmpty()) {
                        tagMatch = true;
                    } else if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() != null && search.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() == null && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().isEmpty()) {
                        tagMatch = true;
                    }
                    equal &= tagMatch;
                }
                if (!equal) continue;
                if (!simulate) {
                    ItemStack extracted = inventory.extractItem(i, count, false);
                    if (!extracted.isEmpty()) {
                        count -= extracted.getCount();
                        invConsumed = true;
                    }
                } else {
                    count -= stack.getCount();
                    invConsumed = true;
                }
                if (count > 0) continue;
                if (inventory instanceof ItemInventoryBase) {
                    ((ItemInventoryBase)inventory).writeItemStack();
                }
                return true;
            }
            if (!invConsumed || !(inventory instanceof ItemInventoryBase)) continue;
            ((ItemInventoryBase)inventory).writeItemStack();
        }
        return false;
    }

    public static boolean consumeAcrossInventories(List<Item> search, int count, boolean ignoreDurability, boolean compareNBT, boolean simulate, List<Pair<IItemHandler, Direction>> inventories) {
        for (int idx = 0; idx < inventories.size(); ++idx) {
            IItemHandler inventory = (IItemHandler)inventories.get(idx).getFirst();
            if (inventory == null) continue;
            boolean invConsumed = false;
            for (int i = 0; i < inventory.getSlots(); ++i) {
                boolean equal;
                ItemStack stack = inventory.extractItem(i, count, true);
                if (stack.isEmpty() || !(equal = search.contains(stack.getItem()))) continue;
                if (!simulate) {
                    ItemStack extracted = inventory.extractItem(i, count, false);
                    if (!extracted.isEmpty()) {
                        count -= extracted.getCount();
                        invConsumed = true;
                    }
                } else {
                    count -= stack.getCount();
                    invConsumed = true;
                }
                if (count > 0) continue;
                if (inventory instanceof ItemInventoryBase) {
                    ((ItemInventoryBase)inventory).writeItemStack();
                }
                return true;
            }
            if (!invConsumed || !(inventory instanceof ItemInventoryBase)) continue;
            ((ItemInventoryBase)inventory).writeItemStack();
        }
        return false;
    }

    public static int countItem(ItemStack search, IItemHandler inventory, Direction face, boolean compareNBT, boolean ignoreDurability) {
        int i = 0;
        for (int j = 0; j < inventory.getSlots(); ++j) {
            boolean equal;
            ItemStack stack = inventory.getStackInSlot(j);
            boolean bl = ignoreDurability ? ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) : (equal = ItemStack.isSameItem((ItemStack)stack, (ItemStack)search) && stack.getDamageValue() == search.getDamageValue());
            if (!equal) continue;
            if (compareNBT) {
                boolean tagMatch = false;
                tagMatch = search.getItem() == ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get() ? EntityCrystal.getEntityType(search) == EntityCrystal.getEntityType(stack) : ManaAndArtificeMod.getItemHelper().AreTagsEqual(stack, search);
                if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() == null && search.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() != null && search.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().isEmpty()) {
                    tagMatch = true;
                } else if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() != null && search.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() == null && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().isEmpty()) {
                    tagMatch = true;
                }
                equal &= tagMatch;
            }
            if (!equal) continue;
            i += stack.getCount();
        }
        return i;
    }

    public static int countItem(DynamicItemFilter filter, IItemHandler inventory, Direction face) {
        int i = 0;
        for (int j = 0; j < inventory.getSlots(); ++j) {
            ItemStack stack = inventory.getStackInSlot(j);
            boolean equal = filter.matches(stack);
            if (!equal) continue;
            i += stack.getCount();
        }
        return i;
    }

    public static ItemStack removeSingleItemFromInventory(ResourceLocation search, Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || !MATags.isItemEqual(stack, search)) continue;
            return stack.split(1);
        }
        return ItemStack.EMPTY;
    }

    public static void redirectCaptureOrDrop(Player player, Level world, List<ItemStack> drops, boolean allowRedirect) {
        if (allowRedirect) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            BlockPos redirLoc = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getLocation(mainHand.getItem() instanceof IPositionalItem ? mainHand : offHand);
            if (redirLoc != null) {
                java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler;
                BlockEntity te;
                Direction redirFace = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getFace(mainHand.getItem() instanceof IPositionalItem ? mainHand : offHand);
                if (world.isLoaded(redirLoc) && (te = world.getBlockEntity(redirLoc)) != null && (handler = MNACaps.itemHandler(te, redirFace)).isPresent()) {
                    for (int i = 0; i < drops.size(); ++i) {
                        if (!InventoryUtilities.mergeIntoInventory(handler.get(), drops.get(i))) continue;
                        drops.remove(i);
                        --i;
                    }
                }
            }
        }
        ArrayList<InventoryRitualKit> playerKits = new ArrayList<InventoryRitualKit>();
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof PractitionersPouch) || ((PractitionersPouch)stack.getItem()).getPatchLevel(stack, PractitionersPouchPatches.COLLECTION) <= 0) continue;
            playerKits.add(new InventoryRitualKit(stack));
        }
        for (ItemStack stack : drops) {
            InventoryRitualKit kit;
            Iterator iterator = playerKits.iterator();
            while (iterator.hasNext() && !(stack = (kit = (InventoryRitualKit)iterator.next()).addItem(stack)).isEmpty()) {
            }
            if (stack.isEmpty() || player.addItem(stack)) continue;
            player.spawnAtLocation(stack);
        }
        for (InventoryRitualKit kit : playerKits) {
            kit.writeItemStack();
        }
    }

    public static Pair<Boolean, Boolean> getCaptureAndRedirect(Player player) {
        boolean captureDrops = false;
        boolean redirectDrops = false;
        if (CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)player, (Item)ItemInit.COLLECTOR_RING_GREATER.get()).isPresent()) {
            captureDrops = true;
            redirectDrops = true;
        } else if (CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)player, (Item)ItemInit.COLLECTOR_RING_LESSER.get()).isPresent()) {
            captureDrops = true;
        }
        return new Pair(captureDrops, redirectDrops);
    }

    public static void DropItemAt(ItemStack stack, Vec3 pos, Level world, boolean randomVelocity) {
        InventoryUtilities.DropItemAt(stack, pos, world, randomVelocity ? new Vec3(((double)world.random.nextFloat() - 0.5) * 0.1, ((double)world.random.nextFloat() - 0.5) * 0.1, ((double)world.random.nextFloat() - 0.5) * 0.1) : Vec3.ZERO);
    }

    public static void DropItemAt(ItemStack stack, Vec3 pos, Level world, Vec3 velocity) {
        ItemEntity item = new ItemEntity(world, pos.x, pos.y, pos.z, stack.copy());
        item.setDeltaMovement(velocity);
        world.addFreshEntity((Entity)item);
    }

    public static boolean hasEmptySlot(IItemHandler inventory) {
        for (int i = 0; i < inventory.getSlots(); ++i) {
            if (!inventory.getStackInSlot(i).isEmpty()) continue;
            return true;
        }
        return false;
    }

    public static void consumeByTag(IItemHandler inventory, ResourceLocation tag, int quantity) {
        List<Item> items = MATags.getItemTagContents(tag);
        InventoryUtilities.consumeAcrossInventories(items, quantity, true, false, false, Arrays.asList(new Pair(inventory, Direction.UP)));
    }
}

