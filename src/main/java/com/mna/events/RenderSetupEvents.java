/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$RegisterLayerDefinitions
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.events;

import com.mna.blocks.tileentities.models.FixedBookModel;
import com.mna.entities.models.AncientWizardModel;
import com.mna.entities.models.BubbleBoatModel;
import com.mna.entities.models.DemonStoneModel;
import com.mna.entities.models.MagicBroomModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class RenderSetupEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MagicBroomModel.LAYER_LOCATION, MagicBroomModel::createBodyLayer);
        event.registerLayerDefinition(DemonStoneModel.LAYER_LOCATION, DemonStoneModel::createBodyLayer);
        event.registerLayerDefinition(AncientWizardModel.LAYER_LOCATION, AncientWizardModel::createBodyLayer);
        event.registerLayerDefinition(BubbleBoatModel.LAYER_LOCATION, BubbleBoatModel::createBodyLayer);
        event.registerLayerDefinition(FixedBookModel.LAYER_LOCATION, FixedBookModel::createBodyLayer);
    }
}


