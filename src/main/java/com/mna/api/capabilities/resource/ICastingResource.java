/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.capabilities.resource;

import com.mna.api.capabilities.resource.SyncStatus;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ICastingResource {
    public ResourceLocation getRegistryName();

    public SyncStatus getSyncStatus();

    public void clearSyncStatus();

    public float getAmount();

    public boolean hasEnough(LivingEntity var1, float var2);

    public boolean hasEnoughAbsolute(LivingEntity var1, float var2);

    public void setAmount(float var1);

    public void consume(LivingEntity var1, float var2);

    public void restore(float var1);

    public float getMaxAmount();

    public float getMaxAmountBaseline();

    public void setMaxAmount(float var1);

    public void setMaxAmountByLevel(int var1);

    public void addModifier(String var1, float var2);

    public HashMap<String, Float> getModifiers();

    public void removeModifier(String var1);

    public void clearModifiers();

    public int getRegenerationRate(LivingEntity var1);

    public void setRegenerationRate(int var1);

    public void addRegenerationModifier(String var1, float var2);

    public HashMap<String, Float> getRegenerationModifiers();

    public void removeRegenerationModifier(String var1);

    public void clearRegenerationModifiers();

    public float getRegenerationModifier(LivingEntity var1);

    default public boolean hungerAffectsRegenRate() {
        return true;
    }

    default public boolean artificialRestore() {
        return true;
    }

    default public boolean canRechargeFrom(ItemStack batteryItem) {
        return true;
    }

    default public void copyFrom(ICastingResource other) {
        if (other.getRegistryName() != this.getRegistryName()) {
            return;
        }
        this.clearModifiers();
        other.getModifiers().forEach((k, v) -> this.addModifier((String)k, v.floatValue()));
        this.clearRegenerationModifiers();
        other.getRegenerationModifiers().forEach((k, v) -> this.addRegenerationModifier((String)k, v.floatValue()));
        this.setMaxAmount(other.getMaxAmount());
        this.setAmount(other.getAmount());
    }

    default public void writeNBT(CompoundTag nbt) {
        CompoundTag resourceNBT = new CompoundTag();
        resourceNBT.putFloat("amount", this.getAmount());
        resourceNBT.putFloat("maximum", this.getMaxAmountBaseline());
        ListTag maximumModifiers = new ListTag();
        for (Map.Entry<String, Float> entry : this.getModifiers().entrySet()) {
            CompoundTag modifier = new CompoundTag();
            modifier.putString("key", entry.getKey());
            modifier.putFloat("value", entry.getValue().floatValue());
            maximumModifiers.add(modifier);
        }
        resourceNBT.put("maximum_modifiers", (Tag)maximumModifiers);
        ListTag regenerationModifiers = new ListTag();
        for (Map.Entry<String, Float> entry : this.getRegenerationModifiers().entrySet()) {
            CompoundTag modifier = new CompoundTag();
            modifier.putString("key", entry.getKey());
            modifier.putFloat("value", entry.getValue().floatValue());
            regenerationModifiers.add(modifier);
        }
        resourceNBT.put("regeneration_modifiers", (Tag)regenerationModifiers);
        nbt.put(this.getRegistryName().toString(), (Tag)resourceNBT);
    }

    default public void readNBT(CompoundTag nbt) {
        if (nbt.contains(this.getRegistryName().toString())) {
            CompoundTag resourceNBT = nbt.getCompound(this.getRegistryName().toString());
            this.setAmount(resourceNBT.getFloat("amount"));
            this.setMaxAmount(resourceNBT.getFloat("maximum"));
            ListTag maximumModifiers = resourceNBT.getList("maximum_modifiers", 10);
            this.getModifiers().clear();
            maximumModifiers.forEach(inbt -> this.addModifier(((CompoundTag)inbt).getString("key"), ((CompoundTag)inbt).getFloat("value")));
            ListTag regenerationModifiers = resourceNBT.getList("regeneration_modifiers", 10);
            this.getRegenerationModifiers().clear();
            regenerationModifiers.forEach(inbt -> this.addRegenerationModifier(((CompoundTag)inbt).getString("key"), ((CompoundTag)inbt).getFloat("value")));
        }
    }

    public void setNeedsSync();
}

