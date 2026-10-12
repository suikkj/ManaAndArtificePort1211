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
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.OcculusTile;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class OcculusRenderer
implements BlockEntityRenderer<OcculusTile> {
    private final ResourceLocation interior = ResourceLocation.fromNamespaceAndPath("mna", "block/occulus_eye_shell_interior");
    private final ResourceLocation frame = ResourceLocation.fromNamespaceAndPath("mna", "block/occulus_eye_shell");
    private static final Random RANDOM = new Random(31100L);

    public OcculusRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(OcculusTile tileEntityIn, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Level world = tileEntityIn.getLevel();
        BlockPos pos = tileEntityIn.getBlockPos();
        BlockState state = tileEntityIn.getBlockState();
        Minecraft mc = Minecraft.getInstance();
        Vec3 teDir = new Vec3((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
        Vec3 playerPos = mc.player.position();
        Vec3 between = playerPos.subtract(teDir);
        double angle = -Math.atan2(between.z, between.x) / 2.0 / Math.PI * 360.0 + 90.0;
        Quaternionf eyeRotation = Axis.YP.rotationDegrees((float)angle);
        matrixStack.pushPose();
        float translateUp = (float)Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks) / 40.0f) * 0.1f;
        float addedHeight = MathUtils.clamp01(0.5f - (float)(between.length() / 10.0));
        float finalHeight = addedHeight / 2.0f + translateUp * addedHeight;
        matrixStack.translate(0.5f, finalHeight, 0.5f);
        matrixStack.mulPose(eyeRotation);
        float colorMod = 0.15f;
        this.renderModelWithRandomColor(matrixStack, bufferIn.getBuffer(RenderType.endPortal()), world, pos, state, colorMod, combinedLightIn, combinedOverlayIn);
        ModelUtils.renderModel(bufferIn, world, pos, state, this.frame, matrixStack, combinedLightIn, combinedOverlayIn);
        matrixStack.popPose();
    }

    private void renderModelWithRandomColor(PoseStack stack, VertexConsumer builder, Level world, BlockPos pos, BlockState state, float colorMod, int light, int overlay) {
        float r = (RANDOM.nextFloat() * 0.5f + 0.1f) * colorMod;
        float g = (RANDOM.nextFloat() * 0.5f + 0.4f) * colorMod;
        float b = (RANDOM.nextFloat() * 0.5f + 0.5f) * colorMod;
        ModelUtils.renderModel(builder, world, pos, state, this.interior, stack, new float[]{1.0f, r, g, b}, light, overlay);
    }
}

