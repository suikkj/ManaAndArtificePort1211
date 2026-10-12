/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.mna.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mna.effects.beneficial.EffectSoar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Player.class})
public class PlayerElytra {
    @ModifyExpressionValue(method={"tryToStartFallFlying"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z", remap=false)})
    public boolean elytraOverride(boolean original) {
        return original || EffectSoar.canSoar((LivingEntity)(Object)this);
    }
}

