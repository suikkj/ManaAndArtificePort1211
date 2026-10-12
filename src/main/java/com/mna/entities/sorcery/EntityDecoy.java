 
package com.mna.entities.sorcery;

import com.mna.entities.EntityInit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class EntityDecoy
extends Mob {
    private static final EntityDataAccessor<Optional<UUID>> RENDER_AS_UUID = SynchedEntityData.defineId(EntityDecoy.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(EntityDecoy.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(EntityDecoy.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAGNITUDE = SynchedEntityData.defineId(EntityDecoy.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private Player _cachedRenderAs;
    private HashSet<LivingEntity> _affectedEntities = new HashSet();

    public EntityDecoy(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }

    public EntityDecoy(Player caster, Level world, Vec3 position, float speed, float radius, float magnitude) {
        this((EntityType<? extends Mob>)((EntityType)EntityInit.DECOY_ENTITY.get()), world);
        this.moveTo(position.x, position.y, position.z, caster.getViewYRot(0.0f), caster.getViewXRot(0.0f));
        this.setPlayer(caster);
        this.entityData.set(SPEED, Float.valueOf(speed));
        this.entityData.set(RADIUS, Float.valueOf(radius));
        this.entityData.set(MAGNITUDE, Float.valueOf(magnitude));
        this._affectedEntities = new HashSet();
    }

    public void tick() {
        super.tick();
        if (this.navigation.isDone() && !this.level().isClientSide()) {
            Vec3 fwd = this.position().add(Vec3.directionFromRotation((Vec2)this.getRotationVector()).normalize().scale(30.0));
            this.navigation.moveTo(fwd.x, fwd.y, fwd.z, (double)((Float)this.entityData.get(SPEED)).floatValue());
        }
        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)((Float)this.entityData.get(RADIUS)).floatValue(), 2.0, (double)((Float)this.entityData.get(RADIUS)).floatValue()), e -> e instanceof PathfinderMob && Math.random() <= (double)(((Float)this.entityData.get(MAGNITUDE)).floatValue() / 10.0f)).stream().map(e -> (PathfinderMob)e).forEach(e -> {
                if (!this._affectedEntities.contains(e)) {
                    e.setTarget((LivingEntity)this);
                    this._affectedEntities.add((LivingEntity)e);
                }
            });
        }
        if (this.tickCount > 300) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public Player getPlayer() {
        if (this._cachedRenderAs == null) {
            Optional param = (Optional)this.entityData.get(RENDER_AS_UUID);
            if (!param.isPresent()) {
                return null;
            }
            UUID uuid = (UUID)param.get();
            this._cachedRenderAs = this.level().getPlayerByUUID(uuid);
        }
        return this._cachedRenderAs;
    }

    public void setPlayer(Player player) {
        this.entityData.set(RENDER_AS_UUID, Optional.of(player.getUUID()));
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RENDER_AS_UUID, Optional.empty());
        builder.define(RADIUS, Float.valueOf(3.0f));
        builder.define(SPEED, Float.valueOf(1.0f));
        builder.define(MAGNITUDE, Float.valueOf(5.0f));
    }

    protected void registerGoals() {
    }

    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<ItemStack>();
    }

    public ItemStack getItemBySlot(EquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, (double)0.35f).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}


