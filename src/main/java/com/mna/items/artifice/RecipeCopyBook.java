 
package com.mna.items.artifice;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.Registries;
import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.items.TieredItem;
import com.mna.blocks.tileentities.EldrinAltarTile;
import com.mna.blocks.tileentities.ManaweavingAltarTile;
import com.mna.items.base.IRadialInventorySelect;
import com.mna.items.renderers.books.RecipeCopyBookRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import com.mna.api.tools.MNADyeableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import com.mna.api.tools.MNAItemData;

public class RecipeCopyBook
extends TieredItem
implements MNADyeableItem,
IRadialInventorySelect {
    private static final String NBT_RECIPE_OUTPUT = "recipe_output";
    private static final String NBT_RECIPE_COST = "recipe_cost";
    private static final String NBT_RECIPE_INPUTS = "recipe_inputs";
    private static final String NBT_RECIPE_FACTION = "faction";
    private static final String NBT_RECIPE_TIER = "tier";
    private static final String NBT_RECIPE_PATTERN_COUNT = "pattern_count";
    private static final String NBT_RECIPE_TYPE = "recipe_type";
    private static final String NBT_RECIPE_CONTAINER = "recipes";
    private static final String NBT_MODE = "mode";
    public static final int RECIPE_SLOTS = 16;
    private static final byte RECIPE_TYPE_MANAWEAVING = 0;
    private static final byte RECIPE_TYPE_ELDRIN = 1;

    public RecipeCopyBook() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    public InteractionResult useOn(UseOnContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        BlockEntity tile = pContext.getLevel().getBlockEntity(pos);
        ItemStack stack = pContext.getItemInHand();
        Modes mode = RecipeCopyBook.getMode(stack);
        if (pContext.getPlayer().isCrouching()) {
            RecipeCopyBook.toggleMode(stack);
        } else if (tile != null) {
            if (tile instanceof ManaweavingAltarTile) {
                ManaweavingAltarTile mwTile = (ManaweavingAltarTile)tile;
                if (mode == Modes.COPY ? !RecipeCopyBook.copyRememberedRecipe(stack, mwTile) : !RecipeCopyBook.pasteRememberedRecipe(stack, mwTile, pContext.getPlayer())) {
                    return InteractionResult.FAIL;
                }
            } else if (tile instanceof EldrinAltarTile) {
                EldrinAltarTile eTile = (EldrinAltarTile)tile;
                if (mode == Modes.COPY ? !RecipeCopyBook.copyRememberedRecipe(stack, eTile) : !RecipeCopyBook.pasteRememberedRecipe(stack, eTile, pContext.getPlayer())) {
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.sidedSuccess((boolean)pContext.getLevel().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, net.minecraft.world.item.Item.TooltipContext pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        Modes mode = RecipeCopyBook.getMode(pStack);
        if (mode == Modes.COPY) {
            pTooltipComponents.add((Component)Component.translatable((String)"item.mna.recipe_copy_book.copy").withStyle(ChatFormatting.AQUA));
        } else {
            int slot = RecipeCopyBook.getSlot(pStack);
            ItemStack output = RecipeCopyBook.getOutputItem(pStack, slot);
            MutableComponent tileEntityName = Component.translatable((String)(RecipeCopyBook.getTileType(pStack, slot) == 0 ? "block.mna.manaweaving_altar" : "block.mna.eldrin_altar")).withStyle(ChatFormatting.AQUA);
            pTooltipComponents.add((Component)Component.translatable((String)"item.mna.recipe_copy_book.paste").withStyle(ChatFormatting.AQUA).append((Component)Component.translatable((String)"item.mna.recipe_copy_book.paste_2", (Object[])new Object[]{Component.translatable((String)output.getDescriptionId()).withStyle(ChatFormatting.AQUA), tileEntityName}).withStyle(ChatFormatting.WHITE)));
        }
        IRadialInventorySelect.super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    public boolean isFoil(ItemStack pStack) {
        return RecipeCopyBook.getMode(pStack) == Modes.PASTE;
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new RecipeCopyBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public static boolean copyRememberedRecipe(ItemStack stack, ManaweavingAltarTile tile) {
        ItemStack output = tile.getReCraftOutput();
        List<ItemStack> inputs = tile.getReCraftInput();
        IFaction faction = tile.getLastCraftFaction();
        int tier = tile.getLastCraftTier();
        int patternCount = tile.getLastCraftPatternCount();
        if (output.isEmpty() || inputs.size() == 0) {
            return false;
        }
        CompoundTag outputTag = new CompoundTag();
        output.save(outputTag);
        ListTag inputsTag = new ListTag();
        inputs.forEach(is -> {
            CompoundTag item = new CompoundTag();
            is.save(item);
            inputsTag.add(item);
        });
        CompoundTag data = new CompoundTag();
        data.put(NBT_RECIPE_OUTPUT, (Tag)outputTag);
        data.put(NBT_RECIPE_INPUTS, (Tag)inputsTag);
        data.putInt(NBT_RECIPE_TIER, tier);
        data.putInt(NBT_RECIPE_PATTERN_COUNT, patternCount);
        if (faction != null) {
            data.putString(NBT_RECIPE_FACTION, Registries.Factions.getKey(faction).toString());
        }
        data.putByte(NBT_RECIPE_TYPE, (byte)0);
        int slot = RecipeCopyBook.getSlot(stack);
        RecipeCopyBook.setSlotData(stack, slot, data);
        return true;
    }

    public static boolean copyRememberedRecipe(ItemStack stack, EldrinAltarTile tile) {
        ItemStack output = tile.getReCraftOutput();
        List<ItemStack> inputs = tile.getReCraftInput();
        IFaction faction = tile.getLastCraftFaction();
        int tier = tile.getLastCraftTier();
        if (output.isEmpty() || inputs.size() == 0) {
            return false;
        }
        CompoundTag outputTag = new CompoundTag();
        output.save(outputTag);
        ListTag inputsTag = new ListTag();
        inputs.forEach(is -> {
            CompoundTag item = new CompoundTag();
            is.save(item);
            inputsTag.add(item);
        });
        CompoundTag affTag = new CompoundTag();
        tile.getReCraftEldrin().entrySet().forEach(e -> affTag.putFloat(((Affinity)((e.getKey()))).name(), ((Float)e.getValue()).floatValue()));
        CompoundTag data = new CompoundTag();
        data.put(NBT_RECIPE_OUTPUT, (Tag)outputTag);
        data.put(NBT_RECIPE_INPUTS, (Tag)inputsTag);
        data.put(NBT_RECIPE_COST, (Tag)affTag);
        data.putInt(NBT_RECIPE_TIER, tier);
        if (faction != null) {
            data.putString(NBT_RECIPE_FACTION, Registries.Factions.getKey(faction).toString());
        }
        data.putByte(NBT_RECIPE_TYPE, (byte)1);
        int slot = RecipeCopyBook.getSlot(stack);
        RecipeCopyBook.setSlotData(stack, slot, data);
        return true;
    }

    public static boolean pasteRememberedRecipe(ItemStack stack, ManaweavingAltarTile tile, @Nullable Player player) {
        int slot = RecipeCopyBook.getSlot(stack);
        CompoundTag data = RecipeCopyBook.getSlotData(stack, slot);
        if (data == null) {
            return false;
        }
        if (!(data.contains(NBT_RECIPE_TYPE) && data.getByte(NBT_RECIPE_TYPE) == 0 && data.contains(NBT_RECIPE_TIER, 3) && data.contains(NBT_RECIPE_PATTERN_COUNT, 3) && data.contains(NBT_RECIPE_INPUTS, 9) && data.contains(NBT_RECIPE_OUTPUT, 10))) {
            return false;
        }
        CompoundTag outputTag = data.getCompound(NBT_RECIPE_OUTPUT);
        ListTag inputTag = data.getList(NBT_RECIPE_INPUTS, 10);
        int tier = data.getInt(NBT_RECIPE_TIER);
        int patternCount = data.getInt(NBT_RECIPE_PATTERN_COUNT);
        String factionID = data.getString(NBT_RECIPE_FACTION);
        ItemStack output = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)outputTag).orElse(ItemStack.EMPTY);
        ArrayList<ItemStack> inputs = new ArrayList<ItemStack>();
        inputTag.forEach(tag -> inputs.add(ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)((CompoundTag)tag)).orElse(ItemStack.EMPTY)));
        IFaction faction = (IFaction)Registries.Factions.getValue(ResourceLocation.parse(factionID));
        tile.setReCraftRecipe(output, inputs, faction, tier, patternCount);
        if (player != null) {
            tile.reCraft(player);
        }
        return true;
    }

    public static boolean pasteRememberedRecipe(ItemStack stack, EldrinAltarTile tile, @Nullable Player player) {
        int slot = RecipeCopyBook.getSlot(stack);
        CompoundTag data = RecipeCopyBook.getSlotData(stack, slot);
        if (!(data.contains(NBT_RECIPE_TYPE) && data.getByte(NBT_RECIPE_TYPE) == 1 && data.contains(NBT_RECIPE_TIER, 3) && data.contains(NBT_RECIPE_INPUTS, 9) && data.contains(NBT_RECIPE_OUTPUT, 10) && data.contains(NBT_RECIPE_COST, 10))) {
            return false;
        }
        CompoundTag outputTag = data.getCompound(NBT_RECIPE_OUTPUT);
        CompoundTag outputCost = data.getCompound(NBT_RECIPE_COST);
        ListTag inputTag = data.getList(NBT_RECIPE_INPUTS, 10);
        int tier = data.getInt(NBT_RECIPE_TIER);
        String factionID = data.getString(NBT_RECIPE_FACTION);
        ItemStack output = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)outputTag).orElse(ItemStack.EMPTY);
        ArrayList<ItemStack> inputs = new ArrayList<ItemStack>();
        inputTag.forEach(tag -> inputs.add(ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)((CompoundTag)tag)).orElse(ItemStack.EMPTY)));
        IFaction faction = (IFaction)Registries.Factions.getValue(ResourceLocation.parse(factionID));
        HashMap<Affinity, Float> powerRequirements = new HashMap<Affinity, Float>();
        for (Affinity aff : Affinity.values()) {
            if (!outputCost.contains(aff.name())) continue;
            powerRequirements.put(aff, Float.valueOf(outputCost.getFloat(aff.name())));
        }
        tile.setReCraftRecipe(output, inputs, faction, tier, powerRequirements);
        if (player != null) {
            tile.reCraft(player);
        }
        return true;
    }

    @Override
    public int capacity() {
        return 16;
    }

    @Override
    public int getIndex(ItemStack stack) {
        return RecipeCopyBook.getSlot(stack);
    }

    @Override
    public void setIndex(ItemStack stack, int index) {
        RecipeCopyBook.setSlot(stack, index);
    }

    @Override
    public IItemHandlerModifiable getInventory(final ItemStack stackEquipped, @Nullable Player player) {
        return new IItemHandlerModifiable(){

            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return false;
            }

            @NotNull
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @NotNull
            public ItemStack getStackInSlot(int slot) {
                return RecipeCopyBook.getOutputItem(stackEquipped, slot);
            }

            public int getSlots() {
                return RecipeCopyBook.this.capacity();
            }

            public int getSlotLimit(int slot) {
                return 64;
            }

            @NotNull
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            }
        };
    }

    public static Modes getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag == null) {
            return Modes.COPY;
        }
        return Modes.values()[tag.getInt(NBT_MODE) % Modes.values().length];
    }

    public static void toggleMode(ItemStack stack) {
        int idx = (RecipeCopyBook.getMode(stack).ordinal() + 1) % Modes.values().length;
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(NBT_MODE, idx);
    }

    public static int getSlot(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag == null) {
            return 0;
        }
        return tag.getInt("index");
    }

    public static void setSlot(ItemStack stack, int slot) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("index", slot);
        ItemStack output = RecipeCopyBook.getOutputItem(stack, slot);
        Modes mode = RecipeCopyBook.getMode(stack);
        if (output.isEmpty() && mode == Modes.PASTE) {
            RecipeCopyBook.toggleMode(stack);
        } else if (!output.isEmpty() && mode == Modes.COPY) {
            RecipeCopyBook.toggleMode(stack);
        }
    }

    public static CompoundTag getSlotData(ItemStack stack, int slot) {
        if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() == null) {
            return null;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("recipes_" + slot);
    }

    public static void setSlotData(ItemStack stack, int slot, CompoundTag data) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put("recipes_" + slot, (Tag)data);
        if (RecipeCopyBook.getSlot(stack) == slot && RecipeCopyBook.getMode(stack) == Modes.COPY) {
            RecipeCopyBook.toggleMode(stack);
        }
    }

    public static ItemStack getOutputItem(ItemStack stack, int slot) {
        CompoundTag tag = RecipeCopyBook.getSlotData(stack, slot);
        if (tag == null || !tag.contains(NBT_RECIPE_OUTPUT, 10)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)tag.getCompound(NBT_RECIPE_OUTPUT)).orElse(ItemStack.EMPTY);
    }

    public static int getTileType(ItemStack stack, int slot) {
        CompoundTag tag = RecipeCopyBook.getSlotData(stack, slot);
        if (tag == null || !tag.contains(NBT_RECIPE_TYPE, 1)) {
            return 0;
        }
        return tag.getInt(NBT_RECIPE_TYPE);
    }

    public static int getTier(ItemStack stack, int slot) {
        CompoundTag tag = RecipeCopyBook.getSlotData(stack, slot);
        if (tag == null || !tag.contains(NBT_RECIPE_TIER, 3)) {
            return 0;
        }
        return tag.getInt(NBT_RECIPE_TIER);
    }

    public static IFaction getFaction(ItemStack stack, int slot) {
        CompoundTag tag = RecipeCopyBook.getSlotData(stack, slot);
        if (tag == null || !tag.contains(NBT_RECIPE_FACTION, 8)) {
            return null;
        }
        return (IFaction)Registries.Factions.getValue(ResourceLocation.parse(tag.getString(NBT_RECIPE_FACTION)));
    }

    public static enum Modes {
        COPY,
        PASTE;

    }
}


