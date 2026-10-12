 
package com.mna.entities.utility;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.spells.crafting.SpellRecipe;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class SpellFX
extends Entity {
    private static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(SpellFX.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Integer> SFX = SynchedEntityData.defineId(SpellFX.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(SpellFX.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private SpellRecipe _cachedRecipe;
    private int age = 0;
    private boolean playedSound = false;

    public SpellFX(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void tick() {
        if (!this.playedSound) {
            this.getRecipe().iterateComponents(c -> {
                if (((SpellEffect)c.getPart()).SoundEffect() != null) {
                    this.level().playSound((Entity)this.getCaster(this.level()), this.blockPosition(), ((SpellEffect)c.getPart()).SoundEffect(), SoundSource.PLAYERS, ((SpellEffect)c.getPart()).getSoundVolume(), (float)((double)0.9f + Math.random() * (double)0.1f));
                }
            });
            this.playedSound = true;
        }
        if (this.level().isClientSide() && this.getRecipe() != null) {
            this.getRecipe().iterateComponents(c -> ((SpellEffect)c.getPart()).SpawnParticles(this.level(), this.position(), this.getDeltaMovement(), this.age, this.getCaster(this.level()), this.getRecipe()));
        }
        ++this.age;
        if (this.age >= 100) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public boolean isPickable() {
        return false;
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public void push(double x, double y, double z) {
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(SFX, -1);
        builder.define(CASTER_ID, -1);
        builder.define(SPELL_RECIPE, new CompoundTag());
    }

    public void setRecipe(ISpellDefinition recipe) {
        CompoundTag nbt = new CompoundTag();
        recipe.writeToNBT(nbt);
        this.entityData.set(SPELL_RECIPE, nbt);
    }

    @Nullable
    private SpellRecipe getRecipe() {
        if (this._cachedRecipe == null) {
            CompoundTag nbt = (CompoundTag)this.entityData.get(SPELL_RECIPE);
            this._cachedRecipe = SpellRecipe.fromNBT(nbt);
        }
        return this._cachedRecipe;
    }

    public void setCaster(@Nullable LivingEntity caster) {
        if (caster != null) {
            this.entityData.set(CASTER_ID, caster.getId());
        }
    }

    @Nullable
    public LivingEntity getCaster(Level world) {
        int id = (Integer)this.entityData.get(CASTER_ID);
        try {
            Entity e = world.getEntity(id);
            if (e instanceof LivingEntity) {
                return (LivingEntity)e;
            }
        }
        catch (Exception exception) {
             
        }
        return null;
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}


