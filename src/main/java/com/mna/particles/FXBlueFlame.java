/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  javax.annotation.Nonnull
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.particles;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.base.MAParticleBase;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXBlueFlame
extends MAParticleBase {
    private static final ParticleRenderType NORMAL_RENDER = new ParticleRenderType(){

        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            FXBlueFlame.beginRenderCommon(bufferBuilder, textureManager);
            return bufferBuilder;
        }

        public String toString() {
            return "mna:blue_flame";
        }
    };

    public FXBlueFlame(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.setColor(1.0f, 0.0f, 1.0f);
        this.quadSize *= 0.45f;
        this.lifetime = 20;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprite);
    }

    private void setMoveVelocity(double x, double y, double z) {
        this.xd = x;
        this.yd = y;
        this.zd = z;
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (float)(this.lifetime - this.age + 1) / (float)this.lifetime;
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF00000;
    }

    @Override
    @Nonnull
    public ParticleRenderType getRenderType() {
        return NORMAL_RENDER;
    }

    protected static void beginRenderCommon(BufferBuilder buffer, TextureManager textureManager) {
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((int)770, (int)1);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)TextureAtlas.LOCATION_PARTICLES);
        AbstractTexture tex = textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES);
        tex.setBlurMipmap(true, false);
    }

    protected static void endRenderCommon() {
        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)true);
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXBlueFlameRandomFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXBlueFlameRandomFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXBlueFlame particle = new FXBlueFlame(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.85f);
            particle.setColor(1.0f, 1.0f, 1.0f);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


