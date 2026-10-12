/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleRenderType
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
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXBlood
extends MAParticleBase {
    public FXBlood(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd = 0.0;
        this.xd = 0.0;
        this.zd = 0.0;
        this.setColor(0.42f, 0.05f, 0.05f);
        this.setAlpha(0.0f);
        this.oRoll = this.roll = (float)(-45.0 + Math.random() * 90.0 / (Math.PI / 180));
        this.gravity = 0.003f;
        this.lifetime = 30;
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
        this.fixedSprite = true;
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
        BlockPos blockpos = BlockPos.containing((double)this.x, (double)this.y, (double)this.z);
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor((BlockAndTintGetter)this.level, (BlockPos)blockpos) : 0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXBloodFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXBloodFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXBlood particle = new FXBlood(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.gravity = 0.01f;
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


