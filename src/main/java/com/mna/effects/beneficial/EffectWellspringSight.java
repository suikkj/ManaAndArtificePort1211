 
package com.mna.effects.beneficial;

import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.network.ServerMessageDispatcher;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectWellspringSight
extends MobEffect
implements INoCreeperLingering {
    public EffectWellspringSight() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer && entity.level().getGameTime() % 100L == 0L) {
            ServerMessageDispatcher.sendWellspringSyncMessage((ServerLevel)entity.level(), (ServerPlayer)entity);

        return true;
    }
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

