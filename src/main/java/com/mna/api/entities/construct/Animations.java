/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  software.bernie.geckolib.animation.RawAnimation
 */
package com.mna.api.entities.construct;

import software.bernie.geckolib.animation.RawAnimation;

public enum Animations {
    IDLE("idle"),
    FLY_IDLE("fly_idle"),
    SWIM_IDLE("swim_idle"),
    WALK("walk"),
    RUN("run"),
    RUN_HAPPY("run_happy"),
    SPRINT("sprint"),
    FLY_MOVING("fly_moving"),
    SWIM_MOVING("swim_loop"),
    DRILL_ENTER("drill_enter", "drill_loop"),
    DRILL_EXIT("drill_leave"),
    LEAPING("leap", "airborne_leaping"),
    LEAP_LAND("leap_land"),
    SWING_LEFT("swing_left"),
    SWING_RIGHT("swing_right"),
    SWING_DUAL("dual_sweep"),
    SPRAY_LEFT("spray_left"),
    SPRAY_RIGHT("spray_right"),
    SPRAY_DUAL("spray_both"),
    SPRINKLER("sprinkler"),
    FISHING_LEFT("swing_left", "fish_loop_left"),
    FISHING_RIGHT("swing_right", "fish_loop_right"),
    EAT_LEFT("eat_left"),
    EAT_RIGHT("eat_right"),
    ADVENTURE_LEAVE("adventure_leave"),
    ADVENTURE_RETURN("adventure_return"),
    CHANNEL_LEFT("channel_cast_left"),
    CHANNEL_RIGHT("channel_cast_right"),
    SHOOT_LEFT("shoot_left"),
    SHOOT_RIGHT("shoot_right"),
    SHOOT_DUAL("shoot_dual"),
    DEFEATED("defeated_enter", "defeated_loop"),
    DEFEATED_EXIT("defeated_exit"),
    RUMMAGE("rummage"),
    RUMMAGE_THROW_LEFT("rummage_throw_left", "rummage"),
    RUMMAGE_THROW_RIGHT("rummage_throw_right", "rummage"),
    READING("reading");

    private String[] sequence;

    private Animations(String ... sequence) {
        this.sequence = sequence;
    }

    public RawAnimation play() {
        RawAnimation anim = RawAnimation.begin();
        for (int i = 0; i < this.sequence.length - 1; ++i) {
            anim.thenLoop("animation.construct." + this.sequence[i]);
        }
        anim.thenLoop("animation.construct." + this.sequence[this.sequence.length - 1]);
        return anim;
    }
}

