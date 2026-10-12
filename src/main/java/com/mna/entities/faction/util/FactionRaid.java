 
package com.mna.entities.faction.util;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.entities.FactionRaidRegistry;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.FactionIDs;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.entities.EntityInit;
import com.mna.factions.Factions;
import com.mna.tools.SummonUtils;
import com.mojang.datafixers.util.Pair;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;

public class FactionRaid
extends Entity {
    private static final EntityDataAccessor<String> FACTION = SynchedEntityData.defineId(FactionRaid.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private Player player;
    private UUID playerUUID;
    private int strength;
    private boolean isProtective;
    private MobEffectInstance[] additionalEffects;
    private IFaction __cachedFaction;

    public FactionRaid(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public FactionRaid(Level world, Player target, int strength) {
        super((EntityType)EntityInit.FACTION_RAID_ENTITY.get(), world);
        this.player = target;
        this.strength = strength;
    }

    public void setProtective(Player guard) {
        this.player = guard;
        this.isProtective = true;
    }

    public void setAdditionalEffects(MobEffectInstance[] effects) {
        this.additionalEffects = effects;
    }

    public void tick() {
        IFaction faction;
        if (!this.level().isClientSide() && this.tickCount % 20 == 0 && !this.spawnRaidEntity()) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
        if (this.level().isClientSide()) {
            if (this.getFaction().is(FactionIDs.DEMONS)) {
                this.spawnDemonParticles();
            } else if (this.getFaction().is(FactionIDs.UNDEAD)) {
                this.spawnUndeadParticles();
            }
        } else if (this.tickCount == 20 && (faction = this.getFaction()) != null) {
            this.playSound(faction.getRaidSound(), 1.0f, 1.0f);
        }
    }

    private void spawnDemonParticles() {
        int i;
        for (i = 0; i < 15; ++i) {
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), this.getX(), this.getY(), this.getZ(), (double)-0.05f + Math.random() * (double)0.1f, (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f);
        }
        this.level().addParticle((ParticleOptions)ParticleTypes.LAVA, this.getX() - 0.5 + Math.random() * 1.0, this.getY(), this.getZ() - 0.5 + Math.random() * 1.0, 0.0, (double)0.05f, 0.0);
        for (i = 0; i < 5; ++i) {
            this.level().addParticle((ParticleOptions)ParticleTypes.LANDING_LAVA, this.getX() - 1.5 + Math.random() * 3.0, this.getY(), this.getZ() - 1.5 + Math.random() * 3.0, 0.0, (double)0.05f, 0.0);
        }
    }

    private void spawnUndeadParticles() {
        int i;
        for (i = 0; i < 15; ++i) {
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), this.getX(), this.getY(), this.getZ(), (double)-0.05f + Math.random() * (double)0.1f, (double)0.1f, (double)-0.05f + Math.random() * (double)0.1f);
        }
        for (i = 0; i < 5; ++i) {
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BONE.get())), this.getX() - 1.5 + Math.random() * 3.0, this.getY(), this.getZ() - 1.5 + Math.random() * 3.0, 0.0, (double)0.05f, 0.0);
        }
    }

    private Player getPlayer() {
        if (this.player == null && this.playerUUID != null) {
            this.player = this.level().getPlayerByUUID(this.playerUUID);
        }
        return this.player;
    }

    private boolean spawnRaidEntity() {
        if (this.getPlayer() == null) {
            return false;
        }
        Pair<EntityType<? extends IFactionEnemy<? extends Mob>>, Integer> soldier = FactionRaidRegistry.getSoldier(this.getFaction(), this.strength);
        if (soldier == null) {
            return false;
        }
        IFactionEnemy entity = (IFactionEnemy)((EntityType)soldier.getFirst()).create(this.level());
        if (!this.isProtective) {
            entity.setRaidTarget(this.getPlayer());
        } else {
            SummonUtils.setSummon((Mob)entity, (LivingEntity)this.player, 1200);
        }
        entity.setTier((Integer)soldier.getSecond());
        ((LivingEntity)entity).setPos(this.getX(), this.getY(), this.getZ());
        ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 3));
        if (this.additionalEffects != null) {
            for (MobEffectInstance inst : this.additionalEffects) {
                ((LivingEntity)entity).addEffect(new MobEffectInstance(inst.getEffect(), inst.getDuration(), inst.getAmplifier(), inst.isAmbient(), inst.isVisible(), inst.showIcon()));
            }
        }
        this.level().addFreshEntity((Entity)entity);
        int entity_strength_rating = FactionRaidRegistry.getStrengthRating(this.getFaction(), (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)soldier.getFirst()), (Integer)soldier.getSecond());
        if (entity_strength_rating == -1) {
            return false;
        }
        this.strength -= entity_strength_rating;
        return this.strength > 0;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(FACTION, FactionIDs.COUNCIL.toString());
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        ListTag nbt;
        if (compound.contains("strength")) {
            this.strength = compound.getInt("strength");
        }
        if (compound.contains("protecting")) {
            this.isProtective = compound.getBoolean("protecting");
        }
        if (compound.contains("faction")) {
            this.entityData.set(FACTION, compound.getString("faction"));
        }
        if (compound.contains("afx") && (nbt = compound.getList("afx", 10)) != null) {
            this.additionalEffects = new MobEffectInstance[nbt.size()];
            MutableInt idx = new MutableInt(0);
            nbt.forEach(n -> {
                this.additionalEffects[idx.intValue()] = MobEffectInstance.load((CompoundTag)((CompoundTag)n));
            });
        }
        if (compound.contains("target")) {
            try {
                this.playerUUID = UUID.fromString(compound.getString("target"));
            }
            catch (Exception ex) {
                ManaAndArtifice.LOGGER.error("Failed to load player UUID when loading faction raid.  Skipping and despawning the raid.");
            }
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("strength", this.strength);
        compound.putString("faction", (String)this.entityData.get(FACTION));
        compound.putBoolean("protecting", this.isProtective);
        if (this.additionalEffects != null) {
            ListTag afx = new ListTag();
            for (MobEffectInstance inst : this.additionalEffects) {
                CompoundTag instSaved = (CompoundTag)inst.save();
                afx.add(instSaved);
            }
            compound.put("afx", (Tag)afx);
        }
        if (this.player != null) {
            compound.putString("target", this.player.getUUID().toString());
        }
    }

    public IFaction getFaction() {
        if (this.__cachedFaction == null) {
            ResourceLocation factionID = ResourceLocation.parse((String)this.entityData.get(FACTION));
            this.__cachedFaction = Factions.INSTANCE.getFaction(factionID);
        }
        return this.__cachedFaction;
    }

    public void setFaction(IFaction faction) {
        this.entityData.set(FACTION, Registries.Factions.getKey(faction).toString());
    }

    public void setFaction(ResourceLocation factionID) {
        this.entityData.set(FACTION, factionID.toString());
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (pKey == FACTION) {
            this.__cachedFaction = null;
        }
    }
}


