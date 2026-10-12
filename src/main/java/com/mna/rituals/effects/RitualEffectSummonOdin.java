/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.boss.effects.Bifrost;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RitualEffectSummonOdin
extends RitualEffect {
    public RitualEffectSummonOdin(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        if (!context.getLevel().canSeeSky(context.getCenter())) {
            return Component.translatable((String)"mna:rituals/odins_call.sky");
        }
        if (!context.getLevel().isRainingAt(context.getCenter()) || !context.getLevel().isThundering()) {
            return Component.translatable((String)"mna:rituals/odins_call.storm");
        }
        return super.canRitualStart(context);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        Bifrost bifrost = new Bifrost(context.getLevel(), new Vec3((double)context.getCenter().getX(), (double)(context.getCenter().getY() + 10), (double)context.getCenter().getZ()));
        context.getLevel().addFreshEntity((Entity)bifrost);
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }
}

