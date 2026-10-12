 
package com.mna.items.ritual;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.items.ITieredItem;
import com.mna.api.rituals.IRitualReagent;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.Ritual;
import com.mna.gui.containers.providers.NamedRitualKit;
import com.mna.inventory.InventoryRitualKit;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.base.IRadialMenuItem;
import com.mna.items.base.ItemBagBase;
import com.mna.items.filters.ItemFilterGroup;
import com.mna.items.manaweaving.ItemManaweaveBottle;
import com.mna.items.ritual.PractitionersPatch;
import com.mna.items.ritual.PractitionersPouchPatches;
import com.mna.items.ritual.PurifiedVinteumDust;
import com.mna.items.ritual.WizardChalk;
import com.mna.items.runes.ItemRuneMarking;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.rituals.RitualRecipe;
import com.mna.rituals.MatchedRitual;
import com.mna.rituals.contexts.RitualCheckContext;
import com.mna.tools.InventoryUtilities;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import com.mna.api.tools.MNADyeableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNAItemData;
import com.mna.api.tools.MNACaps;

public class PractitionersPouch
extends ItemBagBase
implements IRadialMenuItem,
ITieredItem<PractitionersPouch>,
MNADyeableItem {
    private static final String TAG_MASTER = "ritual_kit_data";
    private static final String KEY_INDEX = "ritual_kit_index";
    private static final String TAG_RITUAL_RLOC = "ritual_rloc";
    private static final String TAG_RITUAL_ROTATION = "ritual_rotation";
    private static final String TAG_RITUAL_POSITION_COUNT = "ritual_position_count";
    private static final String TAG_RITUAL_POSITION_PREFIX = "position_";
    private static final String TAG_RITUAL_REAGENT_COUNT = "ritual_reagent_count";
    private static final String TAG_RITUAL_REAGENT_PREFIX = "reagent_";
    private static final String TAG_PATCHES = "patches";
    public static final int MAX_RITUALS = 8;
    public static final int MAX_PATCHES = 4;
    private int tier;
    public static String NBT_ID = "mna:ritual_bag_data";

    private boolean storeRitual(MatchedRitual match, Level world, ItemStack stack) {
        int i;
        CompoundTag compound = new CompoundTag();
        compound.putString(TAG_RITUAL_RLOC, match.getRitual().getId().toString());
        ArrayList<ItemStack> reagents = new ArrayList<ItemStack>();
        ArrayList<BlockPos> reagent_positions = new ArrayList<BlockPos>();
        ArrayList<BlockPos> rune_positions = new ArrayList<BlockPos>();
        for (RitualBlockPos rbp : match.getPositions()) {
            BlockPos pos = rbp.getBlockPos().subtract((Vec3i)match.getCenter());
            rune_positions.add(pos);
            IRitualReagent reagent = rbp.getReagent();
            if (reagent == null || reagent.isEmpty()) continue;
            ChalkRuneTile tecr = (ChalkRuneTile)world.getBlockEntity(match.getCenter().offset(pos.getX(), 0, pos.getZ()));
            if (tecr == null) {
                return false;
            }
            ItemStack tecrStack = tecr.getItem(0);
            if (tecrStack.isEmpty()) {
                if (reagent.isOptional()) continue;
                return false;
            }
            ItemStack tecrCopy = tecrStack.copy();
            MNAItemData.element(tecrCopy, "ritual_tags").putBoolean("noConsumeReagent", !reagent.shouldConsumeReagent());
            reagents.add(tecrCopy);
            reagent_positions.add(new BlockPos(pos.getX(), 0, pos.getZ()));
        }
        compound.putInt(TAG_RITUAL_POSITION_COUNT, rune_positions.size());
        for (i = 0; i < rune_positions.size(); ++i) {
            CompoundTag positionData = new CompoundTag();
            positionData.putInt("x", ((BlockPos)rune_positions.get(i)).getX());
            positionData.putInt("y", ((BlockPos)rune_positions.get(i)).getY());
            positionData.putInt("z", ((BlockPos)rune_positions.get(i)).getZ());
            compound.put(TAG_RITUAL_POSITION_PREFIX + i, (Tag)positionData);
        }
        compound.putInt(TAG_RITUAL_REAGENT_COUNT, reagents.size());
        for (i = 0; i < reagents.size(); ++i) {
            CompoundTag reagentData = new CompoundTag();
            reagentData.putInt("x", ((BlockPos)reagent_positions.get(i)).getX());
            reagentData.putInt("y", ((BlockPos)reagent_positions.get(i)).getY());
            reagentData.putInt("z", ((BlockPos)reagent_positions.get(i)).getZ());
            com.mna.api.tools.MNAItemData.save((ItemStack)reagents.get(i), reagentData);
            compound.put(TAG_RITUAL_REAGENT_PREFIX + i, (Tag)reagentData);
        }
        Component baseDisplayName = new ItemStack((ItemLike)stack.getItem()).getHoverName();
        MutableComponent stc = Component.literal((String)"").append(baseDisplayName).append(": ");
        stc = stc.append((Component)Component.translatable((String)match.getRitual().getId().toString())).withStyle(ChatFormatting.GOLD);
        stack.set(DataComponents.ITEM_NAME, (Component)stc);
        CompoundTag mainCompound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = null;
        list = mainCompound.contains(TAG_MASTER, 9) ? mainCompound.getList(TAG_MASTER, 10) : new ListTag();
        int index = PractitionersPouch.getIndex(stack);
        while (list.size() < 8) {
            list.add(new CompoundTag());
        }
        list.set(index, (Tag)compound);
        mainCompound.put(TAG_MASTER, (Tag)list);
        return true;
    }

    private boolean placeRitual(ItemStack stack, BlockPos center, Level world, MatchedRitual inWorld) {
        ArrayList<ItemStack> reagents = this.getReagents(stack);
        List<BlockPos> positions = this.getReagentLocations(stack);
        List<BlockPos> rune_positions = this.getRuneLocations(stack);
        if (reagents.size() != positions.size()) {
            return false;
        }
        if (inWorld != null) {
            Direction inWorldRotation = inWorld.getRotation();
            Direction storedRotation = this.getStoredRitualRotation(stack, world);
            if (inWorldRotation == null || storedRotation == null) {
                return false;
            }
            positions = this.rotateFromTo(positions, storedRotation, inWorldRotation);
            rune_positions = this.rotateFromTo(rune_positions, storedRotation, inWorldRotation);
        }
        return this._placeRitual(stack, center, world, reagents, positions, rune_positions, inWorld == null);
    }

    private boolean _placeRitual(ItemStack stack, BlockPos center, Level world, List<ItemStack> reagents, List<BlockPos> positions, List<BlockPos> rune_positions, boolean offset) {
        for (BlockPos runePos : rune_positions) {
            BlockPlaceContext bpc;
            BlockPos pos = center.offset(runePos.getX(), offset ? 1 : 0, runePos.getZ());
            BlockState existing = world.getBlockState(pos);
            if (existing.canBeReplaced(bpc = new BlockPlaceContext(world, null, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf((Vec3i)pos), Direction.UP, pos, true)))) {
                world.setBlockAndUpdate(pos, (BlockState)((BlockState)((BlockState)((Block)BlockInit.CHALK_RUNE.get()).defaultBlockState().setValue(ChalkRuneBlock.RUNEINDEX, Integer.valueOf((int)Math.floor(Math.random() * (double)(ChalkRuneBlock.RUNEINDEX.getPossibleValues().size() - 1))))).setValue(ChalkRuneBlock.METAL, Boolean.valueOf(false))).setValue(ChalkRuneBlock.ACTIVATED, Boolean.valueOf(true)));
            } else if (existing.getBlock() == BlockInit.CHALK_RUNE.get()) {
                world.setBlockAndUpdate(pos, (BlockState)existing.setValue(ChalkRuneBlock.ACTIVATED, Boolean.valueOf(true)));
            } else {
                return false;
            }
            ChalkRuneTile rune = (ChalkRuneTile)world.getBlockEntity(pos);
            if (rune == null) {
                return false;
            }
            int count = 0;
            for (BlockPos reagent_pos : positions) {
                if (reagent_pos.getX() == runePos.getX() && reagent_pos.getZ() == runePos.getZ()) {
                    if (reagents.get(count).isEmpty()) break;
                    boolean ghost = false;
                    ItemStack runeStack = reagents.get(count).copy();
                    if (MNAItemData.element(runeStack, "ritual_tags").getBoolean("noConsumeReagent")) {
                        ghost = true;
                    }
                    com.mna.api.tools.MNAItemData.remove(runeStack, "ritual_tags");
                    rune.setReadOnly(false);
                    rune.setGhostItem(false);
                    rune.setItem(0, runeStack.copy());
                    rune.setGhostItem(ghost);
                    break;
                }
                ++count;
            }
            rune.setReadOnly(true);
        }
        return true;
    }

    @Nullable
    protected Entity activateRitual(Player caster, ItemStack stack, RitualRecipe ritual, BlockPos center, Level world) {
        Pair<Direction, NonNullList<RitualBlockPos>> matched = ritual.matchInWorld(center, world);
        if (matched == null) {
            return null;
        }
        IPlayerProgression progression = (IPlayerProgression)caster.getData(PlayerProgressionProvider.PROGRESSION.get());
        float speed = 1.0f - 0.2f * (float)(progression.getTier() - ritual.getTier());
        int numSpeedPatches = this.getPatchLevel(stack, PractitionersPouchPatches.SPEED);
        float speedReduction = 0.3f * (float)Math.min(numSpeedPatches, 3);
        speed -= speed * speedReduction;
        InventoryRitualKit inv = new InventoryRitualKit(stack);
        Ritual ritualEntity = new Ritual((EntityType<? extends Ritual>)((EntityType)EntityInit.RITUAL_ENTITY.get()), world);
        ritualEntity.setPos((float)center.getX() + 0.5f, center.getY(), (float)center.getZ() + 0.5f);
        ritualEntity.setRitualBlockLocations((NonNullList<RitualBlockPos>)((NonNullList)matched.getSecond()));
        ritualEntity.setSpeed(speed);
        ritualEntity.setRitualName(ritual.getId());
        ritualEntity.setForceConsumeReagents(true);
        ritualEntity.setCasterUUID(caster.getUUID());
        if (this.getPatchLevel(stack, PractitionersPouchPatches.WEAVE) > 0) {
            ArrayList<Pair<IItemHandler, Direction>> inventories = new ArrayList<Pair<IItemHandler, Direction>>();
            Pair<IItemHandler, Direction> remoteInv = this.resolveRemoteInventory(stack, caster.level());
            if (remoteInv != null) {
                inventories.add(remoteInv);
            }
            if (this.getPatchLevel(stack, PractitionersPouchPatches.RIFT) > 0) {
                MNACap.get(caster, PlayerMagicProvider.MAGIC).ifPresent(m -> inventories.add(new Pair(new InvWrapper((Container)m.getRiftInventory()), Direction.UP)));
            }
            inventories.add(new Pair(inv, Direction.UP));
            ritualEntity.preConsumePatterns(this.consumePatternItems(inventories, ritual));
        }
        if (ritualEntity.confirmRitualReagents()) {
            world.addFreshEntity((Entity)ritualEntity);
        }
        return ritualEntity;
    }

    private ArrayList<ItemStack> getReagents(ItemStack stack) {
        ArrayList<ItemStack> out = new ArrayList<ItemStack>();
        CompoundTag nbt = PractitionersPouch.getCurrentCompound(stack, PractitionersPouch.getIndex(stack));
        if (nbt == null || !nbt.contains(TAG_RITUAL_REAGENT_COUNT)) {
            return out;
        }
        int count = nbt.getInt(TAG_RITUAL_REAGENT_COUNT);
        for (int i = 0; i < count; ++i) {
            if (!nbt.contains(TAG_RITUAL_REAGENT_PREFIX + i)) {
                out.clear();
                return out;
            }
            CompoundTag reagent_data = nbt.getCompound(TAG_RITUAL_REAGENT_PREFIX + i);
            ItemStack reagent = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)reagent_data);
            if (reagent == null) {
                out.clear();
                return out;
            }
            out.add(reagent);
        }
        return out;
    }

    private ArrayList<BlockPos> getReagentLocations(ItemStack stack) {
        ArrayList<BlockPos> out = new ArrayList<BlockPos>();
        CompoundTag nbt = PractitionersPouch.getCurrentCompound(stack, PractitionersPouch.getIndex(stack));
        if (nbt == null || !nbt.contains(TAG_RITUAL_REAGENT_COUNT)) {
            return out;
        }
        int count = nbt.getInt(TAG_RITUAL_REAGENT_COUNT);
        for (int i = 0; i < count; ++i) {
            if (!nbt.contains(TAG_RITUAL_REAGENT_PREFIX + i)) {
                out.clear();
                return out;
            }
            CompoundTag reagent_data = nbt.getCompound(TAG_RITUAL_REAGENT_PREFIX + i);
            if (!(reagent_data.contains("x") && reagent_data.contains("y") && reagent_data.contains("z"))) {
                out.clear();
                return out;
            }
            out.add(new BlockPos(reagent_data.getInt("x"), reagent_data.getInt("y"), reagent_data.getInt("z")));
        }
        return out;
    }

    private ArrayList<BlockPos> getRuneLocations(ItemStack stack) {
        ArrayList<BlockPos> out = new ArrayList<BlockPos>();
        CompoundTag nbt = PractitionersPouch.getCurrentCompound(stack, PractitionersPouch.getIndex(stack));
        if (nbt == null || !nbt.contains(TAG_RITUAL_POSITION_COUNT)) {
            return out;
        }
        int count = nbt.getInt(TAG_RITUAL_POSITION_COUNT);
        for (int i = 0; i < count; ++i) {
            if (!nbt.contains(TAG_RITUAL_POSITION_PREFIX + i)) {
                out.clear();
                return out;
            }
            CompoundTag position_data = nbt.getCompound(TAG_RITUAL_POSITION_PREFIX + i);
            if (!(position_data.contains("x") && position_data.contains("y") && position_data.contains("z"))) {
                out.clear();
                return out;
            }
            out.add(new BlockPos(position_data.getInt("x"), position_data.getInt("y"), position_data.getInt("z")));
        }
        return out;
    }

    private Direction getStoredRitualRotation(ItemStack stack, Level world) {
        int idx = PractitionersPouch.getIndex(stack);
        CompoundTag nbt = PractitionersPouch.getCurrentCompound(stack, idx);
        if (nbt == null) {
            return Direction.NORTH;
        }
        if (nbt.contains(TAG_RITUAL_ROTATION)) {
            return Direction.values()[nbt.getInt(TAG_RITUAL_ROTATION)];
        }
        RitualRecipe recipe = PractitionersPouch.getRitual(world, stack);
        ArrayList<BlockPos> runeLocs = this.getRuneLocations(stack);
        if (recipe == null || runeLocs == null) {
            return Direction.NORTH;
        }
        Direction calculatedRotation = recipe.getMatchedDirection(runeLocs);
        if (calculatedRotation == null) {
            calculatedRotation = Direction.NORTH;
        }
        nbt.putInt(TAG_RITUAL_ROTATION, calculatedRotation.ordinal());
        return calculatedRotation;
    }

    private List<BlockPos> rotateFromTo(List<BlockPos> original, Direction from, Direction to) {
        if (from == to) {
            return original;
        }
        return original.stream().map(bp -> this.rotateBlockPos((BlockPos)bp, from, to)).collect(Collectors.toList());
    }

    private BlockPos rotateBlockPos(BlockPos pos, Direction from, Direction to) {
        BlockPos output = pos;
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; ++i) {
            output = new BlockPos(-output.getZ(), output.getY(), output.getX());
        }
        return output;
    }

    private boolean consumeItem(ItemStack stack, InventoryRitualKit inventory, Container riftInv, IItemHandler remoteInv, Direction remoteFace, boolean save) {
        boolean removed = false;
        if (!removed && riftInv != null) {
            removed = InventoryUtilities.removeItemFromInventory(stack, true, true, (IItemHandler)new InvWrapper(riftInv));
        }
        if (!removed && remoteInv != null) {
            removed = InventoryUtilities.removeItemFromInventory(stack, true, true, remoteInv, remoteFace);
        }
        if (!removed) {
            removed = InventoryUtilities.removeItemFromInventory(stack, true, true, (IItemHandler)inventory);
        }
        if (save) {
            inventory.writeItemStack();
        }
        return removed;
    }

    private boolean containsItem(ItemStack stack, InventoryRitualKit inventory, Container riftInv, IItemHandler remoteInv, Direction remoteFace) {
        if (riftInv != null && InventoryUtilities.hasStackInInventory(stack, true, true, (IItemHandler)new InvWrapper(riftInv))) {
            return true;
        }
        if (remoteInv != null && InventoryUtilities.hasStackInInventory(stack, true, true, remoteInv, remoteFace)) {
            return true;
        }
        return InventoryUtilities.hasStackInInventory(stack, true, true, (IItemHandler)inventory);
    }

    private boolean ritualReagentsPresent(ItemStack stack, Container riftInv, IItemHandler remoteInv, Direction remoteFace) {
        InventoryRitualKit inventory = new InventoryRitualKit(stack);
        ArrayList<ItemStack> reagents = this.getReagents(stack);
        ArrayList<ItemStack> combinedReagents = new ArrayList<ItemStack>();
        for (ItemStack reagent : reagents) {
            boolean found = false;
            for (ItemStack listStack : combinedReagents) {
                if (listStack.getItem() != reagent.getItem()) continue;
                listStack.setCount(listStack.getCount() + 1);
                found = true;
                break;
            }
            if (found) continue;
            combinedReagents.add(reagent.copy());
        }
        for (ItemStack reagent : combinedReagents) {
            int totalCount = 0;
            ItemStack searchCopy = reagent.copy();
            if (searchCopy.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
                searchCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove("ritual_tags");
                if (searchCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getAllKeys().size() == 0) {
                    searchCopy.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                }
            }
            if (riftInv != null) {
                totalCount += InventoryUtilities.countItem(searchCopy, (IItemHandler)new InvWrapper(riftInv), remoteFace, true, true);
            }
            if (remoteInv != null) {
                totalCount += InventoryUtilities.countItem(searchCopy, remoteInv, remoteFace, true, true);
            }
            if ((totalCount += InventoryUtilities.countItem(searchCopy, (IItemHandler)inventory, remoteFace, true, true)) >= searchCopy.getCount()) continue;
            return false;
        }
        return true;
    }

    private boolean chalkPresent(ItemStack stack, Container riftInv, IItemHandler remoteInv, Direction remoteFace, int requiredUses) {
        ItemStack invStack;
        int i;
        InventoryRitualKit inventory = new InventoryRitualKit(stack);
        int foundUses = 0;
        if (riftInv != null) {
            for (i = 0; i < riftInv.getContainerSize(); ++i) {
                invStack = riftInv.getItem(i);
                if (!(invStack.getItem() instanceof WizardChalk)) continue;
                foundUses += invStack.getMaxDamage() - invStack.getDamageValue();
            }
        }
        if (remoteInv != null) {
            for (i = 0; i < remoteInv.getSlots(); ++i) {
                invStack = remoteInv.extractItem(i, 1, true);
                if (!(invStack.getItem() instanceof WizardChalk)) continue;
                foundUses += invStack.getMaxDamage() - invStack.getDamageValue();
            }
        }
        for (i = 0; i < inventory.getSlots(); ++i) {
            invStack = inventory.getStackInSlot(i);
            if (!(invStack.getItem() instanceof WizardChalk)) continue;
            foundUses += invStack.getMaxDamage() - invStack.getDamageValue();
        }
        return foundUses >= requiredUses;
    }

    private boolean ritualCatalystPresent(ItemStack pouchStack, Container riftInv, IItemHandler remoteInv, Direction remoteFace) {
        InventoryRitualKit inventory = new InventoryRitualKit(pouchStack);
        List<ItemStack> stacks = MATags.getItemTagContents(MATags.Items.Dusts.PURIFIED_VINTEUM).stream().map(i -> new ItemStack((ItemLike)i)).toList();
        for (ItemStack checkStack : stacks) {
            if (!this.containsItem(checkStack, inventory, riftInv, remoteInv, remoteFace)) continue;
            return true;
        }
        return false;
    }

    private boolean consumeChalk(ItemStack stack, IItemHandlerModifiable riftInv, IItemHandler remoteInv, Direction remoteFace, int amount) {
        InventoryRitualKit inventory = new InventoryRitualKit(stack);
        int remaining = amount;
        if (remaining > 0 && riftInv != null) {
            remaining = this.consumeChalkFromInventory(riftInv, remaining);
        }
        if (remaining > 0 && remoteInv != null) {
            remaining = this.consumeChalkFromInventory(remoteInv, remoteFace, remaining);
        }
        if (remaining > 0 && inventory != null) {
            remaining = this.consumeChalkFromInventory(inventory, remaining);
            inventory.writeItemStack();
        }
        return remaining == 0;
    }

    private List<ResourceLocation> consumePatternItems(List<Pair<IItemHandler, Direction>> inventories, RitualRecipe recipe) {
        String[] requiredPatterns = recipe.getManaweavePatterns();
        ArrayList<ResourceLocation> consumedPatterns = new ArrayList<ResourceLocation>();
        block0: for (String pattern : requiredPatterns) {
            ResourceLocation rLoc = ResourceLocation.parse(pattern);
            for (int i = 0; i < inventories.size(); ++i) {
                IItemHandler inventory = (IItemHandler)inventories.get(i).getFirst();
                if (inventory == null || !this.consumePatternJar(inventory, rLoc)) continue;
                consumedPatterns.add(rLoc);
                continue block0;
            }
        }
        return consumedPatterns;
    }

    public boolean consumePatternJar(IItemHandler inventory, ResourceLocation rLoc) {
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ManaweavingPattern pattern;
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !ItemManaweaveBottle.hasPattern(stack) || !(pattern = ItemManaweaveBottle.getPattern(stack)).getId().equals(rLoc) || inventory.extractItem(i, 1, false).isEmpty()) continue;
            if (inventory instanceof ItemInventoryBase) {
                ((ItemInventoryBase)inventory).writeItemStack();
            }
            return true;
        }
        return false;
    }

    private int consumeChalkFromInventory(IItemHandlerModifiable inventory, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack inventoryStack = inventory.getStackInSlot(i);
            if (!(inventoryStack.getItem() instanceof WizardChalk)) continue;
            int uses = inventoryStack.getMaxDamage() - inventoryStack.getDamageValue();
            if (uses > remaining) {
                inventoryStack.setDamageValue(inventoryStack.getDamageValue() + remaining);
                inventory.setStackInSlot(i, inventoryStack);
                return 0;
            }
            if (uses == remaining) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                return 0;
            }
            remaining -= uses;
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        return remaining;
    }

    private int consumeChalkFromInventory(IItemHandler handler, Direction remoteFace, int amount) {
        int remaining = amount;
        for (int i = 0; i < handler.getSlots(); ++i) {
            ItemStack inventoryStack = handler.extractItem(i, 1, true);
            if (!(inventoryStack.getItem() instanceof WizardChalk)) continue;
            int uses = inventoryStack.getMaxDamage() - inventoryStack.getDamageValue();
            if (uses > remaining) {
                inventoryStack = handler.extractItem(i, 1, false);
                inventoryStack.setDamageValue(inventoryStack.getDamageValue() + remaining);
                inventoryStack = handler.insertItem(i, inventoryStack, false);
                if (!inventoryStack.isEmpty()) break;
                return 0;
            }
            if (uses == remaining) {
                handler.extractItem(i, 1, false);
                return 0;
            }
            remaining -= uses;
            handler.extractItem(i, 1, false);
        }
        return remaining;
    }

    private boolean consumeRitualReagents(RitualRecipe recipe, Container riftInv, IItemHandler remoteInv, Direction remoteFace, ItemStack stack) {
        InventoryRitualKit inventory = new InventoryRitualKit(stack);
        ArrayList<ItemStack> reagents = this.getReagents(stack);
        for (ItemStack reagent : reagents) {
            ItemStack searchCopy = reagent.copy();
            if (searchCopy.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
                if (MNAItemData.element(searchCopy, "ritual_tags").getBoolean("noConsumeReagent")) continue;
                searchCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove("ritual_tags");
                if (searchCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().isEmpty()) {
                    searchCopy.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                }
            }
            this.consumeItem(searchCopy, inventory, riftInv, remoteInv, remoteFace, false);
        }
        this.consumeItem(new ItemStack((ItemLike)ItemInit.PURIFIED_VINTEUM_DUST.get()), inventory, riftInv, remoteInv, remoteFace, false);
        inventory.writeItemStack();
        return true;
    }

    public boolean hasRoomForItem(ItemStack bag, ItemStack stack) {
        InventoryRitualKit inv = new InventoryRitualKit(bag);
        return inv.canMergeItem(stack);
    }

    public ItemStack insertItem(ItemStack bag, ItemStack stack) {
        InventoryRitualKit inv = new InventoryRitualKit(bag);
        ItemStack resultantStack = inv.addItem(stack);
        inv.writeItemStack();
        return resultantStack;
    }

    public boolean shouldVoidItem(ItemStack bag, ItemStack stack) {
        InventoryRitualKit inv = new InventoryRitualKit(bag);
        return inv.shouldVoidItem(stack);
    }

    private CompoundTag getPatchTag(ItemStack stack) {
        return com.mna.api.tools.MNAItemData.getOrCreateTagElement(stack, TAG_PATCHES);
    }

    public boolean isPatchValid(ItemStack stack, PractitionersPouchPatches patch, int level) {
        int existingLevel = this.getPatchLevel(stack, patch);
        if (existingLevel >= level) {
            return false;
        }
        if (existingLevel < level) {
            return true;
        }
        return this.getAppliedPatches(stack).length < 4;
    }

    public boolean addPatch(ItemStack stack, PractitionersPouchPatches patch, int level) {
        if (this.isPatchValid(stack, patch, level)) {
            this.getPatchTag(stack).putInt(patch.name(), Math.min(level, patch.getLevels()));
            return true;
        }
        return false;
    }

    public int getPatchLevel(ItemStack stack, PractitionersPouchPatches patch) {
        CompoundTag patchTag = this.getPatchTag(stack);
        if (patchTag.contains(patch.name())) {
            return patchTag.getInt(patch.name());
        }
        return 0;
    }

    public PractitionersPouchPatches[] getAppliedPatches(ItemStack stack) {
        CompoundTag patchTag = this.getPatchTag(stack);
        List<PractitionersPouchPatches> patchList = patchTag.getAllKeys().stream().map(s -> {
            try {
                return PractitionersPouchPatches.valueOf(s);
            }
            catch (Exception ex) {
                return null;
            }
        }).filter(p -> p != null).collect(Collectors.toList());
        return (PractitionersPouchPatches[])((patchList).toArray(new PractitionersPouchPatches[0]));
    }

    public int countAppliedPatchesForLimit(ItemStack stack, ItemStack patchStack) {
        if (patchStack.getItem() instanceof PractitionersPatch && ((PractitionersPatch)patchStack.getItem()).getPatch() == PractitionersPouchPatches.SPEED) {
            return 0;
        }
        CompoundTag patchTag = this.getPatchTag(stack);
        List patchList = patchTag.getAllKeys().stream().map(s -> {
            try {
                return PractitionersPouchPatches.valueOf(s);
            }
            catch (Exception ex) {
                return null;
            }
        }).filter(p -> p != null && p != PractitionersPouchPatches.SPEED).collect(Collectors.toList());
        return patchList.size();
    }

    @Nullable
    public Pair<IItemHandler, Direction> resolveRemoteInventory(ItemStack stack, Level world) {
        InventoryRitualKit inv = new InventoryRitualKit(stack);
        ItemStack markStack = inv.getStackInSlot(21);
        if (markStack.isEmpty() || !(markStack.getItem() instanceof ItemRuneMarking)) {
            return new Pair(null, Direction.UP);
        }
        BlockPos pos = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getLocation(markStack);
        Direction face = ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getFace(markStack);
        if (pos == null) {
            return new Pair(null, Direction.UP);
        }
        if (!world.isLoaded(pos)) {
            return new Pair(null, Direction.UP);
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) {
            return new Pair(null, Direction.UP);
        }
        java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(te, face);
        if (!handler.isPresent()) {
            return new Pair(null, Direction.UP);
        }
        return new Pair((handler.get()), face);
    }

    @Nullable
    private static RitualRecipe getRitual(Level world, ItemStack stack) {
        ResourceLocation rLoc = ResourceLocation.parse(PractitionersPouch.getRitualRLoc(stack, PractitionersPouch.getIndex(stack)));
        return RitualRecipe.find(world, rLoc);
    }

    public static String getRitualRLoc(ItemStack stack, int index) {
        CompoundTag compound = PractitionersPouch.getCurrentCompound(stack, index);
        if (compound == null || !compound.contains(TAG_RITUAL_RLOC)) {
            return "";
        }
        return compound.getString(TAG_RITUAL_RLOC);
    }

    @Nullable
    private static CompoundTag getCurrentCompound(ItemStack stack, int index) {
        ListTag list;
        if (stack == null || !stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(TAG_MASTER)) {
            return null;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TAG_MASTER, 10)) {
            return tag.getCompound(TAG_MASTER);
        }
        if (tag.contains(TAG_MASTER, 9) && index < (list = (ListTag)tag.get(TAG_MASTER)).size()) {
            return list.getCompound(index);
        }
        return null;
    }

    public static void setIndex(ItemStack stack, int index) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_INDEX, index);
        String translationKey = PractitionersPouch.getRitualRLoc(stack, PractitionersPouch.getIndex(stack));
        if (translationKey != "") {
            Component baseDisplayName = new ItemStack((ItemLike)stack.getItem()).getHoverName();
            MutableComponent stc = Component.literal((String)"").append(baseDisplayName).append(": ");
            stc = stc.append((Component)Component.translatable((String)translationKey)).withStyle(ChatFormatting.GOLD);
            stack.set(DataComponents.ITEM_NAME, (Component)stc);
        } else {
            stack.remove(DataComponents.CUSTOM_NAME);
        }
    }

    public static int getIndex(ItemStack stack) {
        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_INDEX);
        }
        return 0;
    }

    public static boolean consumeItem(ItemStack bagStack, Container riftInv, IItemHandler remoteInv, Direction remoteFace, ItemStack consumeStack) {
        InventoryRitualKit inventory = new InventoryRitualKit(bagStack);
        return ((PractitionersPouch)ItemInit.PRACTITIONERS_POUCH.get()).consumeItem(consumeStack, inventory, riftInv, remoteInv, remoteFace, true);
    }

    public static boolean containsItem(ItemStack bagStack, Container riftInv, IItemHandler remoteInv, Direction remoteFace, ItemStack searchStack) {
        return ((PractitionersPouch)ItemInit.PRACTITIONERS_POUCH.get()).containsItem(searchStack, new InventoryRitualKit(bagStack), riftInv, remoteInv, remoteFace);
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        if (!world.isClientSide && hand == InteractionHand.MAIN_HAND) {
            this.openGuiIfModifierPressed(player.getItemInHand(hand), player, world);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public InteractionResult useOn(UseOnContext context) {
        Component cantStartReason;
        Level world = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        if (!world.isClientSide && context.getHand() == InteractionHand.MAIN_HAND && this.openGuiIfModifierPressed(context.getItemInHand(), context.getPlayer(), context.getLevel())) {
            return InteractionResult.SUCCESS;
        }
        IPlayerMagic magic = MNACap.get(context.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
        IPlayerProgression progression = MNACap.get(context.getPlayer(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (!magic.isMagicUnlocked()) {
            return InteractionResult.FAIL;
        }
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockState state = world.getBlockState(pos);
        MatchedRitual ritualMatch = null;
        RitualRecipe ritual = PractitionersPouch.getRitual(context.getLevel(), stack);
        boolean clickedOnRune = false;
        boolean activatingNonStoredRitual = false;
        boolean placingChalkRunes = true;
        BlockState aboveState = world.getBlockState(pos.above());
        if (aboveState.is((Block)BlockInit.CHALK_RUNE.get())) {
            pos = pos.above();
            state = aboveState;
        }
        if (state.is((Block)BlockInit.CHALK_RUNE.get())) {
            clickedOnRune = true;
            placingChalkRunes = false;
            ritualMatch = RitualRecipe.matchAnyInWorld(pos, world);
            if (context.getPlayer().isCrouching()) {
                if (ritualMatch != null) {
                    if (!ritualMatch.getRitual().getIsKittable()) {
                        context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.not_kittable"));
                    } else if (this.storeRitual(ritualMatch, world, stack)) {
                        context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.stored"));
                    } else {
                        context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.store_failed"));
                    }
                } else {
                    context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.not_found"));
                }
                return InteractionResult.SUCCESS;
            }
            if (((Boolean)state.getValue((Property)ChalkRuneBlock.ACTIVATED)).booleanValue()) {
                return InteractionResult.FAIL;
            }
        }
        if (ritualMatch == null && clickedOnRune) {
            return InteractionResult.SUCCESS;
        }
        if (ritual == null) {
            if (ritualMatch != null) {
                ritual = ritualMatch.getRitual();
                activatingNonStoredRitual = true;
            } else {
                return InteractionResult.SUCCESS;
            }
        }
        if (ritual.getTier() > progression.getTier()) {
            context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:ritual-start-tier-fail"));
            return InteractionResult.FAIL;
        }
        if (!clickedOnRune) {
            int bound = ritual.getLowerBound();
            for (int i = -bound; i <= bound; ++i) {
                for (int j = -bound; j <= bound; ++j) {
                    BlockPlaceContext bpc;
                    BlockPos offsetPos = pos.offset(i, 0, j);
                    if (!Block.canSupportRigidBlock((BlockGetter)world, (BlockPos)offsetPos)) {
                        return InteractionResult.PASS;
                    }
                    BlockPos abovePos = offsetPos.above();
                    aboveState = world.getBlockState(abovePos);
                    if (aboveState.canBeReplaced(bpc = new BlockPlaceContext(context.getPlayer(), context.getHand(), stack, new BlockHitResult(Vec3.atCenterOf((Vec3i)abovePos), Direction.UP, abovePos, true)))) continue;
                    return InteractionResult.PASS;
                }
            }
        }
        ResourceLocation ritualID = ritual.getId();
        RitualEffect matchedEffect = Registries.RitualEffect.getValues().stream().filter(r -> r.matchRitual(ritualID)).findFirst();
        if (matchedEffect == null) {
            context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:ritual-start-failed"));
            return InteractionResult.FAIL;
        }
        if ((!context.getPlayer().isCreative() || matchedEffect.applyStartCheckInCreative()) && (cantStartReason = matchedEffect.canRitualStart(new RitualCheckContext(context.getPlayer(), (ServerLevel)world, ritual, pos.above(), null))) != null) {
            context.getPlayer().sendSystemMessage(cantStartReason);
            return InteractionResult.FAIL;
        }
        SimpleContainer riftInv = null;
        Pair<IItemHandler, Direction> remoteInv = new Pair<IItemHandler, Direction>(null, Direction.UP);
        if (this.getPatchLevel(stack, PractitionersPouchPatches.RIFT) > 0) {
            riftInv = magic.getRiftInventory();
        }
        if (this.getPatchLevel(stack, PractitionersPouchPatches.CONVEYANCE) > 0) {
            remoteInv = this.resolveRemoteInventory(stack, world);
        }
        if (!activatingNonStoredRitual) {
            if (ritualMatch != null) {
                NonNullList<RitualBlockPos> positions = ritualMatch.getPositions();
                for (int i = 0; i < positions.size(); ++i) {
                    BlockEntity be = world.getBlockEntity(((RitualBlockPos)positions.get(i)).getBlockPos());
                    if (be == null) {
                        return InteractionResult.PASS;
                    }
                    if (be instanceof ChalkRuneTile) {
                        ChalkRuneTile crTile = (ChalkRuneTile)be;
                        if (crTile.isEmpty()) continue;
                        context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.metal_runes_not_empty"));
                        return InteractionResult.PASS;
                    }
                    return InteractionResult.PASS;
                }
            }
            if (!this.ritualReagentsPresent(stack, (Container)riftInv, (IItemHandler)remoteInv.getFirst(), (Direction)remoteInv.getSecond()) || !this.ritualCatalystPresent(stack, (Container)riftInv, (IItemHandler)remoteInv.getFirst(), (Direction)remoteInv.getSecond())) {
                context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.not_enough_items"));
                return InteractionResult.PASS;
            }
            if (placingChalkRunes && !this.chalkPresent(stack, (Container)riftInv, (IItemHandler)remoteInv.getFirst(), (Direction)remoteInv.getSecond(), ritual.countRunes())) {
                context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.not_enough_items"));
                return InteractionResult.PASS;
            }
            if (!this.consumeRitualReagents(ritual, (Container)riftInv, (IItemHandler)remoteInv.getFirst(), (Direction)remoteInv.getSecond(), stack)) {
                ManaAndArtifice.LOGGER.error("Failed to consume ritual reagents from the ritual bag.  This is likely due to a conveyance rune marked on an inventory it can't insert AND extract from.");
                return InteractionResult.PASS;
            }
            if (placingChalkRunes && !this.consumeChalk(stack, (IItemHandlerModifiable)(riftInv != null ? new InvWrapper((Container)riftInv) : null), (IItemHandler)remoteInv.getFirst(), (Direction)remoteInv.getSecond(), ritual.countRunes())) {
                ManaAndArtifice.LOGGER.error("Failed to consume ritual reagents from the ritual bag.  This is likely due to a conveyance rune marked on an inventory it can't insert AND extract from.");
                return InteractionResult.PASS;
            }
        } else if (!PractitionersPouch.containsItem(stack, (Container)riftInv, (IItemHandler)remoteInv.getFirst(), (Direction)remoteInv.getSecond(), new ItemStack((ItemLike)ItemInit.PURIFIED_VINTEUM_DUST.get()))) {
            context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.practitioners_pouch.not_enough_items"));
            return InteractionResult.PASS;
        }
        if (!activatingNonStoredRitual) {
            if (this.placeRitual(stack, pos, world, ritualMatch)) {
                this.activateRitual(context.getPlayer(), stack, ritual, ritualMatch != null ? pos : pos.above(), world);
            }
        } else {
            ((PurifiedVinteumDust)ItemInit.PURIFIED_VINTEUM_DUST.get()).updateBlockStates(context, ritualMatch.getPositions(), true);
            PurifiedVinteumDust.RitualInteractResult result = ((PurifiedVinteumDust)ItemInit.PURIFIED_VINTEUM_DUST.get()).commandRitualEntity(context, ritualMatch.getPositions());
            if (result == PurifiedVinteumDust.RitualInteractResult.SPAWN) {
                ((PurifiedVinteumDust)ItemInit.PURIFIED_VINTEUM_DUST.get()).spawnRitualEntity(context, ritualMatch.getRitual(), ritualMatch.getPositions());
            } else if (result == PurifiedVinteumDust.RitualInteractResult.STARTED) {
                 
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        RitualRecipe ritual = PractitionersPouch.getRitual(worldIn.level(), stack);
        if (Screen.hasShiftDown()) {
            Object patchString = I18n.get((String)"item.mna.patch.prompt", (Object[])new Object[0]);
            PractitionersPouchPatches[] patches = this.getAppliedPatches(stack);
            if (patches.length > 0) {
                for (int i = 0; i < patches.length; ++i) {
                    ItemStack patchStack = PractitionersPatch.getItemFor(patches[i], this.getPatchLevel(stack, patches[i]));
                    patchString = (String)patchString + I18n.get((String)(patchStack.getDescriptionId() + ".simple"), (Object[])new Object[0]) + ", ";
                }
                patchString = ((String)patchString).substring(0, ((String)patchString).length() - 2);
                tooltip.add((Component)Component.literal((String)patchString).withStyle(ChatFormatting.YELLOW));
            }
            if (ritual == null) {
                tooltip.add((Component)Component.translatable((String)"item.mna.practitioners_pouch.no_ritual_stored"));
            } else {
                ChatFormatting notFoundColor;
                boolean hasRemoteInvPatch;
                ArrayList<ItemStack> reagents = this.getReagents(stack);
                InventoryRitualKit irk = new InventoryRitualKit(stack);
                ArrayList<ItemStack> combinedReagents = new ArrayList<ItemStack>();
                tooltip.add((Component)Component.translatable((String)"item.mna.practitioners_pouch.reagents"));
                for (ItemStack reagent : reagents) {
                    boolean found = false;
                    for (ItemStack listStack : combinedReagents) {
                        if (listStack.getItem() != reagent.getItem()) continue;
                        listStack.setCount(listStack.getCount() + 1);
                        found = true;
                        break;
                    }
                    if (found) continue;
                    combinedReagents.add(reagent.copy());
                }
                combinedReagents.add(new ItemStack((ItemLike)ItemInit.PURIFIED_VINTEUM_DUST.get()));
                boolean hasRiftPatch = this.getPatchLevel(stack, PractitionersPouchPatches.RIFT) > 0;
                boolean bl = hasRemoteInvPatch = this.getPatchLevel(stack, PractitionersPouchPatches.CONVEYANCE) > 0;
                ChatFormatting chatFormatting = hasRiftPatch && hasRemoteInvPatch ? ChatFormatting.LIGHT_PURPLE : (hasRiftPatch && !hasRemoteInvPatch ? ChatFormatting.DARK_PURPLE : (notFoundColor = !hasRiftPatch && hasRemoteInvPatch ? ChatFormatting.YELLOW : ChatFormatting.RED));
                if (this.chalkPresent(stack, null, null, Direction.UP, ritual.countRunes())) {
                    tooltip.add((Component)Component.translatable((String)((WizardChalk)ItemInit.WIZARD_CHALK.get()).getDescriptionId()).withStyle(ChatFormatting.GREEN));
                } else {
                    tooltip.add((Component)Component.translatable((String)((WizardChalk)ItemInit.WIZARD_CHALK.get()).getDescriptionId()).withStyle(notFoundColor));
                }
                for (ItemStack listStack : combinedReagents) {
                    if (listStack.isEmpty()) continue;
                    ItemStack searchCopy = listStack.copy();
                    if (searchCopy.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
                        searchCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove("ritual_tags");
                        if (searchCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getAllKeys().size() == 0) {
                            searchCopy.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        }
                    }
                    if (InventoryUtilities.countItem(searchCopy, (IItemHandler)irk, Direction.UP, true, true) >= listStack.getCount()) {
                        tooltip.add((Component)Component.translatable((String)listStack.getItem().getDescriptionId()).withStyle(ChatFormatting.GREEN));
                        continue;
                    }
                    tooltip.add((Component)Component.translatable((String)listStack.getItem().getDescriptionId()).withStyle(notFoundColor));
                }
            }
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.spell.shift_prompt").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
        }
        IRadialMenuItem.super.appendHoverText(stack, worldIn, tooltip, flagIn);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ItemFilterGroup filterGroup() {
        return ItemFilterGroup.ALL_ITEMS;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedRitualKit(stack, null);
    }

    @Override
    public void fillByteBuffer(FriendlyByteBuf buf) {
        buf.writeBoolean(false);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains(NBT_ID)) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
        return null;
    }

    @Override
    public int getCachedTier() {
        return this.tier;
    }

    @Override
    public void setCachedTier(int tier) {
        this.tier = tier;
    }
}


