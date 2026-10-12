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
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 */
package com.mna.entities.renderers.sorcery;

import com.mna.api.affinity.Affinity;
import com.mna.entities.renderers.sorcery.SpellProjectileRenderer;
import com.mna.entities.sorcery.targeting.Smite;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SmiteProjectileRenderer
extends EntityRenderer<Smite> {
    final Minecraft mc = Minecraft.getInstance();

    public SmiteProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(Smite entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (!Minecraft.getInstance().isPaused()) {
            entityIn.spawnParticles(10, partialTicks);
        }
        ArrayList<Affinity> affs = entityIn.getAffinity();
        Affinity aff = Affinity.UNKNOWN;
        if (affs != null) {
            aff = affs.get(0);
        }
        int[] clr = aff.getColor();
        int[] sec = aff.getSecondaryColor();
        VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());
        int override = entityIn.getOverrideColor();
        if (override != -1) {
            clr = new int[]{FastColor.ARGB32.red((int)override), FastColor.ARGB32.green((int)override), FastColor.ARGB32.blue((int)override)};
            sec = clr;
        }
        switch (aff) {
            case ARCANE: {
                this.renderWaterProjectile(entityIn, builder, matrixStackIn, packedLightIn, sec);
                matrixStackIn.translate(0.0, 0.0, 0.01);
                this.renderWaterProjectile(entityIn, builder, matrixStackIn, packedLightIn, clr);
                break;
            }
            case LIGHTNING: {
                break;
            }
            default: {
                this.renderWaterProjectile(entityIn, builder, matrixStackIn, packedLightIn, clr);
            }
        }
        super.render((Entity)entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void renderWaterProjectile(Smite entityIn, VertexConsumer builder, PoseStack matrixStackIn, int packedLightIn, int[] color) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, SpellProjectileRenderer.model_water, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f}, 0.75f);
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, SpellProjectileRenderer.model_water2, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        matrixStackIn.popPose();
    }

    public ResourceLocation getTextureLocation(Smite entity) {
        return null;
    }
}

