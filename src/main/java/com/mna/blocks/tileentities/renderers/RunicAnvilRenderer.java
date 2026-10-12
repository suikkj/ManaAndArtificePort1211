/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.blocks.tileentities.models.RunicAnvilModel;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class RunicAnvilRenderer
extends GeoBlockRenderer<RunicAnvilTile> {
    public static final ResourceLocation ring_small = RLoc.create("block/runic_anvil/ring_small");
    public static final ResourceLocation ring_large = RLoc.create("block/runic_anvil/ring_large");
    private final ItemRenderer itemRenderer;
    protected Minecraft mc = Minecraft.getInstance();

    public RunicAnvilRenderer(BlockEntityRendererProvider.Context context) {
        super((GeoModel)new RunicAnvilModel());
        this.itemRenderer = this.mc.getItemRenderer();
    }

    public void actuallyRender(PoseStack poseStack, RunicAnvilTile animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        this.renderItems(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    public void renderRecursively(PoseStack stack, RunicAnvilTile animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (this.mc == null) {
            return;
        }
        stack.pushPose();
        RenderUtil.translateMatrixToBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.translateToPivotPoint((PoseStack)stack, (GeoBone)bone);
        RenderUtil.rotateMatrixAroundBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.scaleMatrixForBone((PoseStack)stack, (GeoBone)bone);
        BlockState state = animatable.getBlockState();
        BlockPos pos = animatable.getBlockPos();
        if (!bone.isHidden()) {
            stack.pushPose();
            switch (bone.getName()) {
                case "CIRCLE_BASE": {
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, ring_large, stack, packedLight, packedOverlay);
                    break;
                }
                case "CIRCLE_FRONT": 
                case "CIRCLE_REAR": {
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, ring_small, stack, packedLight, packedOverlay);
                }
            }
            stack.popPose();
            for (GeoBone childBone : bone.getChildBones()) {
                this.renderRecursively(stack, animatable, childBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
            }
        }
        stack.popPose();
    }

    private void renderItems(RunicAnvilTile anvil, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack[] stacks = anvil.getDisplayedItems();
        float offset = 0.03f;
        float craftPct = (float)anvil.craftProgress / (float)anvil.getMaxCraftProgress();
        matrixStackIn.pushPose();
        if (!stacks[0].isEmpty()) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 1.025, 0.5);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            this.itemRenderer.renderStatic(stacks[0], ItemDisplayContext.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
            matrixStackIn.popPose();
        }
        if (!stacks[1].isEmpty()) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 1.025 + (double)(offset - offset * craftPct), 0.5);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            this.itemRenderer.renderStatic(stacks[1], ItemDisplayContext.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
    }
}

