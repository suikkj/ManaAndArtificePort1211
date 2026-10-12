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

public class FXSoul
extends MAParticleBase {
    public FXSoul(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        Vector3 color = new Vector3(0.2980392277240753, 0.24705882370471954, 0.7490196228027344);
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
    public static class FXSoulFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXSoulFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXSoul particle = new FXSoul(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.gravity = 0.003f;
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


