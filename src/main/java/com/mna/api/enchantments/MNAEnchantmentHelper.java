package com.mna.api.enchantments;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * [PORT 1.21.1] {@code ItemStack.getEnchantmentLevel(...)} foi REMOVIDO.
 * A leitura agora e estatica, exigindo a entidade dona do stack:
 * {@link EnchantmentHelper#getEnchantmentLevel(Holder, LivingEntity)}.
 *
 * Esta classe preserva os call-sites do MNA.
 */
public final class MNAEnchantmentHelper {
    private MNAEnchantmentHelper() {}

    /** Equivalente ao antigo {@code stack.getEnchantmentLevel(e)}. */
    public static int getEnchantmentLevel(Holder<Enchantment> enchantment, LivingEntity entity) {
        return entity == null ? 0 : EnchantmentHelper.getEnchantmentLevel(enchantment, entity);
    }

    /** Variante para entidade nullable (usa o holder de um item conhecido). */
    public static int getEnchantmentLevel(Holder<Enchantment> enchantment, LivingEntity entity, int fallback) {
        return entity == null ? fallback : EnchantmentHelper.getEnchantmentLevel(enchantment, entity);
    }

    /**
     * [PORT 1.21.1] Le o nivel de um enchantment por ResourceKey direto do ItemStack,
     * sem precisar resolver o Holder via registry (que virou data-driven).
     */
    public static int getItemEnchantmentLevel(net.minecraft.resources.ResourceKey<Enchantment> key, ItemStack stack) {
        if (key == null || stack == null || stack.isEmpty()) {
            return 0;
        }
        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().unwrapKey().filter(k -> k.equals(key)).isPresent()) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    /** Soma o nivel do enchantment (por ResourceKey) nos slots de equipamento da entidade. */
    public static int getEnchantmentLevel(net.minecraft.resources.ResourceKey<Enchantment> key, LivingEntity entity) {
        if (key == null || entity == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack stack : entity.getAllSlots()) {
            total += getItemEnchantmentLevel(key, stack);
        }
        return total;
    }

    /** [PORT 1.21.1] com.mna.api.enchantments.MNAEnchantmentHelper.getBlockEfficiency(LivingEntity) foi removido. */
    public static int getBlockEfficiency(net.minecraft.world.entity.LivingEntity entity) {
        return (int)EnchantmentHelper.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY, entity);
    }

    /** [PORT 1.21.1] EnchantmentHelper.getDamageBonus removido; o bonus agora passa pelo pipeline modifyDamage. */
    public static float getDamageBonus(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.LivingEntity target) {
        return 0.0f;
    }

    /** [PORT 1.21.1] com.mna.api.enchantments.MNAEnchantmentHelper.getKnockbackBonus(LivingEntity) removido; knockback agora via pipeline de ataque. */
    public static int getKnockbackBonus(net.minecraft.world.entity.LivingEntity entity) {
        return 0;
    }

    /** [PORT 1.21.1] com.mna.api.enchantments.MNAEnchantmentHelper.getFireAspect(LivingEntity) removido. */
    public static int getFireAspect(net.minecraft.world.entity.LivingEntity entity) {
        return 0;
    }

    /** [PORT 1.21.1] EnchantmentHelper.doPostHurtEffects removido -> no-op. */
    public static void doPostHurtEffects(net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.Entity attacker) {
    }

    /** [PORT 1.21.1] EnchantmentHelper.doPostDamageEffects removido -> no-op. */
    public static void doPostDamageEffects(net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.Entity attacker) {
    }

    /** [PORT 1.21.1] EnchantmentHelper.hasChanneling removido -> false (comportamento via enchantment effect). */
    public static boolean hasChanneling(ItemStack stack) {
        return false;
    }
}
