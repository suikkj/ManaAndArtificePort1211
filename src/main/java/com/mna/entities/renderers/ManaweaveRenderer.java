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
package com.mna.entities.renderers;

import com.mna.api.recipes.IManaweavePattern;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
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

public class ManaweaveRenderer
extends EntityRenderer<Manaweave> {
    private static final ResourceLocation SPARKLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("mna", "textures/particle/sparkle.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucentCull((ResourceLocation)SPARKLE_TEXTURE);

    public ManaweaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(Manaweave entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        ArrayList<IManaweavePattern> patterns = entityIn.getPatterns();
        if (patterns == null) {
            return;
        }
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0, 0.5, 0.0);
        for (IManaweavePattern p : patterns) {
            this.RenderPattern(p, matrixStackIn, bufferIn, entityIn.tickCount, partialTicks);
        }
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void RenderPattern(IManaweavePattern pattern, PoseStack matrixStackIn, MultiBufferSource bufferIn, int life, float partialTicks) {
        byte[][] points = pattern.get();
        float offsetX = (float)points.length / 2.0f;
        float offsetY = (float)points[0].length / 2.0f;
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(cameraRotation.x(), cameraRotation.y(), cameraRotation.z(), cameraRotation.w());
        float baseScale = 0.125f;
        float fadeInTicks = 5.0f;
        Vector3 colorA = new Vector3(153.0, 0.0, 247.0);
        Vector3 colorB = new Vector3(245.0, 66.0, 144.0);
        int a = (int)(MathUtils.clamp01((float)life / fadeInTicks) * 200.0f);
        float baseAlphaMod = ((float)life + partialTicks) / 9.0f;
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(portalRotation);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0f));
        matrixStackIn.translate(-2.6 * (double)baseScale, -2.6 * (double)baseScale, 0.0);
        matrixStackIn.scale(baseScale, baseScale, baseScale);
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RENDER_TYPE);
        for (int i = 0; i < points.length; ++i) {
            for (int j = 0; j < points[i].length; ++j) {
                if (points[i][j] != 1) continue;
                int index = i * j + j;
                float originX = offsetX - (float)j * 0.5f;
                float originY = offsetY - (float)i * 0.5f;
                double pct = Math.abs(Math.sin(baseAlphaMod + (float)index));
                int loopA = MathUtils.clamp((int)((double)a - 75.0 * pct), 0, 255);
                pct = 1.0 - Math.cos(pct * pct);
                Vector3 color = Vector3.lerp(colorA, colorB, (float)pct);
                int r = (int)color.x;
                int g = (int)color.y;
                int b = (int)color.z;
                ManaweaveRenderer.addVertex(vertexBuilder, matrixstack$entry, 0.0f + originX, 0.0f + originY, 0.0f, 1.0f, r, g, b, loopA);
                ManaweaveRenderer.addVertex(vertexBuilder, matrixstack$entry, 1.0f + originX, 0.0f + originY, 1.0f, 1.0f, r, g, b, loopA);
                ManaweaveRenderer.addVertex(vertexBuilder, matrixstack$entry, 1.0f + originX, 1.0f + originY, 1.0f, 0.0f, r, g, b, loopA);
                ManaweaveRenderer.addVertex(vertexBuilder, matrixstack$entry, 0.0f + originX, 1.0f + originY, 0.0f, 0.0f, r, g, b, loopA);
            }
        }
        matrixStackIn.popPose();
    }

    private static void addVertex(VertexConsumer vertexBuilder_, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float x, float y, float u, float v, int r, int g, int b, int a) {
        vertexBuilder_.addVertex(pose, x - 0.5f, y - 0.25f, 0.0f).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    public ResourceLocation getTextureLocation(Manaweave entity) {
        return SPARKLE_TEXTURE;
    }
}

