 
package com.mna.particles.trail;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.FXMovementType;
import com.mna.particles.base.MAParticleBase;
import com.mna.particles.bolt.Segment;
import com.mna.particles.trail.ParticleTrail;
import com.mna.particles.types.render.ParticleRenderTypes;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FXTrail
extends MAParticleBase {
    private ParticleTrail data;
    private Entity followEntity;
    private Vector3 origin;

    public FXTrail(ClientLevel worldIn, double startX, double startY, double startZ, double entityID, double randSeed, double maxAge, SpriteSet sprite) {
        super(worldIn, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.sprite = sprite.get(this.random);
        this.data = new ParticleTrail(worldIn.random.nextLong());
        this.setPos(startX, startY, startZ);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.origin = new Vector3(startX, startY, startZ);
        this.quadSize = 0.04f;
        this.setSize(3.0f, 3.0f);
        this.rCol = 0.2f;
        this.gCol = 0.2f;
        this.bCol = 0.8f;
    }

    private void setMoveFollowEntity(Level world, int entityID, long seed, int maxAge) {
        this.followEntity = world.getEntity(entityID);
        this.lifetime = maxAge;
        this.movementType = FXMovementType.STATIONARY;
        this.origin = null;
    }

    @Override
    public void tick() {
        if (this.followEntity == null) {
            super.tick();
        } else {
            if (!this.followEntity.isAlive()) {
                this.remove();
            }
            this.moveFollowEntity();
        }
        double time = (float)this.age / 4.0f;
        Vector3 prevPos = new Vector3(this.xo, this.yo, this.zo);
        Vector3 curPos = new Vector3(this.x, this.y, this.z);
        if (this.age > 5) {
            this.data.tick(prevPos, curPos);
        }
    }

    private void moveFollowEntity() {
        if (this.lifetime == -1) {
            if (this.followEntity == null || !this.followEntity.isAlive()) {
                this.remove();
            } else {
                this.removed = false;
            }
        } else {
            ++this.age;
            if (this.age > this.lifetime) {
                this.remove();
            }
        }
        if (!this.removed) {
            this.xo = this.followEntity.xo;
            this.yo = this.followEntity.yo;
            this.zo = this.followEntity.zo;
            this.setPos(this.followEntity.getX(), this.followEntity.getY(), this.followEntity.getZ());
        }
    }

    public boolean shouldCull() {
        return false;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vector3 particleOrigin;
        Vec3 vec3d = renderInfo.getPosition();
        double lerpX = Mth.lerp((double)partialTicks, (double)this.xo, (double)this.x);
        double lerpY = Mth.lerp((double)partialTicks, (double)this.yo, (double)this.y);
        double lerpZ = Mth.lerp((double)partialTicks, (double)this.zo, (double)this.z);
        float f = (float)(lerpX - vec3d.x());
        float f1 = (float)(lerpY - vec3d.y());
        float f2 = (float)(lerpZ - vec3d.z());
        Vector3 renderViewOffset = new Vector3(f, f1, f2);
        Vector3 vector3 = particleOrigin = this.origin != null ? this.origin : new Vector3(this.x, this.y, this.z);
        if (this.origin != null) {
            Vector3 originDelta = new Vector3(lerpX, lerpY, lerpZ).sub(particleOrigin);
            particleOrigin = particleOrigin.add(originDelta);
        }
        int count = 0;
        Vector3 lastEnd1 = null;
        Vector3 lastEnd2 = null;
        for (Segment s : this.data.getSegments()) {
            float width = this.quadSize;
            Vector3 start = s.getStart().sub(particleOrigin);
            Vector3 end = s.getEnd().sub(particleOrigin);
            Vector3 dir = end.sub(start).normalize().scale(5.0E-4f);
            Vector3[] avector3f = new Vector3[]{lastEnd1 == null ? start.add(new Vector3(-width, 0.0, -width)) : lastEnd1.sub(dir), lastEnd2 == null ? start.add(new Vector3(-width, 0.0, width)) : lastEnd2.sub(dir), end.add(new Vector3(width, 0.0, width)), end.add(new Vector3(width, 0.0, -width))};
            lastEnd1 = avector3f[2];
            lastEnd2 = avector3f[3];
            for (int i = 0; i < 4; ++i) {
                avector3f[i] = avector3f[i].add(renderViewOffset);
            }
            float startAlpha = this.alpha;
            if (count > this.data.getSegments().size() - 10) {
                startAlpha = 0.1f * (float)(this.data.getSegments().size() - count);
            }
            float minU = this.getU0();
            float maxU = this.getU1();
            float minV = this.getV0();
            float maxV = this.getV1();
            int j = 0xF000F0;
            buffer.addVertex((float)avector3f[3].x, (float)avector3f[3].y, (float)avector3f[3].z).setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[2].x, (float)avector3f[2].y, (float)avector3f[2].z).setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[0].x, (float)avector3f[0].y, (float)avector3f[0].z).setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[1].x, (float)avector3f[1].y, (float)avector3f[1].z).setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, startAlpha).setLight(j);
            buffer.addVertex((float)avector3f[1].x, (float)avector3f[1].y, (float)avector3f[1].z).setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[0].x, (float)avector3f[0].y, (float)avector3f[0].z).setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[2].x, (float)avector3f[2].y, (float)avector3f[2].z).setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
            buffer.addVertex((float)avector3f[3].x, (float)avector3f[3].y, (float)avector3f[3].z).setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, startAlpha).setLight(j);
            ++count;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.ADDITIVE;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXTrailBezierFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXTrailBezierFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXTrail particle = new FXTrail(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            particle.setMoveBezier(x, y, z, xSpeed, ySpeed, zSpeed);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXTrailSphereOrbitFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXTrailSphereOrbitFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXTrail particle = new FXTrail(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            particle.setMoveSphereOrbit(x, y, z, xSpeed, ySpeed, zSpeed);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXTrailOrbitFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXTrailOrbitFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXTrail particle = new FXTrail(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            particle.setMoveOrbit(x, y, z, xSpeed, ySpeed, zSpeed);
            particle.lifetime = 50;
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXTrailVelocityFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXTrailVelocityFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXTrail particle = new FXTrail(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            particle.setMoveVelocity(xSpeed, ySpeed, zSpeed, false);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class FXTrailFollowEntityFactory
    extends MAParticleBase.FXParticleFactoryBase {
        public FXTrailFollowEntityFactory(SpriteSet spriteSet) {
            super(spriteSet);
        }

        @Override
        public Particle createParticle(MAParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXTrail particle = new FXTrail(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            particle.setMoveFollowEntity((Level)worldIn, (int)xSpeed, (long)ySpeed, (int)zSpeed);
            this.configureParticle(particle, typeIn);
            return particle;
        }
    }
}


