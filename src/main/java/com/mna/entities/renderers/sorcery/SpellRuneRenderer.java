/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.EntityHitResult
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package com.mna.entities.renderers.sorcery;

import com.mna.ManaAndArtifice;
import com.mna.entities.sorcery.targeting.SpellSigil;
import com.mna.gui.GuiTextures;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SpellRuneRenderer
extends EntityRenderer<SpellSigil> {
    public SpellRuneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(SpellSigil entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        float alpha;
        float scaleFactor = entityIn.getSize();
        Minecraft mc = Minecraft.getInstance();
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityTranslucent((ResourceLocation)GuiTextures.affinityBadges.get(entityIn.getAffinity())));
        float f = entityIn.isOnCooldown() ? 192.0f + (float)((int)(64.0 * Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks) / 10.0f))) : (alpha = entityIn.getCharges() == 0 ? 128.0f : 255.0f);
        if (entityIn.isInvisible()) {
            alpha = 32.0f;
        }
        matrixStackIn.pushPose();
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(-90.0f));
        matrixStackIn.translate(0.0f, -0.25f, 0.0f);
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        float nrmV = 1.0f;
        float nrmH = 1.0f;
        SpellRuneRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, alpha);
        SpellRuneRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, alpha);
        SpellRuneRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, alpha);
        SpellRuneRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLightIn, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, alpha);
        matrixStackIn.popPose();
        if (entityIn.isPermanent()) {
            String text = String.format("%d", entityIn.getCharges());
            if (mc.hitResult instanceof EntityHitResult && ((EntityHitResult)mc.hitResult).getEntity() == entityIn && entityIn.isCaster((Player)mc.player)) {
                Font font = mc.font;
                matrixStackIn.pushPose();
                matrixStackIn.translate(0.0f, 1.0f, 0.0f);
                matrixStackIn.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                matrixStackIn.scale(-0.025f, -0.025f, 0.025f);
                Matrix4f matrix4f = matrixStackIn.last().pose();
                float opacity = 0.25f;
                int textColor = (int)(opacity * 255.0f) << 24;
                float hOffset = -font.width(text) / 2;
                font.drawInBatch(text, hOffset, 0.0f, 0x20FFFFFF, false, matrix4f, bufferIn, Font.DisplayMode.NORMAL, textColor, packedLightIn);
                font.drawInBatch(text, hOffset, 0.0f, -1, false, matrix4f, bufferIn, Font.DisplayMode.NORMAL, 0, packedLightIn);
                matrixStackIn.popPose();
            }
        }
    }

    private static void addVertex(VertexConsumer vertexBuilder_, com.mojang.blaze3d.vertex.PoseStack.Pose pose, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, float alpha) {
        vertexBuilder_.addVertex(pose, x - 0.5f, y - 0.25f, 0.0f).setColor(255, 255, 255, (int)alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nrmH, nrmV, nrmH);
    }

    public ResourceLocation getTextureLocation(SpellSigil entity) {
        return null;
    }
}

