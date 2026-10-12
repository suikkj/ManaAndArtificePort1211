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

public class FXBlueSparkle
extends MAParticleBase {
    public FXBlueSparkle(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        Vector3 color = Vector3.lerp(new Vector3(0.02745098f, 0.16862745583057404, 0.6078431606292725), new Vector3(0.4f, 0.6196078658103943, 0.9137254953384399), (float)Math.random());
        this.setColor(color.x, color.y, color.z);
        this.quadSize = (float)((double)this.quadSize * ((double)0.15f + Math.random() * (double)0.15f));
        this.alpha = 0.0f;
        this.maxAlpha = 1.0f;
        this.lifetime = (int)(20.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = true;
        this.setSpriteFromAge(sprite);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXBlueSparkleVelocityFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXBlueSparkleVelocityFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXBlueSparkle particle = new FXBlueSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, false);
            particle.quadSize = 0.05f;
            particle.lifetime = (int)(10.0 / (Math.random() * 0.8 + 0.2));
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXBlueSparkleOrbitFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXBlueSparkleOrbitFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double forward, double up, double radius) {
            FXBlueSparkle particle = new FXBlueSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveOrbit(x, y, z, forward, up, radius);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXBlueSparkleSphereOrbitFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXBlueSparkleSphereOrbitFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double forward, double tilt, double radius) {
            FXBlueSparkle particle = new FXBlueSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveSphereOrbit(x, y, z, forward, tilt, radius);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXBlueSparkleStationaryFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXBlueSparkleStationaryFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXBlueSparkle particle = new FXBlueSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveStationary();
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXBlueSparkleGravityFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXBlueSparkleGravityFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXBlueSparkle particle = new FXBlueSparkle(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.gravity = 0.003f;
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


