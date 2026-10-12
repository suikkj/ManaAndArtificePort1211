/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.level.Level
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.entities.renderers.living;

import com.mna.entities.models.WanderingWizardModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mna.entities.utility.WanderingWizard;
import com.mna.tools.render.MARenderTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

public class WanderingWizardRenderer
extends MAGeckoRenderer<WanderingWizard> {
    private final Minecraft mc = Minecraft.getInstance();

    public WanderingWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new WanderingWizardModel());
    }

    public void renderRecursively(PoseStack poseStack, WanderingWizard animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (bone.getName().equals("scroll") && !((WanderingWizard)this.animatable).renderScroll()) {
        }
        if (bone.getName().equals("staff_planted") && ((WanderingWizard)this.animatable).renderPlantedStaff()) {
            poseStack.pushPose();
            RenderUtil.translateMatrixToBone((PoseStack)poseStack, (GeoBone)bone);
            RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoBone)bone);
            RenderUtil.rotateMatrixAroundBone((PoseStack)poseStack, (GeoBone)bone);
            RenderUtil.scaleMatrixForBone((PoseStack)poseStack, (GeoBone)bone);
            poseStack.translate(0.0f, 0.6f, 0.0f);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            Minecraft.getInstance().getItemRenderer().renderStatic(animatable.staff(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, packedOverlay, poseStack, bufferSource, (Level)this.mc.level, 0);
            poseStack.popPose();
            bufferSource.getBuffer(renderType);
        }
        if (bone.getName().equals("staff") && !((WanderingWizard)this.animatable).renderPlantedStaff()) {
            poseStack.pushPose();
            RenderUtil.translateMatrixToBone((PoseStack)poseStack, (GeoBone)bone);
            RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoBone)bone);
            RenderUtil.rotateMatrixAroundBone((PoseStack)poseStack, (GeoBone)bone);
            RenderUtil.scaleMatrixForBone((PoseStack)poseStack, (GeoBone)bone);
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0f));
            poseStack.scale(0.5f, 0.5f, 0.5f);
            Minecraft.getInstance().getItemRenderer().renderStatic(animatable.staff(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, packedOverlay, poseStack, bufferSource, (Level)this.mc.level, 0);
            poseStack.popPose();
            bufferSource.getBuffer(renderType);
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public void render(WanderingWizard entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.tickCount < 60) {
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)MARenderTypes.PORTAL_TEXTURE);
            Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
            Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
            float spawnPct = entityIn.tickCount < 20 ? (float)entityIn.tickCount / 20.0f : (entityIn.tickCount < 40 ? 1.0f : (float)(20 - (entityIn.tickCount - 40)) / 20.0f);
            float scaleFactor = 4.0f * spawnPct;
            float portalSpinDegrees = entityIn.tickCount * 3 % 360;
            float verticalOffset = 1.5f;
            int[] colors = new int[]{68, 13, 112};
            VertexConsumer vertexBuilder = bufferIn.getBuffer(MARenderTypes.PORTAL_RENDER);
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
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, colors);
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, colors);
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, colors);
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, colors);
            matrixStackIn.popPose();
            colors = new int[]{82, 167, 217};
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
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, colors);
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, colors);
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, colors);
            WanderingWizardRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, colors);
            matrixStackIn.popPose();
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)this.getTextureLocation((Entity)entityIn));
        }
        if (entityIn.tickCount >= 20) {
            super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        }
    }

    private static void addVertex(VertexConsumer vertexBuilder_, Matrix4f renderMatrix, Matrix3f normalMatrix, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] rgb) {
        vertexBuilder_.addVertex(renderMatrix, x - 0.5f, y - 0.25f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], 230).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalMatrix, nrmH, nrmV, nrmH);
    }
}

