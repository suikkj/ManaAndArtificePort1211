 
package com.mna.effects;

import com.mna.ManaAndArtifice;
import com.mna.effects.interfaces.IDoubleTapEndEarly;
import com.mna.entities.summon.Simulacrum;
import com.mna.tools.SummonUtils;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class EffectHelper {
    public static void removeDoubleTapEvents(Player player) {
        EffectHelper.removeDoubleTapEarlyEffects((LivingEntity)player);
        SummonUtils.iterateTrackedEntities((LivingEntity)player, "mna:simulacrum_ids", e -> {
            Entity entity = player.level().getEntity(e.intValue());
            if (entity instanceof Simulacrum) {
                Simulacrum simulacrum = (Simulacrum)entity;
                EffectHelper.removeDoubleTapEarlyEffects((LivingEntity)simulacrum);
            }
        });
    }

    private static void removeDoubleTapEarlyEffects(LivingEntity living) {
        List toRemove = living.getActiveEffects().stream().filter(e -> {
            IDoubleTapEndEarly iee;
            MobEffect patt1067$temp = e.getEffect();
            return patt1067$temp instanceof IDoubleTapEndEarly && (iee = (IDoubleTapEndEarly)patt1067$temp).canEndEarly(living, (MobEffectInstance)e);
        }).collect(Collectors.toList());
        for (MobEffectInstance inst : toRemove) {
            if (inst == null) continue;
            ((IDoubleTapEndEarly)inst.getEffect()).onRemoved(living, inst);
            if (living.level().isClientSide()) continue;
            try {
                living.removeEffect(inst.getEffect());
            }
            catch (Exception e2) {
                ManaAndArtifice.LOGGER.warn("Attempted to remove potion effect " + BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect()).toString() + " early, but it failed.  Recovering and moving on.  The error was: " + e2.getMessage());
            }
        }
    }
}


