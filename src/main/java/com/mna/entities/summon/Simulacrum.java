 
package com.mna.entities.summon;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.entities.EntityInit;
import com.mna.spells.SpellCaster;
import com.mna.tools.math.MathUtils;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Simulacrum
extends Mob
implements OwnableEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(Simulacrum.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> OFFSET = SynchedEntityData.defineId(Simulacrum.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MANA = SynchedEntityData.defineId(Simulacrum.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_MANA = SynchedEntityData.defineId(Simulacrum.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    private Player __owner;
    private ISpellDefinition currentSpell;

    public Simulacrum(Level pLevel, float mana, Player owner, float offset) {
        this((EntityType<? extends Mob>)((EntityType)EntityInit.SIMULACRUM.get()), pLevel);
        if (owner != null) {
            this.entityData.set(MAX_MANA, Float.valueOf(mana));
            this.entityData.set(MANA, Float.valueOf(mana));
            this.entityData.set(OWNER, Optional.of(owner.getGameProfile().getId()));
            this.entityData.set(OFFSET, Float.valueOf(offset));
            this.copyTransformAndVelocityFromOwner();
        }
    }

    public Simulacrum(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public void tick() {
        if (this.getOwner() == null || this.getOwner().isDeadOrDying()) {
            this.discard();
            return;
        }
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.isUsingItem() && this.getUseItem().getItem() instanceof ICanContainSpell && this.currentSpell != null) {
                this.consumeMana(this.currentSpell.getManaCost());
            }
            if (this.tickCount % 20 == 0) {
                this.consumeMana(1.0f);
            }
            if (this.getMana() <= 0.0f) {
                this.discard();
            }
        } else {
            Vec3 fwd = this.getForward().normalize().scale((double)0.4f);
            Vec3 side = this.getForward().normalize().cross(new Vec3(0.0, 1.0, 0.0)).scale((double)0.9f);
            int particleCount = this.swinging ? 5 : 1;
            for (int i = 0; i < particleCount; ++i) {
                Vec3 particleVelocity = this.swinging ? this.getForward().add(0.0, 0.25, 0.0) : Vec3.ZERO;
                Vec3 particlePos = this.position().add((MathUtils.RandomBetween(-fwd.x, fwd.x) + MathUtils.RandomBetween(-side.x, side.x)) * this.getBoundingBox().getXsize(), (double)MathUtils.RandomBetween(0.25f, 1.25f) * this.getBoundingBox().getYsize(), (MathUtils.RandomBetween(-fwd.z, fwd.z) + MathUtils.RandomBetween(-side.z, side.z)) * this.getBoundingBox().getZsize());
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD).setGravity(0.05f).setPhysics(true), particlePos.x, particlePos.y, particlePos.z, particleVelocity.x, particleVelocity.y, particleVelocity.z);
            }
        }
    }

    public void travel(Vec3 pTravelVector) {
        super.travel(pTravelVector);
        if (this.getOwner() != null) {
            this.copyTransformAndVelocityFromOwner();
        }
    }

    private void copyTransformAndVelocityFromOwner() {
        Player owner = this.getOwner();
        if (owner == null) {
            return;
        }
        Vec3 side = Vec3.directionFromRotation((Vec2)new Vec2(0.0f, owner.getYRot())).normalize().cross(UP).scale((double)((Float)this.entityData.get(OFFSET)).floatValue());
        this.setPos(owner.position().add(side).add(0.0, 0.25, 0.0));
        this.setRot(owner.getYRot(), owner.getXRot());
        this.setDeltaMovement(owner.getDeltaMovement());
        this.swinging = owner.swinging;
        this.swingingArm = owner.swingingArm;
        this.swingTime = owner.swingTime;
    }

    public void castSpell(ItemStack stack, ISpellDefinition spell, InteractionHand hand) {
        if (this.tickCount == 0) {
            return;
        }
        SpellCastingResult res = SpellCaster.Affect(stack.copy(), spell, this.level(), new SpellSource((LivingEntity)this, hand), null, new SpellContext(this.level(), spell));
        if (res.getCode().isConsideredSuccess()) {
            this.consumeMana(spell.getManaCost());
            this.currentSpell = spell;
        }
    }

    public float getMana() {
        return ((Float)this.entityData.get(MANA)).floatValue();
    }

    public void setMana(float mana) {
        this.entityData.set(MANA, Float.valueOf(mana));
    }

    public void consumeMana(float amount) {
        this.setMana(this.getMana() - amount);
    }

    public float getManaPct() {
        return Mth.clamp((float)(((Float)this.entityData.get(MANA)).floatValue() / Math.max(((Float)this.entityData.get(MAX_MANA)).floatValue(), 1.0f)), (float)0.0f, (float)1.0f);
    }

    public ItemStack getItemInHand(InteractionHand pHand) {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getItemInHand(pHand);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getItemBySlot(pSlot);
        }
        return ItemStack.EMPTY;
    }

    public InteractionHand getUsedItemHand() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getUsedItemHand();
        }
        return InteractionHand.MAIN_HAND;
    }

    public ItemStack getUseItem() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getUseItem();
        }
        return ItemStack.EMPTY;
    }

    public int getUseItemRemainingTicks() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getUseItemRemainingTicks();
        }
        return 0;
    }

    public int getTicksUsingItem() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getTicksUsingItem();
        }
        return 0;
    }

    public boolean isUsingItem() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.isUsingItem();
        }
        return false;
    }

    public boolean isVisuallyCrawling() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.isVisuallyCrawling();
        }
        return false;
    }

    public boolean isVisuallySwimming() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.isVisuallySwimming();
        }
        return false;
    }

    public boolean isFallFlying() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.isFallFlying();
        }
        return false;
    }

    public Pose getPose() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getPose();
        }
        return Pose.STANDING;
    }

    public float getSwimAmount(float pPartialTicks) {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getSwimAmount(pPartialTicks);
        }
        return 0.0f;
    }

    public boolean isCrouching() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.isCrouching();
        }
        return false;
    }

    public Vec3 getDeltaMovement() {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getDeltaMovement();
        }
        return Vec3.ZERO;
    }

    public float getAttackAnim(float pPartialTick) {
        Player owner = this.getOwner();
        if (owner != null) {
            return owner.getAttackAnim(pPartialTick);
        }
        return 0.0f;
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return false;
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean canCollideWith(Entity pEntity) {
        return !(pEntity instanceof LivingEntity);
    }

    protected boolean isHorizontalCollisionMinor(Vec3 pDeltaMovement) {
        return false;
    }

    public boolean canBeHitByProjectile() {
        return true;
    }

    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public void push(double pX, double pY, double pZ) {
    }

    public void push(Entity pEntity) {
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 100; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLOOD.get())), this.getX() + MathUtils.RandomBetween(-0.25, 0.25), this.getY() + (double)MathUtils.RandomBetween(0.0f, 2.0f), this.getZ() + MathUtils.RandomBetween(-0.25, 0.25), MathUtils.RandomBetween(-0.05, 0.05), MathUtils.RandomBetween(0.02, 0.08), MathUtils.RandomBetween(-0.05, 0.05));
            }
        } else {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SFX.Entity.BloodGolem.DEATH, this.getSoundSource(), 1.0f, MathUtils.RandomBetween(0.9f, 1.1f));
        }
    }

    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 200; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET_LERP.get())).setColor(Affinity.BLOOD).setMaxAge(10 + (int)(Math.random() * 5.0)), this.getX() + MathUtils.RandomBetween(-2.25, 2.25), this.getY(), this.getZ() + MathUtils.RandomBetween(-2.25, 2.25), this.getX(), this.getY() + (double)MathUtils.RandomBetween(0.0f, 2.0f), this.getZ());
            }
        } else {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SFX.Entity.BloodGolem.SUMMON, this.getSoundSource(), 1.0f, MathUtils.RandomBetween(0.9f, 1.1f));
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
        builder.define(OFFSET, Float.valueOf(0.0f));
        builder.define(MANA, Float.valueOf(0.0f));
        builder.define(MAX_MANA, Float.valueOf(0.0f));
    }

    public UUID getOwnerUUID() {
        Optional ownerID = (Optional)this.entityData.get(OWNER);
        if (ownerID.isPresent()) {
            return (UUID)ownerID.get();
        }
        return null;
    }

    public Player getOwner() {
        LivingEntity resolved;
        if (this.__owner == null && (resolved = super.getOwner()) instanceof Player) {
            Player player;
            this.__owner = player = (Player)resolved;
        }
        return this.__owner;
    }
}

