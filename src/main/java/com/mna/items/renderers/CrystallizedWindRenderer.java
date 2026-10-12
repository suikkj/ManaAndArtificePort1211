/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
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

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.items.relic.CrystallizedWind;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CrystallizedWindRenderer
extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation gem_model = RLoc.create("item/special/crystallized_wind_gem");
    public static final ResourceLocation wisps_model = RLoc.create("item/special/crystallized_wind_wisps");

    public CrystallizedWindRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems);
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext ItemDisplayContext2, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof CrystallizedWind) {
            matrixStack.pushPose();
            matrixStack.mulPose(Axis.YN.rotationDegrees((float)ManaAndArtifice.instance.proxy.getGameTicks() + ManaAndArtifice.instance.proxy.getPartialTick()));
            ModelUtils.renderEntityModel(buffer.getBuffer(RenderType.translucent()), ManaAndArtifice.instance.proxy.getClientWorld(), gem_model, matrixStack, combinedLight, combinedOverlay);
            matrixStack.popPose();
            matrixStack.pushPose();
            matrixStack.mulPose(Axis.YP.rotationDegrees((float)ManaAndArtifice.instance.proxy.getGameTicks() + ManaAndArtifice.instance.proxy.getPartialTick()));
            ModelUtils.renderEntityModel(buffer.getBuffer(RenderType.translucent()), ManaAndArtifice.instance.proxy.getClientWorld(), wisps_model, matrixStack, combinedLight, combinedOverlay);
            matrixStack.popPose();
        }
    }
}

