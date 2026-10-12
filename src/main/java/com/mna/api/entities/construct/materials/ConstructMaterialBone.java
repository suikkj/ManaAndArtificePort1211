/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.item.Item
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;

public class ConstructMaterialBone
extends ConstructMaterial {
    public static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/animated_construct/stone.png");

    @Override
    public int getHealth() {
        return 5;
    }

    @Override
    public float getBuoyancy() {
        return 1.0f;
    }

    @Override
    public float getSpeed() {
        return 0.13f;
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public float getExplosionResistance() {
        return 0.0f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0f;
    }

    @Override
    public Tier getEquivalentTier() {
        return Tiers.IRON;
    }

    @Override
    public List<ItemStack> getDeathLootMaterialDrops(IConstruct<?> constrcut, DamageSource source) {
        ArrayList<Item> items = new ArrayList<Item>();
        items.add(Items.BONE);
        if (items != null && items.size() > 0) {
            return Arrays.asList(new ItemStack((ItemLike)items.get((int)(Math.random() * (double)items.size())), 3));
        }
        return Arrays.asList(new ItemStack[0]);
    }

    @Override
    public float getCooldownMultiplierFor(ConstructCapability action) {
        return 0.9f;
    }

    @Override
    public int getArmorBonus(ConstructSlot slot) {
        return 4;
    }

    @Override
    public int getToughnessBonus(ConstructSlot slot) {
        return 1;
    }

    @Override
    public float getDamageBonus() {
        return 3.0f;
    }

    @Override
    public int getIntelligenceBonus() {
        return 8;
    }

    @Override
    public int getManaStorage() {
        return 500;
    }

    @Override
    public float getRangedDamageBonus() {
        return 3.0f;
    }

    @Override
    public float getRangedManaCost() {
        return 25.0f;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("mna", "bone");
    }

    @Override
    public int getBackpackCapacityBoost() {
        return 8;
    }

    @Override
    public int getCastingTierEquivalent() {
        return 3;
    }
}

