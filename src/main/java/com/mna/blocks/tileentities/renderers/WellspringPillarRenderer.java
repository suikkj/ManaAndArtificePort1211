/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.WellspringPillarTile;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WellspringPillarRenderer
implements BlockEntityRenderer<WellspringPillarTile> {
    private final ResourceLocation cap = RLoc.create("block/eldrin/wellspring_pillar_cap");
    private static final Random RANDOM = new Random(31100L);

    public WellspringPillarRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(WellspringPillarTile tileEntityIn, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Level world = tileEntityIn.getLevel();
        BlockPos pos = tileEntityIn.getBlockPos();
        BlockState state = tileEntityIn.getBlockState();
        matrixStack.pushPose();
        float colorMod = 0.15f;
        this.renderModelWithRandomColor(matrixStack, bufferIn.getBuffer(RenderType.endPortal()), world, pos, state, colorMod, combinedLightIn, combinedOverlayIn);
        matrixStack.popPose();
    }

    private void renderModelWithRandomColor(PoseStack stack, VertexConsumer builder, Level world, BlockPos pos, BlockState state, float colorMod, int light, int overlay) {
        float r = (RANDOM.nextFloat() * 0.5f + 0.1f) * colorMod;
        float g = (RANDOM.nextFloat() * 0.5f + 0.4f) * colorMod;
        float b = (RANDOM.nextFloat() * 0.5f + 0.5f) * colorMod;
        ModelUtils.renderModel(builder, world, pos, state, this.cap, stack, new float[]{1.0f, r, g, b}, light, overlay);
    }

    public boolean shouldRenderOffScreen(WellspringPillarTile pBlockEntity) {
        return true;
    }
}

