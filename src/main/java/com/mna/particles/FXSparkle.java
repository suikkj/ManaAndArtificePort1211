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
import com.mna.particles.FXMovementType;
import com.mna.particles.base.MAParticleBase;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXSparkle
extends MAParticleBase {
    public FXSparkle(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.setColor(1.0f, 0.0f, 1.0f);
        this.quadSize *= 0.15f;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(sprite);
    }

    private void gravity(double x, double y, double z) {
        this.movementType = FXMovementType.VELOCITY;
        this.xd = x;
        this.yd = y;
        this.zd = z;
        this.gravity = 0.06f;
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (float)(this.lifetime - this.age + 1) / (float)this.lifetime;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF00000;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXSparkleStationaryFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXSparkleStationaryFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXSparkle particle = new FXSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setAlpha(0.85f);
            particle.setColor(1.0f, 1.0f, 1.0f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXSparkleGravityFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXSparkleGravityFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXSparkle particle = new FXSparkle(worldIn, x, y, z, this.spriteSet);
            particle.gravity(xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.85f);
            particle.setColor(1.0f, 1.0f, 1.0f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXSparkleVelocityFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXSparkleVelocityFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXSparkle particle = new FXSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, false);
            particle.setAlpha(0.85f);
            particle.setColor(1.0f, 1.0f, 1.0f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXSparkleBezierPointFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXSparkleBezierPointFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXSparkle particle = new FXSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveBezier(x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.85f);
            particle.setColor(1.0f, 1.0f, 1.0f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXSparkleLerpPointFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXSparkleLerpPointFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXSparkle particle = new FXSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveLerp(x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.85f);
            particle.setColor(1.0f, 1.0f, 1.0f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXSparkleRandomFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXSparkleRandomFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXSparkle particle = new FXSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveRandomly(xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.85f);
            particle.setColor(1.0f, 1.0f, 1.0f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


