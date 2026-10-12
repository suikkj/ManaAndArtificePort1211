/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities.renderers.boss.attacks;

import com.mna.entities.boss.attacks.PumpkinKingIncinerate;
import com.mna.events.ClientEventHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class PumpkinKingIncinerateRenderer
extends EntityRenderer<PumpkinKingIncinerate> {
    public PumpkinKingIncinerateRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(PumpkinKingIncinerate p_225623_1_, float p_225623_2_, float p_225623_3_, PoseStack p_225623_4_, MultiBufferSource p_225623_5_, int p_225623_6_) {
        ClientEventHandler.setFogAmount(Math.min(ClientEventHandler.getFogAmount() + 0.025f, 0.85f));
        ClientEventHandler.fogColor[0] = 0.314f;
        ClientEventHandler.fogColor[1] = 0.14f;
        ClientEventHandler.fogColor[2] = 0.06f;
        super.render((Entity)p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    public ResourceLocation getTextureLocation(PumpkinKingIncinerate p_110775_1_) {
        return null;
    }
}

