/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  org.joml.Quaternionf
 */
package com.mna.entities.renderers.ritual;

import com.mna.api.tools.MATags;
import com.mna.entities.utility.DisplayReagents;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

public class DisplayReagentsRenderer
extends EntityRenderer<DisplayReagents> {
    private final ItemRenderer itemRenderer;
    private final Minecraft mc = Minecraft.getInstance();

    public DisplayReagentsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = this.mc.getItemRenderer();
    }

    public ResourceLocation getTextureLocation(DisplayReagents entity) {
        return null;
    }

    public void render(DisplayReagents entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        List<ResourceLocation> reagents = entityIn.getResourceLocations();
        float displayItemWidth = 0.0625f;
        float offset = (float)reagents.size() * displayItemWidth;
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        int multiItemIndex = entityIn.getAge() / 40;
        int count = 0;
        float scaleFactor = 0.25f;
        for (ResourceLocation location : reagents) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(offset * (float)reagents.size() / 2.0f - displayItemWidth * 2.0f, 1.0f, 0.0f);
            matrixStackIn.translate(-offset * (float)count, 0.0f, 0.0f);
            matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(portalRotation);
            List<Item> item = MATags.smartLookupItem(location);
            ItemStack renderStack = item.size() == 1 ? new ItemStack((ItemLike)item.get(0)) : new ItemStack((ItemLike)item.get(multiItemIndex % item.size()));
            if (renderStack != null) {
                this.itemRenderer.renderStatic(renderStack, ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
            }
            matrixStackIn.popPose();
            this.renderNameTag(entityIn, (Component)Component.literal((String)("" + (count + 1))), matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.popPose();
            ++count;
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }
}

