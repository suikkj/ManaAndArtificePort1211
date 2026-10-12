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

public class FXFrost
extends MAParticleBase {
    public FXFrost(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        Vector3 firstColor = new Vector3(0.40784314274787903, 0.6431372761726379, 0.972549f);
        Vector3 secondColor = new Vector3(0.5490196347236633, 0.5882353186607361, 0.627451f);
        Vector3 color = Vector3.lerp(firstColor, secondColor, (float)Math.random());
        this.setColor(color.x, color.y, color.z);
        this.oRoll = this.roll = (float)(-45.0 + Math.random() * 90.0 / (Math.PI / 180));
        this.quadSize *= 0.15f;
        this.lifetime = 30;
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (1.0f + (float)this.age / (float)this.lifetime * 3.0f);
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        this.roll = (float)((double)this.roll + Math.random() * Math.PI / 180.0);
        super.tick();
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF00000;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXFrostLerpFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXFrostLerpFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXFrost particle = new FXFrost(worldIn, x, y, z, this.spriteSet);
            particle.setMoveLerp(x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.85f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXMistFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXMistFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXFrost particle = new FXFrost(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.setAlpha(0.85f);
            particle.quadSize = 0.25f;
            particle.lifetime = 20;
            particle.setColor(0.49019608f, 0.6784314f, 0.5411765f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXFrostFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXFrostFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXFrost particle = new FXFrost(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.setAlpha(0.85f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


