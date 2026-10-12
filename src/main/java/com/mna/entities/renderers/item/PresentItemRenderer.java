/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemEntityRenderer
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemDisplayContext
 */
package com.mna.entities.renderers.item;

import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;

public class PresentItemRenderer
extends ItemEntityRenderer {
    public PresentItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        int[] innerColor = new int[]{156, 56, 207};
        int[] outerColor = new int[]{42, 90, 173};
        int preRadiantSize = com.mna.tools.MNAPoseStackAccess.size(matrixStackIn);
        matrixStackIn.pushPose();
        BakedModel ibakedmodel = Minecraft.getInstance().getItemRenderer().getModel(entityIn.getItem(), entityIn.level(), (LivingEntity)null, 0);
        float f1 = Mth.sin((float)(((float)entityIn.getAge() + partialTicks) / 10.0f + entityIn.bobOffs)) * 0.1f + 0.1f;
        float f2 = true ? ibakedmodel.getTransforms().getTransform((ItemDisplayContext)ItemDisplayContext.GROUND).scale.y() : 0.0f;
        matrixStackIn.translate(0.0, (double)(f1 + 0.5f * f2), 0.0);
        WorldRenderUtils.renderRadiant((Entity)entityIn, matrixStackIn, bufferIn, innerColor, outerColor, 128, 0.1f);
        while (com.mna.tools.MNAPoseStackAccess.size(matrixStackIn) > preRadiantSize) {
            matrixStackIn.popPose();
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }
}

