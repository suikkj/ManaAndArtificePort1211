/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.entity.SpectralArrowRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.mna.entities.renderers.faction.attacks;

import com.mna.entities.projectile.WitchhunterTrickshot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.SpectralArrowRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class WitchhunterTrickshotRenderer
extends EntityRenderer<WitchhunterTrickshot> {
    private final Minecraft mc;
    private final ItemRenderer itemRenderer;
    private final ItemStack potionItem = new ItemStack((ItemLike)Items.POTION);

    public WitchhunterTrickshotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.mc = Minecraft.getInstance();
        this.itemRenderer = this.mc.getItemRenderer();
    }

    public void render(WitchhunterTrickshot entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        Vec3 offset = entityIn.getPotionPosition(partialTicks).subtract(entityIn.position());
        matrixStackIn.pushPose();
        matrixStackIn.translate(offset.x, offset.y, offset.z);
        float ticks = ((float)entityIn.tickCount + partialTicks) * 10.0f;
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(ticks));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(ticks));
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(ticks));
        this.itemRenderer.renderStatic(this.potionItem, ItemDisplayContext.FIXED, packedLightIn, 0, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
        matrixStackIn.popPose();
        if (entityIn.renderArrow()) {
            offset = entityIn.getArrowPosition(partialTicks).subtract(entityIn.position());
            matrixStackIn.pushPose();
            matrixStackIn.translate(offset.x, offset.y, offset.z);
            this.renderArrow(matrixStackIn, partialTicks, entityIn, bufferIn, packedLightIn);
            matrixStackIn.popPose();
        }
    }

    public ResourceLocation getTextureLocation(WitchhunterTrickshot entity) {
        return SpectralArrowRenderer.SPECTRAL_ARROW_LOCATION;
    }

    private void renderArrow(PoseStack matrixStackIn, float partialTicks, WitchhunterTrickshot entityIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp((float)partialTicks, (float)entityIn.yRotO, (float)entityIn.getYRot()) - 90.0f));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp((float)partialTicks, (float)entityIn.xRotO, (float)entityIn.getXRot())));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(45.0f));
        matrixStackIn.scale(0.05625f, 0.05625f, 0.05625f);
        matrixStackIn.translate(-4.0, 0.0, 0.0);
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutout((ResourceLocation)this.getTextureLocation(entityIn)));
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, -2, 0.0f, 0.15625f, -1, 0, 0, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, 2, 0.15625f, 0.15625f, -1, 0, 0, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, 2, 0.15625f, 0.3125f, -1, 0, 0, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, -2, 0.0f, 0.3125f, -1, 0, 0, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, -2, 0.0f, 0.15625f, 1, 0, 0, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, 2, 2, 0.15625f, 0.15625f, 1, 0, 0, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, 2, 0.15625f, 0.3125f, 1, 0, 0, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -7, -2, -2, 0.0f, 0.3125f, 1, 0, 0, packedLightIn);
        for (int j = 0; j < 4; ++j) {
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -8, -2, 0, 0.0f, 0.0f, 0, 1, 0, packedLightIn);
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, 8, -2, 0, 0.5f, 0.0f, 0, 1, 0, packedLightIn);
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, 8, 2, 0, 0.5f, 0.15625f, 0, 1, 0, packedLightIn);
            this.drawVertex(matrix4f, matrix3f, ivertexbuilder, -8, 2, 0, 0.0f, 0.15625f, 0, 1, 0, packedLightIn);
        }
        matrixStackIn.popPose();
    }

    public void drawVertex(Matrix4f matrix, Matrix3f normals, VertexConsumer vertexBuilder, int offsetX, int offsetY, int offsetZ, float textureX, float textureY, int p_229039_9_, int p_229039_10_, int p_229039_11_, int packedLightIn) {
        com.mojang.blaze3d.vertex.PoseStack.Pose __pose = new com.mojang.blaze3d.vertex.PoseStack.Pose(matrix, normals);
        vertexBuilder.addVertex(__pose, (float)offsetX, (float)offsetY, (float)offsetZ).setColor(255, 255, 255, 255).setUv(textureX, textureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLightIn).setNormal(__pose, (float)p_229039_9_, (float)p_229039_11_, (float)p_229039_10_);
    }
}

