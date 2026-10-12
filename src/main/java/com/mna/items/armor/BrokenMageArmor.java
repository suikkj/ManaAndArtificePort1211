 
package com.mna.items.armor;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.ITieredItem;
import com.mna.items.ItemInit;
import com.mna.items.armor.MAArmorMaterial;
import com.mna.items.base.INoCreativeTab;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import com.mna.api.tools.MNADyeableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class BrokenMageArmor
extends ArmorItem
implements MNADyeableItem, ITieredItem<BrokenMageArmor>,
INoCreativeTab {
    private static final String NBT_RESTORE = "previous_item";
    private static final String NBT_OTHER_ID = "previous_item_id";
    private int _tier = -1;

    public BrokenMageArmor(ArmorItem.Type slot) {
        super(ArmorMaterialInit.BROKEN, slot, new Item.Properties().stacksTo(1).setNoRepair().durability(slot.getDurability(5)));
    }

    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return 0;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add((Component)Component.translatable((String)"item.mna.broken_repair_prompt").withStyle(ChatFormatting.AQUA));
    }

    public static BrokenMageArmor getBySlot(EquipmentSlot slot) {
        if (slot == null) {
            return null;
        }
        switch (slot) {
            case CHEST: {
                return (BrokenMageArmor)ItemInit.BROKEN_ROBES.get();
            }
            case FEET: {
                return (BrokenMageArmor)ItemInit.BROKEN_BOOTS.get();
            }
            case HEAD: {
                return (BrokenMageArmor)ItemInit.BROKEN_HOOD.get();
            }
            case LEGS: {
                return (BrokenMageArmor)ItemInit.BROKEN_LEGGINGS.get();
            }
        }
        return null;
    }

    public ItemStack convertFrom(ItemStack other) {
        if (!(other.getItem() instanceof ArmorItem) || ((ArmorItem)other.getItem()).getType() != this.getType()) {
            return ItemStack.EMPTY;
        }
        ItemStack outputStack = new ItemStack((ItemLike)this);
        if (other.getItem() instanceof MNADyeableItem) {
            MNADyeableItem.setColor(outputStack, MNADyeableItem.getColor(other));
        }
        CompoundTag tag = outputStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (other.has(DataComponents.CUSTOM_DATA)) {
            tag.put(NBT_RESTORE, (Tag)other.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        }
        tag.putString(NBT_OTHER_ID, BuiltInRegistries.ITEM.getKey(other.getItem()).toString());
        return outputStack;
    }

    public ItemStack restore(ItemStack me) {
        CompoundTag tag = me.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        ResourceLocation restore = ResourceLocation.parse(tag.getString(NBT_OTHER_ID));
        Item item = (Item)BuiltInRegistries.ITEM.get(restore);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack outputStack = new ItemStack((ItemLike)item);
        if (tag.contains(NBT_RESTORE)) {
            outputStack.set(DataComponents.CUSTOM_DATA, CustomData.of((CompoundTag)tag.get(NBT_RESTORE)));
        }
        return outputStack;
    }

    public static boolean hasRestore(ItemStack stack) {
        ResourceLocation restore;
        Item item;
        return !(stack.getItem() instanceof BrokenMageArmor) || !stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_RESTORE) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_OTHER_ID) || (item = (Item)BuiltInRegistries.ITEM.get(restore = ResourceLocation.parse(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(NBT_OTHER_ID)))) != null;
    }

    @Override
    public void setCachedTier(int tier) {
        this._tier = tier;
    }

    @Override
    public int getCachedTier() {
        return this._tier;
    }
}


