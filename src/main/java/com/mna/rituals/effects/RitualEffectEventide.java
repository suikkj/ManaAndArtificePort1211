/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.TimeChangeBall;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class RitualEffectEventide
extends RitualEffect {
    public RitualEffectEventide(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if (!context.getLevel().isDay()) {
            return false;
        }
        TimeChangeBall auroraBall = new TimeChangeBall((EntityType)EntityInit.STARBALL_ENTITY.get(), context.getLevel());
        auroraBall.setPos(Vec3.atCenterOf((Vec3i)context.getCenter().above(2)));
        context.getLevel().addFreshEntity((Entity)auroraBall);
        auroraBall.setTimeChangeType(TimeChangeBall.TIME_CHANGE_NIGHT);
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 120;
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        if (!context.getLevel().isDay()) {
            return Component.translatable((String)"ritual.mna.eventide.nighttime");
        }
        return null;
    }
}

