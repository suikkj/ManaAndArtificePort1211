 
package com.mna.effects.harmful;

import com.mna.tools.EnchantmentUtils;
import java.util.ArrayList;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.config.GeneralConfigValues;
import com.mna.effects.harmful.SimpleHarmfulEffect;
import com.mna.effects.interfaces.INoCreeperLingering;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class EffectDisjunction
extends SimpleHarmfulEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public static final String NBT_DISJUNCTION = "disjunction";
    public static final String NBT_ENCHANTMENTS = "Enchantments";

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                EffectDisjunction.ApplyDisjunction(pLivingEntity.getItemBySlot(slot), pAmplifier + 1);
            }
    
    }

    public void addAttributeModifiers(AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pAttributeMap, pAmplifier);
    }

    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                EffectDisjunction.RemoveDisjunction(pLivingEntity.getItemBySlot(slot));
            }
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }

    public static void ApplyDisjunction(ItemStack stack, int maxLevels) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || maxLevels < 1 || stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_DISJUNCTION)) {
            return;
        }
        ListTag listtag = new ListTag();
        if (listtag.size() == 0) {
            return;
        }
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put(NBT_DISJUNCTION, (Tag)listtag);
        java.util.Map<net.minecraft.core.Holder<Enchantment>, Integer> chants = EnchantmentUtils.enchantmentMap((ItemStack)stack);
        List<net.minecraft.core.Holder<Enchantment>> keys = new ArrayList<net.minecraft.core.Holder<Enchantment>>(chants.keySet());
        Collections.shuffle(keys);
        int total = chants.values().stream().mapToInt(Integer::intValue).sum();
        int suppression = (int)Math.min(Math.ceil((float)total / (float)chants.size()), (double)maxLevels);
        int totalSuppressed = 0;
        for (net.minecraft.core.Holder<Enchantment> e : keys) {
            if (GeneralConfigValues.DisjunctionBlacklist.contains(String.valueOf(com.mna.api.enchantments.MNAEnchantmentRegistry.idOf(e.value())))) continue;
            Integer existing = (Integer)chants.get(e);
            int loopSuppression = Math.min(suppression, maxLevels - totalSuppressed);
            if (existing > loopSuppression) {
                totalSuppressed += loopSuppression;
                chants.put(e, existing - loopSuppression);
            } else {
                totalSuppressed += existing.intValue();
                chants.remove(e);
            }
            if (totalSuppressed < maxLevels) continue;
            break;
        }
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable chMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        chants.forEach(chMut::set);
        ((ItemStack)stack).set(DataComponents.ENCHANTMENTS, chMut.toImmutable());
    }

    public static void RemoveDisjunction(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_DISJUNCTION)) {
            return;
        }
        ListTag backup = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList(NBT_DISJUNCTION, 10);
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put(NBT_ENCHANTMENTS, (Tag)backup);
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove(NBT_DISJUNCTION);
    }
}


