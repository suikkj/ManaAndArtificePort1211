 
package com.mna.particles;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.base.MAParticleBase;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class FXArcane
extends MAParticleBase {
    private boolean stretchRender = false;
    private boolean shrink = false;

    public FXArcane(ClientLevel world, double x, double y, double z, SpriteSet sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.yd *= (double)0.2f;
        this.xd = 0.0;
        this.zd = 0.0;
        this.colorTransitions.add(new Vector3(234.0, 95.0, 64.0));
        this.colorTransitions.add(new Vector3(203.0, 8.0, 112.0));
        this.colorTransitions.add(new Vector3(151.0, 25.0, 151.0));
        this.colorTransitions.add(new Vector3(77.0, 133.0, 207.0));
        this.setColor(((Vector3)this.colorTransitions.get((int)0)).x / 255.0f, ((Vector3)this.colorTransitions.get((int)0)).y / 255.0f, ((Vector3)this.colorTransitions.get((int)0)).z / 255.0f);
        this.oRoll = this.roll = (float)(-45.0 + Math.random() * 90.0 / (Math.PI / 180));
        this.quadSize *= 0.15f;
        this.setAlpha(0.0f);
        this.lifetime = 30 + (int)Math.ceil(Math.random() * 10.0);
        this.hasPhysics = false;
        this.sprite = sprite.get((int)(Math.random() * 30.0), this.lifetime);
    }

    private void setStretchRender() {
        this.stretchRender = true;
    }

    public float getQuadSize(float partialTicks) {
        if (!this.shrink) {
            return this.quadSize * (1.0f + (float)this.age / (float)this.lifetime * 3.0f);
        }
        return this.quadSize - (float)this.age / (float)this.lifetime;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        this.roll = (float)((double)this.roll + Math.random() * Math.PI / 180.0);
        super.tick();
    }

    public Vector3 getPosition() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xA00000;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        if (this.stretchRender) {
            this.renderStretched(buffer, renderInfo, partialTicks);
        } else {
            super.render(buffer, renderInfo, partialTicks);
        }
    }

    protected void renderStretched(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Quaternionf quaternion;
        Vec3 vector3d = renderInfo.getPosition();
        float f = (float)(Mth.lerp((double)partialTicks, (double)this.xo, (double)this.x) - vector3d.x());
        float f1 = (float)(Mth.lerp((double)partialTicks, (double)this.yo, (double)this.y) - vector3d.y());
        float f2 = (float)(Mth.lerp((double)partialTicks, (double)this.zo, (double)this.z) - vector3d.z());
        if (this.roll == 0.0f) {
            quaternion = renderInfo.rotation();
        } else {
            quaternion = new Quaternionf((Quaternionfc)renderInfo.rotation());
            float f3 = Mth.lerp((float)partialTicks, (float)this.oRoll, (float)this.roll);
            quaternion.mul((Quaternionfc)Axis.ZP.rotation(f3));
        }
        Vector3f vector3f1 = new Vector3f(-1.0f, -1.0f, 0.0f);
        vector3f1.rotate((Quaternionfc)quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0f, -2.0f, 0.0f), new Vector3f(-1.0f, 2.0f, 0.0f), new Vector3f(1.0f, 2.0f, 0.0f), new Vector3f(1.0f, -2.0f, 0.0f)};
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
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXArcaneMagelight
    extends MAParticleBase.FXParticleFactoryBase {
        public FXArcaneMagelight(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXArcane particle = new FXArcane(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            particle.lifetime = 30;
            particle.shrink = true;
            particle.quadSize = 1.0f;
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXArcaneStretchLerp
    extends MAParticleBase.FXParticleFactoryBase {
        public FXArcaneStretchLerp(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXArcane particle = new FXArcane(worldIn, x, y, z, this.spriteSet);
            particle.setMoveLerp(new Vector3(x, y, z), new Vector3(xSpeed, ySpeed, zSpeed));
            particle.setStretchRender();
            particle.lifetime = 15;
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXArcaneRandomFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXArcaneRandomFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXArcane particle = new FXArcane(worldIn, x, y, z, this.spriteSet);
            particle.setMoveRandomly(xSpeed, ySpeed, zSpeed);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXArcaneFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXArcaneFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXArcane particle = new FXArcane(worldIn, x, y, z, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, true);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


