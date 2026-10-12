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
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.ManaweaveCacheTile;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ManaweaveCacheRenderer
implements BlockEntityRenderer<ManaweaveCacheTile> {
    public static final ResourceLocation interior = RLoc.create("block/special/manaweave_cache_inner");
    public static final ResourceLocation exterior = RLoc.create("block/special/manaweave_cache_outer");

    public ManaweaveCacheRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(ManaweaveCacheTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Level world = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        BlockState state = tile.getBlockState();
        for (int i = 0; i < 4; ++i) {
            this.renderOuterSegment(tile, partialTicks, i, bufferIn, world, pos, state, matrixStack, combinedLightIn, combinedOverlayIn);
        }
        if (tile.openPct() > 0.0f) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.0, 0.5);
            ModelUtils.renderModel(bufferIn, world, pos, state, interior, matrixStack, combinedLightIn, combinedOverlayIn);
            if (!tile.isBuff()) {
                matrixStack.translate(0.0, 1.5, 0.0);
                WorldRenderUtils.renderRadiant((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks, matrixStack, bufferIn, new int[]{128, 128, 128}, new int[]{128, 128, 128}, 67, 1.0f, false);
            }
            matrixStack.popPose();
        }
    }

    private void renderOuterSegment(ManaweaveCacheTile tile, float partialTicks, int quadrant, MultiBufferSource bufferIn, Level world, BlockPos pos, BlockState state, PoseStack matrixStack, int combinedLight, int combinedOverlay) {
        matrixStack.pushPose();
        float openPct = 0.0f;
        openPct = tile.isOpen() ? (tile.openPct() == 0.0f ? 0.0f : (tile.openPct() < 1.0f ? tile.openPct() + 0.1f * partialTicks : 1.0f)) : (tile.openPct() == 0.0f ? 0.0f : (tile.openPct() < 1.0f ? tile.openPct() - 0.1f * partialTicks : 1.0f));
        switch (quadrant) {
            case 0: {
                matrixStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                matrixStack.translate(-0.5, 0.0, 0.5);
                break;
            }
            case 1: {
                matrixStack.translate(0.5, 0.0, 0.5);
                break;
            }
            case 2: {
                matrixStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
                matrixStack.translate(0.5, 0.0, -0.5);
                break;
            }
            case 3: {
                matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                matrixStack.translate(-0.5, 0.0, -0.5);
            }
        }
        matrixStack.translate(0.25f * openPct, 0.0f, -0.25f * openPct);
        ModelUtils.renderModel(bufferIn, world, pos, state, exterior, matrixStack, combinedLight, combinedOverlay);
        ManaweavingPattern[] patterns = tile.getRequiredPatterns();
        if (patterns.length > 0) {
            matrixStack.translate(0.0f, 1.57f, 0.0f);
            matrixStack.translate(0.27f, 0.0f, -0.27f);
            matrixStack.scale(0.2f, 0.2f, 0.2f);
            for (int i = 0; i < patterns.length; ++i) {
                if (patterns[i] != null) {
                    if (tile.isPatternAdded(i)) {
                        WorldRenderUtils.renderManaweavePatternNoTransparent(patterns[i], Axis.YP.rotationDegrees(-45.0f), matrixStack, bufferIn, false);
                    } else {
                        WorldRenderUtils.renderManaweavePatternNoTransparent(patterns[i], Axis.YP.rotationDegrees(-45.0f), matrixStack, bufferIn, false, new int[]{255, 0, 0, 255});
                    }
                }
                matrixStack.translate(0.0f, -1.25f, 0.0f);
            }
        }
        matrixStack.popPose();
    }
}

