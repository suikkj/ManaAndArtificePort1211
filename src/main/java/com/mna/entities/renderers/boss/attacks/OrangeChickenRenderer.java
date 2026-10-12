/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.model.ChickenModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities.renderers.boss.attacks;

import com.mna.entities.boss.attacks.OrangeChickenProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class OrangeChickenRenderer
extends EntityRenderer<OrangeChickenProjectile> {
    private static final ResourceLocation CHICKEN_LOCATION = ResourceLocation.parse("textures/entity/chicken.png");
    private ChickenModel<OrangeChickenProjectile> model;

    public OrangeChickenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new ChickenModel(ctx.bakeLayer(ModelLayers.CHICKEN));
    }

    public ResourceLocation getTextureLocation(OrangeChickenProjectile pEntity) {
        return CHICKEN_LOCATION;
    }

    public void render(OrangeChickenProjectile pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        pMatrixStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        pMatrixStack.translate(0.0, -1.25, 0.0);
        float legAngle = (float)Math.sin((float)pEntity.tickCount + pPartialTicks) / 3.0f;
        float wingAngle = (float)(Math.sin((float)pEntity.tickCount + pPartialTicks) + 1.5707963705062866);
        this.model.setupAnim(pEntity, 0.5f, legAngle, wingAngle, 0.0f, 0.0f);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.getTextureLocation(pEntity)));
        this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, -1);
        pMatrixStack.popPose();
    }
}

