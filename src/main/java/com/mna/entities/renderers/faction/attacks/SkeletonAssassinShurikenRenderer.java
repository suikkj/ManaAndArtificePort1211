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
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package com.mna.entities.renderers.faction.attacks;

import com.mna.entities.projectile.SkeletonAssassinShuriken;
import com.mna.items.ItemInit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class SkeletonAssassinShurikenRenderer
extends EntityRenderer<SkeletonAssassinShuriken> {
    private static final ResourceLocation shurikenTexture = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/shuriken.png");
    private static ItemStack shurikenStack;
    private static ItemRenderer itemRenderer;
    private final Minecraft mc = Minecraft.getInstance();

    public SkeletonAssassinShurikenRenderer(EntityRendererProvider.Context context) {
        super(context);
        if (shurikenStack == null) {
            shurikenStack = new ItemStack((ItemLike)ItemInit.SKELETON_SHURIKEN.get());
        }
        if (itemRenderer == null) {
            itemRenderer = this.mc.getItemRenderer();
        }
    }

    public void render(SkeletonAssassinShuriken entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        float ticks = ((float)entityIn.tickCount + partialTicks) * 50.0f;
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp((float)partialTicks, (float)entityIn.yRotO, (float)entityIn.getYRot()) - 90.0f));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp((float)partialTicks, (float)entityIn.xRotO, (float)entityIn.getXRot())));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
        if (!entityIn.isInGround()) {
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(ticks));
        }
        matrixStackIn.scale(0.5f, 0.5f, 0.25f);
        itemRenderer.renderStatic(shurikenStack, ItemDisplayContext.FIXED, 0xF00000, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
        matrixStackIn.popPose();
    }

    public ResourceLocation getTextureLocation(SkeletonAssassinShuriken entity) {
        return shurikenTexture;
    }
}

