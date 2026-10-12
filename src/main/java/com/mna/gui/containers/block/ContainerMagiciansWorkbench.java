/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.player.StackedContents
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.RecipeBookMenu
 *  net.minecraft.world.inventory.RecipeBookType
 *  net.minecraft.world.inventory.ResultContainer
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.inventory.TransientCraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.level.Level
 */
package com.mna.gui.containers.block;

import com.mna.blocks.tileentities.wizard_lab.MagiciansWorkbenchTile;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.containers.slots.SlotMagiciansWorkbenchOutput;
import com.mna.network.ClientMessageDispatcher;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ContainerMagiciansWorkbench
extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    private final MagiciansWorkbenchTile table;
    public static int SIZE = 18;
    public static int INVENTORY_STORAGE_START = 0;
    public static int INVENTORY_STORAGE_END = 17;
    public static int FIRST_CRAFT_GRID_START = 18;
    public static int FIRST_OUTPUT_SLOT = 27;
    public static int SECOND_CRAFT_GRID_START = 28;
    public static int SECOND_OUTPUT_SLOT = 37;
    public static int PLAYER_INVENTORY_START = 38;
    public static int PLAYER_ACTION_BAR_START = 65;
    public static int PLAYER_ACTION_BAR_END = 73;
    public CraftingContainer firstCraftMatrix;
    public CraftingContainer secondCraftMatrix;
    private Inventory playerInv;
    private boolean initializing;

    public ContainerMagiciansWorkbench(int i, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(i, playerInventory, ((MagiciansWorkbenchTile)playerInventory.player.level().getBlockEntity(packetBuffer.readBlockPos())).readFrom(packetBuffer));
    }

    public ContainerMagiciansWorkbench(int windowId, Inventory playerInventory, MagiciansWorkbenchTile inventory) {
        super((MenuType)ContainerInit.MAGICIANS_WORKBENCH.get(), windowId);
        int j;
        int i;
        int ypos;
        int xpos;
        this.table = inventory;
        this.playerInv = playerInventory;
        inventory.startOpen(playerInventory.player);
        this.firstCraftMatrix = new TransientCraftingContainer((AbstractContainerMenu)this, 3, 3);
        this.secondCraftMatrix = new TransientCraftingContainer((AbstractContainerMenu)this, 3, 3);
        this.updateCraftingMatrices();
        int slotIndex = 0;
        for (xpos = 0; xpos < 3; ++xpos) {
            for (ypos = 0; ypos < 6; ++ypos) {
                this.addSlot(new Slot((Container)this.table, slotIndex++, 196 + xpos * 18, 8 + ypos * 18));
            }
        }
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.addSlot(new Slot((Container)this.firstCraftMatrix, j + i * 3, 67 + j * 20, 11 + i * 20));
            }
        }
        this.addSlot(new SlotMagiciansWorkbenchOutput(playerInventory.player, (Container)this.firstCraftMatrix, (Container)this.table.firstResultInv, this, 0, 87, 91));
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.addSlot(new Slot((Container)this.secondCraftMatrix, j + i * 3, 133 + j * 20, 11 + i * 20));
            }
        }
        this.addSlot(new SlotMagiciansWorkbenchOutput(playerInventory.player, (Container)this.secondCraftMatrix, (Container)this.table.secondResultInv, this, 0, 153, 91));
        for (xpos = 0; xpos < 9; ++xpos) {
            for (ypos = 0; ypos < 3; ++ypos) {
                this.addSlot(new Slot((Container)playerInventory, xpos + ypos * 9 + 9, 48 + xpos * 18, 121 + ypos * 18));
            }
        }
        for (xpos = 0; xpos < 9; ++xpos) {
            this.addSlot(new Slot((Container)playerInventory, xpos, 48 + xpos * 18, 179));
        }
    }

    public void updateCraftingMatrices() {
        this.initializing = true;
        for (int i = 0; i < 9; ++i) {
            this.firstCraftMatrix.setItem(i, this.table.getItem(i + FIRST_CRAFT_GRID_START));
            this.secondCraftMatrix.setItem(i, this.table.getItem(i + SECOND_CRAFT_GRID_START));
        }
        this.initializing = false;
    }

    public boolean stillValid(Player playerIn) {
        return this.table.stillValid(playerIn);
    }

    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == FIRST_OUTPUT_SLOT || index == SECOND_OUTPUT_SLOT) {
                itemstack1.getItem().onCraftedBy(itemstack1, this.table.getLevel(), playerIn);
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_ACTION_BAR_END ? !this.moveItemStackTo(itemstack1, INVENTORY_STORAGE_START, FIRST_CRAFT_GRID_START, false) && (index < PLAYER_ACTION_BAR_START ? !this.moveItemStackTo(itemstack1, PLAYER_ACTION_BAR_START, PLAYER_ACTION_BAR_END, false) : !this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_START, false)) : (index >= FIRST_CRAFT_GRID_START && index < FIRST_OUTPUT_SLOT ? !this.moveItemStackTo(itemstack1, INVENTORY_STORAGE_START, FIRST_CRAFT_GRID_START, false) && !this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, false) : (index >= SECOND_CRAFT_GRID_START && index < SECOND_OUTPUT_SLOT ? !this.moveItemStackTo(itemstack1, INVENTORY_STORAGE_START, FIRST_CRAFT_GRID_START, false) && !this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, false) : !this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, false)))) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
            if (index == 0) {
                playerIn.drop(itemstack1, false);
            }
        }
        return itemstack;
    }

    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.table.stopOpen(playerIn);
    }

    public void slotsChanged(Container p_75130_1_) {
        this.updateCraftingResult(this.containerId, this.table.getLevel(), this.playerInv.player, this.firstCraftMatrix, this.table.firstResultInv, FIRST_OUTPUT_SLOT);
        this.updateCraftingResult(this.containerId, this.table.getLevel(), this.playerInv.player, this.secondCraftMatrix, this.table.secondResultInv, SECOND_OUTPUT_SLOT);
        if (!this.initializing) {
            for (int i = 0; i < 9; ++i) {
                this.table.setItem(i + FIRST_CRAFT_GRID_START, this.firstCraftMatrix.getItem(i));
                this.table.setItem(i + SECOND_CRAFT_GRID_START, this.secondCraftMatrix.getItem(i));
            }
        }
        this.broadcastChanges();
    }

    protected boolean moveItemStackTo(ItemStack stack, int source, int dest, boolean loop) {
        return super.moveItemStackTo(stack, source, dest, loop);
    }

    protected void updateCraftingResult(int id, Level world, Player player, CraftingContainer inventory, ResultContainer inventoryResult, int syncSlot) {
        if (!world.isClientSide) {
            ServerPlayer serverplayerentity = (ServerPlayer)player;
            ItemStack itemstack = ItemStack.EMPTY;
            CraftingInput craftingInput = inventory.asCraftInput();
            Optional<RecipeHolder<CraftingRecipe>> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, world);
            if (optional.isPresent()) {
                inventoryResult.setRecipeUsed(optional.get());
                itemstack = optional.get().value().assemble(craftingInput, world.registryAccess());
            }
            inventoryResult.setItem(0, itemstack);
            serverplayerentity.connection.send((Packet)new ClientboundContainerSetSlotPacket(id, this.incrementStateId(), syncSlot, itemstack));
        }
    }

    public MagiciansWorkbenchTile getWorkbench() {
        return this.table;
    }

    private StackedContents getStackedContents() {
        StackedContents stackedContents = new StackedContents();
        for (int i = INVENTORY_STORAGE_START; i <= INVENTORY_STORAGE_END; ++i) {
            stackedContents.accountSimpleStack(this.table.getItem(i));
        }
        return stackedContents;
    }

    public boolean hasComponents(int recipeIndex) {
        MagiciansWorkbenchTile.RememberedRecipe remembered = this.table.getRememberedRecipeItems().get(recipeIndex);
        StackedContents contents = this.getStackedContents();
        return contents.canCraft((Recipe)remembered.getRecipe(this.table.getLevel()), (IntList)null);
    }

    private void setRecipeItemsToGrid(int recipeIndex) {
        MagiciansWorkbenchTile.RememberedRecipe recipe = this.table.getRememberedRecipeItems().get(recipeIndex);
        int count = 0;
        int startSlot = -1;
        if (this.craftingGridIsEmpty(true)) {
            startSlot = SECOND_CRAFT_GRID_START;
        } else if (this.craftingGridIsEmpty(false)) {
            startSlot = FIRST_CRAFT_GRID_START;
        }
        if (startSlot == -1) {
            return;
        }
        CraftingRecipe craftingRecipe = recipe.getRecipe(this.table.getLevel());
        if (craftingRecipe == null) {
            return;
        }
        if (craftingRecipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)craftingRecipe;
            Ingredient[] ingreds = recipe.getComponents(this.table.getLevel());
            block0: for (int j = 0; j < shapedRecipe.getHeight(); ++j) {
                for (int i = 0; i < shapedRecipe.getWidth(); ++i) {
                    int idx = i + j * shapedRecipe.getWidth();
                    int slotIdx = i + j * this.getGridWidth();
                    Slot slot = this.getSlot(startSlot + slotIdx);
                    Ingredient ingredient = ingreds[idx];
                    if (!ingredient.isEmpty()) {
                        int ingredSlot = this.getSlotForIngredient(ingredient);
                        if (ingredSlot == -1) continue block0;
                        ItemStack existing = this.getSlot(ingredSlot).getItem();
                        ItemStack copyStack = existing.copy();
                        existing.shrink(1);
                        copyStack.setCount(1);
                        slot.set(copyStack);
                        continue;
                    }
                    slot.set(ItemStack.EMPTY);
                }
            }
        } else {
            for (Ingredient ingred : recipe.getComponents(this.table.getLevel())) {
                Slot slot = this.getSlot(startSlot + count);
                if (!ingred.isEmpty()) {
                    int ingredSlot = this.getSlotForIngredient(ingred);
                    if (ingredSlot == -1) break;
                    ItemStack existing = this.getSlot(ingredSlot).getItem();
                    ItemStack copyStack = existing.copy();
                    existing.shrink(1);
                    copyStack.setCount(1);
                    slot.set(copyStack);
                } else {
                    slot.set(ItemStack.EMPTY);
                }
                slot.setChanged();
                ++count;
            }
        }
    }

    private int getSlotForIngredient(Ingredient ingred) {
        for (int i = INVENTORY_STORAGE_START; i <= INVENTORY_STORAGE_END; ++i) {
            if (!ingred.test(this.getSlot(i).getItem())) continue;
            return i;
        }
        return -1;
    }

    public boolean gridIsFreeFor(int recipeIndex) {
        return this.craftingGridIsEmpty(false) || this.craftingGridIsEmpty(true);
    }

    public void tryClearGrid(boolean second) {
        if (this.table.getLevel().isClientSide()) {
            ClientMessageDispatcher.sendMagiciansWorkbenchClearMessage(second);
            return;
        }
        int startIndex = second ? SECOND_CRAFT_GRID_START : FIRST_CRAFT_GRID_START;
        for (int i = 0; i < 9; ++i) {
            ItemStack gridStack = this.table.getItem(i + startIndex);
            if (gridStack.isEmpty() || !this.moveItemStackTo(gridStack, INVENTORY_STORAGE_START, INVENTORY_STORAGE_END, true) && !this.moveItemStackTo(gridStack, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, false)) continue;
            this.table.setItem(i + startIndex, ItemStack.EMPTY);
        }
        this.updateCraftingMatrices();
        this.slotsChanged(this.table);
        this.broadcastChanges();
    }

    public boolean craftingGridIsEmpty(boolean second) {
        int startSlot = second ? SECOND_CRAFT_GRID_START : FIRST_CRAFT_GRID_START;
        for (int i = 0; i < 9; ++i) {
            if (this.getWorkbench().getItem(startSlot + i).isEmpty()) continue;
            return false;
        }
        return true;
    }

    public void moveRecipeToCraftingGrid(int recipeIndex) {
        if (this.table.getRememberedRecipeItems().size() <= recipeIndex) {
            return;
        }
        if (!this.gridIsFreeFor(recipeIndex) || this.isRecipeAlreadyInGrid(recipeIndex)) {
            return;
        }
        if (this.table.getLevel().isClientSide()) {
            ClientMessageDispatcher.sendMagiciansWorkbenchRecipeSetMessage(recipeIndex);
            return;
        }
        if (this.hasComponents(recipeIndex)) {
            this.setRecipeItemsToGrid(recipeIndex);
            this.updateCraftingMatrices();
            this.slotsChanged(this.table);
            this.broadcastChanges();
        }
    }

    public boolean isRecipeAlreadyInGrid(int recipeIndex) {
        if (this.table.getRememberedRecipeItems().size() <= recipeIndex) {
            return false;
        }
        MagiciansWorkbenchTile.RememberedRecipe recipe = this.table.getRememberedRecipeItems().get(recipeIndex);
        if (ItemStack.isSameItemSameComponents((ItemStack)this.getWorkbench().firstResultInv.getItem(0), (ItemStack)recipe.getOutput(this.table.getLevel()))) {
            return true;
        }
        return ItemStack.isSameItemSameComponents((ItemStack)this.getWorkbench().secondResultInv.getItem(0), (ItemStack)recipe.getOutput(this.table.getLevel()));
    }

    public int getResultSlotIndex() {
        return 0;
    }

    public int getGridWidth() {
        return this.firstCraftMatrix.getWidth();
    }

    public int getGridHeight() {
        return this.firstCraftMatrix.getHeight();
    }

    public int getSize() {
        return 5;
    }

    public CraftingContainer getCraftSlots() {
        return this.firstCraftMatrix;
    }

    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public boolean shouldMoveToInventory(int p_150591_) {
        return p_150591_ != this.getResultSlotIndex();
    }

    public void fillCraftSlotsStackedContents(StackedContents p_39714_) {
        this.firstCraftMatrix.fillStackedContents(p_39714_);
    }

    public void clearCraftingContent() {
        this.table.firstResultInv.clearContent();
        this.firstCraftMatrix.clearContent();
    }

    public boolean recipeMatches(RecipeHolder<CraftingRecipe> p_39719_) {
        return p_39719_.value().matches(this.firstCraftMatrix.asCraftInput(), this.playerInv.player.level());
    }

    public boolean clickMenuButton(Player player, int buttonIndex) {
        return this.table.toggleRecipeLock(buttonIndex);
    }
}

