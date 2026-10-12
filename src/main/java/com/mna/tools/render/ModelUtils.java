/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ItemBlockRenderTypes
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.client.model.data.ModelData
 */
package com.mna.tools.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ModelUtils {
    private static final RandomSource rnd = RandomSource.create((long)1234L);

    public static void renderModel(MultiBufferSource buffer, Level world, BlockPos pos, BlockState state, ResourceLocation modelLoc, PoseStack stack, int combinedLight, int combinedOverlay) {
        VertexConsumer vertexBuilder = buffer.getBuffer(ItemBlockRenderTypes.getRenderType((BlockState)state, (boolean)false));
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(modelLoc));
        ModelData worldModelData = world.getModelDataManager().getAt(pos);
        ModelData data = model.getModelData((BlockAndTintGetter)world, pos, state, worldModelData == null ? ModelData.EMPTY : worldModelData);
        for (BakedQuad quad : model.getQuads(state, null, rnd, data, null)) {
            vertexBuilder.putBulkData(stack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, combinedLight, combinedOverlay, true);
        }
    }

    public static void renderModel(MultiBufferSource buffer, Level world, BlockPos pos, BlockState state, ResourceLocation modelLoc, PoseStack stack, int combinedLight, int combinedOverlay, RenderType renderType) {
        VertexConsumer vertexBuilder = buffer.getBuffer(renderType);
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(modelLoc));
        ModelData worldModelData = world.getModelDataManager().getAt(pos);
        ModelData data = model.getModelData((BlockAndTintGetter)world, pos, state, worldModelData == null ? ModelData.EMPTY : worldModelData);
        for (BakedQuad quad : model.getQuads(state, null, rnd, data, null)) {
            vertexBuilder.putBulkData(stack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, combinedLight, combinedOverlay, true);
        }
    }

    public static void renderModel(MultiBufferSource buffer, Level world, BlockPos pos, BlockState state, ResourceLocation modelLoc, PoseStack stack, int combinedLight, int combinedOverlay, float[] rgba) {
        VertexConsumer vertexBuilder = buffer.getBuffer(ItemBlockRenderTypes.getRenderType((BlockState)state, (boolean)false));
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(modelLoc));
        ModelData worldModelData = world.getModelDataManager().getAt(pos);
        ModelData data = model.getModelData((BlockAndTintGetter)world, pos, state, worldModelData == null ? ModelData.EMPTY : worldModelData);
        for (BakedQuad quad : model.getQuads(state, null, rnd, data, null)) {
            vertexBuilder.putBulkData(stack.last(), quad, rgba[0], rgba[1], rgba[2], rgba[3], combinedLight, combinedOverlay, true);
        }
    }

    public static void renderEntityModel(VertexConsumer vertexBuilder, Level world, ResourceLocation modelLoc, PoseStack stack, int combinedLight, int combinedOverlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(modelLoc));
        ModelData data = ModelData.EMPTY;
        for (BakedQuad quad : model.getQuads(null, null, rnd, data, null)) {
            vertexBuilder.putBulkData(stack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, combinedLight, combinedOverlay, true);
        }
    }

    public static void renderEntityModel(VertexConsumer vertexBuilder, Level world, ResourceLocation modelLoc, PoseStack stack, int combinedLight, int combinedOverlay, float[] rgb, float a) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(modelLoc));
        ModelData data = ModelData.EMPTY;
        for (BakedQuad quad : model.getQuads(null, null, rnd, data, null)) {
            vertexBuilder.putBulkData(stack.last(), quad, rgb[0], rgb[1], rgb[2], a, combinedLight, combinedOverlay, false);
        }
    }

    public static void renderModel(VertexConsumer vertexBuilder, Level world, BlockPos pos, BlockState state, ResourceLocation modelLoc, PoseStack stack, int combinedLight, int combinedOverlay) {
        ModelUtils.renderModel(vertexBuilder, world, pos, state, modelLoc, stack, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, combinedLight, combinedOverlay);
    }

    public static void renderModel(VertexConsumer vertexBuilder, Level world, BlockPos pos, BlockState state, ResourceLocation modelLoc, PoseStack stack, float[] argb, int combinedLight, int combinedOverlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(modelLoc));
        for (BakedQuad quad : model.getQuads(state, null, rnd, ModelData.EMPTY, null)) {
            vertexBuilder.putBulkData(stack.last(), quad, argb[1], argb[2], argb[3], argb[0], combinedLight, combinedOverlay, true);
        }
    }
}

