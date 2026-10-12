/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.entities.renderers.sorcery;

import com.mna.entities.sorcery.targeting.SpellBeam;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class SpellBeamRenderer
extends EntityRenderer<SpellBeam> {
    public SpellBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(SpellBeam entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.getLastTickImpact() != null) {
            Vec3 endPointOffset = entityIn.getLastTickImpact().subtract(entityIn.position());
            matrixStackIn.pushPose();
            matrixStackIn.translate(endPointOffset.x, endPointOffset.y, endPointOffset.z);
            WorldRenderUtils.renderRadiant(entityIn, matrixStackIn, bufferIn, new int[]{255, 255, 255}, entityIn.getBeamColor(), 128, 0.05f);
            matrixStackIn.popPose();
            WorldRenderUtils.renderBeam(entityIn.level(), partialTicks, matrixStackIn, bufferIn, packedLightIn, entityIn.position(), entityIn.getLastTickImpact(), 1.0f, entityIn.getBeamColor(), 0.2f, MARenderTypes.SPELL_BEAM_RENDER_TYPE);
            if (endPointOffset.y < 0.0) {
                matrixStackIn.translate(0.0, 0.001, 0.0);
            } else {
                matrixStackIn.translate(0.0, -0.001, 0.0);
            }
            WorldRenderUtils.renderBeam(entityIn.level(), partialTicks, matrixStackIn, bufferIn, packedLightIn, entityIn.position(), entityIn.getLastTickImpact(), 1.0f, new int[]{255, 255, 255}, 0.05f, MARenderTypes.SPELL_BEAM_RENDER_TYPE);
        }
    }

    public ResourceLocation getTextureLocation(SpellBeam entity) {
        return null;
    }
}

