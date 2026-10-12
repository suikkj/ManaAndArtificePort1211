 
package com.mna.entities.projectile;

import com.mna.entities.EntityInit;
import com.mna.entities.faction.WitchHunter;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class WitchhunterTrickshot
extends Entity {
    protected static final EntityDataAccessor<String> EFFECT_ID = SynchedEntityData.defineId(WitchhunterTrickshot.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Boolean> SMASH = SynchedEntityData.defineId(WitchhunterTrickshot.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<CompoundTag> LERPDATA = SynchedEntityData.defineId(WitchhunterTrickshot.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final int maxAge = 20;
    private static final int maxAge_SMASH = 5;
    private static final float arrowSpawnAge = 10.0f;
    private static final float radius = 4.0f;
    private int effectDuration;
    private int effectMagnitude;
    private Vec3 cachedOffset;
    private MobEffect cachedEffect;
    private Vec3 potion_control_1;
    private Vec3 potion_control_2;
    private WitchHunter shooter;

    public WitchhunterTrickshot(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public WitchhunterTrickshot(WitchHunter shooter, Vec3 initialPosition, Vec3 destPos, MobEffect effect, int duration, int magnitude, boolean smash) {
        super((EntityType)EntityInit.WITCH_HUNTER_TRICKSHOT.get(), shooter.level());
        this.teleportTo(initialPosition.x, initialPosition.y, initialPosition.z);
        this.setDestinationPoint(destPos);
        this.setEffect(effect);
        this.effectDuration = duration;
        this.effectMagnitude = magnitude;
        this.entityData.set(SMASH, smash);
        this.shooter = shooter;
    }

    public void tick() {
        int myMaxAge;
        ++this.tickCount;
        boolean smash = (Boolean)this.entityData.get(SMASH);
        if (!smash && this.level().isClientSide() && (float)this.tickCount == 10.0f) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 1.0f, (float)(0.9 + Math.random() * 0.2), false);
        }
        int n = myMaxAge = smash ? 5 : 20;
        if (this.tickCount >= myMaxAge) {
            if (!this.level().isClientSide()) {
                this.applyEffect();
            }
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level().isClientSide() && this.getEffect() != null) {
            this.level().levelEvent(2002, BlockPos.containing((Position)this.getDestination()), this.getEffect().getColor());
        }
    }

    public boolean renderArrow() {
        return (Boolean)this.entityData.get(SMASH) == false && (float)this.tickCount > 10.0f;
    }

    public Vec3 getPotionPosition(float partialTicks) {
        float f = ((float)this.tickCount + partialTicks) / 20.0f;
        if (((Boolean)this.entityData.get(SMASH)).booleanValue()) {
            return MathUtils.lerpVector3d(this.position(), this.getDestination(), f);
        }
        if (!this.calculateControlPoints()) {
            return this.position();
        }
        return MathUtils.bezierVector3d(this.position(), this.getDestination(), this.potion_control_1, this.potion_control_2, f);
    }

    public Vec3 getArrowPosition(float partialTicks) {
        float f = MathUtils.clamp01(((float)this.tickCount + partialTicks - 10.0f) / 10.0f);
        return MathUtils.lerpVector3d(this.position(), this.getDestination(), f);
    }

    @Nullable
    public MobEffect getEffect() {
        if (this.cachedEffect == null) {
            ResourceLocation effectID = ResourceLocation.parse((String)this.entityData.get(EFFECT_ID));
            this.cachedEffect = (MobEffect)BuiltInRegistries.MOB_EFFECT.get(effectID);
        }
        return this.cachedEffect;
    }

    private void applyEffect() {
        if (this.level().isClientSide()) {
            return;
        }
        net.minecraft.core.Holder<MobEffect> effect = this.getEffect();
        if (effect == null) {
            return;
        }
        List targets = this.level().getEntities((Entity)this, new AABB(BlockPos.containing((Position)this.getDestination())).inflate(4.0), e -> !SummonUtils.isTargetFriendly(e, (LivingEntity)this.shooter)).stream().map(e -> (LivingEntity)e).collect(Collectors.toList());
        for (LivingEntity e2 : (java.util.List<LivingEntity>)targets) {
            if (effect.isInstantenous()) {
                effect.applyInstantenousEffect((Entity)this.shooter, (Entity)this, e2, this.effectMagnitude, 0.5);
                continue;
            }
            e2.addEffect(new MobEffectInstance(effect, this.effectDuration, this.effectMagnitude));
        }
    }

    public void setEffect(MobEffect effect) {
        this.entityData.set(EFFECT_ID, BuiltInRegistries.MOB_EFFECT.getKey(effect).toString());
    }

    public void setDestinationPoint(Vec3 dest) {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("x", dest.x);
        nbt.putDouble("y", dest.y);
        nbt.putDouble("z", dest.z);
        this.entityData.set(LERPDATA, nbt);
        Vec3 vector3d = dest.subtract(this.position()).normalize();
        double f = vector3d.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)vector3d.x, (double)vector3d.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)vector3d.y, (double)f) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    private boolean calculateControlPoints() {
        if (this.potion_control_1 != null && this.potion_control_2 != null) {
            return true;
        }
        Vec3 start = this.position();
        Vec3 end = this.getDestination();
        if (start == null || end == null) {
            return false;
        }
        this.potion_control_1 = start.add(end.subtract(start).scale(0.3)).add(0.0, 1.0, 0.0);
        this.potion_control_2 = start.add(end.subtract(start).scale(0.6)).add(0.0, 1.0, 0.0);
        return true;
    }

    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(16.0);
    }

    @Nullable
    public Vec3 getDestination() {
        if (this.cachedOffset == null) {
            CompoundTag nbt = (CompoundTag)this.entityData.get(LERPDATA);
            if (!(nbt != null && nbt.contains("x") && nbt.contains("y") && nbt.contains("z"))) {
                return null;
            }
            this.cachedOffset = new Vec3(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
        }
        return this.cachedOffset;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(EFFECT_ID, BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.MOVEMENT_SLOWDOWN.value()).toString());
        builder.define(LERPDATA, new CompoundTag());
        builder.define(SMASH, false);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("effect")) {
            this.entityData.set(EFFECT_ID, compound.get("effect").getAsString());
        }
        if (compound.contains("lerpdata")) {
            this.entityData.set(LERPDATA, ((CompoundTag)compound.get("lerpdata")));
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("effect", (String)this.entityData.get(EFFECT_ID));
        compound.put("lerpdata", (Tag)this.entityData.get(LERPDATA));
    }
}


