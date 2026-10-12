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

public class ConstructMaterialWickerwood
extends ConstructMaterial {
    public static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/animated_construct/wheatwood.png");

    @Override
    public int getHealth() {
        return 2;
    }

    @Override
    public float getBuoyancy() {
        return 2.0f;
    }

    @Override
    public float getSpeed() {
        return 0.1f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0f;
    }

    @Override
    public float getExplosionResistance() {
        return -0.5f;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public Tier getEquivalentTier() {
        return Tiers.WOOD;
    }

    @Override
    public float getCooldownMultiplierFor(ConstructCapability capability) {
        return 1.15f;
    }

    @Override
    public List<ItemStack> getDeathLootMaterialDrops(IConstruct<?> constrcut, DamageSource source) {
        return Arrays.asList(new ItemStack((ItemLike)Items.HAY_BLOCK, 3));
    }

    @Override
    public int getArmorBonus(ConstructSlot slot) {
        return 0;
    }

    @Override
    public int getToughnessBonus(ConstructSlot slot) {
        return 0;
    }

    @Override
    public float getDamageBonus() {
        return 1.0f;
    }

    @Override
    public int getIntelligenceBonus() {
        return 8;
    }

    @Override
    public int getManaStorage() {
        return 250;
    }

    @Override
    public float getRangedDamageBonus() {
        return 2.0f;
    }

    @Override
    public float getRangedManaCost() {
        return 10.0f;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("mna", "wickerwood");
    }

    @Override
    public int getBackpackCapacityBoost() {
        return 4;
    }

    @Override
    public int getCastingTierEquivalent() {
        return 1;
    }
}

