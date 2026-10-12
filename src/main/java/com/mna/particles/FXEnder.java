/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
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
import com.mna.particles.types.render.ParticleRenderTypes;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import javax.annotation.Nonnull;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXEnder
extends MAParticleBase {
    public FXEnder(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.oRoll = this.roll = (float)(-45.0 + Math.random() * 90.0 / (Math.PI / 180));
        this.quadSize *= 0.35f;
        this.lifetime = 30;
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    private void setupColorTransitions() {
        this.setAlpha(0.0f);
        this.colorTransitions.add(new Vector3(0.5490196347236633, 0.2627451f, 0.5764706134796143));
        this.colorTransitions.add(new Vector3(0.0, 0.0, 0.0));
        this.setColor(((Vector3)this.colorTransitions.get((int)0)).x, ((Vector3)this.colorTransitions.get((int)0)).y, ((Vector3)this.colorTransitions.get((int)0)).z);
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (1.0f + (float)this.age / (float)this.lifetime * 4.0f);
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF00000;
    }

    @Override
    protected boolean apply_aging() {
        super.apply_aging();
        if (this.colorTransitions.size() >= 2) {
            float agePct = MathUtils.clamp01((float)this.age / (float)this.lifetime);
            Vector3 startColor = (Vector3)this.colorTransitions.get(0);
            Vector3 endColor = (Vector3)this.colorTransitions.get(1);
            Vector3 color = Vector3.lerp(startColor, endColor, agePct);
            this.setColor(color.x, color.y, color.z);
        }
        return this.removed;
    }

    @Override
    @Nonnull
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.INVERTED;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXEnderVelocity
    extends MAParticleBase.FXParticleFactoryBase {
        public FXEnderVelocity(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXEnder particle = new FXEnder(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.setupColorTransitions();
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXEnderBezier
    extends MAParticleBase.FXParticleFactoryBase {
        public FXEnderBezier(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXEnder particle = new FXEnder(worldIn, x, y, z, this.spriteSet);
            particle.setMoveBezier(x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setupColorTransitions();
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


