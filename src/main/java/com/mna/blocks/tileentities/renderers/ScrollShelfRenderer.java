/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.blocks.decoration.ScrollshelfBlock;
import com.mna.blocks.tileentities.ScrollShelfTile;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ScrollShelfRenderer
implements BlockEntityRenderer<ScrollShelfTile> {
    public static final ResourceLocation scroll = ResourceLocation.fromNamespaceAndPath("mna", "block/special/bookshelf_scroll");
    public static final ResourceLocation bottle = ResourceLocation.fromNamespaceAndPath("mna", "block/special/bookshelf_bottle");

    public ScrollShelfRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(ScrollShelfTile pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        pPoseStack.pushPose();
        switch ((Direction)state.getValue((Property)ScrollshelfBlock.FACING)) {
            case EAST: {
                pPoseStack.translate(1.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
                break;
            }
            case SOUTH: {
                pPoseStack.translate(0.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case WEST: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            default: {
                pPoseStack.translate(1.0f, 0.0f, 0.0f);
            }
        }
        pPoseStack.translate(-0.2325, 1.65, 0.7);
        float[] greenTint = new float[]{0.1f, 0.25f, 0.1f, 1.0f};
        int idx = 0;
        for (int row = 0; row < 7; ++row) {
            double yOffset = -0.1325 * (double)row;
            boolean shortRow = row % 2 != 0;
            double xOffset = shortRow ? -0.1325 : 0.0;
            int cols = shortRow ? 2 : 3;
            for (int col = 0; col < cols; ++col) {
                double colOffset = -0.265 * (double)col;
                if (pBlockEntity.isScrollDisplayable(idx)) {
                    pPoseStack.pushPose();
                    pPoseStack.translate(xOffset + colOffset, yOffset, 0.0);
                    if (pBlockEntity.displayAsBottle(idx)) {
                        pPoseStack.mulPose(Axis.ZP.rotationDegrees(45.0f));
                        ModelUtils.renderModel(pBufferSource, world, pos, state, bottle, pPoseStack, pPackedLight, pPackedOverlay, greenTint);
                    } else {
                        pPoseStack.translate(0.0, -0.03, 0.0);
                        pPoseStack.mulPose(Axis.ZP.rotationDegrees(45.0f));
                        ModelUtils.renderModel(pBufferSource, world, pos, state, scroll, pPoseStack, pPackedLight, pPackedOverlay);
                    }
                    pPoseStack.popPose();
                }
                ++idx;
            }
        }
        pPoseStack.popPose();
    }
}

