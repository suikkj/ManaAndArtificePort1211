/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.RenderTypeHelper
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.affinity.Affinity;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.ElementalSentryTile;
import com.mna.blocks.tileentities.models.FixedBookModel;
import com.mna.blocks.tileentities.renderers.RunicAnvilRenderer;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ArcaneSentryRenderer
implements BlockEntityRenderer<ElementalSentryTile> {
    public static final HashMap<Affinity, ResourceLocation> affinityMaterials = new HashMap();
    private final FixedBookModel modelBook;
    private final Minecraft mc;
    private final Vector3 TARGET_POSITION = new Vector3(0.5, 1.25, 0.5);
    private final Vector3 NO_TARGET_POSITION = new Vector3(0.5, 0.08, 0.7);

    public ArcaneSentryRenderer(BlockEntityRendererProvider.Context context) {
        this.modelBook = new FixedBookModel(context.bakeLayer(FixedBookModel.LAYER_LOCATION));
        this.mc = Minecraft.getInstance();
    }

    public void render(ElementalSentryTile tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float f1;
        float f = (float)tileEntityIn.ticks + partialTicks;
        Level world = tileEntityIn.getLevel();
        BlockPos pos = tileEntityIn.getBlockPos();
        BlockState state = tileEntityIn.getBlockState();
        float t = MathUtils.clamp01(((float)tileEntityIn.ticksSinceTargetChange + partialTicks) / 10.0f);
        matrixStackIn.pushPose();
        Vector3 vOffset = tileEntityIn.hasTarget() ? Vector3.lerp(this.NO_TARGET_POSITION, this.TARGET_POSITION, t) : Vector3.lerp(this.TARGET_POSITION, this.NO_TARGET_POSITION, t);
        matrixStackIn.translate(vOffset.x, vOffset.y, vOffset.z);
        matrixStackIn.translate(0.0, (double)(0.1f + Mth.sin((float)(f * 0.1f)) * 0.01f), 0.0);
        for (f1 = tileEntityIn.nextPageAngle - tileEntityIn.pageAngle; f1 >= (float)Math.PI; f1 -= (float)Math.PI * 2) {
        }
        while (f1 < (float)(-Math.PI)) {
            f1 += (float)Math.PI * 2;
        }
        float f2 = (float)Math.toDegrees(tileEntityIn.pageAngle + f1 * partialTicks);
        float zpRotation = tileEntityIn.hasTarget() ? MathUtils.lerpf(90.0f, 0.0f, t) : MathUtils.lerpf(0.0f, 90.0f, t);
        float ypRotation = tileEntityIn.hasTarget() ? MathUtils.lerpf(90.0f, -f2, t) : MathUtils.lerpf(-f2, 90.0f, t);
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(zpRotation));
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(ypRotation));
        float f3 = Mth.lerp((float)partialTicks, (float)tileEntityIn.oFlip, (float)tileEntityIn.flip);
        float f4 = Mth.frac((float)(f3 + 0.25f)) * 1.6f - 0.3f;
        float f5 = Mth.frac((float)(f3 + 0.75f)) * 1.6f - 0.3f;
        float f6 = Mth.lerp((float)partialTicks, (float)tileEntityIn.pageTurningSpeed, (float)tileEntityIn.nextPageTurningSpeed);
        this.modelBook.setupAnim(f, Mth.clamp((float)f4, (float)0.0f, (float)1.0f), Mth.clamp((float)f5, (float)0.0f, (float)1.0f), f6);
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entitySolid((ResourceLocation)affinityMaterials.getOrDefault(tileEntityIn.getAffinity(), affinityMaterials.get(Affinity.ARCANE))));
        this.modelBook.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStackIn.popPose();
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5f, 0.01f, 0.5f);
        if (tileEntityIn.hasTarget()) {
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(f));
        }
        BakedModel model = this.mc.getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(RunicAnvilRenderer.ring_large));
        ModelData data = model.getModelData((BlockAndTintGetter)world, pos, state, ModelData.EMPTY);
        model.getRenderTypes(state, this.mc.level.random, data).forEach(rT -> {
            RenderType rType = RenderTypeHelper.getEntityRenderType((RenderType)rT, (boolean)false);
            VertexConsumer vertexBuilder = bufferIn.getBuffer(rType);
            for (BakedQuad quad : model.getQuads(null, null, this.mc.level.random, data, rType)) {
                vertexBuilder.putBulkData(matrixStackIn.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, combinedLightIn, combinedOverlayIn, true);
            }
        });
        matrixStackIn.popPose();
    }

    static {
        affinityMaterials.put(Affinity.ARCANE, RLoc.create("textures/block/artifice/sentry/sentry_arcane.png"));
        affinityMaterials.put(Affinity.ENDER, RLoc.create("textures/block/artifice/sentry/sentry_ender.png"));
        affinityMaterials.put(Affinity.EARTH, RLoc.create("textures/block/artifice/sentry/sentry_earth.png"));
        affinityMaterials.put(Affinity.FIRE, RLoc.create("textures/block/artifice/sentry/sentry_fire.png"));
        affinityMaterials.put(Affinity.WIND, RLoc.create("textures/block/artifice/sentry/sentry_wind.png"));
        affinityMaterials.put(Affinity.WATER, RLoc.create("textures/block/artifice/sentry/sentry_water.png"));
    }
}


