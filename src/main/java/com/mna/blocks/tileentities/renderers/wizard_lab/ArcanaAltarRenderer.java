/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 */
package com.mna.blocks.tileentities.renderers.wizard_lab;

import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.models.ArcanaAltarModel;
import com.mna.blocks.tileentities.renderers.wizard_lab.WizardLabRenderer;
import com.mna.blocks.tileentities.wizard_lab.ArcanaAltarTile;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class ArcanaAltarRenderer
extends WizardLabRenderer<ArcanaAltarTile> {
    private final ItemRenderer itemRenderer;
    private final Minecraft mc = Minecraft.getInstance();

    public ArcanaAltarRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn, new ArcanaAltarModel());
        this.itemRenderer = this.mc.getItemRenderer();
    }

    public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        ArcanaAltarTile tile = (ArcanaAltarTile)this.getAnimatable();
        for (int i = 0; i < 6; ++i) {
            Vector3f offset = tile.getCandlePos(i, partialTick);
            poseStack.pushPose();
            poseStack.translate(offset.x(), offset.y(), offset.z());
            ModelUtils.renderModel(bufferSource.getBuffer(RenderType.cutout()), tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), i % 2 == 0 ? ArcanaAltarModel.candle_short : ArcanaAltarModel.candle_long, poseStack, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        float beamPct = Math.max(tile.getBeamPct(partialTick), 0.0f);
        float completionPct = Math.max((float)(tile.getCompletionTicks() - 60 - 20) / 20.0f, 0.0f);
        if (tile.isActive() && tile.getState().ordinal() >= ArcanaAltarTile.States.FINALIZING.ordinal()) {
            int[] color = new int[]{30, 60, 170};
            poseStack.pushPose();
            poseStack.translate(0.0, 2.5, 0.0);
            WorldRenderUtils.renderRadiant(tile.getLevel().getGameTime(), poseStack, bufferSource, color, color, 255, 3.0f - 3.0f * completionPct, false);
            poseStack.popPose();
        }
        if (tile.isActive()) {
            Quaternionf cameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
            Quaternionf orbRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
            VertexConsumer vertexBuilder = bufferSource.getBuffer(MARenderTypes.ORB_RENDER);
            int[] colors = new int[]{255, 255, 255};
            float orbPct = tile.getItemCollectPct(partialTick) - beamPct;
            for (int i = 0; i < 4; ++i) {
                float rotationDegrees = 90.0f * (float)i;
                poseStack.pushPose();
                poseStack.mulPose(orbRotation);
                Direction facing = (Direction)tile.getBlockState().getValue((Property)HorizontalDirectionalBlock.FACING);
                switch (facing) {
                    case WEST: {
                        poseStack.mulPose(Axis.YP.rotationDegrees(270.0f));
                    }
                    case EAST: {
                        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    }
                    case SOUTH: {
                        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                    }
                }
                poseStack.translate(0.0, 2.5, 0.0);
                poseStack.scale(0.25f * orbPct, 0.25f * orbPct, 0.25f * orbPct);
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotationDegrees));
                poseStack.translate(0.5, -0.25, 0.0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
                float nrmV = (float)Math.cos((double)(rotationDegrees - 180.0f) * Math.PI / 180.0);
                float nrmH = (float)Math.cos((double)(rotationDegrees - 180.0f - 90.0f) * Math.PI / 180.0);
                PoseStack.Pose matrixstack$entry = poseStack.last();
                Matrix4f renderMatrix = matrixstack$entry.pose();
                Matrix3f normalMatrix = matrixstack$entry.normal();
                ArcanaAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, colors);
                ArcanaAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, colors);
                ArcanaAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, colors);
                ArcanaAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, colors);
                poseStack.popPose();
            }
            if (tile.getRequestingPattern() != null) {
                Quaternionf patternRotation = Axis.YP.rotationDegrees(0.0f);
                float scale = 0.3f;
                poseStack.pushPose();
                poseStack.translate(0.0, 1.3, 0.0);
                poseStack.scale(scale, scale, scale);
                WorldRenderUtils.renderManaweavePattern(tile.getRequestingPattern(), patternRotation, poseStack, bufferSource, false);
                poseStack.popPose();
            } else if (tile.getRequestingStack().size() > 0) {
                int index = (int)(ManaAndArtifice.instance.proxy.getGameTicks() / 20L % (long)tile.getRequestingStack().size());
                ItemStack stack = new ItemStack((ItemLike)tile.getRequestingStack().get(index));
                float bob = (float)Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTick) / 60.0f) / 40.0f;
                float rotation = ManaAndArtifice.instance.proxy.getGameTicks();
                poseStack.pushPose();
                poseStack.translate(0.0f, 1.9f + bob, 0.0f);
                poseStack.scale(0.25f, 0.25f, 0.25f);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, (Level)this.mc.level, 0);
                poseStack.popPose();
            }
            if (tile.getState() == ArcanaAltarTile.States.FINALIZING) {
                poseStack.pushPose();
                Vec3 start = Vec3.upFromBottomCenterOf((Vec3i)tile.getBlockPos(), (double)1.0);
                Vec3 end = Vec3.upFromBottomCenterOf((Vec3i)tile.getBlockPos(), (double)2.5);
                poseStack.translate(0.0, 2.5, 0.0);
                WorldRenderUtils.renderBeam(tile.getLevel(), partialTick, poseStack, bufferSource, packedLight, end, start, beamPct, new int[]{200, 200, 255}, 255, 0.065f * (1.0f - completionPct), MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                poseStack.popPose();
            }
        }
        super.firePostRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
    }

    private static void addVertex(VertexConsumer vertexBuilder_, com.mojang.blaze3d.vertex.PoseStack.Pose pose, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] rgb) {
        vertexBuilder_.addVertex(pose, x - 0.5f, y - 0.25f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], 230).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nrmH, nrmV, nrmH);
    }
}

