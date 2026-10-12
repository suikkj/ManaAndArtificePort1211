/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 */
package com.mna.entities.renderers.boss;

import com.mna.entities.boss.PumpkinKing;
import com.mna.entities.models.boss.PumpkinKingModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class PumpkinKingRenderer
extends MAGeckoRenderer<PumpkinKing> {
    public PumpkinKingRenderer(EntityRendererProvider.Context context) {
        super(context, new PumpkinKingModel());
    }

    public void preRender(PoseStack poseStack, PumpkinKing animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}

