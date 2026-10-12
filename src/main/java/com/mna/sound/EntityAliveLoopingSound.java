/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 */
package com.mna.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class EntityAliveLoopingSound
extends AbstractTickableSoundInstance {
    private final Entity target;

    public EntityAliveLoopingSound(SoundEvent soundIn, Entity targetEntity) {
        super(soundIn, SoundSource.PLAYERS, targetEntity.level().getRandom());
        this.target = targetEntity;
        this.looping = true;
        this.delay = 0;
    }

    public void tick() {
        if (!this.target.isAlive()) {
            this.volume -= 0.1f;
            if (this.volume <= 0.0f) {
                this.stop();
            }
        }
    }
}

