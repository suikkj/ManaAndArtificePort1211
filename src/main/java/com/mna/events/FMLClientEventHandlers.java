/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$AddLayers
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.events;

import com.mna.entities.renderers.player.HandParticleLayer;
import com.mna.entities.renderers.player.SpectralElytraLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class FMLClientEventHandlers {
    @OnlyIn(value=Dist.CLIENT)
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.AddLayers event) {
        LivingEntityRenderer playerRenderer = event.getSkin("default");
        LivingEntityRenderer slimRenderer = event.getSkin("slim");
        if (playerRenderer != null) {
            playerRenderer.addLayer(new SpectralElytraLayer(playerRenderer, event.getEntityModels()));
            playerRenderer.addLayer(new HandParticleLayer(playerRenderer));
        }
        if (slimRenderer != null) {
            slimRenderer.addLayer(new SpectralElytraLayer(slimRenderer, event.getEntityModels()));
            slimRenderer.addLayer(new HandParticleLayer(slimRenderer));
        }
    }
}


