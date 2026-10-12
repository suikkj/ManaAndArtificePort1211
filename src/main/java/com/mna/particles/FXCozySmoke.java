/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleRenderType
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
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXCozySmoke
extends MAParticleBase {
    public FXCozySmoke(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.quadSize *= 0.35f;
        this.lifetime = 30;
        this.hasPhysics = false;
        this.alpha = 1.0f;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    @Override
    protected void lerpAlpha(float agePct) {
        if (this.age >= this.lifetime - 30 && this.alpha > 0.01f) {
            this.alpha -= 0.03f;
        }
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (1.0f + (float)this.age / (float)this.lifetime * 3.0f);
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXCozySmokeFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXCozySmokeFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXCozySmoke particle = new FXCozySmoke(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, false);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


