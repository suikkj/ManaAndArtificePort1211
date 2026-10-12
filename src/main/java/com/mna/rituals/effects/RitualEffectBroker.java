/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.mna.rituals.effects;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.sound.SFX;
import com.mna.entities.faction.Broker;
import com.mojang.math.Axis;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class RitualEffectBroker
extends RitualEffect {
    HashMap<BlockPos, Vector3f> effectWindDirections = new HashMap();

    public RitualEffectBroker(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if (context.getLevel().isClientSide()) {
            return true;
        }
        Broker entity = new Broker(context.getLevel());
        entity.setPos((double)context.getCenter().getX() + 0.5, context.getCenter().getY(), (double)context.getCenter().getZ() + 0.5);
        entity.setDespawnDelay(12000);
        context.getLevel().addFreshEntity((Entity)entity);
        context.getLevel().playSound(null, (double)context.getCenter().getX(), (double)context.getCenter().getY(), (double)context.getCenter().getZ(), SFX.Event.Ritual.IRON_BELL, SoundSource.AMBIENT, 1.0f, 1.0f);
        this.effectWindDirections.remove(context.getCenter());
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        Vec3 pos;
        BlockPos block;
        int i;
        RandomSource rand = context.getLevel().getRandom();
        Vector3f wind = this.effectWindDirections.getOrDefault(context.getCenter(), new Vector3f(rand.nextFloat() - 0.5f, 0.0f, rand.nextFloat() - 0.5f));
        wind.rotate((Quaternionfc)Axis.YP.rotationDegrees((float)(rand.nextInt(4) - 2)));
        for (i = 0; i < 5; ++i) {
            block = context.getCenter().offset(-3 + rand.nextInt(7), 0, -3 + rand.nextInt(7));
            pos = new Vec3((double)block.getX() + rand.nextDouble(), (double)((float)block.getY() + rand.nextFloat()), (double)block.getZ() + rand.nextDouble());
            context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())), pos.x(), pos.y(), pos.z(), (double)0.04f, (double)0.32f, (double)0.03f);
        }
        for (i = 0; i < 75; ++i) {
            block = context.getCenter().offset(-3 + rand.nextInt(7), 0, -3 + rand.nextInt(7));
            pos = new Vec3((double)block.getX() + rand.nextDouble(), (double)block.getY(), (double)block.getZ() + rand.nextDouble());
            context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), pos.x(), pos.y(), pos.z(), (double)(wind.x() * 0.25f), (double)(wind.y() * 0.25f), (double)(wind.z() * 0.25f));
        }
        if (rand.nextFloat() < 0.1f) {
            BlockPos block2 = context.getCenter().offset(-3 + rand.nextInt(7), 2, -3 + rand.nextInt(7));
            Vec3 pos2 = new Vec3((double)block2.getX() + rand.nextDouble(), (double)((float)block2.getY() + rand.nextFloat() * 2.0f), (double)block2.getZ() + rand.nextDouble());
            for (int i2 = 0; i2 < 50; ++i2) {
                context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_RANDOM.get())), pos2.x(), pos2.y(), pos2.z(), (double)0.05f, (double)0.05f, (double)0.05f);
            }
        }
        this.effectWindDirections.put(context.getCenter(), wind);
        return true;
    }
}

