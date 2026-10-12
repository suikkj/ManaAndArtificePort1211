/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.renderers;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.items.relic.AstroBlade;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AstroBladeRenderer
extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation blade_model = RLoc.create("item/special/astro_blade_blade");
    public static final ResourceLocation handle_model = RLoc.create("item/special/astro_blade_handle");
    public static final ResourceLocation blade_model_warden = RLoc.create("item/special/warden_blade_blade");
    public static final ResourceLocation handle_model_warden = RLoc.create("item/special/warden_blade_handle");

    public AstroBladeRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems);
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext ItemDisplayContext2, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof AstroBlade) {
            matrixStack.pushPose();
            if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains("as_warden")) {
                ModelUtils.renderEntityModel(buffer.getBuffer(RenderType.solid()), ManaAndArtifice.instance.proxy.getClientWorld(), handle_model_warden, matrixStack, combinedLight, combinedOverlay);
                ModelUtils.renderEntityModel(buffer.getBuffer(RenderType.solid()), ManaAndArtifice.instance.proxy.getClientWorld(), blade_model_warden, matrixStack, 0xF000F0, combinedOverlay);
            } else {
                ModelUtils.renderEntityModel(buffer.getBuffer(RenderType.solid()), ManaAndArtifice.instance.proxy.getClientWorld(), handle_model, matrixStack, combinedLight, combinedOverlay);
                ModelUtils.renderEntityModel(buffer.getBuffer(RenderType.endPortal()), ManaAndArtifice.instance.proxy.getClientWorld(), blade_model, matrixStack, combinedLight, combinedOverlay);
            }
            matrixStack.popPose();
        }
    }
}

