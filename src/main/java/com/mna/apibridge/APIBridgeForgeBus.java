/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.event.server.ServerAboutToStartEvent
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.apibridge;

import com.mna.Registries;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class APIBridgeForgeBus {
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registries.Shape.getValues().stream().forEach(s -> s.lookupAttributeConfig());
        Registries.SpellEffect.getValues().stream().forEach(c -> c.lookupAttributeConfig());
    }
}


