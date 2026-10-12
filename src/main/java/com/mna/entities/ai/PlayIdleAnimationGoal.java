/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.phys.Vec3
 *  software.bernie.geckolib.animatable.GeoEntity
 */
package com.mna.entities.ai;

import com.mojang.datafixers.util.Pair;
import java.util.EnumSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;

public class PlayIdleAnimationGoal<T extends GeoEntity>
extends Goal {
    private final T entity;
    private final Function<T, Boolean> canUseCallback;
    private final BiConsumer<T, String> playAnimCallback;
    private final Consumer<T> stopCallback;
    private final Pair<String, Integer>[] animations;
    int countdown = 0;

    @SafeVarargs
    public PlayIdleAnimationGoal(T entity, Function<T, Boolean> canUseCallback, BiConsumer<T, String> playAnimCallback, Consumer<T> stopCallback, Pair<String, Integer> ... animations) {
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
        this.entity = entity;
        this.canUseCallback = canUseCallback;
        this.playAnimCallback = playAnimCallback;
        this.stopCallback = stopCallback;
        this.animations = animations;
        if (this.animations.length == 0) {
            throw new RuntimeException("Need at least one animation for PlayIdleAnimationGoal!");
        }
    }

    public boolean requiresUpdateEveryTick() {
        return this.canContinueToUse();
    }

    public boolean canUse() {
        return this.canUseCallback.apply(this.entity);
    }

    public boolean canContinueToUse() {
        return this.countdown > 0;
    }

    public void start() {
        Pair<String, Integer> anim = this.animations[(int)(Math.random() * (double)this.animations.length)];
        this.playAnimCallback.accept(this.entity, (String)anim.getFirst());
        this.countdown = (Integer)anim.getSecond();
        if (this.entity instanceof PathfinderMob) {
            ((PathfinderMob)this.entity).getNavigation().stop();
        }
        super.start();
    }

    public void stop() {
        this.stopCallback.accept(this.entity);
        super.stop();
    }

    public void tick() {
        T t = this.entity;
        if (t instanceof PathfinderMob) {
            PathfinderMob pfm = (PathfinderMob)t;
            pfm.getNavigation().stop();
            pfm.setDeltaMovement(Vec3.ZERO);
        }
        --this.countdown;
    }
}

