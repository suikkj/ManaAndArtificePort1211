package com.mna.api.enchantments;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;

/**
 * [PORT 1.21.1] No 1.21.1 {@link Enchantment} deixou de ser uma classe
 * herdavel e virou um {@code record} imutavel, composto por:
 * <ul>
 *   <li>{@link EnchantmentDefinition} (supportedItems, weight, maxLevel, custos, slots)</li>
 *   <li>um {@link DataComponentMap} de efeitos</li>
 * </ul>
 *
 * As classes de enchantment do MNA (que antes herdavam {@code Enchantment} para
 * sobrescrever {@code isDiscoverable()}, {@code getMinLevel()} etc.) nao podem
 * mais fazer isso. Este enum/fachadora preserva as PROPRIEDADES antigas como
 * dados, permitindo que o registro em {@code EnchantmentInit} construa o
 * {@code Enchantment} correspondente.
 *
 * Regra de ouro do port: nao tentar simular heranca; usar os dados.
 */
public final class MAEnchantmentBridge {
    /** Equivalente ao antigo {@code Enchantment.Rarity} (removido no 1.21.1). */
    public enum Rarity {
        COMMON(10), UNCOMMON(5), RARE(2), VERY_RARE(1);

        private final int weight;

        Rarity(int weight) {
            this.weight = weight;
        }

        public int weight() {
            return this.weight;
        }
    }

    /** Equivalente ao antigo {@code EnchantmentCategory} (removido no 1.21.1). */
    public enum Category {
        STAFF_OR_WEAPON, STAVES, ARMOR, ARMOR_FEET, WEAPON, DIGGER, BOW, FISHING_ROD, BREAKABLE, WEARABLE
    }

    private MAEnchantmentBridge() {}

    /**
     * Constroi a definicao vanilla equivalente.
     *
     * @param items        itens suportados (equivalente a categoria)
     * @param rarity       peso/raridade
     * @param maxLevel     nivel maximo
     * @param slots        slots de equipamento
     */
    public static EnchantmentDefinition definition(HolderSet<Item> items, Rarity rarity, int maxLevel,
                                                   List<EquipmentSlotGroup> slots) {
        return Enchantment.definition(items, rarity.weight(), maxLevel,
                Enchantment.constantCost(1), Enchantment.constantCost(rarity.weight() + 1), 1,
                slots.toArray(new EquipmentSlotGroup[0]));
    }

    /** Variante com primaryItems separado (equivalente a categorias "primarias"). */
    public static EnchantmentDefinition definition(HolderSet<Item> items, Optional<HolderSet<Item>> primaryItems,
                                                   Rarity rarity, int maxLevel, List<EquipmentSlotGroup> slots) {
        return new EnchantmentDefinition(items, primaryItems, rarity.weight(), maxLevel,
                Enchantment.constantCost(1), Enchantment.constantCost(rarity.weight() + 1), 1, slots);
    }

    /** [PORT] Equivalente a EnchantmentCategory.WEAPON (tag vanilla). */
    public static boolean isWeapon(Item item) {
        return item.builtInRegistryHolder().is(net.minecraft.tags.ItemTags.WEAPON_ENCHANTABLE);
    }

    /** Tag mna:staves. */
    public static boolean isStaff(Item item) {
        return item.builtInRegistryHolder().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, com.mna.api.tools.MATags.Items.STAVES));
    }

    public static boolean isStaffOrWeapon(Item item) {
        return isStaff(item) || isWeapon(item);
    }

    /** Equivalente a EnchantmentCategory.ARMOR_FEET. */
    public static boolean isBoots(Item item) {
        return item.builtInRegistryHolder().is(net.minecraft.tags.ItemTags.FOOT_ARMOR_ENCHANTABLE);
    }

    /** HolderSet de itens a partir de uma tag (para EnchantmentDefinition). */
    public static HolderSet<Item> itemTag(net.minecraft.tags.TagKey<Item> tag) {
        return net.minecraft.core.HolderSet.emptyNamed((net.minecraft.core.HolderOwner<Item>)(Object)net.minecraft.core.registries.BuiltInRegistries.ITEM, tag);
    }

    public static HolderSet<Item> itemTag(net.minecraft.resources.ResourceLocation tag) {
        return itemTag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, tag));
    }

    /** Cria um Enchantment minimo (sem efeitos) a partir de uma definicao. */
    public static Enchantment build(String nameId, EnchantmentDefinition definition) {
        return new Enchantment(
                Component.translatable("enchantment.mna." + nameId),
                definition,
                HolderSet.empty(),
                DataComponentMap.EMPTY);
    }
}
