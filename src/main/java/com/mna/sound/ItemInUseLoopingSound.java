/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.player.Player
 */
package com.mna.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class ItemInUseLoopingSound
extends AbstractTickableSoundInstance {
    private final Player player;

    public ItemInUseLoopingSound(SoundEvent soundIn, Player playerIn) {
        super(soundIn, SoundSource.PLAYERS, playerIn.level().getRandom());
        this.player = playerIn;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.15f;
    }

    public void tick() {
        if (this.player.getUseItemRemainingTicks() <= 0) {
            this.volume -= 0.1f;
            if (this.volume <= 0.0f) {
                this.stop();
            }
        }
    }
}

