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

public class ConstructMaterialObsidian
extends ConstructMaterial {
    public static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/animated_construct/obsidian.png");

    @Override
    public int getHealth() {
        return 8;
    }

    @Override
    public float getBuoyancy() {
        return 0.2f;
    }

    @Override
    public float getSpeed() {
        return 0.09f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.4f;
    }

    @Override
    public float getExplosionResistance() {
        return 0.16f;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public Tier getEquivalentTier() {
        return Tiers.DIAMOND;
    }

    @Override
    public List<ItemStack> getDeathLootMaterialDrops(IConstruct<?> constrcut, DamageSource source) {
        return Arrays.asList(new ItemStack((ItemLike)Items.OBSIDIAN, 3));
    }

    @Override
    public float getCooldownMultiplierFor(ConstructCapability action) {
        if (action == ConstructCapability.MINE || action == ConstructCapability.CHOP_WOOD) {
            return 0.4f;
        }
        return 0.65f;
    }

    @Override
    public int getArmorBonus(ConstructSlot slot) {
        return 5;
    }

    @Override
    public int getToughnessBonus(ConstructSlot slot) {
        return 0;
    }

    @Override
    public float getDamageBonus() {
        return 5.0f;
    }

    @Override
    public int getIntelligenceBonus() {
        return 20;
    }

    @Override
    public int getManaStorage() {
        return 1000;
    }

    @Override
    public float getRangedDamageBonus() {
        return 10.0f;
    }

    @Override
    public float getRangedManaCost() {
        return 100.0f;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("mna", "obsidian");
    }

    @Override
    public int getBackpackCapacityBoost() {
        return 32;
    }

    @Override
    public int getCastingTierEquivalent() {
        return 4;
    }
}

