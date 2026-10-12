/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.mna.mixins;

import com.mna.api.entities.DamageHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Player.class})
public class PlayerConflagrateNoHurtArmor {
    @Inject(at={@At(value="HEAD")}, method={"hurtArmor"}, cancellable=true)
    public void mna$hurtArmor(DamageSource pDamageSource, float pDamage, CallbackInfo cir) {
        if (pDamageSource.is(DamageHelper.CONFLAGRATE)) {
            cir.cancel();
        }
    }
}

