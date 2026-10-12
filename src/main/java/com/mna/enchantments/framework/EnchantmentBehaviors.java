package com.mna.enchantments.framework;

import com.mna.enchantments.base.MAEnchantmentBase;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * [PORT 1.21.1] {@code Enchantment} virou um {@code record} imutavel: as classes de
 * comportamento do MNA (Aura, Bouncing, Cloudstep, ...) nao podem mais SER o enchantment.
 *
 * Esta classe associa cada enchantment registrado (por {@link ResourceKey}) ao seu objeto
 * de comportamento, que executa a logica custom (apply de auras, checks de compatibilidade etc.).
 */
public final class EnchantmentBehaviors {
    private static final Map<ResourceKey<Enchantment>, MAEnchantmentBase> BEHAVIORS = new HashMap<>();

    private EnchantmentBehaviors() {
    }

    public static void put(DeferredHolder<Enchantment, Enchantment> holder, MAEnchantmentBase behavior) {
        BEHAVIORS.put(holder.getKey(), behavior);
    }

    public static MAEnchantmentBase get(ResourceKey<Enchantment> key) {
        return BEHAVIORS.get(key);
    }

    public static MAEnchantmentBase get(Holder<Enchantment> holder) {
        return holder.unwrapKey().map(EnchantmentBehaviors::get).orElse(null);
    }

    public static MAEnchantmentBase get(Enchantment enchantment) {
        if (enchantment == null) {
            return null;
        }
        return java.util.Optional.ofNullable(com.mna.api.enchantments.MNAEnchantmentRegistry.keyOf(enchantment)).map(EnchantmentBehaviors::get).orElse(null);
    }
}
