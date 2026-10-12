package com.mna.api.enchantments;

import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Compat 1.21.1: era @ObjectHolder (removido) -> todos os campos nulos (NPE em jogo).
 */
public class Enchantments {
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_HEALING = com.mna.enchantments.framework.EnchantmentInit.HEALING;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_RUNNING = com.mna.enchantments.framework.EnchantmentInit.RUNNING;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_PILGRIM = com.mna.enchantments.framework.EnchantmentInit.PILGRIM;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_JUMPING = com.mna.enchantments.framework.EnchantmentInit.JUMPING;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_DEPTHS = com.mna.enchantments.framework.EnchantmentInit.WATER_BREATHING;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_MINING = com.mna.enchantments.framework.EnchantmentInit.MINING_BOOST;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_BULWARK = com.mna.enchantments.framework.EnchantmentInit.BULWARK;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> AURA_REPAIR = com.mna.enchantments.framework.EnchantmentInit.REPAIR;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> TELEKINETIC_RETRIEVAL = com.mna.enchantments.framework.EnchantmentInit.RETURNING;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> ASCENDED_DURATION = com.mna.enchantments.framework.EnchantmentInit.DURATION_MOD;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> ASCENDED_RANGE = com.mna.enchantments.framework.EnchantmentInit.RANGE_MOD;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> ASCENDED_SPEED = com.mna.enchantments.framework.EnchantmentInit.SPEED_MOD;
    public static final net.neoforged.neoforge.registries.DeferredHolder<Enchantment, ? extends Enchantment> EXPERIENCED_SOUL = com.mna.enchantments.framework.EnchantmentInit.XP_TO_SOULS;
}
