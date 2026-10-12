/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.renderers;

import com.mna.items.manaweaving.ItemManaweaveBottle;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ManaweaveBottleRenderer
extends BlockEntityWithoutLevelRenderer {
    private BakedModel bottleModel;
    private final ResourceLocation bottle_texture_location = ResourceLocation.fromNamespaceAndPath("mna", "item/manaweave_bottle_texture");

    public ManaweaveBottleRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems);
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ManaweavingPattern p;
        if (this.bottleModel == null) {
            this.bottleModel = Minecraft.getInstance().getModelManager().getModel(this.bottle_texture_location);
        }
        matrixStack.popPose();
        matrixStack.pushPose();
        if (itemDisplayContext == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
        }
        Minecraft.getInstance().getItemRenderer().render(stack, itemDisplayContext, itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND, matrixStack, buffer, itemDisplayContext == ItemDisplayContext.GUI ? 0xF000F0 : combinedLight, itemDisplayContext == ItemDisplayContext.GUI ? OverlayTexture.NO_OVERLAY : combinedOverlay, this.bottleModel);
        if (ItemManaweaveBottle.hasPattern(stack) && (p = ItemManaweaveBottle.getPattern(stack)) != null) {
            matrixStack.pushPose();
            switch (itemDisplayContext) {
                case FIRST_PERSON_LEFT_HAND: 
                case FIRST_PERSON_RIGHT_HAND: {
                    matrixStack.scale(0.4f, 0.4f, 0.4f);
                    matrixStack.mulPose(Axis.XN.rotationDegrees(15.0f));
                    matrixStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    matrixStack.translate(-0.3, (double)-1.65f, 0.0);
                    break;
                }
                case FIXED: 
                case GUI: 
                case HEAD: {
                    matrixStack.scale(0.75f, 0.75f, 1.0f);
                    matrixStack.translate(0.0f, -2.05f, 0.0f);
                    break;
                }
                case THIRD_PERSON_LEFT_HAND: 
                case THIRD_PERSON_RIGHT_HAND: {
                    matrixStack.scale(0.4f, 0.4f, 0.4f);
                    matrixStack.translate(0.0f, -1.75f, 0.1f);
                    break;
                }
                default: {
                    matrixStack.scale(0.4f, 0.4f, 0.4f);
                    matrixStack.translate(0.0f, -1.75f, 0.0f);
                }
            }
            if (itemDisplayContext == ItemDisplayContext.GROUND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                WorldRenderUtils.renderManaweavePattern(p, Axis.YP.rotationDegrees(0.0f), matrixStack, buffer, true);
            }
            if (itemDisplayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                WorldRenderUtils.renderManaweavePattern(p, Axis.YP.rotationDegrees(180.0f), matrixStack, buffer, true);
            }
            matrixStack.popPose();
        }
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource.endBatch();
        if (itemDisplayContext == ItemDisplayContext.GUI) {
            Lighting.setupFor3DItems();
        }
    }
}

