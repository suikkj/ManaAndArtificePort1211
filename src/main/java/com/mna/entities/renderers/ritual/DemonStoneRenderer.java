/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities.renderers.ritual;

import com.mna.entities.models.DemonStoneModel;
import com.mna.entities.rituals.DemonStone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class DemonStoneRenderer
extends EntityRenderer<DemonStone> {
    private static final ResourceLocation DEMON_STONE_TEXTURE = ResourceLocation.parse("textures/block/nether_bricks.png");
    protected final DemonStoneModel<DemonStone> modelStone = new DemonStoneModel(Minecraft.getInstance().getEntityModels().bakeLayer(DemonStoneModel.LAYER_LOCATION));

    public DemonStoneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(DemonStone entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        int age = entityIn.getAge();
        matrixStackIn.translate(0.0f, -1.0f, 0.0f);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0f));
        float angleDegrees = 0.0f;
        int numPillars = 3;
        for (int i = 0; i < numPillars; ++i) {
            matrixStackIn.pushPose();
            float riseAmount = Math.min((float)(age - i * 10) / 100.0f * 2.5f, 2.5f);
            matrixStackIn.translate(0.0f, -riseAmount, 0.0f);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(angleDegrees));
            matrixStackIn.translate(-2.0f, 0.0f, 0.0f);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0f));
            this.modelStone.setupAnim(entityIn, partialTicks, 0.0f, -0.1f, 0.0f, 0.0f);
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(this.modelStone.renderType(this.getTextureLocation(entityIn)));
            this.modelStone.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
            matrixStackIn.popPose();
            angleDegrees += 360.0f / (float)numPillars;
        }
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    public ResourceLocation getTextureLocation(DemonStone entity) {
        return DEMON_STONE_TEXTURE;
    }
}

