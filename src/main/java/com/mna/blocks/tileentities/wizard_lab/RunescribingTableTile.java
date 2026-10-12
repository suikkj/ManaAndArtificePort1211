/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.Container
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.blocks.tileentities.wizard_lab;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.config.GeneralConfigValues;
import com.mna.blocks.BlockInit;
import com.mna.blocks.runeforging.RunescribingTableBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.events.EventDispatcher;
import com.mna.gui.containers.block.ContainerRunescribingTable;
import com.mna.items.ItemInit;
import com.mna.items.runes.RunePattern;
import com.mna.items.runes.RunescribingRecipePaper;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.messages.to_server.RunescribingTableMutexChangeMessage;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.runeforging.RunescribingRecipe;
import com.mna.tools.ContainerTools;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class RunescribingTableTile
extends WizardLabTile
implements MenuProvider,
Consumer<FriendlyByteBuf> {
    public static final int SLOT_HAMMER = 0;
    public static final int SLOT_CHISEL = 1;
    public static final int SLOT_PATTERN = 2;
    public static final int SLOT_GUIDE = 3;
    public static final int SLOT_CLAY = 4;
    public static final int SLOT_PATTERNS_START = 5;
    public static final int SLOT_PATTERNS_END = 29;
    public static final int INVENTORY_SIZE = 30;

    public RunescribingTableTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state, 30);
    }

    public RunescribingTableTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.RUNESCRIBING_TABLE.get(), pos, state);
    }

    @Override
    public boolean canActivate(Player player) {
        return false;
    }

    @Override
    public float getPctComplete() {
        return 1.0f;
    }

    @Override
    protected boolean canContinue() {
        return true;
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        return Arrays.asList(0, 1, 2, 3, 4);
    }

    public long getHMutex() {
        ItemStack patternStack = this.getItem(2);
        if (patternStack.getItem() == ItemInit.RUNE_PATTERN.get() && patternStack.has(DataComponents.CUSTOM_DATA)) {
            return patternStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLong("hmutex");
        }
        return 0L;
    }

    public long getVMutex() {
        ItemStack patternStack = this.getItem(2);
        if (patternStack.getItem() == ItemInit.RUNE_PATTERN.get() && patternStack.has(DataComponents.CUSTOM_DATA)) {
            return patternStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLong("vmutex");
        }
        return 0L;
    }

    public boolean canUndo(Player player) {
        return !this.getItem(4).isEmpty() && player.totalExperience >= GeneralConfigValues.ExperiencePerRunescribeUndo;
    }

    public boolean hasRequiredItems(boolean toolsRequired) {
        ItemStack patternStack = this.getItem(2);
        ItemStack chiselStack = this.getItem(1);
        ItemStack hammerStack = this.getItem(0);
        if (toolsRequired) {
            return patternStack.getItem() == ItemInit.RUNE_PATTERN.get() && chiselStack.getItem() == ItemInit.RUNESMITH_CHISEL.get() && (hammerStack.getItem() == ItemInit.RUNESMITH_HAMMER.get() || hammerStack.getItem() == ItemInit.RUNIC_MALUS.get());
        }
        return patternStack.getItem() == ItemInit.RUNE_PATTERN.get();
    }

    private void damageHammerAndChisel(RandomSource source) {
        ItemStack chiselStack = this.getItem(1);
        ItemStack hammerStack = this.getItem(0);
        if (chiselStack.getItem() == ItemInit.RUNESMITH_CHISEL.get() && chiselStack.hurt(1, source, null)) {
            this.setItem(1, ItemStack.EMPTY);
        }
        if (hammerStack.getItem() == ItemInit.RUNESMITH_HAMMER.get() && hammerStack.hurt(1, source, null)) {
            this.setItem(0, ItemStack.EMPTY);
        }
    }

    @Nullable
    public RunescribingRecipe getRecipeFromGuideSlot() {
        ItemStack recipe = this.getItem(3);
        if (recipe.isEmpty()) {
            return null;
        }
        return ((RunescribingRecipePaper)ItemInit.RECIPE_SCRAP_RUNESCRIBING.get()).getRecipe(recipe, this.level);
    }

    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerRunescribingTable(id, playerInventory, this);
    }

    public Component getDisplayName() {
        return Component.translatable((String)"mna:container.inscription_table");
    }

    @Override
    public void accept(FriendlyByteBuf data) {
        data.writeBlockPos(this.getBlockPos());
    }

    public RunescribingTableTile readFrom(FriendlyByteBuf data) {
        return this;
    }

    public boolean writeMutexChanges(long hMutex, long vMutex, Player player, int playerTier, boolean isRemove, RandomSource source) {
        RunescribingRecipe recipe;
        ItemStack patternStack;
        boolean returnValue = false;
        if (this.getLevel().isClientSide()) {
            ClientMessageDispatcher.sendRunescribingMutexChange(this, hMutex, vMutex, playerTier, isRemove);
            return returnValue;
        }
        if (isRemove) {
            if (!this.canUndo(player)) {
                return false;
            }
            this.getItem(4).shrink(1);
            player.giveExperiencePoints(-GeneralConfigValues.ExperiencePerRunescribeUndo);
        }
        if ((patternStack = this.getItem(2)).getItem() == ItemInit.RUNE_PATTERN.get()) {
            ((RunePattern)ItemInit.RUNE_PATTERN.get()).setHMutex(patternStack, hMutex);
            ((RunePattern)ItemInit.RUNE_PATTERN.get()).setVMutex(patternStack, vMutex);
        }
        if ((recipe = this.getLevel().getRecipeManager().getRecipeFor(RecipeInit.RUNESCRIBING_TYPE.get(), ContainerTools.createTemporaryContainer(patternStack).asCraftInput(), this.level).map(h -> h.value()).orElse(null)) != null && (playerTier < 0 || recipe.getTier() <= playerTier) && EventDispatcher.DispatchRunescribeCraft(recipe, recipe.getResultItem(), player)) {
            this.setItem(2, recipe.getResultItem());
            if (player instanceof ServerPlayer) {
                CustomAdvancementTriggers.RUNESCRIBE.trigger((ServerPlayer)player, recipe.getResultItem());
            }
            returnValue = true;
        }
        if (playerTier > 0) {
            this.damageHammerAndChisel(source);
        }
        return returnValue;
    }

    private static RunescribingTableTile getAndVerify(Level world, BlockPos position) {
        BlockEntity te;
        if (world.isLoaded(position) && world.getBlockState(position).getBlock() == BlockInit.RUNESCRIBING_TABLE.get() && (te = world.getBlockEntity(position)) != null && te instanceof RunescribingTableTile) {
            return (RunescribingTableTile)te;
        }
        return null;
    }

    public static void handleMutexChangeMessage(ServerPlayer sendingPlayer, RunescribingTableMutexChangeMessage message) {
        RunescribingTableTile te = RunescribingTableTile.getAndVerify(sendingPlayer.level(), message.getPosition());
        if (te == null) {
            return;
        }
        te.writeMutexChanges(message.getHMutex(), message.getVMutex(), (Player)sendingPlayer, message.getPlayerTier(), message.getIsRemove(), sendingPlayer.level().getRandom());
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (!this.getLevel().isClientSide()) {
            if (index == 0) {
                this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RunescribingTableBlock.HAS_HAMMER, Boolean.valueOf(!stack.isEmpty())), 2);
            } else if (index == 1) {
                this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RunescribingTableBlock.HAS_CHISEL, Boolean.valueOf(!stack.isEmpty())), 2);
            }
        }
        super.setItem(index, stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        if (itemStackIn.getCount() != 1) {
            return false;
        }
        if (index == 0 && this.getItem(0).isEmpty() && itemStackIn.getItem() == ItemInit.RUNESMITH_HAMMER.get()) {
            return true;
        }
        if (index == 1 && this.getItem(1).isEmpty() && itemStackIn.getItem() == ItemInit.RUNESMITH_CHISEL.get()) {
            return true;
        }
        if (index == 4 && itemStackIn.getItem() == Items.CLAY_BALL) {
            return true;
        }
        return index == 2 && this.getItem(2).isEmpty() && itemStackIn.getItem() == ItemInit.RUNE_PATTERN.get();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (this.getItem(index).isEmpty() || !ItemStack.matches((ItemStack)this.getItem(index), (ItemStack)stack)) {
            return false;
        }
        return direction == Direction.EAST || direction == Direction.WEST || direction == Direction.NORTH || direction == Direction.SOUTH ? index == 0 || index == 1 || index == 4 : (direction == Direction.DOWN || direction == Direction.UP) && index == 2;
    }

    public boolean canPlaceItem(int slot, ItemStack stack) {
        switch (slot) {
            case 0: {
                return stack.getItem() == ItemInit.RUNESMITH_HAMMER.get() || stack.getItem() == ItemInit.RUNIC_MALUS.get();
            }
            case 1: {
                return stack.getItem() == ItemInit.RUNESMITH_CHISEL.get();
            }
            case 2: {
                return stack.getItem() == ItemInit.RUNE_PATTERN.get();
            }
            case 3: {
                return stack.getItem() == ItemInit.RECIPE_SCRAP_RUNESCRIBING.get();
            }
            case 4: {
                return stack.getItem() == Items.CLAY_BALL;
            }
        }
        if (slot >= 5 && slot <= 29) {
            return stack.getItem() == ItemInit.RECIPE_SCRAP_RUNESCRIBING.get();
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.EAST || side == Direction.WEST || side == Direction.NORTH || side == Direction.SOUTH) {
            return new int[]{0, 1, 4};
        }
        if (side == Direction.UP) {
            return new int[]{2};
        }
        if (side == Direction.DOWN) {
            return new int[]{3};
        }
        return new int[0];
    }

    public int getMaxStackSize() {
        return 64;
    }
}

