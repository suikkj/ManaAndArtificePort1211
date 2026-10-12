/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.renderers;

import com.mna.items.renderers.books.ItemBookRenderer;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemSpellBookRenderer
extends ItemBookRenderer {
    private boolean renderBookInFirstPerson = true;

    public ItemSpellBookRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems, ResourceLocation openModel, ResourceLocation closedModel, boolean renderBookInFirstPerson) {
        super(berd, ems, openModel, closedModel);
        this.renderBookInFirstPerson = renderBookInFirstPerson;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (itemDisplayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            if (!this.renderBookInFirstPerson) {
                return;
            }
        } else if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            if (!this.renderBookInFirstPerson) {
                this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.RIGHT);
            }
            WorldRenderUtils.spawnFirstPersonParticlesForStack(stack, HumanoidArm.RIGHT);
            if (!this.renderBookInFirstPerson) {
                return;
            }
        } else if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            if (!this.renderBookInFirstPerson) {
                this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.LEFT);
            }
            WorldRenderUtils.spawnFirstPersonParticlesForStack(stack, HumanoidArm.LEFT);
            if (!this.renderBookInFirstPerson) {
                return;
            }
        }
        super.renderByItem(stack, itemDisplayContext, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, HumanoidArm side) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)mc.player.getSkinTextureLocation());
        PlayerRenderer playerrenderer = (PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer((Entity)mc.player);
        matrixStackIn.pushPose();
        if (side == HumanoidArm.RIGHT) {
            matrixStackIn.translate(0.75, -0.25, 0.0);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(95.0f));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-30.0f));
            playerrenderer.renderRightHand(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayer)mc.player);
        } else {
            matrixStackIn.translate(0.25, -0.25, 0.0);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(260.0f));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(30.0f));
            playerrenderer.renderLeftHand(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayer)mc.player);
        }
        matrixStackIn.popPose();
    }
}

