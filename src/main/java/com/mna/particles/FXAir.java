 
package com.mna.particles;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.base.MAParticleBase;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import javax.annotation.Nonnull;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class FXAir
extends MAParticleBase {
    private static final ParticleRenderType NORMAL_RENDER = new ParticleRenderType(){

        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            FXAir.beginRenderCommon(bufferBuilder, textureManager);
            return bufferBuilder;
        }

        public String toString() {
            return "mna:air";
        }
    };

    public FXAir(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.oRoll = this.roll = (float)(-45.0 + Math.random() * 90.0 / (Math.PI / 180));
        this.setAlpha(0.0f);
        this.quadSize *= 0.05f;
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (1.0f + (float)this.age / (float)this.lifetime * 3.0f);
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
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)TextureAtlas.LOCATION_PARTICLES);
        AbstractTexture tex = textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES);
        tex.setBlurMipmap(true, false);
    }

    protected static void endRenderCommon() {
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3d = renderInfo.getPosition();
        float f = (float)(Mth.lerp((double)partialTicks, (double)this.xo, (double)this.x) - vec3d.x());
        float f1 = (float)(Mth.lerp((double)partialTicks, (double)this.yo, (double)this.y) - vec3d.y());
        float f2 = (float)(Mth.lerp((double)partialTicks, (double)this.zo, (double)this.z) - vec3d.z());
        Vector3 myPos = new Vector3(this.xo, this.yo, this.zo);
        Vector3 projected = new Vector3(this.x, this.y, this.z);
        Quaternionf quaternion = Vector3.LookAt(myPos, projected);
        Vector3f vector3f1 = new Vector3f(-1.0f, -1.0f, 0.0f);
        vector3f1.rotate((Quaternionfc)quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        float f4 = this.getQuadSize(partialTicks);
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate((Quaternionfc)quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }
        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(partialTicks);
        buffer.addVertex((float)avector3f[0].x(), (float)avector3f[0].y(), (float)avector3f[0].z()).setUv(f8, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex((float)avector3f[1].x(), (float)avector3f[1].y(), (float)avector3f[1].z()).setUv(f8, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex((float)avector3f[2].x(), (float)avector3f[2].y(), (float)avector3f[2].z()).setUv(f7, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex((float)avector3f[3].x(), (float)avector3f[3].y(), (float)avector3f[3].z()).setUv(f7, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex((float)avector3f[3].x(), (float)avector3f[3].y(), (float)avector3f[3].z()).setUv(f8, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex((float)avector3f[2].x(), (float)avector3f[2].y(), (float)avector3f[2].z()).setUv(f8, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex((float)avector3f[1].x(), (float)avector3f[1].y(), (float)avector3f[1].z()).setUv(f7, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex((float)avector3f[0].x(), (float)avector3f[0].y(), (float)avector3f[0].z()).setUv(f7, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXAirLerp
    extends MAParticleBase.FXParticleFactoryBase {
        public FXAirLerp(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXAir particle = new FXAir(worldIn, x, y, z, this.spriteSet);
            particle.setMoveLerp(x, y, z, xSpeed, ySpeed, zSpeed);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXAirOrbit
    extends MAParticleBase.FXParticleFactoryBase {
        public FXAirOrbit(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double forwardSpeed, double upSpeed, double radius) {
            FXAir particle = new FXAir(worldIn, x, y, z, this.spriteSet);
            particle.setMoveOrbit(x, y, z, forwardSpeed, upSpeed, radius);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXAirVelocity
    extends MAParticleBase.FXParticleFactoryBase {
        public FXAirVelocity(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXAir particle = new FXAir(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, false);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


