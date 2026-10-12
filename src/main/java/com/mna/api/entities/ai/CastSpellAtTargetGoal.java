/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.entities.ai;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

public class CastSpellAtTargetGoal<T extends PathfinderMob>
extends Goal {
    protected final T entity;
    protected final double moveSpeedAmp;
    protected int attackCooldown;
    protected final float maxAttackDistance;
    protected int attackTime = -1;
    protected int seeTime;
    protected boolean strafingClockwise;
    protected boolean strafingBackwards;
    protected int strafingTime = -1;
    protected ItemStack spell;
    protected boolean setItems;
    protected boolean hasCast = false;
    protected Consumer<T> startCallback;
    protected Consumer<T> stopCallback;
    protected Consumer<T> resetCallback;
    protected Function<T, Boolean> precastCallback;
    protected Function<T, Boolean> canUseCallback;
    protected LivingEntity target;

    public CastSpellAtTargetGoal(T mob, ItemStack spell, double moveSpeedAmpIn, int attackCooldownIn, float maxAttackDistanceIn) {
        this(mob, spell, moveSpeedAmpIn, attackCooldownIn, maxAttackDistanceIn, true);
    }

    public CastSpellAtTargetGoal(T mob, ItemStack spell, double moveSpeedAmpIn, int attackCooldownIn, float maxAttackDistanceIn, boolean setItems) {
        this.entity = mob;
        this.moveSpeedAmp = moveSpeedAmpIn;
        this.attackCooldown = attackCooldownIn;
        this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
        this.spell = spell;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.setItems = setItems;
    }

    public CastSpellAtTargetGoal<T> setAttackCooldown(int attackCooldownIn) {
        this.attackCooldown = attackCooldownIn;
        return this;
    }

    public CastSpellAtTargetGoal<T> setSpell(ItemStack spell) {
        this.spell = spell.copy();
        return this;
    }

    public CastSpellAtTargetGoal<T> setStartCallback(Consumer<T> callback) {
        this.startCallback = callback;
        return this;
    }

    public CastSpellAtTargetGoal<T> setStopCallback(Consumer<T> callback) {
        this.stopCallback = callback;
        return this;
    }

    public CastSpellAtTargetGoal<T> setResetCallback(Consumer<T> callback) {
        this.resetCallback = callback;
        return this;
    }

    public CastSpellAtTargetGoal<T> setPrecastCallback(Function<T, Boolean> callback) {
        this.precastCallback = callback;
        return this;
    }

    public CastSpellAtTargetGoal<T> setCanUseCallback(Function<T, Boolean> callback) {
        this.canUseCallback = callback;
        return this;
    }

    public boolean canUse() {
        if (this.canUseCallback != null && !this.canUseCallback.apply(this.entity).booleanValue()) {
            return false;
        }
        return this.entity.getTarget() != null && ManaAndArtificeMod.getSpellHelper().containsSpell(this.spell);
    }

    public boolean canContinueToUse() {
        if (this.hasCast) {
            return false;
        }
        return this.canUse() || !this.entity.getNavigation().isDone();
    }

    public void start() {
        super.start();
        this.hasCast = false;
        this.entity.setAggressive(true);
        this.target = this.entity.getTarget();
        if (this.startCallback != null) {
            this.startCallback.accept(this.entity);
        }
        if (this.setItems) {
            this.entity.setItemInHand(InteractionHand.MAIN_HAND, this.spell);
        }
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void stop() {
        super.stop();
        this.entity.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = this.attackCooldown;
        this.hasCast = false;
        if (this.setItems) {
            this.entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        this.entity.stopUsingItem();
        if (this.stopCallback != null) {
            this.stopCallback.accept(this.entity);
        }
    }

    public void tick() {
        LivingEntity livingentity = this.target;
        if (livingentity != null) {
            boolean positiveSeeTime;
            double d0 = this.entity.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
            boolean canSee = this.entity.getSensing().hasLineOfSight((Entity)livingentity);
            boolean bl = positiveSeeTime = this.seeTime > 0;
            if (canSee != positiveSeeTime) {
                this.seeTime = 0;
            }
            this.seeTime = canSee ? ++this.seeTime : --this.seeTime;
            if (!(d0 > (double)this.maxAttackDistance) && this.seeTime >= 20) {
                this.entity.getNavigation().stop();
                ++this.strafingTime;
            } else {
                this.entity.getNavigation().moveTo((Entity)livingentity, this.moveSpeedAmp);
                this.strafingTime = -1;
            }
            if (this.strafingTime >= 20) {
                if ((double)this.entity.getRandom().nextFloat() < 0.3) {
                    boolean bl2 = this.strafingClockwise = !this.strafingClockwise;
                }
                if ((double)this.entity.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }
                this.strafingTime = 0;
            }
            if (this.strafingTime > -1) {
                if (d0 > (double)(this.maxAttackDistance * 0.75f)) {
                    this.strafingBackwards = false;
                } else if (d0 < (double)(this.maxAttackDistance * 0.25f)) {
                    this.strafingBackwards = true;
                }
                this.entity.getMoveControl().strafe(this.strafingBackwards ? -0.5f : 0.5f, this.strafingClockwise ? 0.5f : -0.5f);
                this.entity.lookAt((Entity)livingentity, 30.0f, 30.0f);
            } else {
                this.entity.getLookControl().setLookAt((Entity)livingentity, 30.0f, 30.0f);
            }
            if (--this.attackTime <= 0 && this.seeTime >= -60) {
                ISpellDefinition recipe = ManaAndArtificeMod.getSpellHelper().parseSpellDefinition(this.spell);
                if (!canSee && this.seeTime < -60) {
                    int n = this.attackTime = this.attackCooldown == -1 ? recipe.getCooldown(null) : this.attackCooldown;
                    if (this.resetCallback != null) {
                        this.resetCallback.accept(this.entity);
                    }
                } else if (canSee) {
                    if (this.precastCallback != null && !this.precastCallback.apply(this.entity).booleanValue()) {
                        return;
                    }
                    this.entity.stopUsingItem();
                    SpellTarget targetHint = new SpellTarget((Entity)this.target);
                    ManaAndArtificeMod.getSpellHelper().affect(this.spell, recipe, this.entity.level(), new SpellSource((LivingEntity)this.entity, InteractionHand.MAIN_HAND), targetHint);
                    this.onSpellCast(recipe);
                    this.attackTime = this.attackCooldown == -1 ? recipe.getCooldown(null) : this.attackCooldown;
                    this.hasCast = true;
                }
            }
        }
    }

    protected void onSpellCast(ISpellDefinition spell) {
    }

    protected T getEntity() {
        return this.entity;
    }
}

