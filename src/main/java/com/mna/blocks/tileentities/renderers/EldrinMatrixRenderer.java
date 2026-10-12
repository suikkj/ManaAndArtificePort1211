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
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.EldrinMatrixTile;
import com.mna.gui.GuiTextures;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class EldrinMatrixRenderer
implements BlockEntityRenderer<EldrinMatrixTile> {
    public static final ResourceLocation crystal = RLoc.create("block/eldrin/matrix_crystal");
    public static final ResourceLocation ring = RLoc.create("block/eldrin/matrix_ring");
    static int[][] runes = new int[][]{{15, 8, 7, 1, 11, 12}, {0, 12, 13, 11, 6, 2}, {10, 0, 13, 4, 7, 8}, {8, 2, 14, 7, 5, 4}, {5, 12, 15, 0, 9, 10}, {6, 14, 10, 9, 11, 8}, {8, 10, 3, 11, 4, 9}, {1, 5, 11, 2, 10, 3}};
    Minecraft mc = Minecraft.getInstance();

    public EldrinMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(EldrinMatrixTile pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.render_2(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void render_2(EldrinMatrixTile pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        float scale = 0.5f;
        pPoseStack.translate(0.5f, 0.5f, 0.5f);
        pPoseStack.scale(scale, scale, scale);
        float degrees = ((float)ManaAndArtifice.instance.proxy.getGameTicks() + pPartialTick) / 5.0f;
        pPoseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        ModelUtils.renderModel(pBuffer, (Level)this.mc.level, pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), ring, pPoseStack, pPackedLight, pPackedOverlay);
        double offset = Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + pPartialTick) / 100.0f) / 12.0;
        pPoseStack.translate(0.0, -0.3125 + offset, 0.0);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-degrees * 6.0f));
        ModelUtils.renderModel(pBuffer, (Level)this.mc.level, pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), crystal, pPoseStack, pPackedLight, pPackedOverlay);
    }

    public void render_1(EldrinMatrixTile pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        VertexConsumer vertexBuilder = pBuffer.getBuffer(RenderType.entityTranslucent((ResourceLocation)GuiTextures.Blocks.RUNES));
        pPoseStack.translate(0.5, 0.5, 0.5);
        float scale = 0.03125f;
        pPoseStack.scale(scale, scale, scale);
        Axis[] axes = new Axis[]{Axis.XN, Axis.YP, Axis.ZN, Axis.XP, Axis.YN, Axis.ZP};
        float tickWithPartial = (float)ManaAndArtifice.instance.proxy.getGameTicks() + pPartialTick;
        float mainIntervalRate = 80.0f;
        float mainIntervalPause = 20.0f;
        float mainIntervalClamped = tickWithPartial % mainIntervalRate;
        if (mainIntervalClamped > mainIntervalPause) {
            float mainIntervalLength = mainIntervalRate - mainIntervalPause;
            float mainIntervalTicks = mainIntervalClamped - mainIntervalPause;
            float mainSpinDegrees = 360.0f * (mainIntervalTicks / mainIntervalLength);
            int mainIntervalAxisIndex = (int)(tickWithPartial / mainIntervalRate) % axes.length;
            Axis mainIntervalAxis = axes[mainIntervalAxisIndex];
            pPoseStack.mulPose(mainIntervalAxis.rotationDegrees(mainSpinDegrees));
        }
        float expansionTicks = tickWithPartial % 440.0f;
        float expansion = 0.0f;
        if (expansionTicks > 200.0f) {
            expansion = expansionTicks < 420.0f ? Math.min((expansionTicks - 200.0f) / 20.0f, 1.0f) : (440.0f - expansionTicks) / 20.0f;
        }
        float step = 1.5f + expansion;
        pPoseStack.translate(-(step / 2.0f), -(step / 2.0f), -(step / 2.0f));
        float intervalRate = 20.0f;
        float interval = tickWithPartial / intervalRate;
        int axisIndex = (int)interval % axes.length;
        int cubeIndex = (int)interval % 8;
        Axis spinAxis = axes[axisIndex];
        float individualSpinCount = tickWithPartial % intervalRate / intervalRate;
        float degrees = 360.0f * individualSpinCount;
        int cubeIdx = 0;
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                for (int k = 0; k < 2; ++k) {
                    pPoseStack.pushPose();
                    pPoseStack.translate((float)i * step, (float)j * step, (float)k * step);
                    int idx = cubeIdx;
                    if (cubeIndex == idx) {
                        pPoseStack.mulPose(spinAxis.rotationDegrees(degrees));
                    }
                    this.renderCube(pPoseStack, vertexBuilder, pBlockEntity.getGeneratingAffinity().getColor(), runes[cubeIdx]);
                    ++cubeIdx;
                    pPoseStack.popPose();
                }
            }
        }
    }

    private void renderCube(PoseStack pPoseStack, VertexConsumer consumer, int[] rgb, int[] runes) {
        if (runes.length != 6) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees((float)(90 * i)));
            pPoseStack.translate(0.0f, -0.25f, 0.5f);
            WorldRenderUtils.renderRune(pPoseStack, consumer, 0xF000F0, runes[i], 1.0f, rgb);
            pPoseStack.popPose();
        }
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        pPoseStack.translate(0.0f, -0.25f, -0.5f);
        WorldRenderUtils.renderRune(pPoseStack, consumer, 0xF000F0, runes[4], 1.0f, rgb);
        pPoseStack.popPose();
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        pPoseStack.translate(0.0f, -0.25f, 0.5f);
        WorldRenderUtils.renderRune(pPoseStack, consumer, 0xF000F0, runes[5], 1.0f, rgb);
        pPoseStack.popPose();
    }
}

