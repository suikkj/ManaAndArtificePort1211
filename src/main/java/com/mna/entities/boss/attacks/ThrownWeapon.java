/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.commands.arguments.ParticleArgument
 *  net.minecraft.core.HolderLookup
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.util.Mth
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractHurtingProjectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.boss.attacks;

import net.minecraft.core.HolderLookup;
import com.mna.ManaAndArtifice;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownWeapon
extends AbstractHurtingProjectile
{
    private ItemStack renderStack = ItemStack.EMPTY;
    private ParticleOptions trailParticle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setMaxAge(10);
    private float renderScale = 1.0f;

    public ThrownWeapon(EntityType<? extends AbstractHurtingProjectile> type, Level world) {
        super(type, world);
    }

    public ThrownWeapon(EntityType<? extends AbstractHurtingProjectile> type, LivingEntity shooter, Level worldIn, ItemStack renderStack) {
        this(type, worldIn, shooter.getX(), shooter.getEyeY() - (double)0.1f, shooter.getZ(), renderStack);
        this.setOwner((Entity)shooter);
    }

    public ThrownWeapon(EntityType<? extends AbstractHurtingProjectile> type, Level worldIn, double x, double y, double z, ItemStack renderStack) {
        this(type, worldIn);
        this.setPos(x, y, z);
        this.setRenderStack(renderStack);
    }

    public ThrownWeapon setRenderStack(ItemStack stack) {
        this.renderStack = stack;
        return this;
    }

    public ThrownWeapon setTrailParticle(ParticleOptions particle) {
        this.trailParticle = particle;
        return this;
    }

    public ThrownWeapon setScale(float scale) {
        this.renderScale = scale;
        return this;
    }

    public ItemStack getRenderStack() {
        return this.renderStack;
    }

    public float getRenderScale() {
        return this.renderScale;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 Vector3d = new Vec3(x, y, z).normalize().scale((double)velocity);
        this.setDeltaMovement(Vector3d);
        double f = Vector3d.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)Vector3d.x, (double)Vector3d.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)Vector3d.y, (double)f) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    protected void onHitEntity(EntityHitResult ray) {
        Entity hitEntity = ray.getEntity();
        if (hitEntity instanceof LivingEntity) {
            LivingEntity livingHit = (LivingEntity)hitEntity;
            Entity eOwner = this.getOwner();
            if (eOwner instanceof LivingEntity) {
                livingHit.hurt(this.damageSources().mobAttack((LivingEntity)eOwner), 15.0f);
            }
        }
        if (!this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    protected void onHitBlock(BlockHitResult ray) {
        if (!this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    protected ParticleOptions getTrailParticle() {
        return this.trailParticle;
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 10; ++i) {
                this.level().addParticle(this.trailParticle, this.getX(), this.getY(), this.getZ(), -0.5 + Math.random(), -0.5 + Math.random(), -0.5 + Math.random());
            }
        }
    }

    protected float getInertia() {
        return 0.95f;
    }

    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, (double)-0.05f, 0.0));
        }
        super.tick();
    }

    public boolean hurt(DamageSource p_70097_1_, float p_70097_2_) {
        return false;
    }

    public boolean isPickable() {
        return false;
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeItemStack(this.renderStack, true);
        buffer.writeUtf(this.trailParticle.writeToString());
        buffer.writeFloat(this.renderScale);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.renderStack = net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode(additionalData);
        String trailParticle = additionalData.readUtf();
        try {
            this.trailParticle = ParticleArgument.readParticle((StringReader)new StringReader(trailParticle), (HolderLookup)this.level().holderLookup(Registries.PARTICLE_TYPE));
        }
        catch (CommandSyntaxException commandsyntaxexception) {
            ManaAndArtifice.LOGGER.warn("Couldn't load custom particle {}", trailParticle, commandsyntaxexception);
        }
        this.renderScale = additionalData.readFloat();
    }
}


