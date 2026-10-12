/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.SpectralTile;
import com.mna.blocks.tileentities.models.SpectralModel;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class SpectralRenderer
extends GeoBlockRenderer<SpectralTile> {
    public static final ResourceLocation floating_runes = RLoc.create("block/spectral/conjured_runes");
    public static final ResourceLocation floating_top = RLoc.create("block/spectral/conjured_floaty_top");
    public static final ResourceLocation floating_mid = RLoc.create("block/spectral/conjured_floaty_mid");
    public static final ResourceLocation floating_bot = RLoc.create("block/spectral/conjured_floaty_bot");
    public static final ResourceLocation workbench_top = RLoc.create("block/spectral/conjured_craftingtable");
    public static final ResourceLocation anvil_top = RLoc.create("block/spectral/conjured_anvil");
    public static final ResourceLocation stonecutter_top = RLoc.create("block/spectral/conjured_stonecutter");
    public static final ResourceLocation stonecutter_blade = RLoc.create("block/spectral/conjured_stonecutter_blade");
    protected Minecraft mc = Minecraft.getInstance();

    public SpectralRenderer(BlockEntityRendererProvider.Context context) {
        super((GeoModel)new SpectralModel());
    }

    public RenderType getRenderType(SpectralTile animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.translucent();
    }

    public void renderRecursively(PoseStack stack, SpectralTile animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        stack.pushPose();
        RenderUtil.translateMatrixToBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.translateToPivotPoint((PoseStack)stack, (GeoBone)bone);
        RenderUtil.rotateMatrixAroundBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.scaleMatrixForBone((PoseStack)stack, (GeoBone)bone);
        BlockState state = animatable.getBlockState();
        BlockPos pos = animatable.getBlockPos();
        if (!bone.isHidden()) {
            stack.pushPose();
            switch (bone.getName()) {
                case "GRID": {
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, SpectralRenderer.getTabletopModel(state), stack, 0xF000F0, packedOverlay, renderType);
                    break;
                }
                case "BUZZSAW": {
                    ResourceLocation spinLoc = SpectralRenderer.getSpinModel(state);
                    if (spinLoc == null) break;
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, spinLoc, stack, 0xF000F0, packedOverlay, renderType);
                    break;
                }
                case "RUNES": {
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, floating_runes, stack, 0xF000F0, packedOverlay, renderType);
                    break;
                }
                case "SPINNY_TOP": {
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, floating_top, stack, 0xF000F0, packedOverlay, renderType);
                    break;
                }
                case "SPINNY_MID": {
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, floating_mid, stack, 0xF000F0, packedOverlay, renderType);
                    break;
                }
                case "SPINNY_BOT": {
                    ModelUtils.renderModel(bufferSource, (Level)this.mc.level, pos, state, floating_bot, stack, 0xF000F0, packedOverlay, renderType);
                }
            }
            stack.popPose();
            for (GeoBone childBone : bone.getChildBones()) {
                this.renderRecursively(stack, animatable, childBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
            }
        }
        stack.popPose();
    }

    private static ResourceLocation getTabletopModel(BlockState state) {
        Block block = state.getBlock();
        if (block == BlockInit.SPECTRAL_ANVIL.get()) {
            return anvil_top;
        }
        if (block == BlockInit.SPECTRAL_STONECUTTER.get()) {
            return stonecutter_top;
        }
        return workbench_top;
    }

    private static ResourceLocation getSpinModel(BlockState state) {
        Block block = state.getBlock();
        if (block == BlockInit.SPECTRAL_STONECUTTER.get()) {
            return stonecutter_blade;
        }
        return null;
    }
}

