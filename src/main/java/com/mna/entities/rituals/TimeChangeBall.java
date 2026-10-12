 
package com.mna.entities.rituals;

import com.mna.api.config.GeneralConfigValues;
import com.mna.api.particles.ParticleInit;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TimeChangeBall
extends Entity {
    private static final EntityDataAccessor<Byte> ENTITY_STATE = SynchedEntityData.defineId(TimeChangeBall.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> TIME_CHANGE_TYPE = SynchedEntityData.defineId(TimeChangeBall.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final Byte STATE_WAITING = 0;
    private static final Byte STATE_SHOOTING = 1;
    public static final int WAIT_TIME = 120;
    public static final int LERP_TIME = 100;
    public static final Byte TIME_CHANGE_DAY = 0;
    public static final Byte TIME_CHANGE_NIGHT = 1;
    private static final String EVENT_ID = "timeChange";
    private int age;
    Vector3 startPoint;
    Vector3 endPoint;
    Vector3 controlPoint1;
    Vector3 controlPoint2;
    Vec3 lastPosition;
    boolean pointsSet = false;

    public TimeChangeBall(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.setInvulnerable(true);
    }

    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    public void tick() {
        this.setAge(this.getAge() + 1);
        if (this.level().isClientSide()) {
            int max_particles_per_tick = 5;
            int numParticles = Math.max((int)Math.ceil((float)this.getAge() / 120.0f * (float)max_particles_per_tick), max_particles_per_tick);
            float radiusScale = 0.25f;
            for (int i = 0; i < numParticles; ++i) {
                this.level().addParticle((ParticleOptions)ParticleInit.SPARKLE_VELOCITY.get(), true, this.getX() + (Math.random() - 0.5) * (double)radiusScale, this.getY() + (Math.random() - 0.5) * (double)radiusScale, this.getZ() + (Math.random() - 0.5) * (double)radiusScale, 0.0, -0.5, 0.0);
            }
        }
        if (this.getAge() < 120) {
            float f = (float)this.getAge() / 120.0f;
            this.setDeltaMovement(1.0f + f, -1.0f - f * 9.0f, 1.0f + f);
            return;
        }
        if (this.getAge() >= 120 && this.getState() != STATE_SHOOTING.byteValue()) {
            this.setState(STATE_SHOOTING);
            if (!this.level().isClientSide()) {
                this.queueTimeChange();
            }
        }
        if (this.getState() == STATE_SHOOTING.byteValue()) {
            if (!this.pointsSet) {
                this.setPoints();
            }
            float f = ((float)this.getAge() - 120.0f) / 100.0f;
            float f1 = ((float)this.getAge() + 1.0f - 120.0f) / 100.0f;
            this.lastPosition = this.position();
            Vector3 curPoint = Vector3.bezier(this.startPoint, this.endPoint, this.controlPoint1, this.controlPoint2, f);
            Vector3 nextPoint = Vector3.bezier(this.startPoint, this.endPoint, this.controlPoint1, this.controlPoint2, f1);
            this.setPos(curPoint.x, curPoint.y, curPoint.z);
            Vector3 diff = nextPoint.sub(curPoint).normalize();
            float angle = (float)(Math.atan2(diff.x, diff.y) * 180.0 / Math.PI);
            this.setDeltaMovement(2.0, -10.0, 2.0);
            this.setXRot(angle);
            if (this.getAge() >= 220) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private boolean queueTimeChange() {
        int targetTime = -1;
        long wrappedWorldTime = this.level().getDayTime() % 24000L;
        if (this.level().isDay() && this.getTimeChangeType() == TIME_CHANGE_NIGHT.byteValue()) {
            targetTime = (int)(13000L - wrappedWorldTime);
        } else if (this.level().isNight() && this.getTimeChangeType() == TIME_CHANGE_DAY.byteValue()) {
            targetTime = 24000 - (int)wrappedWorldTime;
        }
        if (targetTime == -1) {
            return false;
        }
        if (!DelayedEventQueue.hasEvent(this.level(), EVENT_ID)) {
            DelayedEventQueue.pushEvent(this.level(), new TimedDelayedEvent<Pair>(EVENT_ID, 100, new Pair(((ServerLevel)this.level()), targetTime), TimeChangeBall::flipDayNight));
            return true;
        }
        return false;
    }

    private static void flipDayNight(String identifier, Pair<ServerLevel, Integer> data) {
        int shift;
        ServerLevel world = (ServerLevel)data.getFirst();
        int timeRemaining = (Integer)data.getSecond();
        int n = shift = GeneralConfigValues.GradualTimeChange ? MathUtils.clamp(timeRemaining, 0, 100) : timeRemaining;
        if (shift == 0) {
            return;
        }
        world.setDayTime(world.getDayTime() + (long)shift);
        ClientboundSetTimePacket pkt = new ClientboundSetTimePacket(world.getGameTime(), world.getDayTime(), world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT));
        world.getServer().getPlayerList().broadcastAll((Packet)pkt, world.dimension());
        DelayedEventQueue.pushEvent((Level)world, new TimedDelayedEvent<Pair>(identifier, 3, new Pair(world, (timeRemaining - shift)), TimeChangeBall::flipDayNight));
    }

    private void setPoints() {
        this.startPoint = new Vector3(this);
        this.endPoint = this.getCelestialPosition();
        this.controlPoint1 = Vector3.lerp(this.startPoint, this.endPoint, 0.25f);
        this.controlPoint2 = Vector3.lerp(this.startPoint, this.endPoint, 0.75f);
        this.controlPoint1.y = this.endPoint.y;
        this.controlPoint2.y = this.endPoint.y;
        this.pointsSet = true;
    }

    private Vector3 getCelestialPosition() {
        float celestialAngleRads = this.level().getSunAngle(0.0f) + 1.5707964f;
        if (this.getTimeChangeType() == TIME_CHANGE_DAY.byteValue()) {
            celestialAngleRads = (float)((double)celestialAngleRads + Math.PI);
        }
        double cAngle = Math.cos(celestialAngleRads);
        double sAngle = Math.sin(celestialAngleRads);
        float distance = 150.0f;
        return new Vector3(this.getX() + cAngle * (double)distance, this.getY() + sAngle * (double)distance, this.getZ());
    }

    public void setTimeChangeType(byte type) {
        this.entityData.set(TIME_CHANGE_TYPE, type);
    }

    public byte getTimeChangeType() {
        return (Byte)this.entityData.get(TIME_CHANGE_TYPE);
    }

    public byte getState() {
        return (Byte)this.entityData.get(ENTITY_STATE);
    }

    private void setState(byte state) {
        this.entityData.set(ENTITY_STATE, state);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(ENTITY_STATE, STATE_WAITING);
        builder.define(TIME_CHANGE_TYPE, TIME_CHANGE_DAY);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    public void checkDespawn() {
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}


