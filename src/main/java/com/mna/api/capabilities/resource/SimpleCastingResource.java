/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package com.mna.api.capabilities.resource;

import com.mna.api.capabilities.resource.ICastingResource;
import com.mna.api.capabilities.resource.SyncStatus;
import java.util.HashMap;
import net.minecraft.world.entity.LivingEntity;

public abstract class SimpleCastingResource
implements ICastingResource {
    protected int ticks_for_regeneration;
    protected float amount;
    protected float maximum_baseline;
    protected SyncStatus sync_status;
    protected HashMap<String, Float> maximumModifiers = new HashMap();
    protected HashMap<String, Float> regenerationModifiers = new HashMap();

    public SimpleCastingResource(int ticks_for_regeneration) {
        this.ticks_for_regeneration = ticks_for_regeneration;
    }

    @Override
    public SyncStatus getSyncStatus() {
        return this.sync_status;
    }

    @Override
    public void clearSyncStatus() {
        this.sync_status = SyncStatus.NOT_NEEDED;
    }

    @Override
    public void setNeedsSync() {
        this.sync_status = SyncStatus.IMMEDIATE;
    }

    @Override
    public float getAmount() {
        return this.amount;
    }

    @Override
    public void setAmount(float amount) {
        if (this.amount == amount) {
            return;
        }
        this.amount = amount;
        if (this.amount > this.getMaxAmount()) {
            this.amount = this.getMaxAmount();
        } else if (this.amount < 0.0f) {
            this.amount = 0.0f;
        }
        if (this.sync_status == SyncStatus.NOT_NEEDED) {
            this.sync_status = SyncStatus.LAZY;
        }
    }

    @Override
    public void consume(LivingEntity caster, float amount) {
        this.setAmount(this.getAmount() - amount);
    }

    @Override
    public boolean hasEnough(LivingEntity caster, float amount) {
        return this.hasEnoughAbsolute(caster, amount);
    }

    @Override
    public boolean hasEnoughAbsolute(LivingEntity caster, float amount) {
        return this.getAmount() >= amount;
    }

    @Override
    public void restore(float amount) {
        this.setAmount(this.getAmount() + amount);
    }

    @Override
    public float getMaxAmount() {
        float calculatedAmount = this.getMaxAmountBaseline();
        for (Float f : this.maximumModifiers.values()) {
            calculatedAmount += f.floatValue();
        }
        return calculatedAmount;
    }

    @Override
    public float getMaxAmountBaseline() {
        return this.maximum_baseline;
    }

    @Override
    public void setMaxAmount(float amount) {
        if (this.maximum_baseline == amount) {
            return;
        }
        this.maximum_baseline = amount;
        if (this.maximum_baseline < 0.0f) {
            this.maximum_baseline = 0.0f;
        }
        if (this.amount > this.getMaxAmount()) {
            this.amount = this.getMaxAmount();
        }
        this.sync_status = SyncStatus.IMMEDIATE;
    }

    @Override
    public void addModifier(String id, float amount) {
        if (!this.maximumModifiers.containsKey(id) || this.maximumModifiers.get(id).floatValue() != amount) {
            this.maximumModifiers.put(id, Float.valueOf(amount));
            this.sync_status = SyncStatus.IMMEDIATE;
        }
    }

    @Override
    public HashMap<String, Float> getModifiers() {
        return this.maximumModifiers;
    }

    @Override
    public void removeModifier(String id) {
        if (this.maximumModifiers.containsKey(id)) {
            this.maximumModifiers.remove(id);
        }
        this.sync_status = SyncStatus.IMMEDIATE;
    }

    @Override
    public void clearModifiers() {
        this.maximumModifiers.clear();
    }

    @Override
    public int getRegenerationRate(LivingEntity caster) {
        return (int)((float)this.ticks_for_regeneration * this.getRegenerationModifier(caster));
    }

    @Override
    public void setRegenerationRate(int ticksForFullRegen) {
        this.ticks_for_regeneration = ticksForFullRegen;
    }

    @Override
    public void addRegenerationModifier(String id, float pct) {
        if (!this.regenerationModifiers.containsKey(id) || this.regenerationModifiers.get(id).floatValue() != pct) {
            this.regenerationModifiers.put(id, Float.valueOf(pct));
            this.sync_status = SyncStatus.IMMEDIATE;
        }
    }

    @Override
    public HashMap<String, Float> getRegenerationModifiers() {
        return this.regenerationModifiers;
    }

    @Override
    public void removeRegenerationModifier(String id) {
        if (this.regenerationModifiers.containsKey(id)) {
            this.regenerationModifiers.remove(id);
        }
        this.sync_status = SyncStatus.IMMEDIATE;
    }

    @Override
    public void clearRegenerationModifiers() {
        this.regenerationModifiers.clear();
    }

    @Override
    public float getRegenerationModifier(LivingEntity caster) {
        float mod = 1.0f;
        for (float value : this.regenerationModifiers.values()) {
            mod += mod * value;
        }
        return mod;
    }
}

