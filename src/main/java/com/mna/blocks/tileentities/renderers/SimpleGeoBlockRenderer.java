/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.blocks.FacingBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SimpleGeoBlockRenderer<V extends BlockEntity & software.bernie.geckolib.animatable.GeoAnimatable>
extends GeoBlockRenderer<V> {
    public SimpleGeoBlockRenderer(BlockEntityRendererProvider.Context rendererProvider, GeoModel<V> model) {
        super(model);
    }

    public void render(BlockEntity tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (tile.getBlockState().getBlock() instanceof FacingBlock) {
            BlockState state = tile.getBlockState();
            Direction dir = (Direction)state.getValue((Property)FacingBlock.FACING);
            int face = (Integer)state.getValue((Property)FacingBlock.SURFACE_TYPE);
            block0 : switch (face) {
                case 2: {
                    poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                    poseStack.translate(0.0f, -1.0f, -1.0f);
                    break;
                }
                case 3: {
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
                    switch (dir) {
                        case EAST: {
                            poseStack.mulPose(Axis.ZP.rotationDegrees(270.0f));
                            poseStack.translate(-1.0f, 0.0f, -1.0f);
                            break block0;
                        }
                        case WEST: {
                            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
                            poseStack.translate(0.0f, -1.0f, -1.0f);
                            break block0;
                        }
                        case NORTH: {
                            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
                            poseStack.translate(-1.0f, -1.0f, -1.0f);
                            break block0;
                        }
                    }
                    poseStack.translate(0.0f, 0.0f, -1.0f);
                    break;
                }
            }
        }
        super.render((V)tile, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);
    }
}

