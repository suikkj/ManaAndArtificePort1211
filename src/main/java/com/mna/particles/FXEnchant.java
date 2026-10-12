/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.levelgen.LegacyRandomSource
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXEnchant
extends MAParticleBase {
    public FXEnchant(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.sprite = sprite.get((RandomSource)new LegacyRandomSource((long)((int)(Math.random() * 4100.0))));
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXEnchantFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXEnchantFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXEnchant particle = new FXEnchant(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, false);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


