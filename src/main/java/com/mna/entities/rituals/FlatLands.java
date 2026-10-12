 
package com.mna.entities.rituals;

import com.mna.ManaAndArtifice;
import com.mna.api.sound.SFX;
import com.mna.entities.rituals.FlatLandsProjectile;
import com.mna.items.ItemInit;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class FlatLands
extends Entity {
    private static final EntityDataAccessor<Vector3f> DATA_ID_TARGET = SynchedEntityData.defineId(FlatLands.class, (EntityDataSerializer)EntityDataSerializers.VECTOR3);
    private BlockPos AABB1;
    private BlockPos AABB2;
    private BlockPos[] targets;
    private int curIndex;
    private int age;
    Player caster_reference;
    UUID casterID;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float nextPageAngle;
    public float pageAngle;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public int ticks;
    public int ticksSinceTargetChange;
    public float yaw;

    public FlatLands(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void tick() {
        if (this.level().isClientSide()) {
            ++this.ticks;
            this.updatePageTurning();
            return;
        }
        if (this.curIndex >= this.targets.length) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        ++this.age;
        if (this.getCaster() == null) {
            if (this.age > 20) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        if (this.age % 10 == 0) {
            BlockPos me = this.blockPosition();
            BlockPos target = this.targets[this.curIndex++];
            if (this.curIndex < this.targets.length) {
                this.entityData.set(DATA_ID_TARGET, new Vector3f((float)this.targets[this.curIndex].getX() + 0.5f, (float)this.targets[this.curIndex].getY() + 0.5f, (float)this.targets[this.curIndex].getZ() + 0.5f));
            }
            FlatLandsProjectile proj = new FlatLandsProjectile(this.level());
            proj.setPoints(Vec3.atCenterOf((Vec3i)me), target);
            proj.setCaster(this.getCaster());
            this.level().playSound(null, (double)me.getX(), (double)me.getY(), (double)me.getZ(), SFX.Spell.Cast.EARTH, this.getSoundSource(), 0.25f, MathUtils.RandomBetween(0.9f, 1.1f));
            this.level().addFreshEntity((Entity)proj);
        }
    }

    public void setBounds(AABB bounds) {
        this.AABB1 = BlockPos.containing((double)bounds.minX, (double)bounds.minY, (double)bounds.minZ);
        this.AABB2 = BlockPos.containing((double)bounds.maxX, (double)bounds.maxY, (double)bounds.maxZ);
        double xDim = bounds.getXsize();
        double yDim = bounds.getYsize();
        double zDim = bounds.getZsize();
        ArrayList<BlockPos> allPoints = new ArrayList<BlockPos>();
        int x = 1;
        while ((double)x <= xDim - 1.0) {
            for (int y = (int)(yDim - 1.0); y > 0; y -= 3) {
                int z = 1;
                while ((double)z <= zDim - 1.0) {
                    allPoints.add(BlockPos.containing((double)(bounds.minX + (double)x), (double)(bounds.minY + (double)y), (double)(bounds.minZ + (double)z)));
                    if (!((double)(z += 3) >= zDim) || !((double)z < zDim + 2.0) || (zDim - 2.0) % 3.0 == 0.0) continue;
                    z = (int)(zDim - 1.0);
                }
            }
            if (!((double)(x += 3) > xDim - 1.0) || !((double)x < xDim + 2.0) || (xDim - 2.0) % 3.0 == 0.0) continue;
            x = (int)(xDim - 1.0);
        }
        this.targets = new BlockPos[allPoints.size()];
        this.targets = allPoints.toArray(this.targets);
        this.curIndex = 0;
    }

    private Vector3f target() {
        return (Vector3f)this.entityData.get(DATA_ID_TARGET);
    }

    public void setCaster(Player caster) {
        this.caster_reference = caster;
    }

    private Player getCaster() {
        if (this.caster_reference == null && this.casterID != null) {
            this.caster_reference = this.level().getPlayerByUUID(this.casterID);
        }
        return this.caster_reference;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_TARGET, new Vector3f(0.0f, 0.0f, 0.0f));
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("min_x") && compound.contains("min_y") && compound.contains("min_z")) {
            this.AABB1 = new BlockPos(compound.getInt("min_x"), compound.getInt("min_y"), compound.getInt("min_z"));
        }
        if (compound.contains("max_x") && compound.contains("max_y") && compound.contains("max_z")) {
            this.AABB2 = new BlockPos(compound.getInt("max_x"), compound.getInt("max_y"), compound.getInt("max_z"));
        }
        if (this.AABB1 != null && this.AABB2 != null) {
            AABB bb = MathUtils.createInclusiveBB(this.AABB1, this.AABB2);
            this.setBounds(bb);
        }
        if (compound.contains("curIndex")) {
            this.curIndex = compound.getInt("curIndex");
        }
        if (compound.contains("age")) {
            this.age = compound.getInt("age");
        }
        if (compound.contains("caster_uuid")) {
            try {
                this.casterID = UUID.fromString(compound.getString("caster_uuid"));
            }
            catch (Exception e) {
                ManaAndArtifice.LOGGER.error("Error loading caster UUID for flat lands projectile!");
            }
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("min_x", this.AABB1.getX());
        compound.putInt("min_y", this.AABB1.getY());
        compound.putInt("min_z", this.AABB1.getZ());
        compound.putInt("max_x", this.AABB2.getX());
        compound.putInt("max_y", this.AABB2.getY());
        compound.putInt("max_z", this.AABB2.getZ());
        compound.putInt("curIndex", this.curIndex);
        compound.putInt("age", this.age);
        compound.putString("caster_uuid", this.caster_reference != null ? this.caster_reference.getUUID().toString() : "");
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (!this.level().isClientSide()) {
            ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack((ItemLike)ItemInit.FLAT_LANDS_BOOK.get()));
            this.level().addFreshEntity((Entity)item);
        }
    }

    private void updatePageTurning() {
        float f2;
        this.pageTurningSpeed = this.nextPageTurningSpeed;
        this.pageAngle = this.nextPageAngle;
        if (this.target() != null) {
            double d0 = (double)this.target().x - this.getX();
            double d1 = (double)this.target().z() - this.getZ();
            this.yaw = (float)Mth.atan2((double)d1, (double)d0);
            this.nextPageTurningSpeed += 0.1f;
            if (this.nextPageTurningSpeed < 0.5f || this.random.nextInt(40) == 0) {
                float f1 = this.flipT;
                do {
                    this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));
                } while (f1 == this.flipT);
            }
        } else {
            this.yaw += 0.02f;
            this.nextPageTurningSpeed -= 0.1f;
        }
        while (this.nextPageAngle >= (float)Math.PI) {
            this.nextPageAngle -= (float)Math.PI * 2;
        }
        while (this.nextPageAngle < (float)(-Math.PI)) {
            this.nextPageAngle += (float)Math.PI * 2;
        }
        while (this.yaw >= (float)Math.PI) {
            this.yaw -= (float)Math.PI * 2;
        }
        while (this.yaw < (float)(-Math.PI)) {
            this.yaw += (float)Math.PI * 2;
        }
        for (f2 = this.yaw - this.nextPageAngle; f2 >= (float)Math.PI; f2 -= (float)Math.PI * 2) {
        }
        while (f2 < (float)(-Math.PI)) {
            f2 += (float)Math.PI * 2;
        }
        this.nextPageAngle += f2 * 0.4f;
        this.nextPageTurningSpeed = Mth.clamp((float)this.nextPageTurningSpeed, (float)0.0f, (float)1.0f);
        ++this.ticks;
        ++this.ticksSinceTargetChange;
        this.oFlip = this.flip;
        float f = (this.flipT - this.flip) * 0.4f;
        f = Mth.clamp((float)f, (float)-0.2f, (float)0.2f);
        this.flipA += (f - this.flipA) * 0.9f;
        this.flip += this.flipA;
    }
}


