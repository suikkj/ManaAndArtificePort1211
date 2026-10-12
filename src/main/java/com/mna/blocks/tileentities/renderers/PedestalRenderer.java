/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.EntityRenderDispatcher
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.blocks.runeforging.PedestalBlock;
import com.mna.blocks.tileentities.PedestalTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class PedestalRenderer
implements BlockEntityRenderer<PedestalTile> {
    private final Minecraft mc = Minecraft.getInstance();
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    private final EntityRenderDispatcher renderDispatcher = this.mc.getEntityRenderDispatcher();
    private final Font font;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.font = this.mc.font;
    }

    public void render(PedestalTile tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack itemstack = tileEntityIn.getDisplayedItem();
        if (!itemstack.isEmpty()) {
            matrixStackIn.pushPose();
            if (itemstack.getItem() instanceof BlockItem) {
                matrixStackIn.translate(0.5, 1.2, 0.5);
            } else {
                matrixStackIn.translate(0.5, 1.0, 0.5);
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
            matrixStackIn.popPose();
            this.renderHoverText(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }

    private void renderHoverText(PedestalTile tileEntityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!((Boolean)tileEntityIn.getBlockState().getValue((Property)PedestalBlock.SHOW_SIGN)).booleanValue()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult brtr = (BlockHitResult)mc.hitResult;
        if (!brtr.getBlockPos().equals(tileEntityIn.getBlockPos())) {
            return;
        }
        if (!brtr.getDirection().equals(tileEntityIn.getBlockState().getValue((Property)HorizontalDirectionalBlock.FACING))) {
            return;
        }
        Vec3 dist = mc.player.position().subtract(Vec3.atCenterOf((Vec3i)tileEntityIn.getBlockPos())).normalize().scale(0.5);
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 1.1, 0.5);
        matrixStackIn.translate(dist.x, dist.y, dist.z);
        matrixStackIn.scale(0.5f, 0.5f, 0.5f);
        this.renderString(tileEntityIn.getDisplayedItem().getDisplayName().getString(), matrixStackIn, bufferIn, combinedLightIn);
        matrixStackIn.popPose();
    }

    protected void renderString(String text, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        stack.pushPose();
        stack.mulPose(this.renderDispatcher.cameraOrientation());
        stack.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = stack.last().pose();
        float opacity = 0.25f;
        int textColor = (int)(opacity * 255.0f) << 24;
        float hOffset = -this.font.width(text) / 2;
        this.font.drawInBatch(text, hOffset, 0.0f, 0x20FFFFFF, false, matrix4f, buffer, Font.DisplayMode.NORMAL, textColor, packedLight);
        this.font.drawInBatch(text, hOffset, 0.0f, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        stack.popPose();
    }
}

