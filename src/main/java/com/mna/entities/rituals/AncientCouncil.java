 
package com.mna.entities.rituals;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.LivingUtilityEntity;
import com.mna.factions.Factions;
import com.mna.tools.math.MathUtils;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AncientCouncil
extends LivingUtilityEntity {
    public static final byte STATE_WAITING_PLAYER = 0;
    public static final byte STATE_IMBUING = 1;
    private float radiant;
    private float radiantLift;
    private float beam;
    private float centerRadiant;

    public AncientCouncil(EntityType<? extends LivingEntity> type, Level worldIn) {
        super(type, worldIn);
        this.stateMachine.addSequenceEntry("imbue", 0).onComplete(() -> {
            this.setCurrentAnimation("imbue_stage_1");
            if (this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Event.Ritual.ANCIENT_IMBUE_1, SoundSource.PLAYERS, 1.0f, 1.0f, false);
            }
        });
        this.stateMachine.addSequenceEntry("imbue", 220).onComplete(() -> {
            this.setCurrentAnimation("imbue_stage_2");
            if (this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Event.Ritual.ANCIENT_IMBUE_2, SoundSource.PLAYERS, 1.0f, 1.0f, false);
            } else if (this.caster != null) {
                this.caster.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()), 180, 1));
            }
        }).onTick(c -> {
            this.animationPct = (float)c.intValue() / 220.0f;
            this.radiant = MathUtils.clamp01((float)(c - 120) / 100.0f);
        });
        this.stateMachine.addSequenceEntry("imbue", 165).onComplete(() -> {
            this.setCurrentAnimation("imbue_stage_3");
            if (this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Event.Ritual.ANCIENT_IMBUE_3, SoundSource.PLAYERS, 1.0f, 1.0f, false);
            } else if (this.caster != null) {
                this.caster.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()), 280, 2));
            }
        }).onTick(c -> {
            this.radiantLift = this.animationPct = (float)c.intValue() / 165.0f;
        });
        this.stateMachine.addSequenceEntry("imbue", 260).onComplete(() -> {
            this.setCurrentAnimation("");
            if (this.caster != null && this.caster.distanceTo((Entity)this) < 10.0f) {
                if (this.level().isClientSide()) {
                    for (int i = 0; i < 75; ++i) {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), this.caster.getX(), this.caster.getY() + 1.0, this.caster.getZ(), -0.25 + Math.random() * 0.5, (double)0.15f, -0.25 + Math.random() * 0.5);
                    }
                } else {
                    IPlayerProgression progression = (IPlayerProgression)this.caster.getData(PlayerProgressionProvider.PROGRESSION.get());
                    if (progression != null && progression.getTier() < 5) {
                        if (progression.getAlliedFaction() == null) {
                            progression.setAlliedFaction(Factions.COUNCIL, this.caster);
                            this.caster.sendSystemMessage((Component)Component.translatable((String)"event.mna.faction_ally_ancients"));
                        }
                        if (progression.getAlliedFaction() == Factions.COUNCIL) {
                            progression.setTier(progression.getTier() + 1, this.caster);
                            this.caster.sendSystemMessage((Component)Component.translatable((String)"mna:progresscondition.advanced", (Object[])new Object[]{progression.getTier()}));
                        }
                    }
                }
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            this.centerRadiant = 0.0f;
            this.beam = 0.0f;
            this.radiant = 0.0f;
        }).onTick(c -> {
            this.animationPct = (float)c.intValue() / 260.0f;
            this.centerRadiant = MathUtils.clamp01(((float)c.intValue() - 30.0f) / 180.0f);
            this.beam = MathUtils.clamp01((float)c.intValue() / 40.0f);
        });
    }

    @Override
    public void tick() {
        byte state;
        super.tick();
        if (this.tickCount < 100) {
            return;
        }
        if (this.caster == null) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
        if ((state = this.getState()) == 0) {
            List centerCollisions = this.level().getEntities((Entity)this, this.getBoundingBox(), e -> true);
            if (centerCollisions.contains(this.caster)) {
                this.caster.setPos(this.getX(), this.getY(), this.getZ());
                if (!this.level().isClientSide()) {
                    this.caster.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()), 240));
                }
                if (!this.level().isClientSide()) {
                    this.setState((byte)1);
                }
                this.stateMachine.runSequence("imbue");
            }
        } else {
            this.stateMachine.tick();
        }
        if (!this.level().isClientSide() && (state == 0 && this.tickCount > 700 || this.caster == null || this.caster.distanceTo((Entity)this) > 10.0f)) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key == STATE && this.getState() == 1) {
            this.stateMachine.runSequence("imbue");
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setState((byte)0);
    }

    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Event.Ritual.ANCIENT_SUMMON, SoundSource.PLAYERS, 1.0f, 1.0f, false);
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level().isClientSide()) {
            int numWizards = 6;
            float angleRadians = 0.0f;
            for (int a = 0; a < numWizards; ++a) {
                for (int i = 0; i < 125; ++i) {
                    double radius = 2.75 + Math.random() * 0.5;
                    double x = this.getX() - 0.5 + Math.random() + radius * Math.cos(angleRadians);
                    double z = this.getZ() - 0.5 + Math.random() + radius * Math.sin(angleRadians);
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), x, this.getY() + Math.random() * 0.5, z, 0.0, 0.02 + Math.random() * (double)0.2f, 0.0);
                }
                angleRadians = (float)((double)angleRadians + (double)(360.0f / (float)numWizards / 180.0f) * Math.PI);
            }
        }
        this.disableFlightFor(this.caster);
    }

    public float getRadiantPct() {
        return this.radiant;
    }

    public float getBeamPct() {
        return this.beam;
    }

    public float getCenterPct() {
        return this.centerRadiant;
    }

    public float getRadiantLift() {
        return this.radiantLift;
    }
}

