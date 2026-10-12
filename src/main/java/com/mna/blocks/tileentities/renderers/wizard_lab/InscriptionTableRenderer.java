/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 */
package com.mna.blocks.tileentities.renderers.wizard_lab;

import com.mna.api.tools.RLoc;
import com.mna.blocks.sorcery.InscriptionTableBlock;
import com.mna.blocks.tileentities.models.ModelInscriptionTable;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class InscriptionTableRenderer
extends GeoBlockRenderer<InscriptionTableTile> {
    public static final ResourceLocation ink = RLoc.create("block/wizard_lab/inscription_table_ink");
    public static final ResourceLocation ash = RLoc.create("block/wizard_lab/inscription_table_ash");
    public static final ResourceLocation vellum = RLoc.create("block/wizard_lab/inscription_table_vellum");

    public InscriptionTableRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super((GeoModel)new ModelInscriptionTable());
    }

    public RenderType getRenderType(InscriptionTableTile animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entitySmoothCutout((ResourceLocation)texture);
    }

    public void postRender(PoseStack poseStack, InscriptionTableTile animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        Level world = animatable.getLevel();
        BlockPos pos = animatable.getBlockPos();
        BlockState state = animatable.getBlockState();
        poseStack.pushPose();
        Direction dir = (Direction)state.getValue((Property)HorizontalDirectionalBlock.FACING);
        switch (dir) {
            case EAST: {
                poseStack.translate(1.0f, 0.0f, 0.0f);
                poseStack.mulPose(Axis.YP.rotationDegrees(dir.toYRot()));
                break;
            }
            case WEST: {
                poseStack.translate(0.0f, 0.0f, 1.0f);
                poseStack.mulPose(Axis.YP.rotationDegrees(dir.toYRot()));
                break;
            }
            case NORTH: {
                break;
            }
            default: {
                poseStack.translate(1.0f, 0.0f, 1.0f);
                poseStack.mulPose(Axis.YP.rotationDegrees(dir.getOpposite().toYRot()));
            }
        }
        if (((Integer)state.getValue((Property)InscriptionTableBlock.CONTAINED_RESOURCES) & 1) != 0) {
            ModelUtils.renderModel(bufferSource.getBuffer(RenderType.cutout()), world, pos, state, ink, poseStack, packedLight, packedOverlay);
        }
        if (((Integer)state.getValue((Property)InscriptionTableBlock.CONTAINED_RESOURCES) & 2) != 0) {
            ModelUtils.renderModel(bufferSource.getBuffer(RenderType.cutout()), world, pos, state, vellum, poseStack, packedLight, packedOverlay);
        }
        if (((Integer)state.getValue((Property)InscriptionTableBlock.CONTAINED_RESOURCES) & 4) != 0) {
            ModelUtils.renderModel(bufferSource.getBuffer(RenderType.cutout()), world, pos, state, ash, poseStack, packedLight, packedOverlay);
        }
        poseStack.popPose();
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    public void actuallyRender(PoseStack poseStack, InscriptionTableTile animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        poseStack.translate(-1.0f, 0.0f, 0.0f);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.popPose();
    }
}

