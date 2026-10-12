/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 */
package com.mna.sound;

import com.mna.tools.math.MathUtils;
import java.util.function.Predicate;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class PredicateLoopingSound
extends AbstractTickableSoundInstance {
    private final Predicate<String> predicate;
    private final String id;

    public PredicateLoopingSound(SoundEvent soundIn, String id, Predicate<String> predicate) {
        super(soundIn, SoundSource.PLAYERS, RandomSource.createThreadSafe());
        this.predicate = predicate;
        this.looping = true;
        this.delay = 0;
        this.id = id;
    }

    public PredicateLoopingSound setPosition(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        return this;
    }

    public PredicateLoopingSound setVolume(float volume) {
        this.volume = volume;
        return this;
    }

    public void tick() {
        if (this.predicate == null || !this.predicate.test(this.id)) {
            this.volume = MathUtils.clamp01(this.volume - 0.1f);
            if (this.volume <= 0.0f) {
                this.stop();
            }
        }
    }
}

