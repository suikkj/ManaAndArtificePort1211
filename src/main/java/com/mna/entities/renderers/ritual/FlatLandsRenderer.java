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
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 */
package com.mna.entities.renderers.ritual;

import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.models.FixedBookModel;
import com.mna.entities.rituals.FlatLands;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FlatLandsRenderer
extends EntityRenderer<FlatLands> {
    private final Vector3 TARGET_POSITION = new Vector3(0.5, 1.25, 0.5);
    private final ResourceLocation bookTexture = RLoc.create("textures/block/artifice/sentry/sentry_earth.png");
    private final FixedBookModel modelBook;

    public FlatLandsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.modelBook = new FixedBookModel(context.bakeLayer(FixedBookModel.LAYER_LOCATION));
    }

    public ResourceLocation getTextureLocation(FlatLands entity) {
        return null;
    }

    public void render(FlatLands pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        float f1;
        float f = (float)pEntity.ticks + pPartialTick;
        float t = MathUtils.clamp01(((float)pEntity.ticksSinceTargetChange + pPartialTick) / 10.0f);
        pPoseStack.pushPose();
        pPoseStack.translate(0.0, (double)(0.1f + Mth.sin((float)(f * 0.1f)) * 0.01f), 0.0);
        for (f1 = pEntity.nextPageAngle - pEntity.pageAngle; f1 >= (float)Math.PI; f1 -= (float)Math.PI * 2) {
        }
        while (f1 < (float)(-Math.PI)) {
            f1 += (float)Math.PI * 2;
        }
        float f2 = (float)Math.toDegrees(pEntity.pageAngle + f1 * pPartialTick);
        float zpRotation = MathUtils.lerpf(90.0f, 0.0f, t);
        float ypRotation = MathUtils.lerpf(90.0f, -f2, t);
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(zpRotation));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(ypRotation));
        float f3 = Mth.lerp((float)pPartialTick, (float)pEntity.oFlip, (float)pEntity.flip);
        float f4 = Mth.frac((float)(f3 + 0.25f)) * 1.6f - 0.3f;
        float f5 = Mth.frac((float)(f3 + 0.75f)) * 1.6f - 0.3f;
        float f6 = Mth.lerp((float)pPartialTick, (float)pEntity.pageTurningSpeed, (float)pEntity.nextPageTurningSpeed);
        this.modelBook.setupAnim(f, Mth.clamp((float)f4, (float)0.0f, (float)1.0f), Mth.clamp((float)f5, (float)0.0f, (float)1.0f), f6);
        VertexConsumer ivertexbuilder = pBuffer.getBuffer(RenderType.entitySolid((ResourceLocation)this.bookTexture));
        this.modelBook.renderToBuffer(pPoseStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        pPoseStack.popPose();
    }
}

