/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.entities.construct;

import com.mna.api.affinity.Affinity;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.spells.base.ISpellDefinition;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IConstructConstruction {
    public boolean isCapabilityEnabled(ConstructCapability var1);

    public boolean areCapabilitiesEnabled(ConstructCapability ... var1);

    public boolean isAnyCapabilityEnabled(ConstructCapability ... var1);

    @Nullable
    public ConstructMaterial getLowestMaterialCooldownMultiplierForCapability(ConstructCapability var1);

    public List<ConstructMaterial> getComposition();

    default public ItemStack setPart(ItemStack partStack) {
        return this.setPart(partStack, false);
    }

    public ItemStack setPart(ItemStack var1, boolean var2);

    public ItemStack getHat();

    public void setHat(ItemStack var1);

    public ItemStack getBanner();

    public void setBanner(ItemStack var1);

    public ISpellDefinition[] getCastableSpells();

    @Nullable
    public ISpellDefinition getCastableSpell(ConstructSlot var1);

    public List<ItemConstructPart> getPartsForMaterial(ConstructMaterial var1);

    public ConstructMaterial getHighestMatieralForCapability(ConstructCapability var1);

    public ConstructMaterial getSlotMaterial(ConstructSlot var1);

    public Affinity getRandomContainedAffinity();

    public void resetParts();

    public int getMaximumTier();

    public int calculateArmor();

    public int calculateAttackRate();

    public float calculateBuoyancy();

    public float calculateDamage();

    public float calculateExplosionResistance();

    public int calculateFluidCapacity();

    public int calculateIntelligence();

    public float calculateKnockback();

    public float calculateKnockbackResistance();

    public float calculateMana();

    public int calculateMaxHealth();

    public int calculatePerception();

    public float calculateRangedDamage();

    public float calculateSpeed();

    public int calculateToughness();

    public boolean isComplete();

    public boolean isEmpty();

    public IConstructConstruction copy();

    public int getAffinityScore(Affinity var1);

    public void setAffinityScore(Affinity var1, int var2);

    public ConstructCapability[] getEnabledCapabilities();

    public Optional<ItemConstructPart> getPart(ConstructSlot var1);

    public ItemStack removePart(ConstructSlot var1);

    public void ReadNBT(CompoundTag var1);

    public void WriteNBT(CompoundTag var1);

    public int calculateInventorySize();

    public int calculateInventoryStackLimit();
}

