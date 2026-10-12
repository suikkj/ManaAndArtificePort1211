/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.utility.WanderingWizard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RitualEffectWanderingWizard
extends RitualEffect {
    public RitualEffectWanderingWizard(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if (context.getLevel().isClientSide()) {
            return true;
        }
        WanderingWizard entity = new WanderingWizard(context.getLevel());
        entity.setPos((double)context.getCenter().getX() + 0.5, context.getCenter().getY(), (double)context.getCenter().getZ() + 0.5);
        entity.setDespawnDelay(1200);
        context.getLevel().addFreshEntity((Entity)entity);
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }
}

