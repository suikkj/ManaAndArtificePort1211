/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  org.apache.commons.lang3.mutable.MutableInt
 *  top.theillusivec4.curios.api.CuriosApi
 */
package com.mna.tools;

import com.mna.tools.EnchantmentUtils;
import com.mna.items.ItemInit;
import com.mna.items.artifice.FortuneRing;
import com.mna.items.artifice.SpellModifierRing;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.commons.lang3.mutable.MutableInt;
import top.theillusivec4.curios.api.CuriosApi;

public class EnchantmentUtils {
    public static Map<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> enchantmentMap(net.minecraft.world.item.ItemStack stack) {
        Map<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> m = new java.util.HashMap<>();
        EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().forEach(en -> m.put(en.getKey(), en.getIntValue()));
        return m;
    }

    public static boolean getSilkTouch(Player player) {
        return ((SpellModifierRing)ItemInit.SILK_TOUCH_RING.get()).isEquippedAndHasMana((LivingEntity)player, 1.0f, false);
    }

    public static int getFortuneLevel(Player player) {
        MutableInt fortune_level = new MutableInt(0);
        Item fortuneRing = null;
        if (((FortuneRing)ItemInit.FORTUNE_RING_GREATER.get()).isEquippedAndHasMana((LivingEntity)player, 1.0f, false)) {
            fortune_level.setValue(3);
            fortuneRing = (Item)ItemInit.FORTUNE_RING_GREATER.get();
        } else if (((FortuneRing)ItemInit.FORTUNE_RING.get()).isEquippedAndHasMana((LivingEntity)player, 1.0f, false)) {
            fortune_level.setValue(2);
        } else if (((FortuneRing)ItemInit.FORTUNE_RING_MINOR.get()).isEquippedAndHasMana((LivingEntity)player, 1.0f, false)) {
            fortune_level.setValue(1);
        }
        if (fortuneRing != null) {
            CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)player, fortuneRing).ifPresent(t -> {
                Map enchants = EnchantmentUtils.enchantmentMap((ItemStack)t.stack());
                fortune_level.add((Number)enchants.getOrDefault(Enchantments.BLOCK_FORTUNE, 0));
            });
        }
        return fortune_level.intValue();
    }
}

