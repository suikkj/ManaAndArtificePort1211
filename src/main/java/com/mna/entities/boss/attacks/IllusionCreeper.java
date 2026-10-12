/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.monster.Creeper
 *  net.minecraft.world.level.Level
 */
package com.mna.entities.boss.attacks;

import com.mna.entities.EntityInit;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_client.SpawnParticleEffectMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class IllusionCreeper
extends Creeper {
    public IllusionCreeper(EntityType<IllusionCreeper> type, Level world) {
        super(type, world);
    }

    public IllusionCreeper(Level world) {
        this((EntityType<IllusionCreeper>)((EntityType)EntityInit.ILLUSION_CREEPER.get()), world);
    }

    public void explodeCreeper() {
        if (!this.level().isClientSide()) {
            ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.level().dimension(), 32.0f, this.getX(), this.getEyeY(), this.getZ(), SpawnParticleEffectMessage.ParticleTypes.MANAWEAVE_CRAFT_COMPLETE);
            this.discard();
        }
    }

    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        return pEffectInstance != null && pEffectInstance.getEffect().isBeneficial();
    }
}

