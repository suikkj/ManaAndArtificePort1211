 
package com.mna.particles.bolt;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.base.MAParticleBase;
import com.mna.particles.bolt.LightningData;
import com.mna.particles.bolt.Segment;
import com.mna.particles.types.render.ParticleRenderTypes;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class FXLightningBolt
extends MAParticleBase {
    private LightningData data;

    public FXLightningBolt(ClientLevel worldIn, double startX, double startY, double startZ, double endX, double endY, double endZ, int maxAge, SpriteSet sprite) {
        super(worldIn, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.sprite = sprite.get(this.random);
        this.data = new LightningData(new Vector3(startX, startY, startZ), new Vector3(endX, endY, endZ), (long)(Math.random() * 9.223372036854776E18), maxAge);
        this.lifetime = this.data.getMaxAge() + 5;
        this.setPos(startX, startY, startZ);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.rCol = 0.2f;
        this.gCol = 0.2f;
        this.bCol = 0.8f;
        this.quadSize = 0.25f;
        this.data.setMaxOffset(0.2f);
    }

    @Override
    public void tick() {
        super.tick();
        this.data.onUpdate();
        if (this.age > this.getLifetime() - 10) {
            float delta;
            this.alpha = delta = (float)(this.getLifetime() - this.age) / 10.0f;
        }
    }

    public boolean shouldCull() {
        return false;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3d = renderInfo.getPosition();
        float f = (float)(Mth.lerp((double)partialTicks, (double)this.xo, (double)this.x) - vec3d.x());
        float f1 = (float)(Mth.lerp((double)partialTicks, (double)this.yo, (double)this.y) - vec3d.y());
        float f2 = (float)(Mth.lerp((double)partialTicks, (double)this.zo, (double)this.z) - vec3d.z());
        Vector3 posOffset = new Vector3(f, f1, f2);
        Vector3 particleOrigin = new Vector3(this.x, this.y, this.z);
        int count = 0;
        int maxIndex = (int)Math.ceil(((float)this.data.getAge() + partialTicks) / (float)this.data.getMaxAge() * (float)this.data.numSegments());
        Vector3 lastEnd1 = null;
        Vector3 lastEnd2 = null;
        for (Segment s : this.data.getSegments()) {
            if (count > maxIndex) break;
            float width = Math.min(0.01f * this.data.getLength(), this.quadSize);
            Vector3 start = s.getStart().sub(particleOrigin);
            Vector3 end = s.getEnd().sub(particleOrigin);
            Vector3 dir = end.sub(start).normalize().scale(this.data.getLength() * 3.0E-4f);
            Vector3f[] avector3f = new Vector3f[]{(lastEnd1 == null ? start.add(new Vector3(-width, 0.0, -width)) : lastEnd1.sub(dir)).toVector3f(), (lastEnd2 == null ? start.add(new Vector3(-width, 0.0, width)) : lastEnd2.sub(dir)).toVector3f(), end.add(new Vector3(width, 0.0, width)).toVector3f(), end.add(new Vector3(width, 0.0, -width)).toVector3f()};
            lastEnd1 = new Vector3(avector3f[2].x(), avector3f[2].y(), avector3f[2].z());
            lastEnd2 = new Vector3(avector3f[3].x(), avector3f[3].y(), avector3f[3].z());
            for (int i = 0; i < 4; ++i) {
                avector3f[i].add(posOffset.x, posOffset.y, posOffset.z);
            }
            float minU = this.getU0();
            float maxU = this.getU1();
            float minV = this.getV0();
            float maxV = this.getV1();
            int j = 0xF000F0;
            buffer.addVertex((float)avector3f[3].x(), (float)avector3f[3].y(), (float)avector3f[3].z()).setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[2].x(), (float)avector3f[2].y(), (float)avector3f[2].z()).setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[0].x(), (float)avector3f[0].y(), (float)avector3f[0].z()).setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[1].x(), (float)avector3f[1].y(), (float)avector3f[1].z()).setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[1].x(), (float)avector3f[1].y(), (float)avector3f[1].z()).setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[0].x(), (float)avector3f[0].y(), (float)avector3f[0].z()).setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[2].x(), (float)avector3f[2].y(), (float)avector3f[2].z()).setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[3].x(), (float)avector3f[3].y(), (float)avector3f[3].z()).setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            ++count;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.ADDITIVE;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXLightningBoltFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXLightningBoltFactory(SpriteSet spriteset) {
            super(spriteset);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXLightningBolt particle = new FXLightningBolt(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.getLife() != null ? typeIn.getLife().value() : 10, this.spriteSet);
            this.configureParticle(particle, typeIn);
            if (typeIn.getGravity() != null) {
                particle.data.setMaxOffset(typeIn.getGravity().value());
            }
            if (typeIn.getLifePadding() != null) {
                particle.setLifePadding(0);
                particle.data.fractalize(typeIn.getLifePadding().value());
            } else {
                particle.data.fractalize();
            }
            particle.data.finalize();
            return particle;
        }
    }
}


