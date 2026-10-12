/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.entities.renderers.boss.attacks;

import com.mna.entities.boss.attacks.PumpkinKingEntangle;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PumpkinKingEntangleRenderer
extends EntityRenderer<PumpkinKingEntangle> {
    public PumpkinKingEntangleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(PumpkinKingEntangle e, float partialTicks, float p_225623_3_, PoseStack stack, MultiBufferSource buffer, int light) {
        if (e.getOrigin() != null) {
            WorldRenderUtils.renderBeam(e.level(), partialTicks, stack, buffer, light, e.position(), e.getOrigin(), 1.0f, new int[]{38, 91, 19}, 0.1f, MARenderTypes.BOSS_VINE);
        }
    }

    public ResourceLocation getTextureLocation(PumpkinKingEntangle p_110775_1_) {
        return null;
    }
}

