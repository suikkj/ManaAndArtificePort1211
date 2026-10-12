/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities.renderers.faction;

import com.mna.api.affinity.Affinity;
import com.mna.entities.faction.LivingWard;
import com.mna.entities.models.faction.LivingWardModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class LivingWardRenderer
extends MAGeckoRenderer<LivingWard> {
    public LivingWardRenderer(EntityRendererProvider.Context context) {
        super(context, new LivingWardModel());
    }

    @Override
    public void render(LivingWard entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        stack.pushPose();
        stack.translate(0.0, 0.25, 0.0);
        WorldRenderUtils.renderFlatRadiant((Entity)entity, stack, bufferIn, Affinity.ARCANE.getSecondaryColor(), Affinity.ARCANE.getColor(), 128, 0.1f);
        stack.popPose();
        stack.scale(0.5f, 0.5f, 0.5f);
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, 0xF000F0);
    }
}

