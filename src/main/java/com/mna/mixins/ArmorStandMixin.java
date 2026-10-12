/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.mna.mixins;

import com.mna.entities.constructs.ConstructAssemblyStand;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ArmorStand.class})
public class ArmorStandMixin {
    @Inject(method={"interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"}, at={@At(value="HEAD")}, cancellable=true)
    public void mna_convertToConstructStand(Player player, Vec3 vector, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        ArmorStand self = (ArmorStand)(Object)this;
        ItemStack stack = player.getItemInHand(hand);
        if (ConstructAssemblyStand.checkAndConvert(self, stack, player, hand)) {
            callback.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

