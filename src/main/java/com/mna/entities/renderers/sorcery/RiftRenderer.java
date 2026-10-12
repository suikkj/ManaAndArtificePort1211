/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 */
package com.mna.entities.renderers.sorcery;

import com.mna.entities.sorcery.Rift;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class RiftRenderer
extends EntityRenderer<Rift> {
    private static final ResourceLocation PORTAL_TEXTURE = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/vortex.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent((ResourceLocation)PORTAL_TEXTURE);

    public RiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(Rift entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        int[] nArray;
        int[] nArray2;
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        float spawnPct = entityIn.getAge() < 20 ? (float)Math.min(entityIn.getAge(), 20) / 20.0f : (entityIn.getAge() > 180 ? 1.0f - (float)Math.min(entityIn.getAge() - 180, 20) / 20.0f : 1.0f);
        float scaleFactor = 1.0f * spawnPct;
        float portalSpinDegrees = entityIn.getAge() * 3 % 360;
        float verticalOffset = 1.0f;
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RENDER_TYPE);
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0f, verticalOffset, 0.0f);
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStackIn.mulPose(portalRotation);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0f));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(portalSpinDegrees));
        matrixStackIn.translate(0.0f, -0.25f, 0.0f);
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        float nrmV = (float)Math.cos((double)portalSpinDegrees * Math.PI / 180.0);
        float nrmH = (float)Math.cos((double)(portalSpinDegrees - 90.0f) * Math.PI / 180.0);
        if (entityIn.getIsEnderChest()) {
            int[] nArray3 = new int[4];
            nArray3[0] = 3;
            nArray3[1] = 38;
            nArray3[2] = 32;
            nArray2 = nArray3;
            nArray3[3] = 230;
        } else {
            int[] nArray4 = new int[4];
            nArray4[0] = 105;
            nArray4[1] = 24;
            nArray4[2] = 152;
            nArray2 = nArray4;
            nArray4[3] = 230;
        }
        int[] color = nArray2;
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, color);
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, color);
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, color);
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, color);
        matrixStackIn.popPose();
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0f, verticalOffset, 0.0f);
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStackIn.mulPose(portalRotation);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0f));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-portalSpinDegrees));
        matrixStackIn.translate(0.0f, -0.25f, 0.001f);
        matrixstack$entry = matrixStackIn.last();
        renderMatrix = matrixstack$entry.pose();
        normalMatrix = matrixstack$entry.normal();
        nrmH = (float)Math.cos((double)(portalSpinDegrees + 90.0f) * Math.PI / 180.0);
        if (entityIn.getIsEnderChest()) {
            int[] nArray5 = new int[4];
            nArray5[0] = 52;
            nArray5[1] = 153;
            nArray5[2] = 136;
            nArray = nArray5;
            nArray5[3] = 230;
        } else {
            int[] nArray6 = new int[4];
            nArray6[0] = 186;
            nArray6[1] = 30;
            nArray6[2] = 180;
            nArray = nArray6;
            nArray6[3] = 230;
        }
        color = nArray;
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, color);
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, color);
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, color);
        RiftRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, color);
        matrixStackIn.popPose();
        super.render((Entity)entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private static void addVertex(VertexConsumer vertexBuilder_, Matrix4f renderMatrix, Matrix3f normalMatrix, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] color) {
        if (color.length != 4) {
        }
        vertexBuilder_.addVertex(renderMatrix, x - 0.5f, y - 0.25f, 0.0f).setColor(color[0], color[1], color[2], color[3]).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalMatrix, nrmH, nrmV, nrmH);
    }

    public ResourceLocation getTextureLocation(Rift entity) {
        return PORTAL_TEXTURE;
    }
}

