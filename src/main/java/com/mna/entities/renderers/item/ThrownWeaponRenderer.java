/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.level.Level
 */
package com.mna.entities.renderers.item;

import com.mna.entities.boss.attacks.ThrownAllfatherAxe;
import com.mna.entities.boss.attacks.ThrownWeapon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;

public class ThrownWeaponRenderer
extends EntityRenderer<ThrownWeapon> {
    final Minecraft mc = Minecraft.getInstance();

    public ThrownWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(ThrownWeapon entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(((float)entityIn.tickCount + partialTicks) * 60.0f));
        float scale = entityIn.getRenderScale();
        matrixStackIn.scale(scale, scale, scale);
        this.mc.getItemRenderer().renderStatic(ThrownAllfatherAxe.renderStack, ItemDisplayContext.NONE, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
        matrixStackIn.popPose();
    }

    public ResourceLocation getTextureLocation(ThrownWeapon entity) {
        return null;
    }
}

