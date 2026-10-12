/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 */
package com.mna.blocks.tileentities.renderers.wizard_lab;

import com.mna.blocks.artifice.FumeBellowsBlock;
import com.mna.blocks.tileentities.renderers.models.FumeBellowsModel;
import com.mna.blocks.tileentities.wizard_lab.FumeBellowsTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FumeBellowsRenderer
extends GeoBlockRenderer<FumeBellowsTile> {
    public FumeBellowsRenderer(BlockEntityRendererProvider.Context context) {
        super((GeoModel)new FumeBellowsModel());
    }

    public void actuallyRender(PoseStack poseStack, FumeBellowsTile animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        BlockState state = animatable.getBlockState();
        if (((Boolean)state.getValue((Property)FumeBellowsBlock.FUME_LEFT)).booleanValue()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
            poseStack.translate(0.0f, 0.0f, -1.0f);
        } else if (((Boolean)state.getValue((Property)FumeBellowsBlock.FUME_RIGHT)).booleanValue()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            poseStack.translate(-1.0f, 0.0f, 0.0f);
        }
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}

