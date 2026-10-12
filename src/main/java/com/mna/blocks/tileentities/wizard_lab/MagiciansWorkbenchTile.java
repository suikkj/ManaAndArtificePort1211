 
package com.mna.blocks.tileentities.wizard_lab;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.gui.containers.block.ContainerMagiciansWorkbench;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.RecipeUtil;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import com.mna.api.tools.MNAItemData;

public class MagiciansWorkbenchTile
extends TileEntityWithInventory
implements MenuProvider,
Consumer<FriendlyByteBuf> {
    public static final int MAX_RECIPES = 8;
    public ResultContainer firstResultInv = new ResultContainer();
    public ResultContainer secondResultInv = new ResultContainer();
    private final LinkedList<RememberedRecipe> rememberedRecipes = new LinkedList();

    public MagiciansWorkbenchTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 38);
    }

    public MagiciansWorkbenchTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.MAGICIANS_WORKBENCH.get(), pos, state);
    }

    public MagiciansWorkbenchTile readFrom(FriendlyByteBuf data) {
        return this;
    }

    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ContainerMagiciansWorkbench(id, playerInventory, this);
    }

    public Component getDisplayName() {
        return Component.translatable((String)"mna:container.magicians_workbench");
    }

    @Override
    public void accept(FriendlyByteBuf data) {
        data.writeBlockPos(this.getBlockPos());
    }

    public void rememberRecipe(ItemStack output, ItemStack[] recipeItems) {
        if (output.isEmpty()) {
            return;
        }
        RecipeUtil.lookupCraftingRecipe(this.getLevel(), recipeItems).ifPresent(resolvedRecipe -> {
            for (RememberedRecipe recipe : this.rememberedRecipes) {
                if (!recipe.recipeId.equals(resolvedRecipe.getId())) continue;
                return;
            }
            if (!this.popRecipe()) {
                return;
            }
            this.rememberedRecipes.add(new RememberedRecipe(resolvedRecipe.getId(), output));
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        });
    }

    private boolean popRecipe() {
        if (this.rememberedRecipes.size() < 8) {
            return true;
        }
        for (int index = 0; index < this.rememberedRecipes.size(); ++index) {
            if (this.rememberedRecipes.get((int)index).isLocked) continue;
            this.rememberedRecipes.remove(index);
            return true;
        }
        return false;
    }

    public boolean toggleRecipeLock(int recipeIndex) {
        if (recipeIndex >= this.getRememberedRecipeItems().size()) {
            return false;
        }
        RememberedRecipe recipe = this.getRememberedRecipeItems().get(recipeIndex);
        if (recipe == null) {
            return false;
        }
        recipe.isLocked = !recipe.isLocked;
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        return true;
    }

    public LinkedList<RememberedRecipe> getRememberedRecipeItems() {
        return this.rememberedRecipes;
    }

    private StackedContents fillStackedContents() {
        StackedContents contents = new StackedContents();
        for (int i = ContainerMagiciansWorkbench.INVENTORY_STORAGE_START; i <= ContainerMagiciansWorkbench.INVENTORY_STORAGE_END; ++i) {
            contents.accountSimpleStack(this.getItem(i));
        }
        return contents;
    }

    public Pair<AutoCraftResult, ItemStack> craftFromRemembered(ItemStack desiredItem) {
        Optional<RememberedRecipe> remembered = this.rememberedRecipes.stream().filter(rr -> ItemStack.isSameItemSameTags((ItemStack)rr.output, (ItemStack)desiredItem)).findFirst();
        if (!remembered.isPresent()) {
            return new Pair(AutoCraftResult.NO_RECIPE, ItemStack.EMPTY);
        }
        StackedContents contents = this.fillStackedContents();
        if (!contents.canCraft((Recipe)remembered.get().getRecipe(this.level), (IntList)null)) {
            return new Pair(AutoCraftResult.MISSING_ITEMS, ItemStack.EMPTY);
        }
        InvWrapper myInvWrapper = new InvWrapper((Container)this);
        block0: for (Ingredient ingred : remembered.get().getRecipe(this.level).getIngredients()) {
            if (ingred.isEmpty()) continue;
            for (int i = ContainerMagiciansWorkbench.INVENTORY_STORAGE_START; i <= ContainerMagiciansWorkbench.INVENTORY_STORAGE_END; ++i) {
                ItemStack existing = this.getItem(i);
                if (existing.isEmpty() || !ingred.test(existing)) continue;
                ItemStack resultStack = existing.getCraftingRemainingItem();
                existing.shrink(1);
                if (InventoryUtilities.mergeIntoInventory((IItemHandler)myInvWrapper, resultStack)) continue block0;
                Vec3 dropPos = Vec3.atCenterOf((Vec3i)this.getBlockPos());
                ItemEntity drop = new ItemEntity(this.getLevel(), dropPos.x, dropPos.y, dropPos.z, resultStack);
                drop.setDeltaMovement(0.0, 0.25, 0.0);
                drop.setDefaultPickUpDelay();
                this.getLevel().addFreshEntity((Entity)drop);
                continue block0;
            }
        }
        return new Pair(AutoCraftResult.SUCCESS, remembered.get().output.copy());
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        ListTag recall = new ListTag();
        for (RememberedRecipe recipe : this.rememberedRecipes) {
            try {
                CompoundTag output = new CompoundTag();
                MNAItemData.save(recipe.getOutput(this.level), output);
                output.putBoolean("isLocked", recipe.isLocked);
                output.putString("recipeId", recipe.recipeId.toString());
                recall.add(output);
            }
            catch (Throwable throwable) {}
        }
        compound.put("rememberedRecipes", (Tag)recall);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag recall = compound.getList("rememberedRecipes", 10);
        this.rememberedRecipes.clear();
        if (recall != null) {
            for (int i = 0; i < recall.size(); ++i) {
                CompoundTag rememberedRecipe = (CompoundTag)recall.get(i);
                if (!rememberedRecipe.contains("recipeId")) continue;
                ResourceLocation recipeId = ResourceLocation.parse(rememberedRecipe.getString("recipeId"));
                ItemStack output = ItemStack.parseOptional(this.getLevel().registryAccess(), (CompoundTag)rememberedRecipe);
                if (output.isEmpty()) continue;
                RememberedRecipe rec = new RememberedRecipe(recipeId, output);
                rec.isLocked = rememberedRecipe.getBoolean("isLocked");
                this.rememberedRecipes.add(rec);
            }
        }
        super.loadAdditional(compound, registries);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        this.loadAdditional(tag, com.mna.api.tools.MNAItemData.provider());
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index < 18 || index > 37;
    }

    public boolean canPlaceItem(int index, ItemStack stack) {
        return index < 18 || index > 37;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        return index < 18 || index > 37;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = new int[18];
        for (int i = 0; i < 18; ++i) {
            slots[i] = i;
        }
        return slots;
    }

    public class RememberedRecipe {
        public final ResourceLocation recipeId;
        private ItemStack output;
        private Ingredient[] ingredients;
        private CraftingRecipe recipe;
        private boolean isLocked;
        private boolean hasResolved = false;

        public RememberedRecipe(ResourceLocation recipeId, ItemStack output) {
            this.recipeId = recipeId;
            this.isLocked = false;
        }

        public void lock() {
            this.isLocked = true;
        }

        public void unlock() {
            this.isLocked = false;
        }

        public boolean isLocked() {
            return this.isLocked;
        }

        public ItemStack getOutput(Level world) {
            if (!this.hasResolved) {
                this.resolveRecipe(world);
            }
            return this.output;
        }

        public Ingredient[] getComponents(Level world) {
            if (!this.hasResolved) {
                this.resolveRecipe(world);
            }
            return this.ingredients;
        }

        @Nullable
        public CraftingRecipe getRecipe(Level world) {
            if (!this.hasResolved) {
                this.resolveRecipe(world);
            }
            return this.recipe;
        }

        private void resolveRecipe(Level world) {
            world.getRecipeManager().byKey(this.recipeId).ifPresent(recipe -> {
                if (recipe instanceof CraftingRecipe) {
                    this.output = recipe.getResultItem(world.registryAccess());
                    this.ingredients = (Ingredient[])recipe.getIngredients().toArray((Object[])new Ingredient[0]);
                    this.recipe = (CraftingRecipe)recipe;
                }
            });
            this.hasResolved = true;
        }
    }

    public static enum AutoCraftResult {
        MISSING_ITEMS,
        NO_RECIPE,
        SUCCESS;

    }
}

