 
package com.mna.entities.sorcery.targeting;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.base.ISpellSigil;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.items.ItemInit;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class SpellSigil
extends Entity
implements ISpellSigil<SpellSigil> {
    private static final EntityDataAccessor<Integer> AFFINITY = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAGNITUDE = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> CASTER = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> PERMANENT = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> COOLDOWN = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CHARGES = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PLAYERLESS = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> SIZE = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> CAST_WITH_BONUS = SynchedEntityData.defineId(SpellSigil.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private UUID myID;
    private ISpellDefinition recipe;
    private Player _cachedCaster;
    private ArrayList<Entity> targetedEntities = new ArrayList();
    private int cooldownTicks = 0;

    public SpellSigil(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.myID = UUID.randomUUID();
    }

    public SpellSigil(Player caster, ISpellDefinition spell) {
        this((EntityType)EntityInit.SPELL_RUNE.get(), caster.level());
        this.setCasterAndSpell(caster, spell);
    }

    public void setCastWithBonus(byte bonus) {
        this.entityData.set(CAST_WITH_BONUS, bonus);
    }

    public void tick() {
        if (!((Boolean)this.entityData.get(PLAYERLESS)).booleanValue() && !this.level().isClientSide() && this.tickCount % 20 == 0) {
            if (this.getCasterID() != null) {
                MNACap.get(this.level(), WorldMagicProvider.MAGIC).ifPresent(m -> {
                    if (m.wasSigilRemoved(this.getCasterID(), this.myID)) {
                        this.remove(Entity.RemovalReason.DISCARDED);
                    }
                });
            } else {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        List entities = this.level().getEntities((Entity)this, this.getBoundingBox());
        if (!this.level().isClientSide()) {
            if (this.cooldownTicks > 0 && entities.size() == 0) {
                --this.cooldownTicks;
            }
            if (this.cooldownTicks <= 0) {
                this.setOnCooldown(false);
            }
        }
        entities.forEach(e -> this.push((Entity)e));
    }

    private void setCasterAndSpell(Player caster, ISpellDefinition spell) {
        this._cachedCaster = caster;
        this.setCasterID(caster.getUUID());
        this.recipe = spell;
        this.entityData.set(AFFINITY, spell.getHighestAffinity().ordinal());
        this.entityData.set(RADIUS, Float.valueOf(this.recipe.getShape().getValue(Attribute.RADIUS)));
        this.entityData.set(MAGNITUDE, ((int)spell.getShape().getValue(Attribute.MAGNITUDE)));
        this.entityData.set(PERMANENT, false);
        this.entityData.set(COOLDOWN, false);
        this.entityData.set(CHARGES, (byte)1);
        this.entityData.set(PLAYERLESS, false);
        this.entityData.set(CAST_WITH_BONUS, (byte)0);
    }

    public void setPermanent() {
        this.entityData.set(PERMANENT, true);
    }

    public boolean isPermanent() {
        return (Boolean)this.entityData.get(PERMANENT);
    }

    public void setSize(byte size) {
        size = (byte)MathUtils.clamp(size, 1, 5);
        this.entityData.set(SIZE, size);
        this.setBoundingBox(this.makeBoundingBox());
    }

    protected AABB makeBoundingBox() {
        byte size = this.getSize();
        return new AABB(this.position().add(-0.5 * (double)size, 0.0, -0.5 * (double)size), this.position().add(0.5 * (double)size, 0.1, 0.5 * (double)size));
    }

    public byte getSize() {
        return (Byte)this.entityData.get(SIZE);
    }

    public void setOnCooldown(boolean onCooldown) {
        this.entityData.set(COOLDOWN, onCooldown);
        this.cooldownTicks = 60;
    }

    public boolean isOnCooldown() {
        return (Boolean)this.entityData.get(COOLDOWN);
    }

    public void addCharge() {
        int numCharges = this.getCharges();
        if (numCharges < 10) {
            this.entityData.set(CHARGES, (numCharges + 1));
        }
    }

    public void consumeCharge() {
        this.entityData.set(CHARGES, (this.getCharges() - 1));
        this.setOnCooldown(true);
    }

    public int getCharges() {
        return (Integer)this.entityData.get(CHARGES);
    }

    public Affinity getAffinity() {
        return Affinity.values()[(Integer)this.entityData.get(AFFINITY)];
    }

    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        if (pPlayer.getItemInHand(pHand).getItem() == ItemInit.__DEBUG.get()) {
            this.entityData.set(PLAYERLESS, true);
            this.entityData.set(CASTER, Optional.of(Util.NIL_UUID));
            this._cachedCaster = null;
            if (!pPlayer.level().isClientSide()) {
                pPlayer.sendSystemMessage((Component)Component.translatable((String)"item.mna.debug_wand.playerless"));
            }
            return InteractionResult.sidedSuccess((boolean)pPlayer.level().isClientSide());
        }
        return super.interact(pPlayer, pHand);
    }

    public boolean isPickable() {
        return true;
    }

    public void push(Entity entityIn) {
        if (!(entityIn instanceof LivingEntity) && !(entityIn instanceof ItemEntity)) {
            return;
        }
        if ((Integer)this.entityData.get(MAGNITUDE) == 2 ? !(entityIn instanceof LivingEntity) : (Integer)this.entityData.get(MAGNITUDE) == 3 && !(entityIn instanceof Player)) {
            return;
        }
        this.trigger(entityIn, false);
    }

    public boolean hurt(DamageSource source, float amount) {
        this.trigger(source.getDirectEntity(), true);
        return true;
    }

    private void trigger(Entity e, boolean forceRemove) {
        if (!this.isAlive() || !forceRemove && this.isOnCooldown()) {
            return;
        }
        if (this.isPermanent() && this.getCharges() <= 0) {
            return;
        }
        float radius = ((Float)this.entityData.get(RADIUS)).floatValue();
        if (this.level().isClientSide()) {
            this.spawnParticles(radius);
        } else {
            boolean applied;
            if (!this.isPermanent() || forceRemove) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            if (this.recipe == null) {
                return;
            }
            SpellContext context = new SpellContext(this.level(), this.recipe, this);
            boolean bl = applied = this.targetEntities(radius, context) || this.targetBlocks(radius, context);
            if (applied) {
                if (this.isPermanent()) {
                    this.consumeCharge();
                }
                if (!(this.level().isClientSide() || this.isAlive() || ((Boolean)this.entityData.get(PLAYERLESS)).booleanValue())) {
                    MNACap.get(this.level(), WorldMagicProvider.MAGIC).ifPresent(m -> m.removeSigil(this.getCaster() != null ? this.getCaster().getUUID() : null, this.myID));
                }
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private void spawnParticles(float radius) {
        Affinity affinity = Affinity.values()[(Integer)this.entityData.get(AFFINITY)];
        MAParticleType particle = null;
        SoundEvent sound = null;
        switch (affinity) {
            case ARCANE: {
                particle = (MAParticleType)(ParticleInit.ARCANE.get());
                sound = radius < 2.0f ? SFX.Spell.Impact.Single.ARCANE : SFX.Spell.Impact.AoE.ARCANE;
                break;
            }
            case EARTH: {
                particle = (MAParticleType)(ParticleInit.DUST.get());
                sound = radius < 2.0f ? SFX.Spell.Impact.Single.EARTH : SFX.Spell.Impact.AoE.EARTH;
                break;
            }
            case ENDER: {
                particle = (MAParticleType)(ParticleInit.ENDER.get());
                sound = SFX.Spell.Impact.Single.ENDER;
                break;
            }
            case FIRE: {
                particle = (MAParticleType)(ParticleInit.FLAME.get());
                sound = radius < 2.0f ? SFX.Spell.Impact.Single.FIRE : SFX.Spell.Impact.AoE.FIRE;
                break;
            }
            case HELLFIRE: {
                particle = (MAParticleType)(ParticleInit.HELLFIRE.get());
                sound = radius < 2.0f ? SFX.Spell.Impact.Single.FIRE : SFX.Spell.Impact.AoE.FIRE;
                break;
            }
            case WATER: {
                particle = (MAParticleType)(ParticleInit.FROST.get());
                sound = SFX.Spell.Impact.Single.ICE;
                break;
            }
            case BLOOD: {
                particle = (MAParticleType)(ParticleInit.BLOOD.get());
                sound = SFX.Spell.Impact.Single.ICE;
                break;
            }
            case WIND: {
                particle = (MAParticleType)(ParticleInit.AIR_VELOCITY.get());
                sound = radius < 2.0f ? SFX.Spell.Impact.Single.WIND : SFX.Spell.Impact.AoE.WIND;
                break;
            }
            default: {
                particle = (MAParticleType)(ParticleInit.SPARKLE_STATIONARY.get());
            }
        }
        int i = 0;
        while ((float)i < 20.0f * radius) {
            Vec3 position = this.position().add((double)(-radius) + Math.random() * (double)radius, (double)(-radius) + Math.random() * (double)radius, (double)(-radius) + Math.random() * (double)radius);
            MAParticleType inst = new MAParticleType(particle);
            if (this.recipe != null) {
                inst = this.recipe.colorParticle(inst, (Entity)this.getOrCreatePlayer());
            }
            this.level().addParticle((ParticleOptions)inst, position.x, position.y, position.z, 0.0, 0.4, 0.0);
            ++i;
        }
        if (sound != null) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), sound, SoundSource.PLAYERS, 1.0f, (float)((double)0.8f + Math.random() * (double)0.4f), false);
        }
    }

    private boolean targetEntities(float radius, SpellContext context) {
        if (this.recipe == null || !this.recipe.isValid()) {
            return false;
        }
        if (!this.recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsEntities())) {
            return false;
        }
        SpellSource source = new SpellSource((LivingEntity)this.getOrCreatePlayer(), InteractionHand.MAIN_HAND);
        MutableBoolean applied_success = new MutableBoolean(false);
        List entities = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)(radius - 1.0f)));
        this.targetedEntities.clear();
        entities.forEach(e -> {
            if (!this.targetedEntities.contains(e)) {
                this.targetedEntities.add((Entity)e);
                if (SpellCaster.ApplyComponents(this.recipe, source, new SpellTarget((Entity)e), context).values().stream().anyMatch(c -> c.is_success)) {
                    applied_success.setTrue();
                }
            }
        });
        return applied_success.booleanValue();
    }

    protected Player getOrCreatePlayer() {
        Player caster = this.getCaster();
        return caster != null && caster.distanceToSqr((Entity)this) < 4096.0 ? caster : FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)this.level()));
    }

    private boolean targetBlocks(float radius, SpellContext context) {
        if (this.recipe == null || !this.recipe.isValid()) {
            return false;
        }
        if (!this.recipe.getComponents().stream().anyMatch(c -> ((SpellEffect)c.getPart()).targetsBlocks())) {
            return false;
        }
        SpellSource source = new SpellSource((LivingEntity)this.getOrCreatePlayer(), InteractionHand.MAIN_HAND);
        MutableBoolean applied_success = new MutableBoolean(false);
        int iRadius = (int)Math.floor(radius);
        for (int i = -iRadius; i <= iRadius; ++i) {
            for (int j = -iRadius; j <= iRadius; ++j) {
                for (int k = -iRadius; k <= iRadius; ++k) {
                    BlockPos offset = new BlockPos(i, j, k);
                    if (!SpellCaster.ApplyComponents(this.recipe, source, new SpellTarget(offset, Direction.DOWN), context).values().stream().anyMatch(c -> c.is_success)) continue;
                    applied_success.setTrue();
                }
            }
        }
        return applied_success.booleanValue();
    }

    @Nullable
    private Player getCaster() {
        if (this._cachedCaster == null && this.getCasterID() != null) {
            this._cachedCaster = this.level().getPlayerByUUID(this.getCasterID());
        }
        return this._cachedCaster;
    }

    @Nullable
    private UUID getCasterID() {
        return (UUID)((Optional)this.entityData.get(CASTER)).get();
    }

    public boolean isCaster(Player player) {
        if (player.getGameProfile() == null || player.getGameProfile().getId() == null || this.getCasterID() == null) {
            return false;
        }
        return this.getCasterID().equals(player.getGameProfile().getId());
    }

    private void setCasterID(UUID casterID) {
        this.entityData.set(CASTER, Optional.of(casterID));
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key == CASTER) {
            this._cachedCaster = null;
        }
        super.onSyncedDataUpdated(key);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(AFFINITY, Affinity.UNKNOWN.ordinal());
        builder.define(RADIUS, Float.valueOf(1.0f));
        builder.define(CASTER, Optional.empty());
        builder.define(MAGNITUDE, 0);
        builder.define(PERMANENT, false);
        builder.define(COOLDOWN, false);
        builder.define(CHARGES, 1);
        builder.define(PLAYERLESS, (byte)1);
        builder.define(SIZE, (byte)1);
        builder.define(CAST_WITH_BONUS, (byte)0);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("caster")) {
            try {
                this.setCasterID(UUID.fromString(compound.getString("caster")));
            }
            catch (Exception ex) {
                ManaAndArtifice.LOGGER.error("Failed to load caster ID when loading rune entity");
                ManaAndArtifice.LOGGER.throwing((Throwable)ex);
            }
        }
        if (compound.contains("recipe")) {
            this.recipe = SpellRecipe.fromNBT(compound.getCompound("recipe"));
        }
        if (compound.contains("affinity")) {
            this.entityData.set(AFFINITY, compound.getInt("affinity"));
        }
        if (compound.contains("charges")) {
            this.entityData.set(CHARGES, compound.getInt("charges"));
        }
        if (compound.contains("permanent")) {
            this.entityData.set(PERMANENT, compound.getBoolean("permanent"));
        }
        if (compound.contains("cooldown")) {
            this.entityData.set(COOLDOWN, compound.getBoolean("cooldown"));
        }
        if (compound.contains("customSize")) {
            this.setSize(compound.getByte("customSize"));
        }
        if (compound.contains("cooldownTicks")) {
            this.cooldownTicks = compound.getInt("cooldownTicks");
        }
        if (compound.contains("invisible")) {
            this.setInvisible(compound.getBoolean("invisible"));
        }
        if (compound.contains("bonus")) {
            this.entityData.set(CAST_WITH_BONUS, compound.getByte("bonus"));
        }
        if (compound.contains("myUUID")) {
            try {
                this.myID = UUID.fromString(compound.getString("myUUID"));
            }
            catch (Exception ex) {
                ManaAndArtifice.LOGGER.error("Failed to load UUID when loading EntitySpellRune");
            }
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("caster", this.getCasterID() != null ? this.getCasterID().toString() : "");
        compound.putString("myUUID", this.myID.toString());
        compound.putInt("affinity", ((Integer)this.entityData.get(AFFINITY)).intValue());
        compound.putInt("charges", ((Integer)this.entityData.get(CHARGES)).intValue());
        compound.putBoolean("permanent", ((Boolean)this.entityData.get(PERMANENT)).booleanValue());
        compound.putBoolean("cooldown", ((Boolean)this.entityData.get(COOLDOWN)).booleanValue());
        compound.putByte("customSize", this.getSize());
        compound.putInt("cooldownTicks", this.cooldownTicks);
        compound.putBoolean("invisible", this.isInvisible());
        compound.putByte("bonus", ((Byte)this.entityData.get(CAST_WITH_BONUS)).byteValue());
        CompoundTag recipeNBT = new CompoundTag();
        if (this.recipe != null) {
            this.recipe.writeToNBT(recipeNBT);
        }
        compound.put("recipe", (Tag)recipeNBT);
    }

    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide() && this.getCaster() != null) {
            MNACap.get(this.level(), WorldMagicProvider.MAGIC).ifPresent(m -> m.addSigil(this.getCaster(), this, (Byte)this.entityData.get(CAST_WITH_BONUS)));
        }
    }

    @Override
    public UUID getID() {
        return this.myID;
    }

    @Override
    public int getCountBonus() {
        return ((Byte)this.entityData.get(CAST_WITH_BONUS)).byteValue();
    }
}