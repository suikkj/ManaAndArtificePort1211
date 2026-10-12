/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.renderers.ritual;

import com.mna.api.tools.RLoc;
import com.mna.entities.models.AncientWizardModel;
import com.mna.entities.rituals.AncientCouncil;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class AncientCouncilRenderer
extends EntityRenderer<AncientCouncil> {
    private static final ResourceLocation ANCIENT_WIZARD_TEXTURE = RLoc.create("textures/entity/ancient_wizard.png");
    protected final AncientWizardModel<AncientCouncil> modelWizard = new AncientWizardModel(Minecraft.getInstance().getEntityModels().bakeLayer(AncientWizardModel.LAYER_LOCATION));

    public AncientCouncilRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(AncientCouncil entity) {
        return ANCIENT_WIZARD_TEXTURE;
    }

    public void render(AncientCouncil entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        int age = entityIn.tickCount;
        matrixStackIn.translate(0.0f, -1.0f, 0.0f);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0f));
        float angleDegrees = 0.0f;
        int numWizards = 6;
        for (int i = 0; i < numWizards; ++i) {
            matrixStackIn.pushPose();
            float riseAmount = Math.min(((float)age + partialTicks) / 70.0f * 2.5f, 2.5f);
            matrixStackIn.translate(0.0f, -riseAmount, 0.0f);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(angleDegrees));
            matrixStackIn.translate(-3.0f, 0.0f, 0.0f);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(270.0f));
            this.modelWizard.setupAnim(entityIn, partialTicks, 0.0f, -0.1f, 0.0f, 0.0f);
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(this.modelWizard.renderType(this.getTextureLocation(entityIn)));
            this.modelWizard.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
            matrixStackIn.popPose();
            if (entityIn.getRadiantPct() > 0.0f) {
                matrixStackIn.pushPose();
                matrixStackIn.translate(0.0f, -riseAmount, 0.0f);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(angleDegrees));
                matrixStackIn.translate(-2.25f, 0.5f - 1.25f * entityIn.getRadiantLift(), 0.0f);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(270.0f));
                WorldRenderUtils.renderRadiant((Entity)entityIn, matrixStackIn, bufferIn, new int[]{203, 8, 112}, new int[]{151, 25, 151}, (int)Math.floor(entityIn.getRadiantPct() * 255.0f), 0.05f);
                matrixStackIn.popPose();
            }
            if (entityIn.getBeamPct() > 0.0f) {
                matrixStackIn.pushPose();
                Vec3 start = entityIn.position().add(new Vec3(-2.25, -3.2, 0.0));
                Vec3 end = entityIn.position().add(0.0, -5.0, 0.0);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(-angleDegrees));
                matrixStackIn.translate(-2.25f, -3.2f, 0.0f);
                WorldRenderUtils.renderBeam(entityIn.level(), partialTicks, matrixStackIn, bufferIn, packedLightIn, start, end, entityIn.getBeamPct(), new int[]{255, 255, 255}, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                matrixStackIn.popPose();
            }
            angleDegrees += 360.0f / (float)numWizards;
        }
        if (entityIn.getCenterPct() > 0.0f) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.0f, -5.0f, 0.0f);
            WorldRenderUtils.renderRadiant((Entity)entityIn, matrixStackIn, bufferIn, new int[]{151, 25, 151}, new int[]{77, 133, 207}, (int)Math.floor(entityIn.getCenterPct() * 255.0f), 0.15f);
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    protected boolean shouldShowName(AncientCouncil entity) {
        return false;
    }
}

