/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockAndTintGetter
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXEarth
extends MAParticleBase {
    public FXEarth(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd = -Math.random() * (double)0.03f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.oRoll = this.roll = (float)(-45.0 + Math.random() * 90.0 / (Math.PI / 180));
        this.setAlpha(0.0f);
        Vector3 first = new Vector3(56.0, 182.0, 106.0);
        Vector3 second = new Vector3(178.0, 162.0, 56.0);
        Vector3 color = Vector3.lerp(first, second, (float)Math.random());
        this.colorTransitions.add(color);
        this.colorTransitions.add(color);
        this.quadSize *= 0.35f;
        this.lifetime = 30;
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    private void setMoveVelocity(double x, double y, double z) {
        this.setColor((float)x, (float)y, (float)z);
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize;
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    protected int getLightColor(float partialTick) {
        BlockPos blockpos = BlockPos.containing((double)this.x, (double)this.y, (double)this.z);
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor((BlockAndTintGetter)this.level, (BlockPos)blockpos) : 0;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXEarthFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXEarthFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXEarth particle = new FXEarth(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


