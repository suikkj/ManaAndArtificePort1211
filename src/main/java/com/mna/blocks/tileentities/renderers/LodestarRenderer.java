/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.tools.RLoc;
import com.mna.blocks.artifice.LodestarBlock;
import com.mna.blocks.tileentities.LodestarTile;
import com.mna.blocks.tileentities.models.ModelLodestar;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class LodestarRenderer
extends GeoBlockRenderer<LodestarTile> {
    public static final ResourceLocation delegation_station = RLoc.create("block/delegation_station");
    public static final ResourceLocation base = RLoc.create("block/lodestar_base");
    public static final ResourceLocation small_crystal = RLoc.create("block/lodestar_crystalsmall");
    public static final ResourceLocation big_crystal = RLoc.create("block/lodestar_crystalbig");
    public static final ResourceLocation small_gear = RLoc.create("block/lodestar_gearsmall");
    public static final ResourceLocation stabilizer = RLoc.create("block/lodestar_stabilizer");
    protected Minecraft mc = Minecraft.getInstance();
    protected boolean low_tier;

    public LodestarRenderer(BlockEntityRendererProvider.Context context) {
        super((GeoModel)new ModelLodestar());
    }

    public RenderType getRenderType(LodestarTile animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entitySmoothCutout((ResourceLocation)texture);
    }

    public void renderRecursively(PoseStack poseStack, LodestarTile animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (this.mc == null) {
            return;
        }
        poseStack.pushPose();
        RenderUtil.translateMatrixToBone((PoseStack)poseStack, (GeoBone)bone);
        RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoBone)bone);
        RenderUtil.rotateMatrixAroundBone((PoseStack)poseStack, (GeoBone)bone);
        RenderUtil.scaleMatrixForBone((PoseStack)poseStack, (GeoBone)bone);
        BlockState state = animatable.getBlockState();
        BlockPos pos = animatable.getBlockPos();
        if (!bone.isHidden()) {
            boolean low_tier = (Boolean)state.getValue((Property)LodestarBlock.LOW_TIER);
            poseStack.pushPose();
            switch (bone.getName()) {
                case "ROOT": {
                    if (!low_tier) {
                        ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, base, poseStack, packedLight, packedOverlay);
                        break;
                    }
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, delegation_station, poseStack, packedLight, packedOverlay);
                    break;
                }
                case "CRYSTAL_BIG": {
                    if (low_tier) break;
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, big_crystal, poseStack, packedLight, packedOverlay);
                    break;
                }
                case "STABILIZER": {
                    if (low_tier) break;
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, stabilizer, poseStack, packedLight, packedOverlay);
                    break;
                }
                case "GEAR_ROTATION": {
                    if (low_tier) break;
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, small_gear, poseStack, packedLight, packedOverlay);
                    break;
                }
                case "CRYSTAL_SMALL": {
                    if (low_tier) break;
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, small_crystal, poseStack, packedLight, packedOverlay);
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

