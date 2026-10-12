 
package com.mna.entities.sorcery.targeting;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import java.util.HashMap;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class BoundBowProjectile
extends AbstractArrow {
    private static final EntityDataAccessor<Integer> AFFINITY = SynchedEntityData.defineId(BoundBowProjectile.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private ISpellDefinition spell;

    public BoundBowProjectile(EntityType<? extends AbstractArrow> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.setInvulnerable(true);
    }

    public void setSpell(ISpellDefinition recipe) {
        this.spell = recipe;
        this.entityData.set(AFFINITY, this.spell.getHighestAffinity().ordinal());
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AFFINITY, Affinity.ARCANE.ordinal());
    }

    public Affinity getAffinity() {
        return Affinity.values()[(Integer)this.entityData.get(AFFINITY)];
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            float spread = 0.2f;
            for (int i = 0; i < 10; ++i) {
                double velMod = Math.random();
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), this.getX() - (double)spread * (Math.random() * (double)spread * 2.0), this.getY() - (double)spread * (Math.random() * (double)spread * 2.0), this.getZ() - (double)spread * (Math.random() * (double)spread * 2.0), this.getDeltaMovement().x * velMod, this.getDeltaMovement().y * velMod, this.getDeltaMovement().z * velMod);
            }
        }
    }

    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        if (this.spell != null && this.spell.isValid()) {
            this.spell.writeToNBT(compound);
        }
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        this.spell = SpellRecipe.fromNBT(compound);
    }

    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide() && this.spell != null && this.spell.isValid()) {
            Entity shooter = this.getOwner();
            if (shooter != null && shooter instanceof LivingEntity) {
                SpellSource source = new SpellSource((LivingEntity)shooter, InteractionHand.MAIN_HAND);
                SpellTarget target = new SpellTarget(result.getEntity());
                SpellContext context = new SpellContext(this.level(), this.spell);
                HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(this.spell, source, target, context);
                if (this.getOwner() instanceof Player) {
                    results.entrySet().forEach(e -> {
                        if (((ComponentApplicationResult)((e.getValue()))).is_success) {
                            SpellCaster.addComponentRoteProgress((Player)this.getOwner(), (SpellEffect)e.getKey());
                        }
                    });
                }
            }
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide() && this.spell != null && this.spell.isValid()) {
            Entity shooter = this.getOwner();
            if (shooter != null && shooter instanceof LivingEntity) {
                SpellSource source = new SpellSource((LivingEntity)shooter, InteractionHand.MAIN_HAND);
                SpellTarget target = new SpellTarget(result.getBlockPos(), result.getDirection());
                SpellContext context = new SpellContext(this.level(), this.spell);
                HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(this.spell, source, target, context);
                if (this.getOwner() instanceof Player) {
                    results.entrySet().forEach(e -> {
                        if (((ComponentApplicationResult)((e.getValue()))).is_success) {
                            SpellCaster.addComponentRoteProgress((Player)this.getOwner(), (SpellEffect)e.getKey());
                        }
                    });
                }
            }
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}


