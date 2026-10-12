/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 */
package com.mna.blocks.tileentities.renderers.wizard_lab;

import com.mna.blocks.tileentities.models.DisenchanterModel;
import com.mna.blocks.tileentities.renderers.wizard_lab.WizardLabRenderer;
import com.mna.blocks.tileentities.wizard_lab.DisenchanterTile;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class DisenchanterRenderer
extends WizardLabRenderer<DisenchanterTile> {
    public DisenchanterRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn, new DisenchanterModel());
    }

    public void actuallyRender(PoseStack poseStack, DisenchanterTile animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (animatable.isActive() && (double)animatable.getPctComplete() > 0.9) {
            float beamPct = (animatable.getPctComplete() - 0.9f) / 0.1f;
            poseStack.pushPose();
            Vec3 start = Vec3.upFromBottomCenterOf((Vec3i)animatable.getBlockPos(), (double)1.0);
            Vec3 end = Vec3.upFromBottomCenterOf((Vec3i)animatable.getBlockPos(), (double)2.0);
            poseStack.translate(0.5, 2.0, 0.5);
            WorldRenderUtil.renderBeam(animatable.getLevel(), partialTick, poseStack, bufferSource, packedLight, end, start, beamPct, new int[]{200, 200, 255}, 255, 0.065f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            poseStack.popPose();
        }
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}

