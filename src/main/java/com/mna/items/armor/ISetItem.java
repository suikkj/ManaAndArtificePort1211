/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.armor;

import com.mna.ManaAndArtifice;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ISetItem {
    public static final EquipmentSlot[] defaultSlotTypes = new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HEAD, EquipmentSlot.LEGS};

    public ResourceLocation getSetIdentifier();

    default public int itemsForSetBonus() {
        return 4;
    }

    default public void applySetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
    }

    default public void removeSetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
    }

    default public EquipmentSlot[] getValidSetSlots() {
        return defaultSlotTypes;
    }

    default public void addSetTooltip(List<Component> tooltip) {
        boolean setEquipped = this.isSetEquipped((LivingEntity)ManaAndArtifice.instance.proxy.getClientPlayer());
        String s = Component.translatable((String)this.getSetIdentifier().toString()).getString();
        for (String spl : s.split("\n")) {
            tooltip.add((Component)Component.literal((String)spl).withStyle(setEquipped ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }

    default public boolean isSetEquipped(LivingEntity living) {
        if (living == null) {
            return false;
        }
        int count = 0;
        for (EquipmentSlot slot : this.getValidSetSlots()) {
            ItemStack stack;
            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND || !((stack = living.getItemBySlot(slot)).getItem() instanceof ISetItem) || !((ISetItem)stack.getItem()).getSetIdentifier().equals(this.getSetIdentifier())) continue;
            ++count;
        }
        return count >= this.itemsForSetBonus();
    }
}

