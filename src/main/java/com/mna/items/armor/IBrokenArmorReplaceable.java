/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.armor;

import com.mna.items.armor.BrokenMageArmor;
import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public interface IBrokenArmorReplaceable<I extends ArmorItem> {
    default public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        if (stack.getDamageValue() + amount >= stack.getMaxDamage() && entity instanceof LivingEntity) {
            BrokenMageArmor brokenItem = BrokenMageArmor.getBySlot(((ArmorItem)this).getEquipmentSlot());
            if (brokenItem == null) {
                return amount;
            }
            ItemStack brokenStack = brokenItem.convertFrom(stack);
            entity.setItemSlot(((ArmorItem)this).getEquipmentSlot(), brokenStack);
            return 0;
        }
        return amount;
    }
}

