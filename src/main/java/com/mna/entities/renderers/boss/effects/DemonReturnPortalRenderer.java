/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.mna.entities.renderers.boss.effects;

import com.mna.entities.boss.effects.DemonReturnPortal;
import com.mna.tools.render.MARenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class DemonReturnPortalRenderer
extends EntityRenderer<DemonReturnPortal> {
    private static final int[] color = new int[]{217, 72, 0};

    public DemonReturnPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(DemonReturnPortal entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        float spawnPct = (float)Math.min(entityIn.getAge(), 20) / 20.0f;
        if (entityIn.getAge() > 160) {
            spawnPct = (float)Math.max(180 - entityIn.getAge(), 0) / 20.0f;
        }
        float scaleFactor = 8.0f * spawnPct;
        float portalSpinDegrees = entityIn.getAge() * 3 % 360;
        VertexConsumer vertexBuilder = bufferIn.getBuffer(MARenderTypes.PORTAL_RENDER);
        matrixStackIn.pushPose();
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(portalSpinDegrees));
        matrixStackIn.translate(0.0f, -0.25f, 0.0f);
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        float nrmV = (float)Math.cos((double)portalSpinDegrees * Math.PI / 180.0);
        float nrmH = (float)Math.cos((double)(portalSpinDegrees - 90.0f) * Math.PI / 180.0);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, color);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, color);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, color);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, color);
        matrixStackIn.popPose();
        matrixStackIn.pushPose();
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-portalSpinDegrees));
        matrixStackIn.translate(0.0f, -0.25f, 0.001f);
        matrixstack$entry = matrixStackIn.last();
        renderMatrix = matrixstack$entry.pose();
        normalMatrix = matrixstack$entry.normal();
        nrmH = (float)Math.cos((double)(portalSpinDegrees + 90.0f) * Math.PI / 180.0);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, color);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, color);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, color);
        DemonReturnPortalRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, color);
        matrixStackIn.popPose();
        super.render((Entity)entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private static void addVertex(VertexConsumer vertexBuilder_, Matrix4f renderMatrix, Matrix3f normalMatrix, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] rgb) {
        vertexBuilder_.addVertex(renderMatrix, x - 0.5f, y - 0.25f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], 230).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalMatrix, nrmH, nrmV, nrmH);
    }

    public ResourceLocation getTextureLocation(DemonReturnPortal entity) {
        return MARenderTypes.PORTAL_TEXTURE;
    }
}

