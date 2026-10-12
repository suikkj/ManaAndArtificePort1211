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

public class FXGlow
extends MAParticleBase {
    private static final Vector3 firstColor = new Vector3(0.12941177189350128, 0.22745098173618317, 0.8117647171020508);
    private static final Vector3 secondColor = new Vector3(0.2901961f, 0.3803921639919281, 0.9372549057006836);

    public FXGlow(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        Vector3 color = Vector3.lerp(firstColor, secondColor, (float)Math.random());
        this.setColor(color.x, color.y, color.z);
        this.roll = 0.0f;
        this.setAlpha(0.0f);
        this.quadSize *= 0.35f;
        this.lifetime = 30;
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXGlowFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXGlowFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXGlow particle = new FXGlow(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, false);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


