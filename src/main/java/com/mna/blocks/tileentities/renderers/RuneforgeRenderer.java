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
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.blocks.runeforging.RuneforgeBlock;
import com.mna.blocks.tileentities.RuneForgeTile;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class RuneforgeRenderer
implements BlockEntityRenderer<RuneForgeTile> {
    private final Minecraft mc = Minecraft.getInstance();
    private final ItemRenderer itemRenderer = this.mc.getItemRenderer();

    public RuneforgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(RuneForgeTile tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack itemstack = tileEntityIn.getDisplayedItem();
        if (!itemstack.isEmpty()) {
            matrixStackIn.pushPose();
            Direction dir = (Direction)tileEntityIn.getBlockState().getValue((Property)RuneforgeBlock.FACING);
            matrixStackIn.translate(0.5, 1.05, 0.5);
            switch (dir) {
                default: {
                    matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
                    break;
                }
                case SOUTH: {
                    matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
                    matrixStackIn.mulPose(Axis.ZP.rotationDegrees(180.0f));
                    break;
                }
                case EAST: {
                    matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
                    matrixStackIn.mulPose(Axis.ZP.rotationDegrees(90.0f));
                    break;
                }
                case WEST: {
                    matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0f));
                    matrixStackIn.mulPose(Axis.ZP.rotationDegrees(270.0f));
                }
            }
            matrixStackIn.scale(0.3f, 0.3f, 0.3f);
            this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
            matrixStackIn.popPose();
            if (((Boolean)tileEntityIn.getBlockState().getValue((Property)RuneforgeBlock.ACTIVE)).booleanValue()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
                    return;
                }
                BlockHitResult brtr = (BlockHitResult)mc.hitResult;
                if (!brtr.getBlockPos().equals(tileEntityIn.getBlockPos())) {
                    return;
                }
                float pct = 1.0f;
                if (((Boolean)tileEntityIn.getBlockState().getValue((Property)RuneforgeBlock.REPAIR)).booleanValue()) {
                    int damage = tileEntityIn.getItem(0).getDamageValue();
                    int maxDamage = tileEntityIn.getItem(0).getMaxDamage();
                    int durabilityRemaining = maxDamage - damage;
                    pct = (float)durabilityRemaining / (float)maxDamage;
                } else {
                    pct = tileEntityIn.getBurnPct();
                }
                int color = Mth.hsvToRgb((float)(pct / 3.0f), (float)1.0f, (float)1.0f) | 0xFF000000;
                int r = FastColor.ARGB32.red((int)color) / 3 * 2;
                int g = FastColor.ARGB32.green((int)color) / 3 * 2;
                int b = FastColor.ARGB32.blue((int)color) / 3 * 2;
                for (int i = 0; i < 4; ++i) {
                    matrixStackIn.pushPose();
                    matrixStackIn.translate(0.5, 0.0, 0.5);
                    matrixStackIn.mulPose(Axis.YP.rotationDegrees((float)(i * 90)));
                    matrixStackIn.translate(0.0, 0.90625, -0.377);
                    matrixStackIn.scale(0.44f, 0.05f, 1.0f);
                    WorldRenderUtils.renderProgressBar(matrixStackIn, bufferIn, pct, new int[]{r, g, b}, 255);
                    matrixStackIn.popPose();
                }
            }
        }
    }
}

