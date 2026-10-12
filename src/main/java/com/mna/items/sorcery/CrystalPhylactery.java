 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.items.IPhylacteryItem;
import com.mna.api.items.TieredItem;
import com.mna.api.tools.MATags;
import com.mna.tools.math.MathUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class CrystalPhylactery
extends TieredItem
implements IPhylacteryItem {
    private static final String NBT_ENTITYTYPE = "mna:summon_entity_type";
    private static final String NBT_AMOUNT = "mna:summon_fill_pct";

    public CrystalPhylactery() {
        super(new Item.Properties());
    }

    public static int getSoulCap() {
        return Math.max(GeneralConfigValues.PhylacteryKillCount, 1);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        IPhylacteryItem.super.appendTooltip(stack, worldIn.level(), tooltip, flagIn);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public int getBarColor(ItemStack stack) {
        float amount = CrystalPhylactery.getFillAmount(stack);
        if (amount < (float)CrystalPhylactery.getSoulCap()) {
            return 0xFFAA00;
        }
        return ChatFormatting.LIGHT_PURPLE.getColor();
    }

    public int getBarWidth(ItemStack stack) {
        float amount = CrystalPhylactery.getFillAmount(stack);
        if (amount > 0.0f && amount < 8.0f) {
            amount = 8.0f;
        }
        return (int)(13.0f * (amount / (float)CrystalPhylactery.getSoulCap()));
    }

    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    public static boolean isFilled(ItemStack stack) {
        return CrystalPhylactery.getFillAmount(stack) == (float)CrystalPhylactery.getSoulCap();
    }

    public static boolean addToPhylactery(Container inventory, EntityType<? extends Mob> type, float amount, Level world, boolean allowAddNew) {
        InvWrapper wrapper = new InvWrapper(inventory);
        return CrystalPhylactery.addToPhylactery((IItemHandler)wrapper, type, amount, world, allowAddNew);
    }

    public static boolean addToPhylactery(IItemHandler inventory, EntityType<? extends Mob> type, float amount, Level world, boolean allowAddNew) {
        if (MATags.isEntityIn(type, MATags.EntityTypes.SUMMON_BLACKLIST)) {
            return false;
        }
        int matching_slot = -1;
        int empty_slot = -1;
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!(stack.getItem() instanceof IPhylacteryItem)) continue;
            EntityType<? extends Mob> containedType = ((IPhylacteryItem)stack.getItem()).getContainedEntity(stack);
            if (containedType == type) {
                float containedAmount = ((IPhylacteryItem)stack.getItem()).getContainedSouls(stack);
                if (!(containedAmount < (float)((IPhylacteryItem)stack.getItem()).getMaximumFill())) continue;
                matching_slot = i;
                continue;
            }
            if (containedType != null) continue;
            empty_slot = i;
        }
        if (matching_slot > -1) {
            ItemStack matching = inventory.getStackInSlot(matching_slot);
            if (matching.getItem() instanceof IPhylacteryItem) {
                float existing = ((IPhylacteryItem)matching.getItem()).getContainedSouls(matching);
                amount = MathUtils.clamp(existing + amount, 0.0f, (float)CrystalPhylactery.getSoulCap());
                ((IPhylacteryItem)matching.getItem()).setContainedSouls(matching, amount);
                return true;
            }
        } else if (allowAddNew && empty_slot > -1) {
            ItemStack empty = inventory.getStackInSlot(empty_slot);
            if (inventory instanceof Inventory) {
                EntityType<? extends Mob> containedType;
                empty = ItemStack.EMPTY;
                Inventory inv = (Inventory)inventory;
                ItemStack mainHand = inv.player.getMainHandItem();
                ItemStack offHand = inv.player.getOffhandItem();
                if (offHand.getItem() instanceof IPhylacteryItem) {
                    EntityType<? extends Mob> containedType2 = ((IPhylacteryItem)offHand.getItem()).getContainedEntity(offHand);
                    if (containedType2 == null) {
                        empty = offHand;
                    }
                } else if (mainHand.getItem() instanceof IPhylacteryItem && (containedType = ((IPhylacteryItem)mainHand.getItem()).getContainedEntity(mainHand)) == null) {
                    empty = mainHand;
                }
            }
            if (!empty.isEmpty()) {
                CrystalPhylactery.setEntityType(empty, type, world);
                CrystalPhylactery.setFillAmount(empty, amount);
                return true;
            }
        }
        return false;
    }

    public static boolean setFillAmount(ItemStack stack, float amount) {
        if (!(stack.getItem() instanceof IPhylacteryItem) || CrystalPhylactery.getEntityType(stack) == null) {
            return false;
        }
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putFloat(NBT_AMOUNT, amount);
        return true;
    }

    public static float getFillAmount(ItemStack stack) {
        if (!(stack.getItem() instanceof IPhylacteryItem) || CrystalPhylactery.getEntityType(stack) == null) {
            return 0.0f;
        }
        return MathUtils.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getFloat(NBT_AMOUNT), 0.0f, (float)CrystalPhylactery.getSoulCap());
    }

    @Nullable
    public static EntityType<? extends Mob> getEntityType(ItemStack stack) {
        if (!(stack.getItem() instanceof IPhylacteryItem && stack.getDamageValue() <= 0 && stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_ENTITYTYPE))) {
            return null;
        }
        String type = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(NBT_ENTITYTYPE);
        ResourceLocation rLoc = ResourceLocation.parse(type);
        try {
            EntityType resolvedType = (EntityType)BuiltInRegistries.ENTITY_TYPE.get(rLoc);
            return resolvedType;
        }
        catch (ClassCastException ex) {
            return null;
        }
    }

    public static boolean setEntityType(ItemStack stack, EntityType<? extends Mob> type, Level world) {
        if (!(stack.getItem() instanceof IPhylacteryItem) || CrystalPhylactery.getEntityType(stack) != null) {
            return false;
        }
        Mob inst = (Mob)type.create(world);
        if (!GeneralConfigValues.BossesAllowedInPhylacteries && !com.mna.tools.MATools.canChangeDimensions(inst)) {
            return false;
        }
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(NBT_ENTITYTYPE, BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        return true;
    }

    @Override
    public boolean fill(IItemHandler inventory, EntityType<? extends Mob> type, float amount, Level world, boolean allowAddNew) {
        return CrystalPhylactery.addToPhylactery(inventory, type, amount, world, allowAddNew);
    }

    @Override
    public boolean fill(Container inventory, EntityType<? extends Mob> type, float amount, Level world, boolean allowAddNew) {
        return CrystalPhylactery.addToPhylactery(inventory, type, amount, world, allowAddNew);
    }

    @Override
    public boolean fill(ItemStack stack, EntityType<? extends Mob> type, float amount, Level world) {
        if (!(stack.getItem() instanceof IPhylacteryItem)) {
            return false;
        }
        EntityType<? extends Mob> containedType = CrystalPhylactery.getEntityType(stack);
        if (containedType != null && containedType != type) {
            return false;
        }
        if (containedType == null && !CrystalPhylactery.setEntityType(stack, type, world)) {
            return false;
        }
        float existing = ((IPhylacteryItem)stack.getItem()).getContainedSouls(stack);
        return CrystalPhylactery.setFillAmount(stack, existing + amount);
    }

    @Override
    public EntityType<? extends Mob> getContainedEntity(ItemStack stack) {
        return CrystalPhylactery.getEntityType(stack);
    }

    @Override
    public float getFillPct(ItemStack stack) {
        return MathUtils.clamp01(CrystalPhylactery.getFillAmount(stack) / (float)CrystalPhylactery.getSoulCap());
    }

    @Override
    public boolean isFull(ItemStack stack) {
        return CrystalPhylactery.isFilled(stack);
    }

    @Override
    public int getMaximumFill() {
        return CrystalPhylactery.getSoulCap();
    }

    @Override
    public float getContainedSouls(ItemStack stack) {
        return CrystalPhylactery.getFillAmount(stack);
    }

    @Override
    public boolean setContainedSouls(ItemStack stack, float amount) {
        return CrystalPhylactery.setFillAmount(stack, amount);
    }
}


