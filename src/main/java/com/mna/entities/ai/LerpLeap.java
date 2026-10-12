/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.commands.arguments.EntityAnchorArgument$Anchor
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.ai;

import com.mna.tools.math.MathUtils;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class LerpLeap<T extends Mob>
extends Goal {
    private int forceLerpTicks = -1;
    private int lerpTicks;
    private int initialDelay;
    private int leapHeight = 3;
    private int endWaitTicks = 0;
    private float leapSpeed = 2.0f;
    private int phaseTicks = 0;
    private int phase = 0;
    private Vec3 start;
    private Vec3 end;
    private Vec3 control_1;
    private Vec3 control_2;
    private T entity;
    private Predicate<T> canUsePredicate;
    private Consumer<Events> onEvent;
    private Function<Vec3, Vec3> endAdjuster;

    public LerpLeap(T entity, int initialDelay, int leapHeight, Predicate<T> canUsePredicate, Consumer<Events> onEvent) {
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
        this.initialDelay = initialDelay;
        this.entity = entity;
        this.canUsePredicate = canUsePredicate;
        this.leapHeight = leapHeight;
        this.onEvent = onEvent;
    }

    public LerpLeap<T> setForceLerpTicks(int ticks) {
        this.forceLerpTicks = ticks;
        return this;
    }

    public LerpLeap<T> setEndWaitTicks(int ticks) {
        this.endWaitTicks = ticks;
        return this;
    }

    public LerpLeap<T> setLeapSpeed(float speed) {
        this.leapSpeed = speed;
        return this;
    }

    public LerpLeap<T> setEndAdjuster(Function<Vec3, Vec3> endCalculator) {
        this.endAdjuster = endCalculator;
        return this;
    }

    public boolean isInterruptable() {
        return false;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public boolean canUse() {
        return this.canUsePredicate.test(this.entity);
    }

    public void stop() {
        this.setPhase(0);
        this.entity.setNoGravity(false);
        this.entity.setDeltaMovement(Vec3.ZERO);
        this.onEvent.accept(Events.STOP);
    }

    public boolean canContinueToUse() {
        return this.phase < 4;
    }

    public void start() {
        this.entity.setNoGravity(true);
        this.entity.getNavigation().stop();
        this.entity.setDeltaMovement(Vec3.ZERO);
        this.setPhase(0);
        this.onEvent.accept(Events.START);
    }

    private void setPhase(int phase) {
        this.phase = phase;
        this.phaseTicks = 0;
    }

    public void tick() {
        ++this.phaseTicks;
        if (this.entity.getTarget() == null) {
            if (this.phaseTicks >= 10) {
                this.setPhase(4);
            }
            return;
        }
        switch (this.phase) {
            case 0: {
                if (this.phaseTicks < this.initialDelay) break;
                Vec3 direction = this.entity.getTarget().position().subtract(this.entity.position()).normalize();
                this.onEvent.accept(Events.LEAP);
                this.start = this.entity.position();
                this.end = this.entity.getTarget().position().subtract(direction);
                if (this.endAdjuster != null) {
                    this.end = this.endAdjuster.apply(this.end);
                }
                double distance = this.start.distanceTo(this.end);
                if (this.forceLerpTicks > -1) {
                    this.lerpTicks = this.forceLerpTicks;
                } else {
                    this.lerpTicks = (int)(distance / (double)this.leapSpeed);
                    if (this.lerpTicks < 15) {
                        this.lerpTicks = 15;
                    }
                }
                Vec3 difference = this.end.subtract(this.start);
                this.control_1 = this.start.add(difference.scale(0.3)).add(0.0, (double)this.leapHeight, 0.0);
                this.control_2 = this.start.add(difference.scale(0.6)).add(0.0, (double)this.leapHeight, 0.0);
                this.setPhase(1);
                break;
            }
            case 1: {
                if (this.phaseTicks <= this.lerpTicks) {
                    if (this.lerpTicks < 5 || this.phaseTicks == this.lerpTicks - 5) {
                        this.onEvent.accept(Events.LAND);
                    }
                    if (this.phaseTicks == this.lerpTicks / 2) {
                        this.onEvent.accept(Events.APEX);
                    }
                    float lerpPct = (float)this.phaseTicks / (float)this.lerpTicks;
                    Vec3 position = MathUtils.bezierVector3d(this.start, this.end, this.control_1, this.control_2, lerpPct);
                    this.entity.lookAt(EntityAnchorArgument.Anchor.FEET, this.end);
                    this.entity.setPos(position.x, position.y, position.z);
                    break;
                }
                this.setPhase(2);
                break;
            }
            case 2: {
                this.onEvent.accept(Events.DAMAGE);
                this.setPhase(3);
                break;
            }
            case 3: {
                if (this.phaseTicks < this.endWaitTicks) break;
                this.setPhase(4);
            }
        }
    }

    public static enum Events {
        START,
        STOP,
        LEAP,
        APEX,
        LAND,
        DAMAGE;

    }
}

