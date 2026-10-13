 
package com.mna.effects.particles;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.capabilities.entity.MAPFX;
import com.mna.capabilities.entity.MAPFXProvider;
import com.mna.entities.boss.DemonLord;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.tools.math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.mna.api.tools.MNACap;

public class EffectParticleSpawner {
    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        LivingEntity entity = (LivingEntity)event.getEntity();
        Level world = entity.level();
        boolean isThirdPerson = entity != ManaAndArtifice.instance.proxy.getClientPlayer() || Minecraft.getInstance().gameRenderer.getMainCamera().isDetached();
        MNACap.get(entity, MAPFXProvider.MAPFX).ifPresent(pfx -> {
            Vec3 pos;
            int i;
            pfx.requestSync(entity);
            if ((pfx.getFlag(entity, MAPFX.Flag.FIRE_SHIELD) || pfx.getFlag(entity, MAPFX.Flag.LIVING_BOMB)) && isThirdPerson) {
                int amount = (int)(entity.getBbHeight() * 5.0f);
                MAParticleType type = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_ORBIT.get()));
                if (entity instanceof DemonLord) {
                    type.setColor(Affinity.HELLFIRE.getColor()[0], Affinity.HELLFIRE.getColor()[1], Affinity.HELLFIRE.getColor()[2]);
                    type.setScale(0.075f);
                }
                for (int i2 = 0; i2 < amount; ++i2) {
                    world.addParticle((ParticleOptions)type, entity.getX(), entity.getY() + (double)(entity.getBbHeight() / 3.0f * 2.0f) * Math.random(), entity.getZ(), (double)0.1f, (double)0.01f + Math.random() * (double)0.02f, 1.0 + Math.random());
                }
            }
            if (pfx.getFlag(entity, MAPFX.Flag.MANA_SHIELD) && isThirdPerson) {
                for (i = 0; i < 3; ++i) {
                    double angle = Math.random() * 360.0 / 180.0 * Math.PI;
                    pos = new Vec3(entity.getX() + Math.cos(angle), entity.getY() + 1.9 + Math.random() * 0.2, entity.getZ() + Math.sin(angle));
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
                }
            }
            if (pfx.getFlag(entity, MAPFX.Flag.BRIARTHORN_BARRIER) && isThirdPerson) {
                for (i = 0; i < 3; ++i) {
                    double angle = Math.random() * 360.0 / 180.0 * Math.PI;
                    pos = new Vec3(entity.getX() + Math.cos(angle), entity.getY() + 0.9 + Math.random() * 0.2, entity.getZ() + Math.sin(angle));
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())), pos.x, pos.y, pos.z, 0.0, MathUtils.RandomBetween(0.01, 0.03), 0.0);
                }
            }
            if (pfx.getFlag(entity, MAPFX.Flag.MIST_FORM) && isThirdPerson) {
                for (i = 0; i < 3; ++i) {
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.MIST.get())), entity.getX(), entity.getY() + (double)(entity.getBbHeight() / 2.0f), entity.getZ(), (Math.random() - 0.5) * (double)0.1f, (Math.random() - 0.5) * (double)0.1f, (Math.random() - 0.5) * (double)0.1f);
                }
            }
            if (pfx.getFlag(entity, MAPFX.Flag.AURA_OF_FROST) && !pfx.getFlag(entity, MAPFX.Flag.MIST_FORM)) {
                for (i = 0; i < 3; ++i) {
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setMover(new ParticleOrbitMover(entity.getX(), entity.getY() + (double)0.05f, entity.getZ(), 0.05f, 0.01f, 1.0 + Math.random() * 2.0)), entity.getX(), entity.getY() + (double)0.05f, entity.getZ(), 0.0, Math.random() * (double)0.05f, 0.0);
                }
                for (i = 0; i < 3; ++i) {
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), entity.getX() - 3.0 + Math.random() * 6.0, entity.getY() + (double)0.05f, entity.getZ() - 3.0 + Math.random() * 6.0, 0.0, Math.random() * (double)0.01f, 0.0);
                }
            }
            if (pfx.getFlag(entity, MAPFX.Flag.CIRCLE_OF_POWER) && isThirdPerson) {
                world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setMaxAge(5), entity.getX() - 0.5 + Math.random(), entity.getY() + Math.random() / 2.0, entity.getZ() - 0.5 + Math.random(), 0.0, (double)0.17f, 0.0);
            }
            if (pfx.getFlag(entity, MAPFX.Flag.WIND_WALL) && isThirdPerson) {
                for (i = 0; i < 10; ++i) {
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.05f).setColor(10, 10, 10), entity.getX(), entity.getY(), entity.getZ(), (double)0.1f, (double)0.04f + Math.random() * (double)0.04f, (double)1.2f);
                }
            }
            if (pfx.getFlag(entity, MAPFX.Flag.SOAKED) && Math.random() < 0.5 && !entity.isInWaterOrBubble() && isThirdPerson) {
                world.addParticle((ParticleOptions)ParticleTypes.FALLING_WATER, entity.getX() - 0.5 + Math.random(), entity.getY() + Math.random() * (double)entity.getBbHeight(), entity.getZ() - 0.5 + Math.random(), 0.0, 0.0, 0.0);
            }
            if (pfx.getFlag(entity, MAPFX.Flag.SOUL_TRAP) && Math.random() < 0.5 && isThirdPerson) {
                world.addParticle((ParticleOptions)ParticleTypes.SCULK_SOUL, entity.getX() - 0.5 + Math.random(), entity.getY() + Math.random() * (double)entity.getBbHeight(), entity.getZ() - 0.5 + Math.random(), 0.0, 0.0, 0.0);
            }
            if (pfx.getFlag(entity, MAPFX.Flag.CURSE_OF_AGONY) && isThirdPerson) {
                world.addParticle((ParticleOptions)ParticleTypes.ENCHANT, entity.getX() - 0.5 + Math.random(), entity.getY() + Math.random() * (double)entity.getBbHeight(), entity.getZ() - 0.5 + Math.random(), 0.0, 0.0, 0.0);
                if (world.getGameTime() % 100L == 0L) {
                    for (i = 0; i < 25; ++i) {
                        world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setGravity(0.05f).setPhysics(true), entity.getX() - 0.5 + Math.random(), entity.getY() + Math.random() * (double)entity.getBbHeight(), entity.getZ() - 0.5 + Math.random(), MathUtils.RandomBetween(-0.25, 0.25), MathUtils.RandomBetween(0.0, 0.05), MathUtils.RandomBetween(-0.25, 0.25));
                    }
                }
            }
            if (pfx.getFlag(entity, MAPFX.Flag.SHACKLES_OF_PAIN) && Math.random() < 0.5 && isThirdPerson) {
                world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_GRAVITY.get())).setColor(150, 220, 181).setScale(0.25f).setPhysics(true), entity.getX() - 0.5 + Math.random(), entity.getY() + Math.random() * (double)entity.getBbHeight(), entity.getZ() - 0.5 + Math.random(), 0.0, 0.0, 0.0);
            }
        });
    }
}


