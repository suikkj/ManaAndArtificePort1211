/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.blocks.decoration.ScrollshelfBlock;
import com.mna.blocks.tileentities.BookshelfTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BookshelfRenderer
implements BlockEntityRenderer<BookshelfTile> {
    private final ItemRenderer itemRenderer;
    private final Minecraft mc = Minecraft.getInstance();

    public BookshelfRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = this.mc.getItemRenderer();
    }

    public void render(BookshelfTile pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        pPoseStack.pushPose();
        switch ((Direction)state.getValue((Property)ScrollshelfBlock.FACING)) {
            case EAST: {
                pPoseStack.translate(1.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
                break;
            }
            case SOUTH: {
                pPoseStack.translate(0.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case WEST: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            default: {
                pPoseStack.translate(1.0f, 0.0f, 0.0f);
            }
        }
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        pPoseStack.translate(0.55, 1.4325, 0.2125);
        int idx = 0;
        for (int col = 0; col < 5; ++col) {
            ItemStack stack = pBlockEntity.getDisplayItem(idx++);
            double xOffset = 0.15 * (double)col;
            if (!stack.isEmpty()) {
                pPoseStack.pushPose();
                pPoseStack.translate(0.0, 0.0, xOffset);
                pPoseStack.scale(0.7f, 0.7f, 0.7f);
                this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, (Level)this.mc.level, 0);
                pPoseStack.popPose();
            }
            if (!(stack = pBlockEntity.getDisplayItem(idx++)).isEmpty()) {
                pPoseStack.pushPose();
                pPoseStack.translate(0.0, -0.5, xOffset);
                pPoseStack.scale(0.7f, 0.7f, 0.7f);
                this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, (Level)this.mc.level, 0);
                pPoseStack.popPose();
            }
            if ((stack = pBlockEntity.getDisplayItem(idx++)).isEmpty()) continue;
            pPoseStack.pushPose();
            pPoseStack.translate(0.0, -1.065, xOffset);
            pPoseStack.scale(0.7f, 0.7f, 0.7f);
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, (Level)this.mc.level, 0);
            pPoseStack.popPose();
        }
        pPoseStack.popPose();
    }
}

