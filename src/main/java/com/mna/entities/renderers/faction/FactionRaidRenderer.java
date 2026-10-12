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
 *  org.joml.Quaternionf
 */
package com.mna.entities.renderers.faction;

import com.mna.entities.faction.util.FactionRaid;
import com.mna.factions.Factions;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
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
import org.joml.Quaternionf;

public class FactionRaidRenderer
extends EntityRenderer<FactionRaid> {
    private static final int[] white = new int[]{255, 255, 255};
    private static final int[] green = new int[]{0, 204, 0};
    private static final int[] blue = new int[]{102, 0, 255};

    public FactionRaidRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(FactionRaid entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.getFaction() == Factions.COUNCIL) {
            this.RenderPortal(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        } else if (entityIn.getFaction() == Factions.FEY) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.0f, 1.0f, 0.0f);
            WorldRenderUtils.renderRadiant(entityIn, matrixStackIn, bufferIn, green, blue, 128, 0.5f);
            matrixStackIn.popPose();
        }
        super.render((Entity)entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void RenderPortal(FactionRaid entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        float spawnPct = (float)Math.min(entityIn.tickCount, 20) / 20.0f;
        float scaleFactor = 4.0f * spawnPct;
        float portalSpinDegrees = entityIn.tickCount * 3 % 360;
        float verticalOffset = 1.0f;
        int[] colors = white;
        VertexConsumer vertexBuilder = bufferIn.getBuffer(MARenderTypes.RAID_PORTAL_RENDER);
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
        int frame = entityIn.tickCount % 25;
        float minU = 0.0f;
        float maxU = 1.0f;
        float minV = 0.04f * (float)frame;
        float maxV = minV + 0.04f;
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 0.0f, 0.0f, minU, maxV, nrmH, nrmV, colors);
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 1.0f, 0.0f, maxU, maxV, nrmH, nrmV, colors);
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 1.0f, 1.0f, maxU, minV, nrmH, nrmV, colors);
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 0.0f, 1.0f, minU, minV, nrmH, nrmV, colors);
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
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 0.0f, 0.0f, minU, maxV, nrmH, nrmV, colors);
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 1.0f, 0.0f, maxU, maxV, nrmH, nrmV, colors);
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 1.0f, 1.0f, maxU, minV, nrmH, nrmV, colors);
        FactionRaidRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 0.0f, 1.0f, minU, minV, nrmH, nrmV, colors);
        matrixStackIn.popPose();
    }

    private static void addVertex(VertexConsumer vertexBuilder_, com.mojang.blaze3d.vertex.PoseStack.Pose pose, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] rgb) {
        vertexBuilder_.addVertex(pose, x - 0.5f, y - 0.25f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], 230).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nrmH, nrmV, nrmH);
    }

    public ResourceLocation getTextureLocation(FactionRaid entity) {
        return MARenderTypes.PORTAL_TEXTURE_RAID;
    }
}

