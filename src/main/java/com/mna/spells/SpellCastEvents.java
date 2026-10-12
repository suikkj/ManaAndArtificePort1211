/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.spells;

import com.mna.api.events.SpellCastEvent;
import com.mna.entities.summon.Simulacrum;
import com.mna.tools.SummonUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class SpellCastEvents {
    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        LivingEntity livingEntity = event.getSource().getCaster();
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            SummonUtils.iterateTrackedEntities((LivingEntity)player, "mna:simulacrum_ids", e -> {
                Entity entity = player.level().getEntity(e.intValue());
                if (entity instanceof Simulacrum) {
                    Simulacrum simulacrum = (Simulacrum)entity;
                    simulacrum.castSpell(event.getStack(), event.getSpell(), event.getSource().getHand());
                }
            });
        }
    }
}


