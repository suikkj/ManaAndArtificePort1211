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
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.mna.entities.renderers.item;

import com.mna.entities.constructs.BubbleBoat;
import com.mna.entities.models.BubbleBoatModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BubbleBoatRenderer
extends EntityRenderer<BubbleBoat> {
    private static final ResourceLocation BUBBLE_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/bubble_boat.png");
    private static final ResourceLocation BRIMSTONE_BOAT_TEXTURE = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/brimstone_boat.png");
    protected final BubbleBoatModel<BubbleBoat> modelBoat = new BubbleBoatModel(Minecraft.getInstance().getEntityModels().bakeLayer(BubbleBoatModel.LAYER_LOCATION));

    public BubbleBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    public void render(BubbleBoat entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        float f2;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0, -2.0, 0.0);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
        float f = (float)entityIn.getHurtTime() - partialTicks;
        float f1 = entityIn.getDamage() - partialTicks;
        if (f1 < 0.0f) {
            f1 = 0.0f;
        }
        if (f > 0.0f) {
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(Mth.sin((float)f) * f * f1 / 10.0f * (float)entityIn.getHurtDir()));
        }
        if (!Mth.equal((float)(f2 = entityIn.getBubbleAngle(partialTicks)), (float)0.0f)) {
            Quaternionf quat = new Quaternionf();
            quat = quat.rotateAxis(entityIn.getBubbleAngle(partialTicks), (Vector3fc)new Vector3f(1.0f, 0.0f, 1.0f));
            matrixStackIn.mulPose(quat);
        }
        matrixStackIn.scale(3.0f, 3.0f, 3.0f);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0f));
        this.modelBoat.setupAnim(entityIn, partialTicks, 0.0f, -0.1f, 0.0f, 0.0f);
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(this.modelBoat.renderType(this.getTextureLocation(entityIn)));
        this.modelBoat.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStackIn.popPose();
        super.render((Entity)entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    public ResourceLocation getTextureLocation(BubbleBoat entity) {
        return entity.isBrimstone() ? BRIMSTONE_BOAT_TEXTURE : BUBBLE_BOAT_TEXTURE;
    }
}

