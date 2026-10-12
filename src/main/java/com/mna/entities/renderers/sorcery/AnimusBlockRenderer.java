/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.SlimeModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package com.mna.entities.renderers.sorcery;

import com.mna.entities.summon.AnimusBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class AnimusBlockRenderer
extends MobRenderer<AnimusBlock, SlimeModel<AnimusBlock>> {
    private final BlockRenderDispatcher dispatcher;

    public AnimusBlockRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, (EntityModel)new SlimeModel(pContext.bakeLayer(ModelLayers.SLIME)), 0.25f);
        this.dispatcher = pContext.getBlockRenderDispatcher();
    }

    public void render(AnimusBlock pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        Level level;
        BlockState blockstate = pEntity.getMimicState();
        if (blockstate.getRenderShape() == RenderShape.MODEL && blockstate != (level = pEntity.level()).getBlockState(pEntity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            pMatrixStack.pushPose();
            BlockPos blockpos = BlockPos.containing((double)pEntity.getX(), (double)pEntity.getBoundingBox().maxY, (double)pEntity.getZ());
            float f = Mth.rotLerp((float)pPartialTicks, (float)pEntity.yBodyRotO, (float)pEntity.yBodyRot);
            pMatrixStack.mulPose(Axis.YP.rotationDegrees(-f));
            this.scale(pEntity, pMatrixStack, pPartialTicks);
            pMatrixStack.translate(-0.5, 0.0, -0.5);
            BakedModel model = this.dispatcher.getBlockModel(blockstate);
            for (RenderType renderType : model.getRenderTypes(blockstate, RandomSource.create((long)432L), ModelData.EMPTY)) {
                this.dispatcher.getModelRenderer().tesselateBlock((BlockAndTintGetter)level, model, blockstate, blockpos, pMatrixStack, pBuffer.getBuffer(renderType), false, RandomSource.create(), 432L, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
            }
            pMatrixStack.popPose();
        }
    }

    protected void scale(AnimusBlock entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        matrixStackIn.scale(0.999f, 0.999f, 0.999f);
        matrixStackIn.translate(0.0, (double)0.001f, 0.0);
        float f1 = 1.0f;
        float f2 = Mth.lerp((float)partialTickTime, (float)entitylivingbaseIn.prevSquishFactor, (float)entitylivingbaseIn.squishFactor) / (f1 * 0.5f + 1.0f);
        float f3 = 1.0f / (f2 + 1.0f);
        matrixStackIn.scale(f3 * f1, 1.0f / f3 * f1, f3 * f1);
    }

    public ResourceLocation getTextureLocation(AnimusBlock entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}


