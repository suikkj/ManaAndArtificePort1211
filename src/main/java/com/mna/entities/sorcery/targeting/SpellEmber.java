 
package com.mna.entities.sorcery.targeting;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import com.mna.api.tools.MNACap;

public class SpellEmber
extends Entity {
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SpellEmber.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(SpellEmber.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Integer> ANGLE = SynchedEntityData.defineId(SpellEmber.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(SpellEmber.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> TARGET_DISTANCE = SynchedEntityData.defineId(SpellEmber.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> HOMING_STRENGTH = SynchedEntityData.defineId(SpellEmber.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private ISpellDefinition cachedRecipe;
    private LivingEntity cachedOwner;
    private LivingEntity target;
    private Vec3 movement;

    public SpellEmber(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SpellEmber(Level pLevel, LivingEntity owner, ISpellDefinition recipe, int angle) {
        super((EntityType)EntityInit.SPELL_EMBER.get(), pLevel);
        this.setPos(owner.position());
        this.entityData.set(OWNER_ID, owner.getId());
        CompoundTag recipeCompound = new CompoundTag();
        recipe.writeToNBT(recipeCompound);
        this.entityData.set(SPELL_RECIPE, recipeCompound);
        this.entityData.set(ANGLE, angle);
        this.setNoGravity(true);
        SummonUtils.addTrackedEntity(owner, this, "mna:ember_ids");
        SummonUtils.tagAsSummon(this, owner);
    }

    public void setHomingStrength(float strength) {
        this.entityData.set(HOMING_STRENGTH, Float.valueOf(MathUtils.clamp01(strength)));
    }

    public void setTargetDistance(byte distance) {
        this.entityData.set(TARGET_DISTANCE, distance);
    }

    public void tick() {
        if (!this.level().isClientSide()) {
            if (this.getOwner() == null || !this.getOwner().isAlive() || this.getOwner().level() != this.level() || this.getSpell() == null || this.getTarget() != null && this.tickCount >= 60) {
                this.remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            if (this.getTarget() == null) {
                if (!this.level().isClientSide() && !SummonUtils.isTracked(this.getOwner(), this, "mna:ember_ids")) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                    return;
                }
                Vec3 desiredPos = this.getPositionAroundOwnerBasedOnLocation();
                this.setPos(desiredPos);
                if (this.level().getGameTime() % 20L == 0L && SummonUtils.isFirstTrackedEntity(this.getOwner(), "mna:ember_ids", this)) {
                    LivingEntity owner = this.getOwner();
                    if (owner instanceof Player) {
                        MNACap.get(owner, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                            if (m.getEmberCooldown() == 0) {
                                this.target();
                            }
                            if (this.getTarget() != null) {
                                m.setEmberCooldown(20);
                            }
                        });
                    } else {
                        long last_ember = owner.getPersistentData().getLong("mna_last_ember_time");
                        long cur_time = this.level().getGameTime();
                        long last_ember_time = cur_time - last_ember;
                        if (last_ember_time >= 20L) {
                            this.target();
                            owner.getPersistentData().putLong("mna_last_ember_time", cur_time);
                        }
                    }
                }
            } else {
                if (!this.getTarget().isAlive()) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                    return;
                }
                Vec3 myPos = this.position();
                Vec3 theirPos = this.getTarget().position().add(0.0, (double)(this.getTarget().getBbHeight() / 2.0f), 0.0);
                if (this.movement == null) {
                    this.movement = theirPos.subtract(myPos).normalize();
                } else {
                    float tickTheta = 7.5f * MathUtils.clamp01(((Float)this.entityData.get(HOMING_STRENGTH)).floatValue());
                    if (tickTheta > 0.0f) {
                        Vec3 desiredHeading = theirPos.subtract(myPos).normalize();
                        this.movement = MathUtils.rotateTowards(this.movement, desiredHeading, tickTheta).normalize().scale(0.5);
                    }
                }
                this.setPos(this.position().add(this.movement));
                if (!this.level().isClientSide()) {
                    SpellContext context = new SpellContext(this.level(), this.getSpell(), this);
                    SpellSource source = new SpellSource(this.getOwner(), InteractionHand.MAIN_HAND);
                    if (myPos.distanceTo(theirPos) < 0.5) {
                        this.remove(Entity.RemovalReason.DISCARDED);
                        if (this.getSpell() != null && this.getOwner() != null) {
                            this.getSpell().iterateComponents(c -> SpellCaster.ApplyComponents(this.getSpell(), source, new SpellTarget((Entity)this.getTarget()), context));
                        }
                    }
                }
            }
        }
        if (this.level().isClientSide()) {
            block11: for (int i = 0; i < 2; ++i) {
                switch (this.getSpell().getHighestAffinity()) {
                    case EARTH: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                        continue block11;
                    }
                    case ENDER: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get())), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                        continue block11;
                    }
                    case FIRE: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                        continue block11;
                    }
                    case HELLFIRE: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                        continue block11;
                    }
                    case ICE: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                        continue block11;
                    }
                    case LIGHTNING: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), this.getX(), this.getY(), this.getZ(), this.getX() - 0.25 + Math.random() * 0.5, this.getY() - 0.25 + Math.random() * 0.5, this.getZ() - 0.25 + Math.random() * 0.5);
                        continue block11;
                    }
                    case WATER: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                        continue block11;
                    }
                    case BLOOD: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setColor(182, 28, 28).setScale(0.1f).setGravity(0.0f).setMaxAge(5), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setGravity(0.025f).setScale(0.05f).setPhysics(true), this.getX() + (double)MathUtils.RandomBetween(-0.25f, 0.25f), this.getY(), this.getZ() + (double)MathUtils.RandomBetween(-0.25f, 0.25f), 0.0, 0.0, 0.0);
                        continue block11;
                    }
                    case WIND: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.05f).setColor(30, 30, 30), this.getX(), this.getY(), this.getZ(), 0.5, (double)0.001f, 0.05);
                        continue block11;
                    }
                    default: {
                        this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get())), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
            }
        }
        super.tick();
    }

    private void target() {
        this.target = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)((Byte)this.entityData.get(TARGET_DISTANCE)).byteValue()), e -> {
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)) {
                return false;
            }
            ClipContext ctx = new ClipContext(this.position(), e.position().add(0.0, (double)(e.getBbHeight() / 2.0f), 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this);
            if (this.level().clip(ctx).getType() == HitResult.Type.BLOCK) {
                return false;
            }
            return e.isAlive() && e instanceof LivingEntity && !SummonUtils.isTargetFriendly(e, this.getOwner());
        }).stream().map(e -> (LivingEntity)e).findFirst().orElse(null);
        if (this.target != null) {
            this.entityData.set(TARGET_ID, this.target.getId());
            this.tickCount = 0;
        }
    }

    public boolean canCollideWith(Entity pEntity) {
        return this.getTarget() == pEntity;
    }

    private Vec3 getPositionAroundOwnerBasedOnLocation() {
        float angle = ManaAndArtifice.instance.proxy.getGameTicks() * 3L + (long)((Integer)this.entityData.get(ANGLE)).intValue();
        Vec3 targetPos = this.getOwner().position().add(0.0, (double)(this.getOwner().getBbHeight() / 2.0f), 0.0);
        Vector3f offset = new Vector3f(1.0f, 0.0f, 0.0f);
        Quaternionf rotation = Axis.YP.rotationDegrees(angle);
        offset.rotate((Quaternionfc)rotation);
        offset.normalize();
        return targetPos.add((double)offset.x(), (double)offset.y(), (double)offset.z());
    }

    @Nullable
    private LivingEntity getOwner() {
        if (this.cachedOwner == null) {
            int ownerID = (Integer)this.entityData.get(OWNER_ID);
            if (ownerID > -1) {
                Entity e = this.level().getEntity(ownerID);
                if (e == null || !(e instanceof LivingEntity)) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                    return null;
                }
                this.cachedOwner = (LivingEntity)e;
            } else {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        return this.cachedOwner;
    }

    @Nullable
    private LivingEntity getTarget() {
        Entity e;
        if (this.target == null && (e = this.level().getEntity(((Integer)this.entityData.get(TARGET_ID)).intValue())) instanceof LivingEntity) {
            this.target = (LivingEntity)e;
        }
        return this.target;
    }

    public void setAngle(int angle) {
        this.entityData.set(ANGLE, angle);
    }

    private ISpellDefinition getSpell() {
        if (this.cachedRecipe == null) {
            CompoundTag spellTag = (CompoundTag)this.entityData.get(SPELL_RECIPE);
            if (spellTag == null) {
                this.remove(Entity.RemovalReason.DISCARDED);
            } else {
                this.cachedRecipe = SpellRecipe.fromNBT(spellTag);
                if (!this.cachedRecipe.isValid()) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
        return this.cachedRecipe;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(SPELL_RECIPE, new CompoundTag());
        builder.define(ANGLE, 0);
        builder.define(TARGET_ID, -1);
        builder.define(HOMING_STRENGTH, Float.valueOf(0.0f));
        builder.define(TARGET_DISTANCE, (byte)16);
    }

    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.contains("owner")) {
            this.entityData.set(OWNER_ID, pCompound.getInt("owner"));
        }
        if (pCompound.contains("spell", 10)) {
            this.entityData.set(SPELL_RECIPE, ((CompoundTag)pCompound.get("spell")));
        }
        if (pCompound.contains("angle")) {
            this.entityData.set(ANGLE, pCompound.getInt("angle"));
        }
        if (pCompound.contains("homingFactor")) {
            this.entityData.set(HOMING_STRENGTH, Float.valueOf(pCompound.getFloat("homingFactor")));
        }
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
        int ownerID = (Integer)this.entityData.get(OWNER_ID);
        if (ownerID > -1) {
            pCompound.putInt("owner", ownerID);
        }
        if (!((CompoundTag)this.entityData.get(SPELL_RECIPE)).isEmpty()) {
            pCompound.put("spell", (Tag)this.entityData.get(SPELL_RECIPE));
        }
        pCompound.putInt("angle", ((Integer)this.entityData.get(ANGLE)).intValue());
        pCompound.putFloat("homingFactor", ((Float)this.entityData.get(HOMING_STRENGTH)).floatValue());
    }
}

