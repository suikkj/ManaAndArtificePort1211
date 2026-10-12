/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.items.renderers.obj_gecko;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.items.artifice.EnderDisk;
import com.mna.items.renderers.models.ModelEnderDisc;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class EnderDiscRenderer
extends GeoItemRenderer<EnderDisk> {
    public static final ResourceLocation disk_model = RLoc.create("item/special/ender_disc");
    protected MultiBufferSource rtb;
    protected Minecraft mc = Minecraft.getInstance();

    public EnderDiscRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super((GeoModel)new ModelEnderDisc());
    }

    public RenderType getRenderType(EnderDisk animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.solid();
    }

    public void renderRecursively(PoseStack poseStack, EnderDisk animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (this.mc == null) {
            return;
        }
        poseStack.pushPose();
        RenderUtil.translateMatrixToBone((PoseStack)poseStack, (GeoBone)bone);
        RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoBone)bone);
        RenderUtil.rotateMatrixAroundBone((PoseStack)poseStack, (GeoBone)bone);
        RenderUtil.scaleMatrixForBone((PoseStack)poseStack, (GeoBone)bone);
        if (!bone.isHidden()) {
            poseStack.pushPose();
            switch (bone.getName()) {
                case "ROOT": {
                    ModelUtils.renderEntityModel(buffer, ManaAndArtifice.instance.proxy.getClientWorld(), disk_model, poseStack, packedLight, OverlayTexture.NO_OVERLAY);
                }
            }
            poseStack.popPose();
            for (GeoBone childBone : bone.getChildBones()) {
                this.renderRecursively(poseStack, animatable, childBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
            }
        }
        poseStack.popPose();
    }
}

