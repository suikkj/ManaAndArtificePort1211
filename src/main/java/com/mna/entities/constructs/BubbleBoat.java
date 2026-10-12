 
package com.mna.entities.constructs;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.EntityInit;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

public class BubbleBoat
extends Boat {
    private static final EntityDataAccessor<Byte> UPGRADE = SynchedEntityData.defineId(BubbleBoat.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private float boatRotation = 0.0f;
    private float prevBoatRotation = 0.0f;

    public BubbleBoat(EntityType<? extends BubbleBoat> p_i50129_1_, Level p_i50129_2_) {
        super(p_i50129_1_, p_i50129_2_);
        this.blocksBuilding = true;
    }

    public float getBoatRotation(float partialTicks) {
        return Mth.lerp((float)partialTicks, (float)this.prevBoatRotation, (float)this.boatRotation);
    }

    public BubbleBoat(Level worldIn, double x, double y, double z) {
        this((EntityType<? extends BubbleBoat>)((EntityType)EntityInit.BUBBLE_BOAT.get()), worldIn);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public BubbleBoat setBrimstone() {
        this.entityData.set(UPGRADE, 1);
        return this;
    }

    public boolean isBrimstone() {
        return (Byte)this.entityData.get(UPGRADE) == 1;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(UPGRADE, (byte)0);
    }

    public void tick() {
        this.underwaterControl();
        this.handleSpecialLavaProperties();
        super.tick();
        if (this.level().isClientSide() && this.getPassengers().size() > 0 && this.status != Boat.Status.IN_AIR && this.status != Boat.Status.ON_LAND) {
            float RADIUS = 3.0f;
            Vec3 OFFSET = new Vec3(-1.0, -0.25, -1.0);
            Vec3 VELOCITY = new Vec3((double)-0.01f + Math.random() * (double)0.02f, (double)-0.01f + Math.random() * (double)0.02f, (double)-0.01f + Math.random() * (double)0.02f);
            int amount = (int)Math.ceil(this.getDeltaMovement().length() * 25.0);
            for (int i = 0; i < amount; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())), OFFSET.x + this.getX() + Math.random() * (double)RADIUS, OFFSET.y + this.getY() + Math.random() * (double)RADIUS, OFFSET.z + this.getZ() + Math.random() * (double)RADIUS, VELOCITY.x, VELOCITY.y, VELOCITY.z);
            }
        }
        this.prevBoatRotation = this.boatRotation;
        if (this.getPassengers().size() > 0) {
            double motionMagnitude = this.getDeltaMovement().length() * 0.25;
            if (motionMagnitude > 0.001) {
                this.boatRotation = (float)((double)this.boatRotation - motionMagnitude);
                this.boatRotation = (float)((double)this.boatRotation % (Math.PI * 2));
            }
        } else {
            double halfPi = 1.5707963267948966;
            this.boatRotation -= 0.1f;
            if ((double)Math.abs(this.boatRotation) % halfPi <= (double)0.1f) {
                this.boatRotation = 0.0f;
                this.prevBoatRotation = 0.0f;
            }
        }
    }

    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
        if (type == NeoForgeMod.WATER_TYPE) {
            return true;
        }
        if (type == NeoForgeMod.LAVA_TYPE.get()) {
            return this.isBrimstone();
        }
        return false;
    }

    public boolean ignoreExplosion() {
        if (this.isBrimstone()) {
            return true;
        }
        return super.ignoreExplosion();
    }

    public boolean fireImmune() {
        if (this.isBrimstone()) {
            return true;
        }
        return super.fireImmune();
    }

    public boolean displayFireAnimation() {
        if (this.isBrimstone()) {
            return false;
        }
        return super.displayFireAnimation();
    }

    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        if (this.isInLava() && this.isBrimstone()) {
            this.fallDistance = 0.0f;
        }
        super.checkFallDamage(y, onGroundIn, state, pos);
    }

    private void underwaterControl() {
        this.outOfControlTicks = 0.0f;
        if ((this.status == Boat.Status.UNDER_WATER || this.status == Boat.Status.UNDER_FLOWING_WATER) && this.getPassengers().size() > 0) {
            double yVel = Math.min(this.getDeltaMovement().y + (double)0.2f, 2.0);
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x, yVel, this.getDeltaMovement().z));
        }
    }

    public Item getDropItem() {
        return (Byte)this.entityData.get(UPGRADE) == 0 ? (Item)ItemInit.BUBBLE_BOAT.get() : (Item)ItemInit.BRIMSTONE_BOAT.get();
    }

    private void handleSpecialLavaProperties() {
        if (this.isInLava()) {
            this.getPassengers().forEach(e -> {
                if (e instanceof LivingEntity && (((LivingEntity)e).getEffect(MobEffects.FIRE_RESISTANCE) == null || ((LivingEntity)e).getEffect(MobEffects.FIRE_RESISTANCE).getDuration() < 20)) {
                    ((LivingEntity)e).addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200));
                }
            });
            this.setOnGround(false);
            CollisionContext iselectioncontext = CollisionContext.of((Entity)this);
            if (iselectioncontext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) && !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                this.setOnGround(true);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * 0.5 + 0.05, this.getDeltaMovement().z);
            }
        }
    }

    public float getStepHeight() {
        if (this.isInLava()) {
            return 2.0f;
        }
        return 1.0f;
    }

    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 1;
    }

    public boolean shouldRiderSit() {
        return false;
    }

    public double getPassengersRidingOffset() {
        return 0.3;
    }
}

