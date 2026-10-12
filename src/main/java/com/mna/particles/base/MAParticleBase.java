/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  javax.annotation.Nonnull
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleProvider
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.particle.TextureSheetParticle
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.mna.particles.base;

import com.mna.api.particles.MAParticleType;
import com.mna.particles.FXMovementType;
import com.mna.particles.types.render.ParticleRenderTypes;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.math.Axis;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public abstract class MAParticleBase
extends TextureSheetParticle {
    protected FXMovementType movementType;
    private Vector3 start;
    private Vector3 end;
    private Vector3 control_a;
    private Vector3 control_b;
    private ItemStack stack = null;
    private float uo;
    private float vo;
    private boolean decay_velocity = false;
    private boolean orbit_clockwise = true;
    private double angle;
    private double flare = -1.0;
    protected float maxAlpha = 1.0f;
    protected float life_padding = 0.0f;
    protected boolean fixedSprite = false;
    protected ArrayList<Vector3> colorTransitions = new ArrayList();

    protected MAParticleBase(ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (!this.apply_aging()) {
            this.move();
        }
    }

    public void setMoveVelocity(double x, double y, double z, boolean decay) {
        this.xd = x;
        this.yd = y;
        this.zd = z;
        this.decay_velocity = decay;
        this.movementType = FXMovementType.VELOCITY;
    }

    public void setMoveStationary() {
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.movementType = FXMovementType.STATIONARY;
    }

    public void setMoveLerp(Vector3 start, Vector3 end) {
        this.start = start;
        this.end = end;
        this.movementType = FXMovementType.LERP_POINT;
    }

    public void setMoveLerp(double x, double y, double z, double goal_x, double goal_y, double goal_z) {
        this.setMoveLerp(new Vector3(x, y, z), new Vector3(goal_x, goal_y, goal_z));
    }

    public void setMoveBezier(Vector3 start, Vector3 end, Vector3 controlA, Vector3 controlB) {
        this.start = start;
        this.end = end;
        this.control_a = controlA;
        this.control_b = controlB;
        this.movementType = FXMovementType.BEZIER_POINT;
    }

    public void setMoveBezier(Vector3 start, Vector3 end) {
        this.start = start;
        this.end = end;
        this.generateBezierControlPoints();
        this.movementType = FXMovementType.BEZIER_POINT;
    }

    public void setMoveBezier(double x, double y, double z, double goal_x, double goal_y, double goal_z) {
        this.setMoveBezier(new Vector3(x, y, z), new Vector3(goal_x, goal_y, goal_z));
    }

    public void setMoveRandomly(double x, double y, double z) {
        this.movementType = FXMovementType.VELOCITY;
        this.xd = -x + Math.random() * 2.0 * y;
        this.yd = -y + Math.random() * 2.0 * y;
        this.zd = -z + Math.random() * 2.0 * z;
    }

    public void setMoveOrbit(double cX, double cY, double cZ, double forwardSpeed, double upSpeed, double radius) {
        this.start = new Vector3(cX, cY, cZ);
        this.end = new Vector3(forwardSpeed, radius, upSpeed);
        this.yd = upSpeed;
        this.orbit_clockwise = Math.random() < 0.5;
        this.lifetime = 30;
        this.angle = Math.random() * 360.0;
        this.movementType = FXMovementType.ORBIT;
        this.moveOrbit();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public void setMoveOrbit(double cX, double cY, double cZ, double forwardSpeed, double upSpeed, double radius, double flare) {
        this.flare = flare;
        this.setMoveOrbit(cX, cY, cZ, forwardSpeed, upSpeed, radius);
    }

    public void setMoveSphereOrbit(double cX, double cY, double cZ, double forwardSpeed, double tilt, double radius) {
        this.start = new Vector3(cX, cY, cZ);
        this.end = new Vector3(forwardSpeed, radius, tilt);
        this.lifetime = 30;
        this.orbit_clockwise = Math.random() < 0.5;
        this.angle = Math.random() * 360.0;
        this.movementType = FXMovementType.SPHERE_ORBIT;
        this.moveSphereOrbit();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public void setRenderAsStackParticles(ItemStack stack) {
        this.stack = stack.copy();
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(this.stack, (Level)this.level, (LivingEntity)null, 0).getParticleIcon(ModelData.EMPTY));
        this.uo = (float)(Math.random() * 3.0);
        this.vo = (float)(Math.random() * 3.0);
        this.quadSize /= 2.0f;
    }

    public void setMaxAlpha(float maxAlpha) {
        this.maxAlpha = MathUtils.clamp01(maxAlpha);
    }

    protected float getU0() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getU((this.uo + 1.0f) / 4.0f * 16.0f);
        }
        return super.getU0();
    }

    protected float getU1() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getU(this.uo / 4.0f * 16.0f);
        }
        return super.getU1();
    }

    protected float getV0() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getV(this.vo / 4.0f * 16.0f);
        }
        return super.getV0();
    }

    protected float getV1() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getV((float)((this.vo + 1.0f) / 4.0f * 16.0f));
        }
        return super.getV1();
    }

    public void setSpriteFromAge(SpriteSet pSprite) {
        if (!this.fixedSprite) {
            super.setSpriteFromAge(pSprite);
        }
    }

    private void generateBezierControlPoints() {
        Vector3 o1 = new Vector3(this.start.x, this.start.y, this.start.z);
        Vector3 midPoint = new Vector3((this.start.x + this.end.x) / 2.0f, (this.start.y + this.end.y) / 2.0f, (this.start.z + this.end.z) / 2.0f);
        midPoint = midPoint.sub(o1);
        midPoint = midPoint.rotateYaw(1.5707964f);
        this.control_a = new Vector3(this.start.x + (this.end.x - this.start.x) / 3.0f, this.start.y + (this.end.y - this.start.y) / 3.0f, this.start.z + (this.end.z - this.start.z) / 3.0f);
        this.control_b = new Vector3(this.start.x + (this.end.x - this.start.x) / 3.0f * 2.0f, this.start.y + (this.end.y - this.start.y) / 3.0f * 2.0f, this.start.z + (this.end.z - this.start.z) / 3.0f * 2.0f);
        this.control_a = this.control_a.add(midPoint);
        this.control_b = this.control_b.add(midPoint);
    }

    protected void move() {
        if (this.movementType != null) {
            switch (this.movementType) {
                case BEZIER_POINT: {
                    this.moveBezier();
                    break;
                }
                case LERP_POINT: {
                    this.moveLerp();
                    break;
                }
                case VELOCITY: {
                    this.moveVelocity();
                    break;
                }
                case ORBIT: {
                    this.moveOrbit();
                    break;
                }
                case SPHERE_ORBIT: {
                    this.moveSphereOrbit();
                    break;
                }
            }
        }
    }

    protected boolean apply_aging() {
        int n = this.age++;
        if ((float)n >= (float)this.lifetime + this.life_padding) {
            this.remove();
        }
        float agePct = MathUtils.clamp01((float)this.age / (float)this.lifetime);
        this.lerpAlpha(agePct);
        this.lerpColors(agePct);
        return this.removed;
    }

    protected void lerpAlpha(float agePct) {
        float alpha_T = this.maxAlpha;
        if (agePct < 0.2f) {
            alpha_T = agePct / 0.2f * this.maxAlpha;
        } else if (agePct > 0.8f) {
            alpha_T = (1.0f - (agePct - 0.8f) / 0.2f) * this.maxAlpha;
        }
        this.setAlpha(alpha_T);
    }

    protected void lerpColors(float agePct) {
        if (this.colorTransitions == null || this.colorTransitions.size() < 2) {
            return;
        }
        int cSize = this.colorTransitions.size();
        int cIndex = (int)Math.floor((float)cSize * agePct) % cSize;
        int nIndex = cIndex + 1;
        if (nIndex > cSize - 1) {
            nIndex = cSize - 1;
        }
        int colorAge = Math.max(this.lifetime / cSize, 1);
        float color_T = (float)(this.age % colorAge) / (float)colorAge;
        Vector3 clr = Vector3.lerp(this.colorTransitions.get(cIndex), this.colorTransitions.get(nIndex), color_T);
        this.setColor(clr.x / 255.0f, clr.y / 255.0f, clr.z / 255.0f);
    }

    private void moveOrbit() {
        this.end.y += 0.01f;
        this.angle = this.orbit_clockwise ? (this.angle += (double)this.end.x) : (this.angle -= (double)this.end.x);
        double expand = this.flare >= 0.0 ? this.flare : (double)this.end.y;
        double pX = (double)this.start.x + Math.cos(this.angle) * expand;
        double pZ = (double)this.start.z + Math.sin(this.angle) * expand;
        this.setPos(pX, this.y, pZ);
        this.move(0.0, this.yd, 0.0);
    }

    private void moveSphereOrbit() {
        this.angle += (double)this.end.x;
        float pX = (float)Math.cos(this.angle);
        float pZ = (float)Math.sin(this.angle);
        Vector3f horizPos = new Vector3f(pX, 0.0f, pZ);
        horizPos.rotate((Quaternionfc)Axis.XP.rotationDegrees(this.end.z + (float)this.age));
        horizPos.mul(this.end.y);
        this.setPos(this.start.x + horizPos.x(), this.start.y + horizPos.y(), this.start.z + horizPos.z());
    }

    private void moveVelocity() {
        this.move(this.xd, this.yd, this.zd);
        if (this.decay_velocity) {
            if (this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }
            this.xd *= (double)0.96f;
            this.yd *= (double)0.96f;
            this.zd *= (double)0.96f;
            if (this.onGround) {
                this.xd *= (double)0.7f;
                this.zd *= (double)0.7f;
            }
        }
        if (this.gravity > 0.0f) {
            this.yd -= (double)this.gravity;
        }
    }

    private void moveLerp() {
        float agePct = (float)this.age / (float)this.lifetime;
        Vector3 pos = Vector3.lerp(this.start, this.end, agePct);
        this.setPos(pos.x, pos.y, pos.z);
    }

    private void moveBezier() {
        this.setPosition(Vector3.bezier(this.start, this.end, this.control_a, this.control_b, (float)this.age / (float)this.lifetime));
    }

    private void setPosition(Vector3 position) {
        this.setPos(position.x, position.y, position.z);
    }

    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public void setLifePadding(int padding) {
        this.life_padding = padding;
    }

    @Nonnull
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.ADDITIVE;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static abstract class FXParticleFactoryBase
    implements ParticleProvider<MAParticleType> {
        protected final SpriteSet spriteSet;

        public FXParticleFactoryBase(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public abstract Particle createParticle(MAParticleType var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13);

        protected void configureParticle(MAParticleBase particle, MAParticleType typeIn) {
            if (typeIn.getColor() != null) {
                if (typeIn.getColor().getColor() != -1) {
                    particle.colorTransitions.clear();
                    particle.setColor(typeIn.getColor().getRed(), typeIn.getColor().getGreen(), typeIn.getColor().getBlue());
                }
                if (typeIn.getColor().getAlpha() != -1.0f) {
                    particle.setMaxAlpha(typeIn.getColor().getAlpha());
                }
            }
            if (typeIn.getScale() != null) {
                particle.quadSize = typeIn.getScale().value();
            }
            if (typeIn.getLife() != null) {
                particle.lifetime = typeIn.getLife().value();
            }
            if (typeIn.getLifePadding() != null) {
                particle.setLifePadding(typeIn.getLife().value());
            }
            if (typeIn.getGravity() != null) {
                particle.gravity = typeIn.getGravity().value();
            }
            if (typeIn.getPhysics() != null) {
                particle.hasPhysics = typeIn.getPhysics().value();
            }
            if (typeIn.getMover() != null) {
                typeIn.getMover().configureParticle(particle);
            }
            if (typeIn.getStack() != null) {
                particle.setRenderAsStackParticles(typeIn.getStack().value());
            }
        }
    }
}


