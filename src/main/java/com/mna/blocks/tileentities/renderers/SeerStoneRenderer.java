/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ItemBlockRenderTypes
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.SeerStoneTile;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SeerStoneRenderer
implements BlockEntityRenderer<SeerStoneTile> {
    public static final ResourceLocation crystal = RLoc.create("block/seer_stone/crystal");
    public static final ResourceLocation crystal_band = RLoc.create("block/seer_stone/crystal_band");

    public SeerStoneRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(SeerStoneTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Level world = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        BlockState state = tile.getBlockState();
        long ticks = ManaAndArtifice.instance.proxy.getGameTicks();
        double yRot = 0.0;
        Entity target = tile.getTarget();
        if (target != null) {
            Vec3 myPos = Vec3.atCenterOf((Vec3i)pos);
            Vec3 targetPos = target.getEyePosition();
            double xDelta = targetPos.x - myPos.x;
            double zDelta = targetPos.z - myPos.z;
            yRot = Mth.wrapDegrees((float)((float)(Mth.atan2((double)zDelta, (double)xDelta) * 57.2957763671875) - 90.0f));
        }
        float angle = MathUtils.clamp((float)tile.getCrystalAngle() + partialTicks, 0.0f, 90.0f);
        float redPct = 1.0f - angle / 90.0f;
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.75 + Math.sin(((float)ticks + partialTicks) / 40.0f) * 0.1, 0.5);
        matrixStack.mulPose(Axis.XP.rotationDegrees(angle));
        if (target != null) {
            matrixStack.mulPose(Axis.ZP.rotationDegrees((float)yRot));
        }
        matrixStack.mulPose(Axis.YP.rotationDegrees((float)(ticks % 360L)));
        VertexConsumer vertexBuilder = bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType((BlockState)state, (boolean)false));
        ModelUtils.renderModel(vertexBuilder, world, pos, state, crystal, matrixStack, new float[]{1.0f, 1.0f, redPct, redPct}, combinedLightIn, combinedOverlayIn);
        ModelUtils.renderModel(bufferIn, world, pos, state, crystal_band, matrixStack, combinedLightIn, combinedOverlayIn);
        matrixStack.popPose();
        if (target != null) {
            float scale = 0.2f;
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.75 + Math.sin(((float)ticks + partialTicks) / 40.0f) * 0.1, 0.5);
            matrixStack.scale(scale, scale, scale);
            Minecraft.getInstance().getEntityRenderDispatcher().render(target, 0.0, 0.25 / (double)scale, 0.0, 0.0f, 0.0f, matrixStack, bufferIn, combinedLightIn);
            matrixStack.popPose();
        }
    }
}

