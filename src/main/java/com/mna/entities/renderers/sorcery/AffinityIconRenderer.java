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
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 */
package com.mna.entities.renderers.sorcery;

import com.mna.api.affinity.Affinity;
import com.mna.api.tools.RLoc;
import com.mna.entities.sorcery.AffinityIcon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class AffinityIconRenderer
extends EntityRenderer<AffinityIcon> {
    private static HashMap<Affinity, ResourceLocation> affinityIconMap = new HashMap();

    public AffinityIconRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(AffinityIcon entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        float scaleFactor = 0.5f;
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityTranslucent((ResourceLocation)affinityIconMap.get(entityIn.getAffinity())));
        matrixStackIn.pushPose();
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStackIn.mulPose(portalRotation);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0f));
        matrixStackIn.translate(0.0f, -0.25f, 0.0f);
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        float nrmV = 1.0f;
        float nrmH = 1.0f;
        AffinityIconRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV);
        AffinityIconRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV);
        AffinityIconRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV);
        AffinityIconRenderer.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV);
        matrixStackIn.popPose();
    }

    private static void addVertex(VertexConsumer vertexBuilder_, Matrix4f renderMatrix, Matrix3f normalMatrix, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV) {
        vertexBuilder_.addVertex(renderMatrix, x - 0.5f, y - 0.25f, 0.0f).setColor(255, 255, 255, 230).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalMatrix, nrmH, nrmV, nrmH);
    }

    public ResourceLocation getTextureLocation(AffinityIcon entity) {
        return null;
    }

    static {
        affinityIconMap.put(Affinity.UNKNOWN, RLoc.create("textures/gui/affinity/arcane.png"));
        affinityIconMap.put(Affinity.ARCANE, RLoc.create("textures/gui/affinity/arcane.png"));
        affinityIconMap.put(Affinity.EARTH, RLoc.create("textures/gui/affinity/earth.png"));
        affinityIconMap.put(Affinity.ENDER, RLoc.create("textures/gui/affinity/ender.png"));
        affinityIconMap.put(Affinity.FIRE, RLoc.create("textures/gui/affinity/fire.png"));
        affinityIconMap.put(Affinity.LIGHTNING, RLoc.create("textures/gui/affinity/lightning.png"));
        affinityIconMap.put(Affinity.HELLFIRE, RLoc.create("textures/gui/affinity/fire.png"));
        affinityIconMap.put(Affinity.WATER, RLoc.create("textures/gui/affinity/water.png"));
        affinityIconMap.put(Affinity.ICE, RLoc.create("textures/gui/affinity/ice.png"));
        affinityIconMap.put(Affinity.WIND, RLoc.create("textures/gui/affinity/air.png"));
        affinityIconMap.put(Affinity.BLOOD, RLoc.create("textures/gui/affinity/blood.png"));
    }
}

