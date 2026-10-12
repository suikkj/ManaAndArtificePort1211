/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.tools.RLoc;
import com.mna.gui.GuiTextures;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PylonRenderer
implements BlockEntityRenderer<PylonTileBase> {
    public static final ResourceLocation crystal = RLoc.create("block/eldrin/pylon_crystal");
    public static final ResourceLocation ring = RLoc.create("block/eldrin/pylon_ring");
    public static final ResourceLocation scroll = RLoc.create("block/eldrin/pylon_scroll");
    private final int[] defaultActiveColor = new int[]{49, 235, 234};

    public PylonRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(PylonTileBase pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        long ticks = pBlockEntity.getLevel().getGameTime();
        Direction facing = (Direction)pBlockEntity.getBlockState().getValue((Property)HorizontalDirectionalBlock.FACING);
        int[] color = pBlockEntity.getColor();
        if (color == null || color.length != 3) {
            color = this.defaultActiveColor;
        }
        float powerUpPct = pBlockEntity.getPowerupPct(pPartialTick);
        int[] runeWord = pBlockEntity.getRuneword();
        float runewordPct = Math.min(powerUpPct / 0.5f, 1.0f);
        pPoseStack.pushPose();
        switch (facing) {
            case EAST: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                pPoseStack.translate(-0.8, 1.65, 0.75);
            }
            case WEST: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
                pPoseStack.translate(0.2, 1.65, -0.25);
            }
            case NORTH: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                pPoseStack.translate(-0.8, 1.65, -0.25);
            }
            default: {
                pPoseStack.translate(0.2, 1.65, 0.75);
            }
        }
        WorldRenderUtils.RenderRuneWord(pPoseStack, pBuffer, GuiTextures.Blocks.RUNES, pPackedLight, color, runeWord, runewordPct);
        ResourceLocation badge = pBlockEntity.getIcon();
        if (badge != null) {
            pPoseStack.translate(0.3, -1.2, 0.07);
            this.renderPotionEffectBadge(pPoseStack, pBuffer, badge, pPackedLight);
        }
        pPoseStack.popPose();
        if (powerUpPct > 0.5f) {
            float radiantPct = Math.min((powerUpPct - 0.5f) / 0.25f, 1.0f);
            int[] color2 = color;
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 2.0, 0.5);
            if (powerUpPct > 0.75f) {
                float beamLengthPct = (powerUpPct - 0.75f) / 0.25f;
                WorldRenderUtils.renderBeam(pBlockEntity.getLevel(), pPartialTick, pPoseStack, pBuffer, pPackedLight, new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, (double)pBlockEntity.getClearHeightAbove(), 0.0), beamLengthPct, color2, 0.05f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            }
            WorldRenderUtils.renderRadiant((float)ticks + pPartialTick, pPoseStack, pBuffer, color, color2, (int)(230.0f * radiantPct), 0.5f, false);
            WorldRenderUtils.renderRadiant((float)(ticks + 1000L) + pPartialTick, pPoseStack, pBuffer, color, color2, (int)(230.0f * radiantPct), 0.5f, false);
            pPoseStack.popPose();
        }
    }

    private void renderPotionEffectBadge(PoseStack matrixStackIn, MultiBufferSource bufferIn, ResourceLocation buffTexture, int packedLightIn) {
        float scaleFactor = 0.25f;
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityTranslucent((ResourceLocation)buffTexture));
        matrixStackIn.pushPose();
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        float nrmV = 1.0f;
        float nrmH = 1.0f;
        int[] activeColor = new int[]{255, 255, 255};
        this.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, activeColor);
        this.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, activeColor);
        this.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, activeColor);
        this.addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, activeColor);
        matrixStackIn.popPose();
    }

    private void addVertex(VertexConsumer vertexBuilder_, Matrix4f renderMatrix, Matrix3f normalMatrix, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] color) {
        vertexBuilder_.addVertex(renderMatrix, x - 0.5f, y - 0.25f, 0.0f).setColor(color[0], color[1], color[2], 230).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normalMatrix, nrmH, nrmV, nrmH);
    }
}

