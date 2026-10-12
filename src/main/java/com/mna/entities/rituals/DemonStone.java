 
package com.mna.entities.rituals;

import com.mna.ManaAndArtifice;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.entities.boss.DemonLord;
import com.mna.sound.EntityAliveLoopingSound;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DemonStone
extends Entity {
    protected int age = 0;
    private static final EntityDataAccessor<String> CASTER_UUID = SynchedEntityData.defineId(DemonStone.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private boolean summonAsHostile = false;

    public DemonStone(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public DemonStone setSummonAsHostile() {
        this.summonAsHostile = true;
        return this;
    }

    public void tick() {
        ++this.age;
        if (this.level().isClientSide()) {
            this.playSounds();
            this.spawnParticles();
        }
        if (this.age > 200 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            DemonLord edl = new DemonLord(this.level());
            edl.setPos(this.position());
            this.level().addFreshEntity((Entity)edl);
            if (!this.summonAsHostile) {
                UUID casterUUID = this.getCasterUUID();
                Player player = null;
                if (casterUUID != null) {
                    player = this.level().getPlayerByUUID(casterUUID);
                }
                edl.setSummoner(player);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private void playSounds() {
        if (this.age == 1) {
            Minecraft.getInstance().getSoundManager().play((SoundInstance)new EntityAliveLoopingSound(SFX.Loops.DEMON_SUMMON, this));
        } else if (this.age == 120) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Event.Ritual.DEMON_SUMMON_END, SoundSource.PLAYERS, 1.0f, 1.0f, false);
        }
    }

    private void spawnParticles() {
        int i;
        for (i = 0; i < 5; ++i) {
            this.level().addParticle((ParticleOptions)ParticleTypes.LANDING_LAVA, this.getX() - 1.5 + Math.random() * 3.0, this.getY(), this.getZ() - 1.5 + Math.random() * 3.0, 0.0, (double)0.05f, 0.0);
        }
        if (this.age < 150) {
            for (i = 0; i < 5; ++i) {
                boolean xAxis = Math.random() > 0.5;
                boolean positive = Math.random() > 0.5;
                this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_BRICKS.defaultBlockState()), xAxis ? (positive ? this.getX() + 2.5 - Math.random() * 0.5 : this.getX() - 2.5 + Math.random() * 0.5) : this.getX() + 2.5 - Math.random() * 5.0, this.getY(), !xAxis ? (positive ? this.getZ() + 2.5 - Math.random() * 0.5 : this.getZ() - 2.5 + Math.random() * 0.5) : this.getZ() + 2.5 - Math.random() * 5.0, 0.0, (double)0.05f, 0.0);
            }
        }
        this.level().addParticle((ParticleOptions)ParticleTypes.LAVA, this.getX() - 0.5 + Math.random() * 1.0, this.getY(), this.getZ() - 0.5 + Math.random() * 1.0, 0.0, (double)0.05f, 0.0);
        if (this.age > 160) {
            for (i = 0; i < 25; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), this.getX() - 0.25 + Math.random() * 0.5, this.getY(), this.getZ() - 0.25 + Math.random() * 0.5, 0.0, (double)0.15f, 0.0);
            }
        }
        if (this.age == 199) {
            for (i = 0; i < 100; ++i) {
                this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_BRICKS.defaultBlockState()), this.getX() - 1.5 + Math.random() * 3.0, this.getY() + 1.5, this.getZ() - 1.5 + Math.random() * 3.0, -0.5 + Math.random(), 0.0, -0.5 + Math.random());
            }
            this.level().addParticle((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    public int getAge() {
        return this.age;
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isPickable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(CASTER_UUID, "");
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("age")) {
            this.age = compound.getInt("age");
        }
        if (compound.contains("caster")) {
            this.entityData.set(CASTER_UUID, compound.getString("caster"));
        }
        if (compound.contains("summonHostile")) {
            this.summonAsHostile = compound.getBoolean("summonHostile");
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("age", this.age);
        compound.putString("caster", (String)this.entityData.get(CASTER_UUID));
        compound.putBoolean("summonHostile", this.summonAsHostile);
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
}


