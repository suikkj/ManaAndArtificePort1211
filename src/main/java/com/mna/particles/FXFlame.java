/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.SpriteSet
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.particles;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.base.MAParticleBase;
import com.mna.tools.math.Vector3;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXFlame
extends MAParticleBase {
    public FXFlame(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.setColor(1.0f, 0.0f, 1.0f);
        this.oRoll = this.roll = (float)(-45.0 + Math.random() * 90.0 / (Math.PI / 180));
        this.quadSize *= 0.25f;
        this.lifetime = 20;
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (1.0f + (float)this.age / (float)this.lifetime * 3.0f);
    }

    private void setupColorTransitionNormal() {
        this.colorTransitions.add(new Vector3(1.0, 0.8470588326454163, 0.0));
        this.colorTransitions.add(new Vector3(1.0, 0.21960784494876862, 0.0));
        this.colorTransitions.add(new Vector3(0.21960784494876862, 0.0, 0.0));
        this.setColor(((Vector3)this.colorTransitions.get((int)0)).x, ((Vector3)this.colorTransitions.get((int)0)).y, ((Vector3)this.colorTransitions.get((int)0)).z);
    }

    private void setupColorTransitionHellfire() {
        this.colorTransitions.add(new Vector3(0.25882354378700256, 0.7176470756530762, 0.07450980693101883));
        this.colorTransitions.add(new Vector3(0.545098066329956, 0.7176470756530762, 0.07450980693101883));
        this.colorTransitions.add(new Vector3(0.25882354378700256, 0.7333333492279053, 0.2627451f));
        this.setColor(((Vector3)this.colorTransitions.get((int)0)).x, ((Vector3)this.colorTransitions.get((int)0)).y, ((Vector3)this.colorTransitions.get((int)0)).z);
    }

    @Override
    protected boolean apply_aging() {
        float agePct;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        float color_T = (double)(agePct = (float)this.age / (float)this.lifetime) < 0.5 ? agePct / 0.5f : (agePct - 0.5f) / 0.5f;
        this.lerpAlpha(agePct);
        if (this.colorTransitions.size() >= 3) {
            Vector3 startColor = (Vector3)this.colorTransitions.get(0);
            Vector3 midColor = (Vector3)this.colorTransitions.get(1);
            Vector3 endColor = (Vector3)this.colorTransitions.get(2);
            Vector3 color = (double)agePct < 0.5 ? Vector3.lerp(startColor, midColor, color_T) : Vector3.lerp(midColor, endColor, color_T);
            this.setColor(color.x, color.y, color.z);
        }
        return this.removed;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF00000;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXHellfireFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXHellfireFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXFlame particle = new FXFlame(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.setAlpha(0.0f);
            particle.setupColorTransitionHellfire();
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXFlameOrbitFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXFlameOrbitFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double forwardSpeed, double upSpeed, double radius) {
            FXFlame particle = new FXFlame(worldIn, x, y, z, this.spriteSet);
            particle.setMoveOrbit(x, y, z, forwardSpeed, upSpeed, radius);
            particle.setAlpha(0.0f);
            particle.setupColorTransitionNormal();
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXFlameLerpFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXFlameLerpFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXFlame particle = new FXFlame(worldIn, x, y, z, this.spriteSet);
            particle.setMoveLerp(x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.0f);
            particle.setupColorTransitionNormal();
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXFlameFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXFlameFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXFlame particle = new FXFlame(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.setAlpha(0.0f);
            particle.setupColorTransitionNormal();
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


