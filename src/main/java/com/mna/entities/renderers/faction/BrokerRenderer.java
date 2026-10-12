/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package com.mna.entities.renderers.faction;

import com.mna.entities.faction.Broker;
import com.mna.entities.models.faction.BrokerModel;
import com.mna.events.ClientEventHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BrokerRenderer
extends GeoEntityRenderer<Broker> {
    public BrokerRenderer(EntityRendererProvider.Context context) {
        super(context, (GeoModel)new BrokerModel());
    }

    public RenderType getRenderType(Broker animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entitySmoothCutout((ResourceLocation)texture);
    }

    public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        ClientEventHandler.setFogAmount(Math.min(ClientEventHandler.getFogAmount() + 0.025f, 0.85f));
        ClientEventHandler.fogColor[0] = 0.85f;
        ClientEventHandler.fogColor[1] = 0.85f;
        ClientEventHandler.fogColor[2] = 0.85f;
        return super.firePreRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
    }
}

