/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.items.renderers;

import com.mna.ManaAndArtifice;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.models.constructs.ConstructModel;
import com.mna.entities.renderers.construct.ConstructRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConstructPartRenderer
extends BlockEntityWithoutLevelRenderer {
    public ConstructPartRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems);
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof ItemConstructPart) {
            ItemConstructPart p = (ItemConstructPart)stack.getItem();
            matrixStack.pushPose();
            Construct dummy = ManaAndArtifice.instance.proxy.getDummyConstructForRender();
            dummy.getConstructData().resetParts();
            dummy.getConstructData().setPart(new ItemStack((ItemLike)p), true);
            ConstructRenderer geoRenderer = ConstructRenderer.instance;
            ConstructModel model = ConstructRenderer.constructModel;
            model.getBakedModel(model.getModelResource(dummy));
            model.setActiveMaterial(p.getMaterial());
            try {
                model.setVisibleParts(null, p.getSlot() == ConstructSlot.HEAD ? p.getModelTypeMutex() : 0, p.getSlot() == ConstructSlot.TORSO ? p.getModelTypeMutex() : 0, p.getSlot() == ConstructSlot.LEGS ? p.getModelTypeMutex() : 0, p.getSlot() == ConstructSlot.LEFT_ARM ? p.getModelTypeMutex() : 0, p.getSlot() == ConstructSlot.RIGHT_ARM ? p.getModelTypeMutex() : 0);
            }
            catch (Exception exception) {
                // empty catch block
            }
            switch (p.getSlot()) {
                case HEAD: {
                    matrixStack.translate(0.51, -1.7, 0.45);
                    matrixStack.scale(2.0f, 2.0f, 2.0f);
                    break;
                }
                case LEFT_ARM: {
                    matrixStack.translate(0.4, -0.2, 0.45);
                    matrixStack.mulPose(Axis.ZP.rotationDegrees(22.0f));
                    matrixStack.mulPose(Axis.YP.rotationDegrees(22.0f));
                    break;
                }
                case LEGS: {
                    matrixStack.translate(0.51, 0.2, 0.55);
                    matrixStack.mulPose(Axis.YP.rotationDegrees(22.0f));
                    break;
                }
                case RIGHT_ARM: {
                    matrixStack.translate(1.0, 0.0, 0.45);
                    matrixStack.mulPose(Axis.ZP.rotationDegrees(22.0f));
                    matrixStack.mulPose(Axis.YP.rotationDegrees(22.0f));
                    break;
                }
                case TORSO: {
                    matrixStack.translate(0.51, -0.35, 0.45);
                    break;
                }
            }
            Lighting.setupForFlatItems();
            if (itemDisplayContext != ItemDisplayContext.GUI) {
                geoRenderer.render(dummy, dummy.getXRot(), 0.0f, matrixStack, buffer, combinedLight);
            } else {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                Lighting.setupForFlatItems();
                geoRenderer.render(dummy, dummy.getXRot(), 0.0f, matrixStack, buffer, combinedLight);
                bufferSource.endBatch();
                RenderSystem.enableDepthTest();
                Lighting.setupFor3DItems();
            }
            matrixStack.popPose();
        }
    }
}

