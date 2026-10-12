 
package com.mna.entities.rituals;

import com.mna.ManaAndArtifice;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.entities.EntityInit;
import com.mna.entities.UtilityEntityBase;
import com.mna.entities.boss.FaerieQueen;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FeyLight
extends UtilityEntityBase {
    private static final EntityDataAccessor<String> CASTER_UUID = SynchedEntityData.defineId(FeyLight.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final EntityDataAccessor<BlockPos> TARGET_POSITION = SynchedEntityData.defineId(FeyLight.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    private float angle = 0.0f;
    private int growTicks = 0;
    private static int maxGrowTicks = 40;

    public FeyLight(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age == 1 && this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY() - 9.0, this.getZ(), SFX.Event.Ritual.FAERIE_SUMMON, SoundSource.PLAYERS, 1.0f, 1.0f, false);
        }
        if (this.updateMovement()) {
            ++this.growTicks;
            if (this.growTicks > maxGrowTicks && !this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
                FaerieQueen efq = new FaerieQueen((EntityType<? extends Monster>)((EntityType)EntityInit.FAERIE_QUEEN.get()), this.level());
                efq.setPos(this.position().subtract(0.0, 1.0, 0.0));
                UUID casterUUID = this.getCasterUUID();
                Player player = null;
                if (casterUUID != null) {
                    player = this.level().getPlayerByUUID(casterUUID);
                }
                efq.setSummoner(player);
                this.level().addFreshEntity((Entity)efq);
            }
        } else if (this.level().isClientSide()) {
            float r = 0.25f;
            float r2 = r * 2.0f;
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_STATIONARY.get())), this.getX() - (double)r + Math.random() * (double)r2, this.getY() - (double)r + Math.random() * (double)r2, this.getZ() - (double)r + Math.random() * (double)r2, 0.0, 0.0, 0.0);
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        double radius = 1.0;
        double velocity = 0.5;
        if (this.level().isClientSide()) {
            RandomSource random = this.level().getRandom();
            for (int i = 0; i < 100; ++i) {
                Vec3 pos = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
                Vec3 vel = pos.scale(velocity);
                pos = pos.scale(radius);
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())), this.getX() + pos.x, this.getY() + pos.y, this.getZ() + pos.z, vel.x, vel.y, vel.z);
            }
        }
    }

    private boolean updateMovement() {
        Vec3 me;
        Vec3 target = new Vec3((double)this.getTargetPos().getX(), (double)this.getTargetPos().getY(), (double)this.getTargetPos().getZ()).add(0.5, 0.0, 0.5);
        if (target.distanceTo(me = this.position()) < (double)0.2f || me.y < target.y) {
            return true;
        }
        Vec3 diff = me.subtract(target);
        float radius = (float)diff.y / 2.0f;
        this.angle = (float)((double)this.angle + 0.08726646324990228);
        Vec3 newPos = new Vec3(target.x + (double)radius * Math.cos(this.angle), target.y + diff.y - (double)0.09f, target.z + (double)radius * Math.sin(this.angle));
        this.setPos(newPos.x, newPos.y, newPos.z);
        return false;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(CASTER_UUID, "");
        builder.define(TARGET_POSITION, this.blockPosition());
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("caster")) {
            this.entityData.set(CASTER_UUID, compound.getString("caster"));
        }
        if (compound.contains("target_position")) {
            CompoundTag targetPos = compound.getCompound("target_position");
            this.entityData.set(TARGET_POSITION, BlockPos.containing((double)targetPos.getFloat("x"), (double)targetPos.getFloat("y"), (double)targetPos.getFloat("z")));
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("caster", (String)this.entityData.get(CASTER_UUID));
        CompoundTag targetPos = new CompoundTag();
        targetPos.putFloat("x", (float)((BlockPos)this.entityData.get(TARGET_POSITION)).getX());
        targetPos.putFloat("y", (float)((BlockPos)this.entityData.get(TARGET_POSITION)).getY());
        targetPos.putFloat("z", (float)((BlockPos)this.entityData.get(TARGET_POSITION)).getZ());
        compound.put("target_position", (Tag)targetPos);
    }

    public UUID getCasterUUID() {
        try {
            UUID uUID = UUID.fromString((String)this.entityData.get(CASTER_UUID));
            return uUID;
        }
        catch (Exception exception) {
        }
        catch (Throwable throwable) {
            throw throwable;
        }
        return null;
    }

    public void setCasterUUID(UUID casterUUID) {
        if (casterUUID != null) {
            this.entityData.set(CASTER_UUID, casterUUID.toString());
        } else {
            ManaAndArtifice.LOGGER.error("Received null UUID for ritual caster.  Some effects may not apply!");
        }
    }

    public BlockPos getTargetPos() {
        return (BlockPos)this.entityData.get(TARGET_POSITION);
    }

    public void setTargetPos(BlockPos target) {
        this.entityData.set(TARGET_POSITION, target);
    }

    public float getGrowthAmount() {
        return (float)this.growTicks / (float)maxGrowTicks;
    }
}

