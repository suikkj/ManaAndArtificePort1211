/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.storage.loot.LootContext
 */
package com.mna.api.entities.construct;

import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.items.TieredItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;

public abstract class ItemConstructPart
extends TieredItem {
    private ConstructMaterial _material;
    private ConstructSlot _slot;
    private int _mutex;

    public ItemConstructPart(ConstructMaterial material, ConstructSlot slot, int modelTypeMutex) {
        super(new Item.Properties().stacksTo(1).setNoRepair());
        this._material = material;
        this._slot = slot;
        this._mutex = modelTypeMutex;
    }

    public ConstructMaterial getMaterial() {
        return this._material;
    }

    public ConstructSlot getSlot() {
        return this._slot;
    }

    public int getModelTypeMutex() {
        return this._mutex;
    }

    public boolean isMutex(int mutex) {
        return (this._mutex & mutex) != 0;
    }

    public boolean isFireResistant() {
        return this._material == ConstructMaterial.OBSIDIAN;
    }

    public float getAttackDamage() {
        return 0.0f;
    }

    public float getRangedAttackDamage() {
        return 0.0f;
    }

    public float getKnockbackBonus() {
        return 0.0f;
    }

    public float getBonusSpeed() {
        return 0.0f;
    }

    public int getArmor() {
        return 0;
    }

    public int getToughness() {
        return 0;
    }

    public int getIntelligenceBonus() {
        return 0;
    }

    public int getPerceptionDistanceBonus() {
        return 0;
    }

    public int getManaCapacity() {
        return 0;
    }

    public int getFluidCapacity() {
        return 0;
    }

    public float getActionSpeed(ConstructCapability capability) {
        return 0.0f;
    }

    public int getAttackSpeedModifier() {
        return 0;
    }

    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[0];
    }

    public int getInventorySizeBonus() {
        return 0;
    }

    public int getBackpackCapacityBoost() {
        return 0;
    }

    public boolean canBeInLoot(LootContext context) {
        return this.getMaterial() != ConstructMaterial.DIAMOND && this.getMaterial() != ConstructMaterial.OBSIDIAN;
    }
}

