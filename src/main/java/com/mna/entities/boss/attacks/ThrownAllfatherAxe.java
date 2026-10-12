 
package com.mna.entities.boss.attacks;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.DamageHelper;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.Odin;
import com.mna.entities.boss.attacks.ThrownWeapon;
import com.mna.items.ItemInit;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.SummonUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import com.mna.api.tools.MNACap;

public class ThrownAllfatherAxe
extends ThrownWeapon {
    private static final EntityDataAccessor<Byte> IS_RETURNING = SynchedEntityData.defineId(ThrownAllfatherAxe.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    public static final ItemStack renderStack = new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE.get());
    private CompoundTag itemData;

    public ThrownAllfatherAxe(EntityType<? extends AbstractHurtingProjectile> type, Level world) {
        super(type, world);
        this.setRenderStack(renderStack);
        this.setTrailParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setMaxAge(0));
    }

    public ThrownAllfatherAxe(LivingEntity shooter, Level worldIn, CompoundTag data) {
        this(worldIn, shooter.getX(), shooter.getEyeY() - (double)0.1f, shooter.getZ());
        this.setOwner((Entity)shooter);
        this.itemData = data;
    }

    public ThrownAllfatherAxe(Level worldIn, double x, double y, double z) {
        super((EntityType<? extends AbstractHurtingProjectile>)((EntityType)EntityInit.ALLFATHER_AXE.get()), worldIn);
        this.setPos(x, y, z);
        this.setNoGravity(true);
        this.setRenderStack(renderStack);
        this.setTrailParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setMaxAge(0));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 Vector3d = new Vec3(x, y, z).normalize().scale((double)velocity);
        this.setDeltaMovement(Vector3d);
        double f = Vector3d.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)Vector3d.x, (double)Vector3d.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)Vector3d.y, (double)f) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void push(Entity pEntity) {
        if (pEntity == this.getOwner()) {
            this.handleHitOwner((LivingEntity)pEntity);
        }
        super.push(pEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult ray) {
        Entity hitEntity = ray.getEntity();
        if (!this.isValidEntity(hitEntity)) {
            return;
        }
        LivingEntity livingHit = (LivingEntity)hitEntity;
        Entity eOwner = this.getOwner();
        if (!(eOwner instanceof LivingEntity)) {
            return;
        }
        if (livingHit == eOwner) {
            this.handleHitOwner(livingHit);
            return;
        }
        livingHit.hurt(DamageHelper.createSourcedType(DamageHelper.FROST, this.level().registryAccess(), this.getOwner()), 15.0f);
        livingHit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 100));
    }

    private boolean handleHitOwner(LivingEntity livingHit) {
        if (this.level().isClientSide() || !this.isReturning()) {
            return false;
        }
        ItemStack returnStack = new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE.get());
        returnStack.set(DataComponents.CUSTOM_DATA, CustomData.of(this.itemData));
        if (livingHit instanceof Player) {
            InventoryUtilities.removeItemFromInventory(new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE_CONTROL.get()), true, false, (IItemHandler)new InvWrapper((Container)((Player)livingHit).getInventory()));
            if (((Player)livingHit).addItem(returnStack)) {
                this.remove(Entity.RemovalReason.DISCARDED);
            } else {
                this.drop();
            }
        } else if (livingHit instanceof Odin) {
            ((Odin)livingHit).showAxe();
            this.remove(Entity.RemovalReason.DISCARDED);
        } else {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
        return true;
    }

    @Override
    protected void onHitBlock(BlockHitResult ray) {
    }

    protected void onInsideBlock(BlockState state) {
        BlockState blockstate = this.level().getBlockState(this.blockPosition());
        if (blockstate.getBlock() == Blocks.WATER) {
            this.level().setBlock(this.blockPosition(), Blocks.ICE.defaultBlockState(), 3);
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(IS_RETURNING, (byte)0);
    }

    @Override
    public void tick() {
        int returnTicks;
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            this.drop();
            return;
        }
        int n = returnTicks = owner instanceof Player ? 40 : 20;
        if (this.tickCount >= returnTicks && !this.isReturning()) {
            this.setReturning();
        }
        if (this.isReturning()) {
            this.setDeltaMovement(owner.position().add(0.0, 1.0, 0.0).subtract(this.position()).normalize());
        }
        if (this.level().isClientSide()) {
            Vec3 dm = this.getDeltaMovement();
            this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())).setScale(0.05f).setGravity(0.01f).setPhysics(true), this.getX() + Math.random() * dm.x, this.getY() + Math.random() * dm.y, this.getZ() + Math.random() * dm.z, 0.0, 0.0, 0.0);
        }
        if (this.firstTick) {
            this.playSound(SFX.Spell.Cast.ICE, 0.05f, 1.0f);
            this.playSound(SFX.Entity.SkeletonAssassin.BOLO_THROW, 0.75f, 0.5f);
        }
        super.tick();
    }

    @Override
    protected float getInertia() {
        return 0.95f;
    }

    @Override
    public boolean hurt(DamageSource p_70097_1_, float p_70097_2_) {
        return false;
    }

    private void drop() {
        if (this.level().isClientSide()) {
            return;
        }
        ItemEntity ie = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE.get()));
        ie.setDeltaMovement(this.getDeltaMovement());
        this.level().addFreshEntity((Entity)ie);
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    public boolean isReturning() {
        return (Byte)this.entityData.get(IS_RETURNING) == 1;
    }

    public void setReturning() {
        this.entityData.set(IS_RETURNING, 1);
        this.xPower = -this.xPower;
        this.yPower = -this.yPower;
        this.zPower = -this.zPower;
    }

    private boolean isValidEntity(Entity e) {
        java.util.Optional op;
        if (!(e instanceof LivingEntity)) {
            return false;
        }
        Entity shooter = this.getOwner();
        if (shooter == null) {
            return false;
        }
        if (!(shooter instanceof Player)) {
            return true;
        }
        Player player = (Player)shooter;
        if (SummonUtils.isSummon(e) && SummonUtils.getSummoner((LivingEntity)e) == player) {
            return false;
        }
        return !(e instanceof IFactionEnemy) || !(op = MNACap.get(player, PlayerProgressionProvider.PROGRESSION)).isPresent() || ((IFactionEnemy)e).getFaction() != ((IPlayerProgression)op.get()).getAlliedFaction();
    }

    @Override
    public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        if (this.getOwner() != null) {
            buffer.writeInt(this.getOwner().getId());
        } else {
            buffer.writeInt(-1);
        }
    }

    @Override
    public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        int ownerID = additionalData.readInt();
        if (ownerID > -1) {
            Entity owner = this.level().getEntity(ownerID);
            this.setOwner(owner);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public boolean save(CompoundTag nbt) {
        if (this.itemData != null) {
            nbt.put("itemData", (Tag)this.itemData);
        }
        return super.save(nbt);
    }

    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("itemData")) {
            this.itemData = nbt.getCompound("itemData");
        }
    }
}

