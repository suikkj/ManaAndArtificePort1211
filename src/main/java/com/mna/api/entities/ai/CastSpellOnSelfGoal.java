/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.entities.ai;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.spells.targeting.SpellSource;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

public class CastSpellOnSelfGoal<T extends Mob>
extends Goal {
    private final T user;
    private final ItemStack stack;
    private final Predicate<? super T> shouldCast;
    private final Consumer<? super T> onStartHandler;
    private final Consumer<? super T> onStopHandler;
    private final int warmup;
    private int counter;

    public CastSpellOnSelfGoal(T user, ItemStack stack, Predicate<? super T> shouldCast) {
        this((T)user, stack, shouldCast, null, null, 0);
    }

    public CastSpellOnSelfGoal(T user, ItemStack stack, Predicate<? super T> shouldCast, @Nullable Consumer<? super T> onStartHandler, @Nullable Consumer<? super T> onStopHandler, int warmup) {
        this.user = user;
        this.stack = stack;
        this.shouldCast = shouldCast;
        this.warmup = warmup;
        this.onStartHandler = onStartHandler;
        this.onStopHandler = onStopHandler;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public boolean canContinueToUse() {
        return this.counter < this.warmup;
    }

    public boolean canUse() {
        return this.shouldCast.test(this.user);
    }

    public void start() {
        if (this.onStartHandler != null) {
            this.onStartHandler.accept(this.user);
        }
        this.counter = 0;
    }

    public void stop() {
        if (this.onStopHandler != null) {
            this.onStopHandler.accept(this.user);
        }
        this.counter = 0;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public boolean isInterruptable() {
        return false;
    }

    public void tick() {
        if (++this.counter >= this.warmup) {
            ManaAndArtificeMod.getSpellHelper().affect(this.stack, ManaAndArtificeMod.getSpellHelper().parseSpellDefinition(this.stack), this.user.level(), new SpellSource((LivingEntity)this.user, InteractionHand.MAIN_HAND));
            if (this.onStopHandler != null) {
                this.onStopHandler.accept(this.user);
            }
        }
    }
}

