/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.api.entities.construct.materials;

import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstruct;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;

public class ConstructMaterialIron
extends ConstructMaterial {
    public static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/animated_construct/iron.png");

    @Override
    public int getHealth() {
        return 14;
    }

    @Override
    public float getBuoyancy() {
        return -3.0f;
    }

    @Override
    public float getSpeed() {
        return 0.09f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.3f;
    }

    @Override
    public float getExplosionResistance() {
        return 0.04f;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public Tier getEquivalentTier() {
        return Tiers.IRON;
    }

    @Override
    public List<ItemStack> getDeathLootMaterialDrops(IConstruct<?> constrcut, DamageSource source) {
        return Arrays.asList(new ItemStack((ItemLike)Items.IRON_INGOT, 3));
    }

    @Override
    public float getCooldownMultiplierFor(ConstructCapability action) {
        if (action == ConstructCapability.MINE || action == ConstructCapability.CHOP_WOOD) {
            return 0.5f;
        }
        return 0.75f;
    }

    @Override
    public int getArmorBonus(ConstructSlot slot) {
        return 8;
    }

    @Override
    public int getToughnessBonus(ConstructSlot slot) {
        return 3;
    }

    @Override
    public float getDamageBonus() {
        return 3.0f;
    }

    @Override
    public int getIntelligenceBonus() {
        return 16;
    }

    @Override
    public int getManaStorage() {
        return 400;
    }

    @Override
    public float getRangedDamageBonus() {
        return 7.0f;
    }

    @Override
    public float getRangedManaCost() {
        return 50.0f;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("mna", "iron");
    }

    @Override
    public int getBackpackCapacityBoost() {
        return 16;
    }

    @Override
    public int getCastingTierEquivalent() {
        return 3;
    }
}

