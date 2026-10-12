/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.utility.PresentItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class RitualEffectCreateEssence
extends RitualEffect {
    public RitualEffectCreateEssence(ResourceLocation ritualName) {
        super(ritualName);
    }

    public abstract ItemStack getOutputStack();

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        this.SpawnItem(context.getCenter(), context.getLevel());
        return true;
    }

    protected void SpawnItem(BlockPos ritualCenter, Level world) {
        PresentItem entity = new PresentItem(world, (float)ritualCenter.getX() + 0.5f, ritualCenter.above().getY(), (float)ritualCenter.getZ() + 0.5f, this.getOutputStack());
        world.addFreshEntity((Entity)entity);
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }
}

