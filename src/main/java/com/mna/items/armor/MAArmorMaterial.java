/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.item.ArmorItem$Type
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 */
package com.mna.items.armor;

import com.mna.items.ItemInit;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public enum MAArmorMaterial
{
    BROKEN("broken", 5, new int[]{0, 0, 0, 0}, 0, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, () -> Ingredient.of((ItemLike[])new ItemLike[]{(ItemLike)ItemInit.INFUSED_SILK.get()})),
    INFUSED_SILK("infused_silk", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, () -> Ingredient.of((ItemLike[])new ItemLike[]{(ItemLike)ItemInit.INFUSED_SILK.get()})),
    BONE("bone", 33, new int[]{3, 6, 8, 3}, 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0f, 0.0f, () -> Ingredient.of((ItemLike[])new ItemLike[]{Items.WITHER_SKELETON_SKULL})),
    DEMON("demon", 33, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f, () -> Ingredient.of((ItemLike[])new ItemLike[]{Items.NETHERITE_INGOT})),
    FEY("fey", 33, new int[]{2, 5, 6, 2}, 9, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, () -> Ingredient.of((ItemLike[])new ItemLike[]{Blocks.MYCELIUM})),
    COUNCIL("council", 33, new int[]{1, 4, 5, 2}, 12, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0f, 0.0f, () -> Ingredient.of((ItemLike[])new ItemLike[]{(ItemLike)ItemInit.RUNIC_SILK.get()}));

    private static final int[] MAX_DAMAGE_ARRAY;
    private final String name;
    private final int maxDamageFactor;
    private final int[] damageReductionAmountArray;
    private final int enchantability;
    private final net.minecraft.core.Holder<SoundEvent> soundEvent;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairMaterial;

    private MAArmorMaterial(String name, int maxDamageFactor, int[] damageReductionAmountArray, int enchantability, net.minecraft.core.Holder<SoundEvent> soundEvent, float toughness, float knockbackResistance, Supplier<Ingredient> repairMaterial) {
        this.name = name;
        this.maxDamageFactor = maxDamageFactor;
        this.damageReductionAmountArray = damageReductionAmountArray;
        this.enchantability = enchantability;
        this.soundEvent = soundEvent;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairMaterial = repairMaterial;
    }

    public int getDurabilityForType(ArmorItem.Type pType) {
        return MAX_DAMAGE_ARRAY[pType.getSlot().getIndex()] * this.maxDamageFactor;
    }

    public int getDefenseForType(ArmorItem.Type pType) {
        return this.damageReductionAmountArray[pType.getSlot().getIndex()];
    }

    public int getEnchantmentValue() {
        return this.enchantability;
    }

    public net.minecraft.core.Holder<SoundEvent> getEquipSound() {
        return this.soundEvent;
    }

    public Ingredient getRepairIngredient() {
        return this.repairMaterial.get();
    }

    public String getName() {
        return "mna:" + this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    static {
        MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    }
}

