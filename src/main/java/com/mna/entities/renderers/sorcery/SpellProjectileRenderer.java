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
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.level.Level
 */
package com.mna.entities.renderers.sorcery;

import com.mna.api.affinity.Affinity;
import com.mna.api.tools.RLoc;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;

public class SpellProjectileRenderer
extends EntityRenderer<SpellProjectile> {
    public static final ResourceLocation model_water = RLoc.create("fx/projectile/spell_projectile_water");
    public static final ResourceLocation model_water2 = RLoc.create("fx/projectile/spell_projectile_water2");
    public static final ResourceLocation model_ice = RLoc.create("fx/projectile/spell_projectile_ice");
    public static final ResourceLocation model_ice2 = RLoc.create("fx/projectile/spell_projectile_ice2");
    public static final ResourceLocation model_earth = RLoc.create("fx/projectile/spell_projectile_earth");
    public static final ResourceLocation model_earth2 = RLoc.create("fx/projectile/spell_projectile_earth2");
    public static final ResourceLocation model_wind = RLoc.create("fx/projectile/spell_projectile_wind");
    public static final ResourceLocation model_wind2 = RLoc.create("fx/projectile/spell_projectile_wind2");
    public static final ResourceLocation model_ender = RLoc.create("fx/projectile/spell_projectile_ender");
    public static final ResourceLocation model_ender2 = RLoc.create("fx/projectile/spell_projectile_ender2");
    final Minecraft mc = Minecraft.getInstance();

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(SpellProjectile entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (!Minecraft.getInstance().isPaused()) {
            entityIn.spawnParticles(2, partialTicks);
        }
        ArrayList<Affinity> affs = entityIn.getAffinity();
        Affinity aff = Affinity.UNKNOWN;
        if (affs != null) {
            aff = affs.get(0);
        }
        Minecraft mc = Minecraft.getInstance();
        int[] clr = aff.getColor();
        int[] sec = aff.getSecondaryColor();
        VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());
        block0 : switch (entityIn.getSpecialRenderType()) {
            case HELLBALL: {
                this.renderWaterProjectile(entityIn, builder, matrixStackIn, packedLightIn, clr);
                WorldRenderUtils.renderRadiant((Entity)entityIn, matrixStackIn, bufferIn, clr, sec, 255, 0.2f);
                break;
            }
            case HALLOWEEN: {
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityIn.getYRot() + 180.0f));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(entityIn.getXRot()));
                matrixStackIn.scale(0.5f, 0.5f, 0.5f);
                mc.getItemRenderer().renderStatic(entityIn.getSpecialRenderStack(), ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)mc.level, 0);
                matrixStackIn.popPose();
                break;
            }
            default: {
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
                        break block0;
                    }
                    case ICE: {
                        this.renderIceProjectile(entityIn, builder, matrixStackIn, packedLightIn, clr);
                        break block0;
                    }
                    case EARTH: {
                        this.renderEarthProjectile(entityIn, bufferIn.getBuffer(RenderType.solid()), matrixStackIn, packedLightIn, clr);
                        break block0;
                    }
                    case WIND: {
                        this.renderWindProjectile(entityIn, builder, matrixStackIn, packedLightIn, clr);
                        break block0;
                    }
                    case ENDER: {
                        if (override != -1) {
                            clr = new int[]{FastColor.ARGB32.red((int)override), FastColor.ARGB32.green((int)override), FastColor.ARGB32.blue((int)override)};
                            sec = clr;
                        } else {
                            clr = new int[]{15, 6, 22};
                            sec = Affinity.ENDER.getColor();
                        }
                        this.renderEnderProjectile(entityIn, builder, matrixStackIn, packedLightIn, clr, sec);
                        break block0;
                    }
                    case LIGHTNING: {
                        break block0;
                    }
                }
                this.renderWaterProjectile(entityIn, builder, matrixStackIn, packedLightIn, clr);
            }
        }
    }

    private void renderWindProjectile(SpellProjectile entityIn, VertexConsumer builder, PoseStack matrixStackIn, int packedLightIn, int[] color) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityIn.getYRot()));
        matrixStackIn.mulPose(Axis.XN.rotationDegrees(entityIn.getXRot()));
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float)(entityIn.tickCount * 10)));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_wind2, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f}, 0.25f);
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float)(entityIn.tickCount * -20)));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_wind, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f}, 0.25f);
        matrixStackIn.popPose();
    }

    private void renderEnderProjectile(SpellProjectile entityIn, VertexConsumer builder, PoseStack matrixStackIn, int packedLightIn, int[] color, int[] sec) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityIn.getYRot()));
        matrixStackIn.mulPose(Axis.XN.rotationDegrees(entityIn.getXRot()));
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float)(entityIn.tickCount * 10)));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_ender2, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f}, 1.0f);
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float)(entityIn.tickCount * -20)));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_ender, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)sec[0] / 255.0f, (float)sec[1] / 255.0f, (float)sec[2] / 255.0f}, 1.0f);
        matrixStackIn.popPose();
    }

    private void renderWaterProjectile(SpellProjectile entityIn, VertexConsumer builder, PoseStack matrixStackIn, int packedLightIn, int[] color) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityIn.getYRot()));
        matrixStackIn.mulPose(Axis.XN.rotationDegrees(entityIn.getXRot()));
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float)(entityIn.tickCount * 10)));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_water, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f}, 0.75f);
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_water2, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        matrixStackIn.popPose();
    }

    private void renderIceProjectile(SpellProjectile entityIn, VertexConsumer builder, PoseStack matrixStackIn, int packedLightIn, int[] color) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityIn.getYRot()));
        matrixStackIn.mulPose(Axis.XN.rotationDegrees(entityIn.getXRot()));
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float)(entityIn.tickCount * 20)));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_ice2, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_ice, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f}, 0.75f);
        matrixStackIn.popPose();
    }

    private void renderEarthProjectile(SpellProjectile entityIn, VertexConsumer builder, PoseStack matrixStackIn, int packedLightIn, int[] color) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityIn.getYRot()));
        matrixStackIn.mulPose(Axis.XN.rotationDegrees(entityIn.getXRot()));
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees((float)(entityIn.tickCount * 20)));
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_earth2, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_earth, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f}, 0.75f);
        matrixStackIn.popPose();
    }

    public ResourceLocation getTextureLocation(SpellProjectile entity) {
        return null;
    }
}

