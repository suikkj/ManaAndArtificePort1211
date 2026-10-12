 
package com.mna.entities.summon;

import com.mna.tools.EnchantmentUtils;
import com.google.common.collect.Multimap;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.ai.CastSpellAtTargetGoal;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.tools.MATags;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.entities.EntityInit;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.SummonUtils;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNAItemData;

public class GreaterAnimus
extends Monster
implements Enemy,
RangedAttackMob,
CrossbowAttackMob {
    private final RangedAttackGoal genericRangedGoal = new RangedAttackGoal((RangedAttackMob)this, 1.0, 20, 15.0f);
    private final RangedBowAttackGoal<GreaterAnimus> bowGoal = new RangedBowAttackGoal((Mob)this, 1.0, 20, 15.0f);
    private final RangedCrossbowAttackGoal<GreaterAnimus> crossbowGoal = new RangedCrossbowAttackGoal((Monster)this, 1.0, 20.0f);
    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal((PathfinderMob)this, 1.2, false);
    private final CastSpellAtTargetGoal<GreaterAnimus> spellGoal = new CastSpellAtTargetGoal<GreaterAnimus>(this, ItemStack.EMPTY, 0.0, -1, 16.0f, false){

        @Override
        public void onSpellCast(ISpellDefinition spell) {
            ((GreaterAnimus)(this.getEntity())).consumeMana(spell);
        }

        @Override
        public void start() {
            super.start();
            ISpellDefinition recipe = ManaAndArtificeMod.getSpellHelper().parseSpellDefinition(this.spell);
            this.attackTime = this.attackCooldown == -1 ? recipe.getCooldown(null) : this.attackCooldown;
        }
    };
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(GreaterAnimus.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final String NBT_HELDITEM = "held";
    private static final String NBT_MANA = "mana";
    private ItemStack heldItem = ItemStack.EMPTY;
    private float mana;
    private boolean isSpellCaster = false;

    public GreaterAnimus(EntityType<? extends Monster> type, Level world) {
        super(type, world);
    }

    public GreaterAnimus(Level world, ItemStack stack, float mana) {
        this((EntityType<? extends Monster>)((EntityType)EntityInit.GREATER_ANIMUS.get()), world);
        this.heldItem = stack;
        this.mana = mana;
        this.reassessLifeGoals();
    }

    public void reassessLifeGoals() {
        if (this.level() != null && !this.level().isClientSide()) {
            this.goalSelector.removeGoal((Goal)this.meleeGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            this.goalSelector.removeGoal(this.spellGoal);
            if (SpellRecipe.stackContainsSpell(this.heldItem)) {
                this.spellGoal.setSpell(this.heldItem);
                this.goalSelector.addGoal(2, this.spellGoal);
                this.isSpellCaster = true;
            } else if (this.heldItem.is(Items.BOW)) {
                int i = 20;
                this.bowGoal.setMinAttackInterval(i);
                this.goalSelector.addGoal(4, this.bowGoal);
            } else if (this.heldItem.is(Items.CROSSBOW)) {
                this.goalSelector.addGoal(4, this.crossbowGoal);
            } else if (MATags.isItemIn(this.heldItem.getItem(), MATags.Items.ANIMUS_BOWS)) {
                this.goalSelector.addGoal(4, (Goal)this.genericRangedGoal);
            } else {
                net.minecraft.world.item.component.ItemAttributeModifiers attrs = this.heldItem.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
                attrs.modifiers().forEach(entry -> {
                    try {
                        AttributeInstance ai = this.getAttribute(entry.attribute());
                        if (ai != null) {
                            ai.addPermanentModifier(entry.modifier());
                        }
                    }
                    catch (Throwable throwable) {
                         
                    }
                });
                this.goalSelector.addGoal(4, (Goal)this.meleeGoal);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0).add(Attributes.MOVEMENT_SPEED, (double)0.33f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.is(DamageTypes.CACTUS) || pSource.is(DamageTypes.DROWN) || pSource.is(DamageTypes.DRY_OUT) || pSource.is(DamageTypes.FALL) || pSource.is(DamageTypes.HOT_FLOOR) || pSource.is(DamageTypes.STARVE) || pSource.is(DamageTypes.SWEET_BERRY_BUSH)) {
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CHARGING_CROSSBOW, false);
    }

    protected void registerGoals() {
        this.targetSelector.addGoal(0, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
    }

    public Iterable<ItemStack> getArmorSlots() {
        return Arrays.asList(new ItemStack[0]);
    }

    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        if (pSlot != EquipmentSlot.MAINHAND) {
            return ItemStack.EMPTY;
        }
        return this.heldItem;
    }

    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
        if (pSlot != EquipmentSlot.MAINHAND) {
            return;
        }
        this.heldItem = pStack.copy();
    }

    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    public void writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, this.heldItem);
    }

    public void readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf additionalData) {
        this.heldItem = net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)additionalData);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putFloat(NBT_MANA, this.mana);
        if (!this.heldItem.isEmpty()) {
            pCompound.put(NBT_HELDITEM, (Tag)MNAItemData.save(this.heldItem, new CompoundTag()));
        }
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.mana = pCompound.getFloat(NBT_MANA);
        if (pCompound.contains(NBT_HELDITEM)) {
            this.heldItem = ItemStack.parseOptional(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(), (CompoundTag)pCompound.getCompound(NBT_HELDITEM));
        }
    }

    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);
        if (this.isRemoved()) {
            LivingEntity summoner = SummonUtils.getSummoner((LivingEntity)this);
            if (!this.heldItem.isEmpty() && summoner != null && summoner instanceof Player) {
                ItemStack dropStack = this.heldItem.copy();
                this.heldItem = ItemStack.EMPTY;
                Map enchants = EnchantmentUtils.enchantmentMap((ItemStack)dropStack);
                if ((enchants.containsKey(Enchantments.LOYALTY) || enchants.containsKey(EnchantmentInit.RETURNING.get())) && summoner.isAlive()) {
                    dropStack = InventoryUtilities.mergeToPlayerInvPrioritizeOffhand((Player)summoner, dropStack);
                }
                if (!dropStack.isEmpty()) {
                    ItemEntity entityItem = new ItemEntity(this.level(), this.position().x, this.position().y, this.position().z, dropStack);
                    this.level().addFreshEntity((Entity)entityItem);
                }
            }
        }
    }

    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand((LivingEntity)this, item -> item instanceof BowItem)));
        AbstractArrow abstractarrow = this.getArrow(itemstack, pDistanceFactor);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            abstractarrow = ((BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
        }
        double d0 = pTarget.getX() - this.getX();
        double d1 = pTarget.getY(0.3333333333333333) - abstractarrow.getY();
        double d2 = pTarget.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        abstractarrow.shoot(d0, d1 + d3 * (double)0.2f, d2, 1.6f, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity((Entity)abstractarrow);
    }

    protected AbstractArrow getArrow(ItemStack pArrowStack, float pDistanceFactor) {
        return ProjectileUtil.getMobArrow((LivingEntity)this, (ItemStack)pArrowStack, (float)pDistanceFactor, this.getMainHandItem());
    }

    public void consumeMana(ISpellDefinition spell) {
        if (this.level().isClientSide()) {
            return;
        }
        this.mana -= spell.getManaCost();
        if (this.mana <= 0.0f) {
            this.dropAllDeathLoot((net.minecraft.server.level.ServerLevel)this.level(), this.damageSources().fellOutOfWorld());
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public void setChargingCrossbow(boolean pIsCharging) {
        this.entityData.set(IS_CHARGING_CROSSBOW, pIsCharging);
    }

    public boolean isChargingCrossbow() {
        return (Boolean)this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isChargingCrossbow()) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
        }
        if (this.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        }
        if (this.isAggressive()) {
            if (this.isSpellCaster) {
                return AbstractIllager.IllagerArmPose.SPELLCASTING;
            }
            if (this.isHolding(is -> is.getItem() instanceof BowItem)) {
                return AbstractIllager.IllagerArmPose.BOW_AND_ARROW;
            }
            return AbstractIllager.IllagerArmPose.ATTACKING;
        }
        return AbstractIllager.IllagerArmPose.NEUTRAL;
    }

    protected void shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy) {
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getY(0.3333333333333333) - projectile.getY();
        double d2 = target.getZ() - shooter.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        projectile.shoot(d0, d1 + d3 * 0.2, d2, velocity, inaccuracy);
    }

    protected void shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy) {
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getY(0.3333333333333333) - projectile.getY();
        double d2 = target.getZ() - shooter.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        projectile.shoot(d0, d1 + d3 * 0.2, d2, velocity, inaccuracy);
    }

    public void shootCrossbowProjectile(LivingEntity p_33275_, ItemStack p_33276_, Projectile p_33277_, float p_33278_) {
        this.shootCrossbowProjectile((LivingEntity)this, p_33275_, p_33277_, p_33278_, 1.6f);
    }

    public void onCrossbowAttackPerformed() {
    }
}

