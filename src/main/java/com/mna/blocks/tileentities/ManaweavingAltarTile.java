 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.Registries;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.blocks.IRequirePlayerReference;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.api.sound.SFX;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.events.EventDispatcher;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_client.SpawnParticleEffectMessage;
import com.mna.recipes.ItemAndPatternCraftingInventory;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import com.mna.recipes.manaweaving.ManaweavingRecipe;
import com.mna.tools.InventoryUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.joml.Vector3f;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNAItemData;

public class ManaweavingAltarTile
extends TileEntityWithInventory
implements IRequirePlayerReference<ManaweavingAltarTile> {
    public static final int MAX_ITEMS = 9;
    private ArrayList<String> pendingPatterns;
    private ArrayList<IManaweavePattern> addedPatterns;
    private boolean crafting = false;
    private int craftTicks = 0;
    private ManaweavingRecipe __cachedRecipe;
    private ItemStack __cachedRecipeOutput = ItemStack.EMPTY;
    private UUID __crafterID;
    private Player __crafter;
    private boolean copyNBT = false;
    private int lastCraftPatternCount = 0;
    private int lastCraftTier = 0;
    private IFaction lastCraftFaction = null;
    private ResourceLocation lastCraftRecipeID;
    private ManaweavingRecipe _lastCraftRecipe;
    private NonNullList<ItemStack> lastCraftItems;
    private NonNullList<ItemStack> lastCraftItemsMerged;
    private ItemStack lastCraftOutput = ItemStack.EMPTY;
    private java.util.Optional<IItemHandlerModifiable> handler;

    public ManaweavingAltarTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state, 9);
        this.addedPatterns = new ArrayList();
        this.pendingPatterns = new ArrayList();
        this.lastCraftItems = NonNullList.create();
        this.lastCraftItemsMerged = NonNullList.create();
    }

    public ManaweavingAltarTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.MANAWEAVING_ALTAR.get(), pos, state);
    }

    public void pushPattern(IManaweavePattern pattern, Player player) {
        if (this.crafting) {
            return;
        }
        this.setPlayerReference(player);
        this.resolvePendingPatterns();
        if (this.addedPatterns.size() == 6) {
            return;
        }
        this.addedPatterns.add(pattern);
        if (this.MatchesRecipe(player)) {
            this.setCrafting(true);
        }
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (this.isCrafting()) {
            return ItemStack.EMPTY;
        }
        ItemStack output = super.removeItemNoUpdate(index);
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
        return output;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (this.isCrafting()) {
            return ItemStack.EMPTY;
        }
        ItemStack output = super.removeItem(index, count);
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
        return output;
    }

    public void popPattern(Player player) {
        if (this.crafting) {
            return;
        }
        this.setPlayerReference(player);
        this.resolvePendingPatterns();
        if (this.addedPatterns.size() <= 0) {
            return;
        }
        this.addedPatterns.remove(this.addedPatterns.size() - 1);
        if (this.MatchesRecipe(player)) {
            this.setCrafting(true);
        }
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    public boolean pushItem(Player player, ItemStack stack) {
        if (this.crafting) {
            return false;
        }
        this.setPlayerReference(player);
        if (stack.getCount() != 1) {
            return false;
        }
        boolean success = false;
        for (int i = 0; i < 9; ++i) {
            if (!this.getItem(i).isEmpty()) continue;
            this.setItem(i, stack);
            success = true;
            break;
        }
        if (success && this.MatchesRecipe(player)) {
            this.setCrafting(true);
        }
        return success;
    }

    public ItemStack popItem(Player player) {
        if (this.crafting) {
            return ItemStack.EMPTY;
        }
        this.setPlayerReference(player);
        ItemStack output = ItemStack.EMPTY;
        for (int i = 8; i >= 0; --i) {
            if (this.getItem(i).isEmpty()) continue;
            output = this.removeItemNoUpdate(i);
            break;
        }
        if (this.MatchesRecipe(player)) {
            this.setCrafting(true);
        }
        return output;
    }

    public void clearPatterns() {
        this.addedPatterns.clear();
    }

    public Collection<IManaweavePattern> getAddedPatterns() {
        this.resolvePendingPatterns();
        return this.addedPatterns;
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        CompoundTag sub = this.writeAdditonal(new CompoundTag());
        base.put("invSync", (Tag)sub);
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        CompoundTag sub = tag.getCompound("invSync");
        this.readAdditional(sub);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag sub = pkt.getTag().getCompound("invSync");
        this.readAdditional(sub);
    }

    public int getMaxStackSize() {
        return 1;
    }

    public ItemStack[] getDisplayedItems() {
        ItemStack[] stacks = new ItemStack[9];
        for (int i = 0; i < 9; ++i) {
            stacks[i] = this.getItem(i);
        }
        return stacks;
    }

    private boolean MatchesRecipe(Player player) {
        IPlayerProgression progression;
        int tier = 1;
        IFaction faction = null;
        if (player != null && (progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get())) != null) {
            tier = progression.getTier();
            faction = progression.getAlliedFaction();
        }
        this.__cachedRecipe = null;
        this.__cachedRecipeOutput = ItemStack.EMPTY;
        ManaweavingRecipe recipe = this.getLevel().getRecipeManager().getRecipeFor(RecipeInit.MANAWEAVING_RECIPE_TYPE.get(), this.createCraftingInventory().asCraftInput(), this.level).map(h -> h.value()).orElse(null);
        if (recipe != null && recipe.getTier() <= tier) {
            this.__cachedRecipe = recipe;
            this.__cachedRecipeOutput = recipe.getResultItem().copy();
            this.copyNBT = recipe.getCopyNBT();
        }
        if (this.__cachedRecipeOutput != null && !this.__cachedRecipeOutput.isEmpty()) {
            if (this.__cachedRecipe.getFactionRequirement() != null && this.__cachedRecipe.getFactionRequirement() != faction) {
                this.__cachedRecipeOutput = ItemStack.EMPTY;
                this.__cachedRecipe = null;
                if (player != null) {
                    player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.wrongfaction"));
                }
                return false;
            }
            this.lastCraftTier = recipe.getTier();
            this.lastCraftFaction = recipe.getFactionRequirement();
            return true;
        }
        return false;
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        this.writeAdditonal(compound);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        this.readAdditional(compound);
        super.loadAdditional(compound, registries);
    }

    private CompoundTag writeAdditonal(CompoundTag compound) {
        MNAItemData.saveItems((CompoundTag)compound, (NonNullList)this.inventoryItems);
        this.resolvePendingPatterns();
        CompoundTag patterns = new CompoundTag();
        int count = 0;
        for (IManaweavePattern pattern : this.addedPatterns) {
            patterns.putString("pattern_" + count++, pattern.getRegistryId().toString());
        }
        compound.putInt("patterns_count", this.addedPatterns.size());
        compound.put("patterns", (Tag)patterns);
        compound.putBoolean("crafting", this.crafting);
        compound.putBoolean("copyNBT", this.copyNBT);
        compound.putInt("craftTicks", this.craftTicks);
        CompoundTag lastCraft = new CompoundTag();
        lastCraft.put("lastCraftReagents", (Tag)MNAItemData.saveItems((CompoundTag)new CompoundTag(), this.lastCraftItems));
        lastCraft.putInt("patterns_count", this.lastCraftPatternCount);
        lastCraft.putInt("tier", this.lastCraftTier);
        if (this.lastCraftFaction != null) {
            lastCraft.putString("faction", Registries.Factions.getKey(this.lastCraftFaction).toString());
        }
        lastCraft.put("lastCraftOutput", (Tag)MNAItemData.save(this.lastCraftOutput, new CompoundTag()));
        if (this.lastCraftRecipeID != null) {
            lastCraft.putString("lastCraftRecipeID", this.lastCraftRecipeID.toString());
        }
        compound.put("lastCraft", (Tag)lastCraft);
        if (this.__crafterID != null) {
            compound.putString("crafter_uuid", this.__crafterID.toString());
        }
        return compound;
    }

    private void readAdditional(CompoundTag compound) {
        this.clearContent();
        this.addedPatterns.clear();
        this.pendingPatterns.clear();
        this.lastCraftItems.clear();
        for (int i = 0; i < 9; ++i) {
            this.lastCraftItems.add(ItemStack.EMPTY);
        }
        MNAItemData.loadItems((CompoundTag)compound, (NonNullList)this.inventoryItems);
        if (compound.contains("patterns") && compound.contains("patterns_count")) {
            int count = compound.getInt("patterns_count");
            CompoundTag patterns = compound.getCompound("patterns");
            for (int i = 0; i < count; ++i) {
                String identifier = "pattern_" + i;
                if (!patterns.contains(identifier)) continue;
                this.pendingPatterns.add(patterns.getString(identifier));
            }
        }
        this.crafting = compound.getBoolean("crafting");
        this.craftTicks = compound.getInt("craftTicks");
        this.copyNBT = compound.getBoolean("copyNBT");
        if (compound.contains("crafter_uuid")) {
            this.__crafterID = UUID.fromString(compound.getString("crafter_uuid"));
        }
        if (compound.contains("lastCraft")) {
            CompoundTag lastCraft = compound.getCompound("lastCraft");
            this.lastCraftOutput = ItemStack.parseOptional(this.getLevel().registryAccess(), lastCraft.getCompound("lastCraftOutput"));
            MNAItemData.loadItems((CompoundTag)lastCraft.getCompound("lastCraftReagents"), this.lastCraftItems);
            this.lastCraftPatternCount = lastCraft.getInt("patterns_count");
            this.lastCraftTier = lastCraft.getInt("tier");
            if (lastCraft.contains("faction")) {
                ResourceLocation factionID = ResourceLocation.parse(lastCraft.getString("faction").toLowerCase());
                this.lastCraftFaction = (IFaction)Registries.Factions.getValue(factionID);
            }
            this.lastCraftRecipeID = ResourceLocation.parse(lastCraft.getString("lastCraftRecipeID"));
            for (ItemStack stack : this.lastCraftItems) {
                Optional<ItemStack> existing = this.lastCraftItemsMerged.stream().filter(is -> ItemStack.matches((ItemStack)is, (ItemStack)stack)).findFirst();
                if (existing.isPresent()) {
                    existing.get().setCount(existing.get().getCount() + stack.getCount());
                    continue;
                }
                this.lastCraftItemsMerged.add(stack.copy());
            }
        }
    }

    public void spawnCraftingParticles() {
        if (!this.getLevel().isClientSide()) {
            return;
        }
        Random rnd = new Random();
        BlockPos pos = this.getBlockPos();
        Vector3f srcPoint = new Vector3f((float)pos.getX() + 0.5f, (float)pos.getY() + 0.85f, (float)pos.getZ() + 0.5f);
        for (int i = 0; i < 10; ++i) {
            Vector3f lightPoint = new Vector3f(srcPoint.x() - 0.2f + rnd.nextFloat() * 0.4f, srcPoint.y(), srcPoint.z() - 0.2f + rnd.nextFloat() * 0.4f);
            this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHT_VELOCITY.get())).setScale(0.1f), (double)lightPoint.x(), (double)lightPoint.y(), (double)lightPoint.z(), 0.0, 0.01, 0.0);
        }
        float SPEED = 0.1f;
        float RADIUS = 0.25f;
        int i = 0;
        while ((double)i < (double)this.craftTicks * 1.5) {
            this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_SPHERE_ORBIT.get())), (double)srcPoint.x(), (double)(srcPoint.y() + 0.4f), (double)srcPoint.z(), (double)SPEED, (double)rnd.nextInt(360), (double)RADIUS);
            ++i;
        }
        if (this.getCraftTicks() > this.getMaxCraftTicks() - 20) {
            RADIUS = 0.6f - rnd.nextFloat() * 0.25f;
            for (i = 0; i < 100; ++i) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_SPHERE_ORBIT.get())), (double)srcPoint.x(), (double)(srcPoint.y() + 0.2f), (double)srcPoint.z(), (double)SPEED + rnd.nextGaussian() * (double)0.1f, (double)(-30 + rnd.nextInt(30)), (double)RADIUS);
            }
        }
    }

    private void resolvePendingPatterns() {
        if (this.level != null && this.pendingPatterns.size() > 0) {
            for (String s : this.pendingPatterns) {
                ManaweavingPattern p = ManaweavingPatternHelper.GetManaweavingRecipe(this.getLevel(), ResourceLocation.parse(s));
                if (p == null) continue;
                this.addedPatterns.add(p);
            }
            this.pendingPatterns.clear();
        }
    }

    private void setCrafting(boolean crafting) {
        this.crafting = crafting;
        if (!this.getLevel().isClientSide() && this.crafting) {
            this.getLevel().playSound(null, this.getBlockPos(), SFX.Event.Block.MANAWEAVE_ALTAR_CRAFT, SoundSource.BLOCKS, 1.0f, (float)((double)0.95f + Math.random() * (double)0.05f));
        }
    }

    public boolean isCrafting() {
        return this.crafting;
    }

    public int getCraftTicks() {
        return this.craftTicks;
    }

    public int getMaxCraftTicks() {
        return 85;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ManaweavingAltarTile tile) {
        if (!tile.crafting) {
            return;
        }
        tile.spawnCraftingParticles();
        ++tile.craftTicks;
        if (tile.craftTicks >= tile.getMaxCraftTicks()) {
            tile.setCrafting(false);
            tile.craftTicks = 0;
            if (!tile.getLevel().isClientSide()) {
                if (!EventDispatcher.DispatchManaweaveCrafting(tile.__cachedRecipe, tile.__cachedRecipeOutput, tile.getCrafter())) {
                    level.sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), 2);
                    return;
                }
                if (tile.copyNBT) {
                    CompoundTag nbt = tile.__cachedRecipeOutput.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    for (ItemStack stack : tile.inventoryItems) {
                        if (!stack.has(DataComponents.CUSTOM_DATA)) continue;
                        nbt.merge(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                    }
                }
                tile.copyCraftInputToLastCraft();
                ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)tile.getLevel().dimension(), 32.0f, (float)pos.getX() + 0.5f, (float)pos.getY() + 1.3f, (float)pos.getZ() + 0.5f, SpawnParticleEffectMessage.ParticleTypes.MANAWEAVE_CRAFT_COMPLETE);
            }
            tile.pendingPatterns.clear();
            tile.addedPatterns.clear();
            tile.clearContent();
            Player crafter = tile.getCrafter();
            if (crafter != null & crafter instanceof ServerPlayer) {
                MutableBoolean factionMatch = new MutableBoolean(false);
                MNACap.get(crafter, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                    if (p.getAlliedFaction() != null && tile.__cachedRecipe.getFactionRequirement() == p.getAlliedFaction()) {
                        factionMatch.setTrue();
                    }
                });
                CustomAdvancementTriggers.MANAWEAVE_CRAFT.trigger((ServerPlayer)crafter, tile.__cachedRecipeOutput, factionMatch.booleanValue());
            }
            if (tile.__cachedRecipe != null) {
                tile.lastCraftRecipeID = tile.__cachedRecipe.getId();
                tile._lastCraftRecipe = tile.__cachedRecipe;
                int count = 1;
                ArrayList<ItemStack> byproducts = tile.__cachedRecipe.rollByproducts(level.getRandom());
                for (ItemStack byproduct : byproducts) {
                    if (count++ < 9) {
                        tile.setItem(count, byproduct);
                        continue;
                    }
                    InventoryUtilities.DropItemAt(byproduct, Vec3.atCenterOf((Vec3i)pos.above()), level, true);
                }
            }
            tile.setOutputItem(tile.__cachedRecipeOutput);
        }
    }

    private void setOutputItem(ItemStack stack) {
        if (!this.getLevel().isClientSide()) {
            this.ignoreQtyLimit = true;
            if (stack != null) {
                this.setItem(0, stack);
            }
            this.ignoreQtyLimit = false;
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    @Override
    public void setPlayerReference(Player player) {
        this.__crafter = player;
        this.__crafterID = player.getUUID();
    }

    private boolean checkReCraftReagents(Player player) {
        if (player.isCreative()) {
            return true;
        }
        MutableBoolean progressionMet = new MutableBoolean(false);
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getTier() < this.lastCraftTier) {
                player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.lowtier"));
            } else if (this.lastCraftFaction != null && p.getAlliedFaction() != this.lastCraftFaction) {
                player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.wrongfaction"));
            } else {
                progressionMet.setTrue();
            }
        });
        if (!progressionMet.booleanValue()) {
            return false;
        }
        float mana = this.getReCraftManaCost();
        MutableBoolean enoughMana = new MutableBoolean(true);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> enoughMana.setValue(m.getCastingResource().hasEnoughAbsolute((LivingEntity)player, mana)));
        if (!enoughMana.getValue().booleanValue()) {
            player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.missing_mana"));
            return false;
        }
        MutableBoolean reagentsPresent = new MutableBoolean(true);
        this.lastCraftItemsMerged.forEach(stack -> {
            if (!stack.isEmpty() && !InventoryUtilities.hasStackInInventory(stack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory()))) {
                reagentsPresent.setFalse();
            }
        });
        if (!reagentsPresent.getValue().booleanValue()) {
            player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.missing_items"));
            return false;
        }
        return true;
    }

    private void consumeReCraftReagents(Player player) {
        if (player.isCreative()) {
            return;
        }
        float mana = this.getReCraftManaCost();
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().consume((LivingEntity)player, mana));
        this.lastCraftItemsMerged.forEach(stack -> InventoryUtilities.removeItemFromInventory(stack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory())));
    }

    private void copyCraftInputToLastCraft() {
        this.lastCraftItems.clear();
        this.lastCraftItemsMerged.clear();
        for (ItemStack stack : this.inventoryItems) {
            this.lastCraftItems.add(stack.copy());
            Optional<ItemStack> existing = this.lastCraftItemsMerged.stream().filter(is -> ItemStack.isSameItemSameComponents((ItemStack)is, (ItemStack)stack)).findFirst();
            if (existing.isPresent()) {
                existing.get().setCount(existing.get().getCount() + stack.getCount());
                continue;
            }
            this.lastCraftItemsMerged.add(stack.copy());
        }
        this.lastCraftPatternCount = this.addedPatterns.size();
        this.lastCraftOutput = this.__cachedRecipeOutput.copy();
    }

    public boolean reCraft(Player player) {
        if (this.addedPatterns.size() != 0 || !this.isEmpty() || this.getLevel().isClientSide()) {
            return false;
        }
        if (this.checkReCraftReagents(player)) {
            ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.getLevel().dimension(), 32.0f, (float)this.getBlockPos().getX() + 0.5f, (float)this.getBlockPos().getY() + 1.1f, (float)this.getBlockPos().getZ() + 0.5f, SpawnParticleEffectMessage.ParticleTypes.MANAWEAVE_CRAFT_COMPLETE);
            this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, (float)(0.9 + Math.random() * 0.2));
            this.setPlayerReference(player);
            this.consumeReCraftReagents(player);
            ItemStack output = this.lastCraftOutput.copy();
            if (player instanceof ServerPlayer) {
                MutableBoolean factionMatch = new MutableBoolean(false);
                MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                    if (p.getAlliedFaction() != null && this.__cachedRecipe != null && this.__cachedRecipe.getFactionRequirement() == p.getAlliedFaction()) {
                        factionMatch.setTrue();
                    }
                });
                CustomAdvancementTriggers.MANAWEAVE_CRAFT.trigger((ServerPlayer)player, output, factionMatch.booleanValue());
            }
            boolean sendUpdate = false;
            if (!player.getInventory().add(output)) {
                this.setOutputItem(output);
                sendUpdate = true;
            }
            if (this._lastCraftRecipe == null && this.lastCraftRecipeID != null) {
                this.level.getRecipeManager().byKey(this.lastCraftRecipeID).ifPresent(recipe -> {
                    if (recipe.value() instanceof ManaweavingRecipe) {
                        ManaweavingRecipe mwRecipe;
                        this._lastCraftRecipe = mwRecipe = (ManaweavingRecipe)recipe.value();
                    }
                });
            }
            if (this._lastCraftRecipe != null) {
                int count = 1;
                ArrayList<ItemStack> byproducts = this._lastCraftRecipe.rollByproducts(this.level.getRandom());
                for (ItemStack byproduct : byproducts) {
                    if (player.getInventory().add(byproduct)) continue;
                    if (count++ < 9) {
                        this.setItem(count, byproduct);
                        sendUpdate = true;
                        continue;
                    }
                    InventoryUtilities.DropItemAt(byproduct, Vec3.atCenterOf((Vec3i)this.getBlockPos().above()), this.level, true);
                }
            }
            if (sendUpdate) {
                this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
            }
            return true;
        }
        return false;
    }

    public ItemStack getReCraftOutput() {
        return this.lastCraftOutput;
    }

    @Nullable
    public IFaction getLastCraftFaction() {
        return this.lastCraftFaction;
    }

    public int getLastCraftTier() {
        return this.lastCraftTier;
    }

    public int getLastCraftPatternCount() {
        return this.lastCraftPatternCount;
    }

    public void setReCraftRecipe(ItemStack output, List<ItemStack> inputs, IFaction faction, int tier, int patternCount) {
        this.lastCraftOutput = output;
        this.lastCraftItems.clear();
        this.lastCraftItemsMerged.clear();
        for (ItemStack stack : inputs) {
            this.lastCraftItems.add(stack.copy());
            Optional<ItemStack> existing = this.lastCraftItemsMerged.stream().filter(is -> ItemStack.isSameItemSameComponents((ItemStack)is, (ItemStack)stack)).findFirst();
            if (existing.isPresent()) {
                existing.get().setCount(existing.get().getCount() + stack.getCount());
                continue;
            }
            this.lastCraftItemsMerged.add(stack.copy());
        }
        this.lastCraftFaction = faction;
        this.lastCraftTier = tier;
        this.lastCraftPatternCount = patternCount;
    }

    public int getReCraftManaCost() {
        return this.lastCraftPatternCount * GeneralConfigValues.AverageManaweaveCost;
    }

    public List<ItemStack> getReCraftInput() {
        return this.lastCraftItems.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
    }

    private Player getCrafter() {
        if (this.__crafter == null && this.__crafterID != null) {
            this.__crafter = this.getLevel().getPlayerByUUID(this.__crafterID);
        }
        return this.__crafter;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        return this.getCrafter() != null && this.getItem(index).isEmpty() && itemStackIn.getCount() == 1;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return !this.isCrafting() && this.getCrafter() != null && !this.getItem(index).isEmpty() && stack.getCount() == 1;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    }

    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().expandTowards(0.0, 1.0, 0.0);
    }

    private ItemAndPatternCraftingInventory createCraftingInventory() {
        ArrayList<String> patterns = new ArrayList<String>();
        for (IManaweavePattern p : this.addedPatterns) {
            patterns.add(p.getRegistryId().toString());
        }
        for (String s : this.pendingPatterns) {
            patterns.add(s);
        }
        ItemAndPatternCraftingInventory craftinginventory = new ItemAndPatternCraftingInventory(9, patterns);
        for (int i = 0; i < 9; ++i) {
            craftinginventory.setItem(i, this.getItem(i));
        }
        return craftinginventory;
    }

    private IItemHandlerModifiable createHandler() {
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof ChestBlock)) {
            return new InvWrapper((Container)this);
        }
        Container inv = ChestBlock.getContainer((ChestBlock)((ChestBlock)state.getBlock()), (BlockState)state, (Level)this.getLevel(), (BlockPos)this.getBlockPos(), (boolean)true);
        return new InvWrapper((Container)(inv == null ? this : inv));
    }

    public boolean canPlaceItem(int index, ItemStack stack) {
        if (this.getCrafter() != null) {
            return super.canPlaceItem(index, stack);
        }
        return false;
    }

    @Override
    public ItemStack getItem(int index) {
        return (ItemStack)this.inventoryItems.get(index);
    }
}


