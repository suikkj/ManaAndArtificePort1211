/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.RunicLightTile;
import com.mna.gui.GuiTextures;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class RunicLightRenderer
implements BlockEntityRenderer<RunicLightTile> {
    public static final ResourceLocation cube = RLoc.create("block/runic_light_cube");
    private final int[] lightRuneColor = new int[]{49, 235, 234};
    private final int[] torchColor = new int[]{252, 111, 3};

    public RunicLightRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(RunicLightTile pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        boolean powered = (Boolean)pBlockEntity.getBlockState().getValue((Property)BlockStateProperties.LIT);
        Direction facing = (Direction)pBlockEntity.getBlockState().getValue((Property)HorizontalDirectionalBlock.FACING);
        long time = pBlockEntity.getLevel().getGameTime();
        pPoseStack.pushPose();
        switch (facing) {
            case EAST: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                pPoseStack.translate(-0.5, 1.0, 0.6);
                break;
            }
            case WEST: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
                pPoseStack.translate(0.5, 1.0, -0.4);
                break;
            }
            case NORTH: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                pPoseStack.translate(-0.5, 1.0, -0.4);
                break;
            }
            default: {
                pPoseStack.translate(0.5, 1.0, 0.6);
            }
        }
        WorldRenderUtils.RenderRuneWord(pPoseStack, pBuffer, GuiTextures.Blocks.RUNES, pPackedLight, pBlockEntity.isTorch() ? this.torchColor : this.lightRuneColor, pBlockEntity.getRuneword(), powered ? 0.0f : 1.0f, 1);
        pPoseStack.popPose();
        if (!pBlockEntity.isTorch()) {
            pPoseStack.pushPose();
            if (!powered) {
                double sinA = Math.sin(((float)time + pPartialTick) / 10.0f);
                double cosZ = Math.cos(((float)time + pPartialTick) / 10.0f);
                double sinB = Math.sin(((float)time + pPartialTick) / 20.0f);
                double offsetY = 0.075 * sinA;
                double offsetX = 0.035 * sinB;
                double offsetZ = 0.035 * cosZ;
                pPoseStack.translate(0.5 + offsetX, 1.575 + offsetY, 0.5 + offsetZ);
                int rotation = (int)(time % 360L);
                pPoseStack.mulPose(Axis.XP.rotationDegrees((float)rotation));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)rotation));
            } else {
                pPoseStack.translate(0.5, 1.4625, 0.5);
            }
            ModelUtils.renderModel(pBuffer, pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), cube, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.popPose();
        }
    }
}

