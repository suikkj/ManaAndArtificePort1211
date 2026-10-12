 
package com.mna.entities.boss.attacks;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PumpkinKingEntangle
extends Entity {
    private Vec3 origin;
    private static final EntityDataAccessor<Optional<UUID>> TARGET = SynchedEntityData.defineId(PumpkinKingEntangle.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);

    public PumpkinKingEntangle(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public PumpkinKingEntangle(Level p_i48580_2_) {
        this((EntityType)EntityInit.PUMPKIN_KING_ENTANGLE.get(), p_i48580_2_);
    }

    public void setTarget(Player player) {
        this.entityData.set(TARGET, Optional.of(player.getUUID()));
    }

    public void tick() {
        super.tick();
        if (this.origin == null) {
            this.origin = this.position();
        }
        if (this.level().isClientSide()) {
            BlockPos cur = BlockPos.containing((double)this.origin.x, (double)this.origin.y, (double)this.origin.z);
            BlockState state = this.level().getBlockState(cur);
            this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, state), this.origin.x - 0.25 + Math.random() * 0.5, this.origin.y + 0.25, this.origin.z - 0.25 + Math.random() * 0.5, 0.0, 0.05 * Math.random(), 0.0);
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())), this.getX(), this.getY(), this.getZ(), 0.10588235408067703, 0.3137255012989044, (double)0.05490196f);
        }
        if (this.tickCount == 20) {
            this.resolveTarget();
        } else if (this.tickCount > 20 && this.tickCount < 79) {
            this.noPhysics = true;
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
        if (this.tickCount > 80) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private void resolveTarget() {
        ((Optional)this.entityData.get(TARGET)).ifPresent(u -> {
            Player player = this.level().getPlayerByUUID(u);
            Vec3 movement = player.position().subtract(this.origin).normalize();
            movement = movement.scale(0.25);
            this.setDeltaMovement(movement);
        });
    }

    public Vec3 getOrigin() {
        return this.origin;
    }

    public void playerTouch(Player player) {
        if (this.tickCount <= 20) {
            return;
        }
        if (!this.level().isClientSide()) {
            if (!player.onGround()) {
                player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get()), 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
            } else {
                player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.ENTANGLE.get()), 60, 1));
            }
        }
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(TARGET, Optional.empty());
    }

    protected void readAdditionalSaveData(CompoundTag p_70037_1_) {
    }

    protected void addAdditionalSaveData(CompoundTag p_213281_1_) {
    }

    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(128.0);
    }
}


