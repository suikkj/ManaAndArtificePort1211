/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package com.mna.entities.renderers.ritual;

import com.mna.entities.rituals.TimeChangeBall;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TimeChangeBallRenderer
extends EntityRenderer<TimeChangeBall> {
    private static final float triangle_height = (float)(Math.sqrt(3.0) / 2.0);
    private static final int[] sun_orange_start = new int[]{255, 255, 0};
    private static final int[] sun_orange_end = new int[]{255, 120, 0};
    private static final int[] moon_blue_start = new int[]{0, 255, 255};
    private static final int[] moon_blue_end = new int[]{0, 0, 255};
    private static final int[] moon_purp_start = new int[]{255, 0, 255};
    private static final int[] moon_purp_end = new int[]{138, 0, 255};

    public TimeChangeBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(TimeChangeBall entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        TimeChangeBallRenderer.renderRadiant(entityIn, matrixStackIn, bufferIn, 0.0f);
    }

    private static void renderRadiant(TimeChangeBall entityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, float offsetFactor) {
        float rotationByAge = ((float)entityIn.getAge() + offsetFactor) / 220.0f;
        Random random = new Random(1234L);
        VertexConsumer lightingBuilder = bufferIn.getBuffer(RenderType.lightning());
        matrixStackIn.pushPose();
        Vec3 motion = entityIn.getDeltaMovement();
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-entityIn.getXRot()));
        matrixStackIn.scale((float)motion.x, (float)motion.y, (float)motion.z);
        for (int i = 0; i < 40; ++i) {
            int[] colorEnd;
            int[] colorStart;
            float f = random.nextFloat();
            if (entityIn.getTimeChangeType() == TimeChangeBall.TIME_CHANGE_DAY.byteValue()) {
                colorStart = sun_orange_start;
                colorEnd = sun_orange_end;
            } else if ((double)f > 0.5) {
                colorStart = moon_blue_start;
                colorEnd = moon_blue_end;
            } else {
                colorStart = moon_purp_start;
                colorEnd = moon_purp_end;
            }
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f + 90.0f * rotationByAge));
            float hOffset = random.nextFloat() * 0.25f;
            float vOffset = random.nextFloat() * 0.25f;
            Matrix4f currentMatrix = matrixStackIn.last().pose();
            int alpha = 255;
            TimeChangeBallRenderer.addStartVertices(lightingBuilder, currentMatrix, alpha, colorStart);
            TimeChangeBallRenderer.addVertexNegativeOffset(lightingBuilder, currentMatrix, hOffset, vOffset, colorEnd);
            TimeChangeBallRenderer.addVertexPositiveOffset(lightingBuilder, currentMatrix, hOffset, vOffset, colorEnd);
            TimeChangeBallRenderer.addStartVertices(lightingBuilder, currentMatrix, alpha, colorStart);
            TimeChangeBallRenderer.addVertexPositiveOffset(lightingBuilder, currentMatrix, hOffset, vOffset, colorEnd);
            TimeChangeBallRenderer.addVertexNoOffset(lightingBuilder, currentMatrix, hOffset, vOffset, colorEnd);
            TimeChangeBallRenderer.addStartVertices(lightingBuilder, currentMatrix, alpha, colorStart);
            TimeChangeBallRenderer.addVertexNoOffset(lightingBuilder, currentMatrix, hOffset, vOffset, colorEnd);
            TimeChangeBallRenderer.addVertexNegativeOffset(lightingBuilder, currentMatrix, hOffset, vOffset, colorEnd);
        }
        matrixStackIn.popPose();
    }

    private static void addStartVertices(VertexConsumer vertexBuilder, Matrix4f renderMatrix, int alpha, int[] colors) {
        vertexBuilder.addVertex(renderMatrix, 0.0f, 0.0f, 0.0f).setColor(255, 255, 255, alpha);
        vertexBuilder.addVertex(renderMatrix, 0.0f, 0.0f, 0.0f).setColor(colors[0], colors[1], colors[2], alpha);
    }

    private static void addVertexNegativeOffset(VertexConsumer vertexBuilder, Matrix4f renderMatrix, float vOffset, float hOffset, int[] colors) {
        vertexBuilder.addVertex(renderMatrix, -triangle_height * hOffset, vOffset, -0.5f * hOffset).setColor(colors[0], colors[1], colors[2], 0);
    }

    private static void addVertexPositiveOffset(VertexConsumer vertexBuilder, Matrix4f renderMatrix, float vOffset, float hOffset, int[] colors) {
        vertexBuilder.addVertex(renderMatrix, triangle_height * hOffset, vOffset, -0.5f * hOffset).setColor(colors[0], colors[1], colors[2], 0);
    }

    private static void addVertexNoOffset(VertexConsumer vertexBuilder, Matrix4f renderMatrix, float vOffset, float hOffset, int[] colors) {
        vertexBuilder.addVertex(renderMatrix, 0.0f, vOffset, hOffset).setColor(colors[0], colors[1], colors[2], 0);
    }

    public ResourceLocation getTextureLocation(TimeChangeBall entity) {
        return null;
    }
}

