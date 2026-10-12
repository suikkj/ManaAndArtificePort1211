 
package com.mna.particles.bolt;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.bolt.LightningData;
import com.mna.particles.bolt.Segment;
import com.mna.particles.types.render.ParticleRenderTypes;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXWraithBolt
extends TextureSheetParticle {
    private LightningData data;

    public FXWraithBolt(ClientLevel worldIn, double startX, double startY, double startZ, double endX, double endY, double endZ, SpriteSet sprite) {
        super(worldIn, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.sprite = sprite.get(this.random);
        this.data = new LightningData(new Vector3(startX, startY, startZ), new Vector3(endX, endY, endZ), worldIn.random.nextLong());
        this.data.setMaxAge(20);
        this.data.setSpeed(2);
        this.lifetime = this.data.getMaxAge();
        this.setPos(startX, startY, startZ);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.rCol = 0.75f;
        this.gCol = 0.12f;
        this.bCol = 0.06f;
        this.data.setMaxOffset(0.25f);
        this.data.fractalize(8);
        this.data.finalize();
    }

    public void tick() {
        super.tick();
        this.data.onUpdate();
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
        if (this.data.getAge() == this.data.getMaxAge()) {
            int ticksRemaining = this.lifetime - this.age;
            this.alpha = (float)ticksRemaining / 10.0f;
        }
        for (Segment s : this.data.getSegments()) {
            if (count > maxIndex) break;
            float width = 0.08f;
            Vector3 start = s.getStart().sub(particleOrigin);
            Vector3 end = s.getEnd().sub(particleOrigin);
            Vector3[] avector3f = new Vector3[]{lastEnd1 == null ? start.add(new Vector3(-width, 0.0, -width)) : lastEnd1, lastEnd2 == null ? start.add(new Vector3(-width, 0.0, width)) : lastEnd2, end.add(new Vector3(width, 0.0, width)), end.add(new Vector3(width, 0.0, -width))};
            lastEnd1 = avector3f[2];
            lastEnd2 = avector3f[3];
            for (int i = 0; i < 4; ++i) {
                avector3f[i] = avector3f[i].add(posOffset);
            }
            float minU = this.getU0();
            float maxU = this.getU1();
            float minV = this.getV0();
            float maxV = this.getV1();
            int j = 0xF00000;
            buffer.addVertex((float)avector3f[3].x, (float)avector3f[3].y, (float)avector3f[3].z).setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[2].x, (float)avector3f[2].y, (float)avector3f[2].z).setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[0].x, (float)avector3f[0].y, (float)avector3f[0].z).setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[1].x, (float)avector3f[1].y, (float)avector3f[1].z).setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[3].x, (float)avector3f[3].y, (float)avector3f[3].z).setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[2].x, (float)avector3f[2].y, (float)avector3f[2].z).setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[0].x, (float)avector3f[0].y, (float)avector3f[0].z).setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[1].x, (float)avector3f[1].y, (float)avector3f[1].z).setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            ++count;
        }
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.ADDITIVE;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXWraithBoltFactory
    implements ParticleProvider<MAParticleType> {
        protected final SpriteSet spriteSet;

        public FXWraithBoltFactory(SpriteSet p_i50846_1_) {
            this.spriteSet = p_i50846_1_;
        }

        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXWraithBolt particle = new FXWraithBolt(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            return particle;
        }
    }
}


