/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.blocks.tileentities.renderers.wizard_lab;

import com.mna.blocks.tileentities.models.WizardLabModel;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class WizardLabRenderer<T extends WizardLabTile>
extends GeoBlockRenderer<T> {
    public WizardLabRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn, WizardLabModel<T> model) {
        super(model);
    }

    public RenderType getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entitySmoothCutout((ResourceLocation)texture);
    }

    public void renderRecursively(PoseStack stack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        stack.pushPose();
        RenderUtil.translateMatrixToBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.translateToPivotPoint((PoseStack)stack, (GeoBone)bone);
        RenderUtil.rotateMatrixAroundBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.scaleMatrixForBone((PoseStack)stack, (GeoBone)bone);
        if (!bone.isHidden()) {
            stack.pushPose();
            stack.translate(-0.5, 0.0, -0.5);
            ((WizardLabModel)this.getGeoModel()).renderBoneAdditions((WizardLabTile)animatable, bone.getName(), stack, bufferSource, renderType, packedLight, packedOverlay);
            stack.popPose();
            bufferSource.getBuffer(renderType);
            RenderUtil.translateAwayFromPivotPoint((PoseStack)stack, (GeoBone)bone);
            this.renderCubesOfBone(stack, bone, buffer, packedLight, packedOverlay, colour);
            this.renderChildBones(stack, animatable, bone, renderType, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
        } else {
            RenderUtil.translateAwayFromPivotPoint((PoseStack)stack, (GeoBone)bone);
        }
        stack.popPose();
    }
}

