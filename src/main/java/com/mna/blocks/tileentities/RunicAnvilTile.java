 
package com.mna.blocks.tileentities;

import com.mna.tools.EnchantmentUtils;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.events.RunicAnvilShouldActivateEvent;
import com.mna.blocks.runeforging.RunicAnvilBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.enchantments.auras.Aura;
import com.mna.events.EventDispatcher;
import com.mna.items.ItemInit;
import com.mna.items.armor.BrokenMageArmor;
import com.mna.items.ritual.PractitionersPatch;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import com.mna.items.runes.Rune;
import com.mna.items.runes.RunePattern;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.manaweaving.ManaweavingRecipe;
import com.mna.recipes.runeforging.RuneforgingRecipe;
import com.mna.tools.ContainerTools;
import com.mna.tools.InventoryUtilities;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import net.neoforged.bus.api.Event;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;
import com.mna.api.tools.MNAItemData;

public class RunicAnvilTile
extends TileEntityWithInventory
implements IBlockEntityExtension,
WorldlyContainer,
GeoBlockEntity {
    private static final int MAX_ITEMS = 2;
    public static final int PATTERN_SLOT_INDEX = 0;
    public static final int MATERIAL_SLOT_INDEX = 1;
    public static final int TOOL_SLOT_INDEX = 2;
    public static final int ADVANCE_RESPONSE_ADVANCED = 0;
    public static final int ADVANCE_RESPONSE_NO_MATCH = 1;
    public static final int ADVANCE_RESPONSE_LOW_TIER = 2;
    public static final int ADVANCE_RESPONSE_CLIENT_WORLD = 3;
    public static final int ADVANCE_RESPONSE_CRAFTING_COMPLETE = 4;
    public static final int ADVANCE_RESPONSE_EVENT_CANCELED = 5;
    public int craftProgress = 0;
    public int maxCraftProgress = 10;
    private ItemStack __cachedRecipeOutput = ItemStack.EMPTY;
    private RuneforgingRecipe __cachedRecipe;
    private Optional<ManaweavingRecipe> __cachedEnchantRecipe;
    private boolean autoCacheRecipe = true;
    private boolean settingCraftOutput = false;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public RunicAnvilTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state, 2);
        this.__cachedRecipeOutput = ItemStack.EMPTY;
    }

    public RunicAnvilTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.RUNIC_ANVIL.get(), pos, state);
    }

    public int advanceCrafting(Player crafter, int playerTier) {
        return this.advanceCrafting(crafter, playerTier, true);
    }

    public int advanceCrafting(Player crafter, int playerTier, boolean allowDurability) {
        int cacheResponse = this.cacheRecipe(crafter, playerTier);
        if (this.getLevel().isClientSide()) {
            return 3;
        }
        if (this.__cachedRecipeOutput.isEmpty()) {
            return cacheResponse;
        }
        ++this.craftProgress;
        int targetProgress = this.getMaxCraftProgress();
        if (this.craftProgress >= targetProgress) {
            this.craftProgress = 0;
            if (this.__cachedRecipe != null ? !EventDispatcher.DispatchRuneforgeCraft(this.__cachedRecipe, this.__cachedRecipeOutput, crafter) : (this.__cachedEnchantRecipe != null || EnchantmentUtils.enchantmentMap((ItemStack)this.__cachedRecipeOutput).size() > 0) && !EventDispatcher.DispatchRuneforgeEnchant(this.__cachedRecipeOutput, crafter)) {
                return 5;
            }
            this.completeCraftingAndReset(crafter, allowDurability);
            this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)RunicAnvilBlock.ACTIVE, (Comparable)Boolean.valueOf(false)));
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
            return 4;
        }
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        return 0;
    }

    private void completeCraftingAndReset(Player crafter, boolean allowPatternDurability) {
        this.settingCraftOutput = true;
        if (this.__cachedRecipe != null) {
            ArrayList<ItemStack> byproducts = this.__cachedRecipe.rollByproducts(this.level.getRandom());
            for (ItemStack byproduct : byproducts) {
                InventoryUtilities.DropItemAt(byproduct, Vec3.atCenterOf((Vec3i)this.getBlockPos().above()), this.level, true);
            }
        }
        ItemStack patternStack = this.getItem(0);
        if (allowPatternDurability && patternStack.getItem() instanceof RunePattern) {
            if (!RunePattern.incrementDamage(patternStack)) {
                this.setItem(1, this.__cachedRecipeOutput);
            } else {
                this.getLevel().playSound(null, this.getBlockPos(), SoundEvents.NETHER_BRICKS_BREAK, SoundSource.BLOCKS, 1.0f, (float)(0.8 + Math.random() * 0.4));
                this.setItem(0, this.__cachedRecipeOutput);
                this.setItem(1, ItemStack.EMPTY);
            }
        } else {
            this.setItem(0, this.__cachedRecipeOutput);
            this.setItem(1, ItemStack.EMPTY);
        }
        this.settingCraftOutput = false;
        if (crafter != null & crafter instanceof ServerPlayer) {
            CustomAdvancementTriggers.RUNIC_ANVIL_CRAFT.trigger((ServerPlayer)crafter, this.getItem(0));
        }
        this.__cachedRecipeOutput = ItemStack.EMPTY;
        this.__cachedRecipe = null;
        this.__cachedEnchantRecipe = null;
    }

    public int getMaxCraftProgress() {
        return this.__cachedRecipe != null ? this.__cachedRecipe.getHits() : this.maxCraftProgress;
    }

    private int cacheRecipe(Player player, int playerTier) {
        int output = 1;
        if (this.cacheSpecialRecipes(playerTier)) {
            boolean isActive = (Boolean)this.getBlockState().getValue((Property)RunicAnvilBlock.ACTIVE);
            if (!isActive) {
                this.getLevel().setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue((Property)RunicAnvilBlock.ACTIVE, (Comparable)Boolean.valueOf(true)), 3);
            }
        } else {
            CraftingContainer inv = this.createDummyCraftingInventory();
            this.__cachedRecipe = this.getLevel().getRecipeManager().getRecipeFor((RecipeType)RecipeInit.RUNEFORGING_TYPE.get(), (Container)inv, this.level).orElse(null);
            this.__cachedRecipeOutput = ItemStack.EMPTY;
            if (this.__cachedRecipe == null) {
                ItemStack rune = this.getItem(1);
                if (!rune.isEmpty()) {
                    output = this.cacheEnchantRecipe(rune, player, playerTier);
                }
            } else if (this.__cachedRecipe.getTier() <= playerTier) {
                this.__cachedRecipeOutput = this.__cachedRecipe.getResultItem();
                output = 0;
            } else {
                this.__cachedRecipe = null;
                this.__cachedRecipeOutput = ItemStack.EMPTY;
                output = 2;
            }
            boolean isActive = (Boolean)this.getBlockState().getValue((Property)RunicAnvilBlock.ACTIVE);
            if (!this.__cachedRecipeOutput.isEmpty()) {
                if (!isActive) {
                    this.getLevel().setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue((Property)RunicAnvilBlock.ACTIVE, (Comparable)Boolean.valueOf(true)), 3);
                }
            } else if (isActive) {
                this.level.setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue((Property)RunicAnvilBlock.ACTIVE, (Comparable)Boolean.valueOf(false)), 0);
            }
        }
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        return output;
    }

    private int cacheEnchantRecipe(ItemStack rune, Player player, int playerTier) {
        java.util.Map<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)rune);
        if (enchantments.size() == 0) {
            this.__cachedEnchantRecipe = null;
            this.__cachedRecipeOutput = ItemStack.EMPTY;
            return 1;
        }
        this.__cachedEnchantRecipe = this.getLevel().getRecipeManager().getRecipes().stream().filter(r -> r.value().getType() == RecipeInit.MANAWEAVING_RECIPE_TYPE.get() && ((ManaweavingRecipe)r.value()).matches(enchantments)).map(r -> (ManaweavingRecipe)r.value()).findFirst();
        int highestEnchantmentLevel = 0;
        for (Integer entry : enchantments.values()) {
            if (entry <= highestEnchantmentLevel) continue;
            highestEnchantmentLevel = entry;
        }
        int freeformTier = 5;
        if (highestEnchantmentLevel <= 2) {
            freeformTier = 3;
        } else if (highestEnchantmentLevel <= 4) {
            freeformTier = 4;
        }
        if (rune.getItem() == ItemInit.RUNE_PROJECTION.get() && GeneralConfigValues.ProjectionCanEnchantAnything) {
            this.__cachedEnchantRecipe = Optional.empty();
            freeformTier = 1;
        }
        if (this.__cachedEnchantRecipe.isPresent() && this.__cachedEnchantRecipe.get().getTier() > playerTier || !this.__cachedEnchantRecipe.isPresent() && playerTier < freeformTier) {
            this.__cachedEnchantRecipe = null;
            this.__cachedRecipeOutput = ItemStack.EMPTY;
            return 2;
        }
        this.__cachedRecipeOutput = this.getItem(0).copy();
        boolean compatible = true;
        for (Map.Entry e2 : enchantments.entrySet()) {
            Aura aura;
            Object object;
            if (player != null && (object = com.mna.enchantments.framework.EnchantmentBehaviors.get((Enchantment)e2.getKey())) instanceof Aura && !(aura = (Aura)(object)).canEnchant(this.__cachedRecipeOutput, player)) {
                compatible = false;
                break;
            }
            if (((Enchantment)e2.getKey()).canEnchant(this.__cachedRecipeOutput)) continue;
            compatible = false;
            break;
        }
        Map existingEnchants = EnchantmentUtils.enchantmentMap((ItemStack)this.__cachedRecipeOutput);
        block2: for (Map.Entry ee : existingEnchants.entrySet()) {
            for (Map.Entry entry : enchantments.entrySet()) {
                Aura aura;
                Object k;
                if (entry.getKey() == ee.getKey() && (Integer)entry.getValue() > (Integer)ee.getValue()) continue;
                if (player != null && (k = com.mna.enchantments.framework.EnchantmentBehaviors.get((Enchantment)entry.getKey())) instanceof Aura && !(aura = (Aura)(k)).canEnchant(this.__cachedRecipeOutput, player)) {
                    compatible = false;
                    continue block2;
                }
                if (!((Enchantment)entry.getKey()).canEnchant(this.__cachedRecipeOutput)) {
                    compatible = false;
                    continue block2;
                }
                if (entry.getKey() == ee.getKey() && (Integer)entry.getValue() <= (Integer)ee.getValue()) {
                    compatible = false;
                    continue block2;
                }
                if (player != null && (k = com.mna.enchantments.framework.EnchantmentBehaviors.get((Enchantment)entry.getKey())) instanceof Aura && !(aura = (Aura)(k)).isCompatibleWith((Enchantment)ee.getKey(), player)) {
                    compatible = false;
                    continue block2;
                }
                if (((Enchantment)entry.getKey()).isCompatibleWith((Enchantment)ee.getKey())) continue;
                compatible = false;
                continue block2;
            }
        }
        if (compatible) {
            existingEnchants.forEach((e, i) -> {
                if (!enchantments.containsKey(e) || (Integer)enchantments.get(e) < i) {
                    enchantments.put(e, i);
                }
            });
            EnchantmentHelper.setEnchantments((Map)enchantments, (ItemStack)this.__cachedRecipeOutput);
            return 0;
        }
        this.__cachedRecipeOutput = ItemStack.EMPTY;
        return 1;
    }

    private boolean cacheSpecialRecipes(int playerTier) {
        RunicAnvilShouldActivateEvent event = new RunicAnvilShouldActivateEvent(this.getItem(0), this.getItem(1));
        NeoForge.EVENT_BUS.post((Event)event);
        if (event.getResult() == Result.ALLOW) {
            return true;
        }
        return this.cachePouch() || this.cachePattern() || this.cacheRepairs();
    }

    private boolean cacheRepairs() {
        ItemStack armorStack = this.getItem(0);
        if (!(armorStack.getItem() instanceof BrokenMageArmor) || !BrokenMageArmor.hasRestore(armorStack)) {
            return false;
        }
        ItemStack patchStack = this.getItem(1);
        return patchStack.getItem() == ItemInit.INFUSED_SILK.get();
    }

    private boolean cachePouch() {
        ItemStack pouchStack = this.getItem(0);
        if (!(pouchStack.getItem() instanceof PractitionersPouch)) {
            return false;
        }
        ItemStack patchStack = this.getItem(1);
        if (!(patchStack.getItem() instanceof PractitionersPatch)) {
            return false;
        }
        PractitionersPouchPatches patch = ((PractitionersPatch)patchStack.getItem()).getPatch();
        int patchLevel = ((PractitionersPatch)patchStack.getItem()).getLevel();
        if (((PractitionersPouch)pouchStack.getItem()).getPatchLevel(pouchStack, patch) >= patchLevel) {
            return false;
        }
        return ((PractitionersPouch)pouchStack.getItem()).countAppliedPatchesForLimit(pouchStack, patchStack) < 4;
    }

    private boolean cachePattern() {
        ItemStack runeStack = this.getItem(0);
        if (!(runeStack.getItem() instanceof Rune)) {
            return false;
        }
        ItemStack patchStack = this.getItem(1);
        return patchStack.getItem() == Items.PAPER;
    }

    public ItemStack getOutputStack() {
        return this.__cachedRecipeOutput;
    }

    @Nullable
    public RuneforgingRecipe getCachedRecipe() {
        return this.__cachedRecipe;
    }

    private CraftingContainer createDummyCraftingInventory() {
        return ContainerTools.createTemporaryContainer(2, 1, (NonNullList<ItemStack>)NonNullList.of(null, (Object[])new ItemStack[]{this.getItem(0), this.getItem(1)}));
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putInt("craft_progress", this.craftProgress);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        this.craftProgress = compound.getInt("craft_progress");
        super.loadAdditional(compound, registries);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        CompoundTag sub_1 = new CompoundTag();
        MNAItemData.save(this.getItem(0), sub_1);
        base.put("invSync_1", (Tag)sub_1);
        CompoundTag sub_2 = new CompoundTag();
        MNAItemData.save(this.getItem(1), sub_2);
        base.put("invSync_2", (Tag)sub_2);
        base.putInt("craft_progress", this.craftProgress);
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        CompoundTag sub = tag.getCompound("invSync_1");
        this.setItem(0, ItemStack.parseOptional(this.getLevel().registryAccess(), sub));
        CompoundTag sub2 = tag.getCompound("invSync_2");
        this.setItem(1, ItemStack.parseOptional(this.getLevel().registryAccess(), sub2));
        this.craftProgress = tag.getInt("craft_progress");
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag data = pkt.getTag();
        CompoundTag sub = data.getCompound("invSync_1");
        this.setItem(0, ItemStack.parseOptional(this.getLevel().registryAccess(), sub));
        CompoundTag sub2 = data.getCompound("invSync_2");
        this.setItem(1, ItemStack.parseOptional(this.getLevel().registryAccess(), sub2));
        this.craftProgress = data.getInt("craft_progress");
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    public int getMaxStackSize() {
        return this.settingCraftOutput ? 64 : 1;
    }

    public boolean pushItemStack(ItemStack stack, Player player, int playerTier) {
        if (stack.getCount() != 1) {
            return false;
        }
        if (this.getItem(0).isEmpty()) {
            this.setItem(0, stack);
            return true;
        }
        if (this.getItem(1).isEmpty()) {
            this.setInventorySlotContentsNoCache(1, stack);
            this.cacheRecipe(player, playerTier);
            return true;
        }
        return false;
    }

    private void setInventorySlotContentsNoCache(int index, ItemStack stack) {
        this.autoCacheRecipe = false;
        this.setItem(index, stack);
        this.autoCacheRecipe = true;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        if (this.autoCacheRecipe) {
            this.cacheRecipe(null, 5);
        }
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return this.removeItemNoUpdate(index);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack output = super.removeItemNoUpdate(index);
        if (!output.isEmpty() && this.autoCacheRecipe) {
            this.cacheRecipe(null, 5);
        }
        return output;
    }

    public ItemStack popItemStack() {
        if (this.craftProgress > 0) {
            return ItemStack.EMPTY;
        }
        this.__cachedRecipeOutput = ItemStack.EMPTY;
        if (!this.getItem(1).isEmpty()) {
            return this.removeItemNoUpdate(1);
        }
        if (!this.getItem(0).isEmpty()) {
            return this.removeItemNoUpdate(0);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack[] getDisplayedItems() {
        return new ItemStack[]{this.getItem(0), this.getItem(1)};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        if (itemStackIn.getCount() != 1) {
            return false;
        }
        if (index == 1) {
            return !this.getItem(0).isEmpty() && this.getItem(1).isEmpty();
        }
        if (index == 0) {
            return this.getItem(1).isEmpty() && this.getItem(0).isEmpty();
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (!ItemStack.matches((ItemStack)this.getItem(index), (ItemStack)stack)) {
            return false;
        }
        if (index == 0) {
            return this.getItem(1).isEmpty() && !this.getItem(0).isEmpty();
        }
        if (index == 1) {
            return !this.getItem(1).isEmpty();
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1};
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> {
            if (((Boolean)this.getBlockState().getValue((Property)RunicAnvilBlock.ACTIVE)).booleanValue()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.anvil_armature.in").thenLoop("animation.anvil_armature.idle"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.anvil_armature.out").thenLoop("animation.anvil_armature.hidden"));
        })});
    }
}


