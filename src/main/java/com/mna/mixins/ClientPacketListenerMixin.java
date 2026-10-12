/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket
 *  net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.mna.mixins;

import com.mna.effects.EffectInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;

    @Inject(at={@At(value="RETURN")}, method={"handleRemoveMobEffect"})
    public void mna$handleRemoveMobEffect(ClientboundRemoveMobEffectPacket packet, CallbackInfo cir) {
        Entity entity = packet.getEntity((Level)this.level);
        if (entity instanceof LivingEntity && packet.getEffect().value() == EffectInit.REDUCE.get()) {
            entity.refreshDimensions();
        }
    }

    @Inject(at={@At(value="RETURN")}, method={"handleUpdateMobEffect"})
    public void mna$handleUpdateMobEffect(ClientboundUpdateMobEffectPacket packet, CallbackInfo cir) {
        MobEffect mobeffect;
        Entity entity = this.level.getEntity(packet.getEntityId());
        if (entity != null && entity instanceof LivingEntity && (mobeffect = packet.getEffect().value()) != null && entity instanceof LivingEntity && mobeffect == EffectInit.REDUCE.get()) {
            entity.refreshDimensions();
        }
    }
}

