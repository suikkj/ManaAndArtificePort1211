 
package com.mna.entities;

import com.mna.ManaAndArtifice;
import com.mna.entities.state.EntityStateMachine;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class LivingUtilityEntity
extends LivingEntity {
    protected static final EntityDataAccessor<String> CASTER_UUID = SynchedEntityData.defineId(LivingUtilityEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(LivingUtilityEntity.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<String> CURRENT_ANIMATION = SynchedEntityData.defineId(LivingUtilityEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<String> CURRENT_SEQUENCE = SynchedEntityData.defineId(LivingUtilityEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    protected int stateTicks = 0;
    protected float lastTickAnimationPct;
    protected int lastAnimationChangeTimer = 0;
    protected float animationPct;
    protected EntityStateMachine<LivingUtilityEntity> stateMachine = new EntityStateMachine<LivingUtilityEntity>(this, CURRENT_SEQUENCE);
    protected Player caster;

    protected LivingUtilityEntity(EntityType<? extends LivingEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setNoGravity(true);
        this.setSilent(false);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public void tick() {
        super.tick();
        ++this.stateTicks;
        ++this.lastAnimationChangeTimer;
        this.lastTickAnimationPct = this.animationPct;
        if (this.caster == null) {
            this.caster = this.getCaster();
        }
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0);
    }

    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<ItemStack>();
    }

    public ItemStack getItemBySlot(EquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
    }

    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public boolean isPickable() {
        return true;
    }

    public boolean isPushable() {
        return false;
    }

    public boolean canBeAffected(MobEffectInstance effectInstanceIn) {
        return false;
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isAffectedByPotions() {
        return false;
    }

    protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CASTER_UUID, "");
        builder.define(STATE, (byte)0);
        builder.define(CURRENT_ANIMATION, "neutral");
        builder.define(CURRENT_SEQUENCE, "");
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("caster", (String)this.entityData.get(CASTER_UUID));
        compound.putByte("state", this.getState());
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("caster")) {
            this.entityData.set(CASTER_UUID, compound.getString("caster"));
        }
        if (compound.contains("state")) {
            this.entityData.set(STATE, compound.getByte("state"));
        }
    }

    @Nullable
    protected Player getCaster() {
        UUID caster = this.getCasterUUID();
        if (caster != null) {
            return this.level().getPlayerByUUID(caster);
        }
        return null;
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

    protected void setState(byte state) {
        this.entityData.set(STATE, state);
        this.stateTicks = 0;
    }

    public byte getState() {
        return (Byte)this.entityData.get(STATE);
    }

    public float getAnimationPct(float partialTicks) {
        return MathUtils.lerpf(this.lastTickAnimationPct, this.animationPct, partialTicks);
    }

    public void setCurrentAnimation(String anim) {
        this.entityData.set(CURRENT_ANIMATION, anim);
        this.lastAnimationChangeTimer = 0;
        this.lastTickAnimationPct = 0.0f;
        this.animationPct = 0.0f;
    }

    public String getCurrentAnimation() {
        return (String)this.entityData.get(CURRENT_ANIMATION);
    }

    public int getLastAnimChangeTimer() {
        return this.lastAnimationChangeTimer;
    }

    protected void enableFlightFor(Player player) {
        if (player == null) {
            return;
        }
        player.setNoGravity(true);
        ManaAndArtifice.instance.proxy.setFlightEnabled(player, true);
    }

    protected void disableFlightFor(Player player) {
        if (player == null) {
            return;
        }
        player.setNoGravity(false);
        ManaAndArtifice.instance.proxy.setFlightEnabled(player, false);
    }
}
