/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.items.armor;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.items.armor.ISetItem;
import java.util.ArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class ArmorEventHandler {
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        int isSetApplied;
        ItemStack gear;
        EquipmentSlot slot;
        int n;
        ArrayList<EquipmentSlot> equippedSlots;
        EquipmentSlot newSlot = event.getSlot();
        ItemStack newStack = event.getTo();
        ItemStack oldStack = event.getFrom();
        if (newStack.getItem() instanceof ISetItem && oldStack.getItem() instanceof ISetItem && ((ISetItem)newStack.getItem()).getSetIdentifier().equals(((ISetItem)oldStack.getItem()).getSetIdentifier())) {
            return;
        }
        if (oldStack.getItem() instanceof ISetItem) {
            int oldSetCount = 1;
            ISetItem oldGear = (ISetItem)oldStack.getItem();
            equippedSlots = new ArrayList<EquipmentSlot>();
            equippedSlots.add(newSlot);
            EquipmentSlot[] equipmentSlotArray = EquipmentSlot.values();
            int n2 = equipmentSlotArray.length;
            for (n = 0; n < n2; ++n) {
                slot = equipmentSlotArray[n];
                if (slot == newSlot || !((gear = event.getEntity().getItemBySlot(slot)).getItem() instanceof ISetItem) || !((ISetItem)gear.getItem()).getSetIdentifier().equals(oldGear.getSetIdentifier())) continue;
                equippedSlots.add(slot);
                ++oldSetCount;
            }
            boolean wasSetApplied = oldGear.itemsForSetBonus() <= oldSetCount;
            int n3 = isSetApplied = oldGear.itemsForSetBonus() <= oldSetCount - 1 ? 1 : 0;
            if (wasSetApplied && isSetApplied == 0) {
                oldGear.removeSetBonus(event.getEntity(), equippedSlots.toArray(new EquipmentSlot[0]));
            }
        }
        if (newStack.getItem() instanceof ISetItem) {
            int newSetCount = 1;
            ISetItem newGear = (ISetItem)newStack.getItem();
            equippedSlots = new ArrayList();
            equippedSlots.add(newSlot);
            EquipmentSlot[] wasSetApplied = EquipmentSlot.values();
            isSetApplied = wasSetApplied.length;
            for (n = 0; n < isSetApplied; ++n) {
                slot = wasSetApplied[n];
                if (slot == newSlot || !((gear = event.getEntity().getItemBySlot(slot)).getItem() instanceof ISetItem) || !((ISetItem)gear.getItem()).getSetIdentifier().equals(newGear.getSetIdentifier())) continue;
                equippedSlots.add(slot);
                ++newSetCount;
            }
            boolean wasSetApplied2 = newGear.itemsForSetBonus() <= newSetCount - 1;
            int n4 = isSetApplied = newGear.itemsForSetBonus() <= newSetCount ? 1 : 0;
            if (!wasSetApplied2 && isSetApplied != 0) {
                newGear.applySetBonus(event.getEntity(), equippedSlots.toArray(new EquipmentSlot[0]));
                if (event.getEntity() instanceof ServerPlayer) {
                    CustomAdvancementTriggers.SET_BONUS.trigger((ServerPlayer)event.getEntity(), newGear.getSetIdentifier());
                }
            }
        }
    }
}


