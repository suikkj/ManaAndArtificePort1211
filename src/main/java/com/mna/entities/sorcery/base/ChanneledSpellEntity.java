 
package com.mna.entities.sorcery.base;

import com.mna.tools.EnchantmentUtils;
import com.mna.api.affinity.Affinity;
import com.mna.api.sound.SFX;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.sound.EntityAliveLoopingSound;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public abstract class ChanneledSpellEntity
extends Entity {
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(ChanneledSpellEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(ChanneledSpellEntity.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final String NBT_CASTER_UUID = "caster_uuid";
    private static final String NBT_SPELL = "spell";
    private LivingEntity __cachedCaster = null;
    private SpellRecipe __cachedRecipe = null;
    private boolean __playedsound = false;
    private Affinity[] cachedAffinities = null;

    public ChanneledSpellEntity(EntityType<? extends ChanneledSpellEntity> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public ChanneledSpellEntity(EntityType<? extends ChanneledSpellEntity> entityType, LivingEntity caster, ISpellDefinition spell, Level world) {
        this(entityType, world);
        this.setCaster(caster);
        this.setSpell(spell);
    }

    public void tick() {
        LivingEntity caster = this.getCaster();
        if (caster == null || !caster.isAlive() || !caster.level().dimension().equals(this.level().dimension()) || caster.getUseItemRemainingTicks() <= 0 || caster.getUseItem().isEmpty()) {
            if (!this.level().isClientSide()) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        if (!this.level().isClientSide() && this.tickCount >= this.getMaxAge()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        SpellRecipe recipe = this.getSpell();
        if (!this.level().isClientSide() && !recipe.isValid()) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        boolean distanceSkip = this.position().distanceToSqr(this.xo, this.yo, this.zo) > 4.0;
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (distanceSkip) {
            return;
        }
        if (!this.level().isClientSide()) {
            if (this.tickCount > 0 && (this.getApplicationRate() == 1 || this.tickCount % this.getApplicationRate() == 0)) {
                this.applyEffect(caster.getUseItem(), recipe, caster, (ServerLevel)this.level());
            }
        } else {
            this.spawnParticles();
            this.playSounds();
        }
    }

    protected int getApplicationRate() {
        return 10;
    }

    protected abstract void applyEffect(ItemStack var1, SpellRecipe var2, LivingEntity var3, ServerLevel var4);

    protected SoundEvent getSoundEffect(SpellRecipe recipe) {
        switch (recipe.getHighestAffinity()) {
            case ARCANE: {
                return SFX.Loops.ARCANE;
            }
            case EARTH: {
                return SFX.Loops.EARTH;
            }
            case ENDER: {
                return SFX.Loops.ENDER;
            }
            case FIRE: {
                return SFX.Loops.FIRE;
            }
            case HELLFIRE: {
                return SFX.Loops.FIRE;
            }
            case LIGHTNING: {
                return SFX.Loops.LIGHTNING;
            }
            case WATER: {
                return SFX.Loops.WATER;
            }
            case ICE: {
                return SFX.Loops.ICE;
            }
            case WIND: {
                return SFX.Loops.AIR;
            }
            case BLOOD: {
                return SFX.Loops.BLOOD;
            }
        }
        return SFX.Loops.MANAWEAVING;
    }

    protected abstract void spawnAirParticles(SpellRecipe var1);

    protected abstract void spawnEarthParticles(SpellRecipe var1);

    protected abstract void spawnFireParticles(SpellRecipe var1, boolean var2, boolean var3);

    protected abstract void spawnWaterParticles(SpellRecipe var1, boolean var2);

    protected abstract void spawnEnderParticles(SpellRecipe var1, boolean var2);

    protected abstract void spawnArcaneParticles(SpellRecipe var1);

    protected void spawnParticles() {
        SpellRecipe recipe = this.getSpell();
        if (recipe == null) {
            return;
        }
        if (this.cachedAffinities == null) {
            this.cachedAffinities = recipe.getAffinity().keySet().toArray(new Affinity[0]);
        }
        switch (this.cachedAffinities[(int)(Math.random() * (double)this.cachedAffinities.length)]) {
            case ARCANE: {
                this.spawnArcaneParticles(recipe);
                break;
            }
            case EARTH: {
                this.spawnEarthParticles(recipe);
                break;
            }
            case ENDER: {
                this.spawnEnderParticles(recipe, false);
                break;
            }
            case BLOOD: {
                this.spawnEnderParticles(recipe, true);
                break;
            }
            case FIRE: {
                this.spawnFireParticles(recipe, false, false);
                break;
            }
            case HELLFIRE: {
                this.spawnFireParticles(recipe, true, false);
                break;
            }
            case LIGHTNING: {
                this.spawnFireParticles(recipe, false, true);
                break;
            }
            case WATER: {
                this.spawnWaterParticles(recipe, false);
                break;
            }
            case ICE: {
                this.spawnWaterParticles(recipe, true);
                break;
            }
            case WIND: {
                this.spawnAirParticles(recipe);
                break;
            }
            default: {
                this.spawnArcaneParticles(recipe);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    protected void playSounds() {
        if (this.__playedsound) {
            return;
        }
        SpellRecipe recipe = this.getSpell();
        if (recipe == null) {
            return;
        }
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new EntityAliveLoopingSound(this.getSoundEffect(recipe), this));
        this.__playedsound = true;
    }

    protected float getShapeAttributeByAge(Attribute attr) {
        float base;
        SpellRecipe recipe = this.getSpell();
        if (recipe == null || recipe.getShape() == null) {
            return 0.0f;
        }
        float adjusted = ((ModifiedSpellPart)recipe.getShape()).getValue(attr);
        if (adjusted < (base = ((ModifiedSpellPart)recipe.getShape()).getDefaultValue(attr))) {
            return adjusted;
        }
        return base + (adjusted - base) * Math.min((float)this.tickCount / 30.0f, 1.0f);
    }

    @Nullable
    public LivingEntity getCaster() {
        if (this.__cachedCaster == null) {
            int id = (Integer)this.entityData.get(CASTER_ID);
            Entity found = this.level().getEntity(id);
            if (found != null && found instanceof LivingEntity) {
                this.__cachedCaster = (LivingEntity)found;
            }
        }
        return this.__cachedCaster;
    }

    public void setCaster(LivingEntity caster) {
        if (caster != null) {
            this.entityData.set(CASTER_ID, caster.getId());
        }
    }

    public float getShapeAttribute(Attribute attr) {
        SpellRecipe recipe = this.getSpell();
        if (recipe == null || recipe.getShape() == null) {
            return 0.0f;
        }
        return ((ModifiedSpellPart)recipe.getShape()).getValue(attr);
    }

    protected int getMaxAge() {
        if (this.getSpell() == null || this.getSpell().getShape() == null || ((ModifiedSpellPart)this.getSpell().getShape()).getPart() == null || this.getCaster() == null) {
            return 100;
        }
        float multiplier = 1.0f;
        if (this.getCaster() != null && EnchantmentUtils.enchantmentMap((ItemStack)this.getCaster().getUseItem()).containsKey(Enchantments.CHANNELING)) {
            multiplier = 2.0f;
        }
        return (int)((float)((Shape)((ModifiedSpellPart)this.getSpell().getShape()).getPart()).maxChannelTime(this.getSpell().getShape()) * multiplier);
    }

    public SpellRecipe getSpell() {
        if (this.__cachedRecipe == null) {
            this.__cachedRecipe = SpellRecipe.fromNBT((CompoundTag)this.entityData.get(SPELL_RECIPE));
        }
        return this.__cachedRecipe;
    }

    public void setSpell(ISpellDefinition spell) {
        CompoundTag nbt = new CompoundTag();
        spell.writeToNBT(nbt);
        this.entityData.set(SPELL_RECIPE, nbt);
    }

    protected boolean losCheck(Entity other) {
        Vec3 vector3d = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 vector3d1 = new Vec3(other.getX(), other.getEyeY(), other.getZ());
        return this.level().clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this)).getType() == HitResult.Type.MISS;
    }

    protected boolean losCheck(BlockPos pos) {
        if (this.__cachedRecipe != null && this.__cachedRecipe.getComponents().stream().noneMatch(c -> ((SpellEffect)c.getPart()).getUseTag() == SpellPartTags.HARMFUL)) {
            return true;
        }
        Vec3 vector3d = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 vector3d1 = Vec3.atCenterOf((Vec3i)pos);
        BlockHitResult result = this.level().clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)this));
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getBlockPos().equals(pos);
        }
        return result.getType() == HitResult.Type.MISS;
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put(NBT_SPELL, (Tag)this.entityData.get(SPELL_RECIPE));
        int casterId = (Integer)this.entityData.get(CASTER_ID);
        compound.putInt(NBT_CASTER_UUID, casterId);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains(NBT_SPELL)) {
            this.entityData.set(SPELL_RECIPE, ((CompoundTag)compound.get(NBT_SPELL)));
        }
        if (compound.contains(NBT_CASTER_UUID)) {
            this.entityData.set(CASTER_ID, compound.getInt(NBT_CASTER_UUID));
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(CASTER_ID, -1);
        builder.define(SPELL_RECIPE, new CompoundTag());
    }

    public final int getOverrideColor() {
        LivingEntity caster = this.getCaster();
        SpellRecipe recipe = this.getSpell();
        if (caster == null || recipe == null) {
            return -1;
        }
        MutableInt color = new MutableInt(-1);
        if (caster instanceof Player) {
            MNACap.get(caster, PlayerMagicProvider.MAGIC).ifPresent(m -> color.setValue(m.getParticleColorOverride()));
        }
        if (color.getValue() == -1) {
            color.setValue(recipe.getParticleColorOverride());
        }
        return color.getValue();
    }
}


