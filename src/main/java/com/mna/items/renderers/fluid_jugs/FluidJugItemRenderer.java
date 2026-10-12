 
package com.mna.items.renderers.fluid_jugs;

import com.mna.api.tools.RLoc;
import com.mna.items.ItemInit;
import com.mna.items.artifice.FluidJug;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FluidJugItemRenderer
extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation jug_base = RLoc.create("item/special/fluid_jug");
    public static final ResourceLocation jug_artifact = RLoc.create("item/special/fluid_jug_artifact");
    private final ResourceLocation modelRLoc;
    private BakedModel jugModel;
    private final ItemColors itemColors;

    public FluidJugItemRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems, ResourceLocation modelRLoc) {
        super(berd, ems);
        this.modelRLoc = modelRLoc;
        this.itemColors = Minecraft.getInstance().getItemColors();
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext ItemDisplayContext2, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (!(stack.getItem() instanceof FluidJug)) {
        }
        if (this.jugModel == null) {
            this.jugModel = Minecraft.getInstance().getModelManager().getModel(this.modelRLoc);
        }
        FluidStack containedFluid = ((FluidJug)ItemInit.FLUID_JUG.get()).getFluidTagData(stack);
        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of((Fluid)containedFluid.getFluid());
        matrixStack.pushPose();
        for (BakedModel model : this.jugModel.getRenderPasses(stack, true)) {
            for (RenderType rendertype : model.getRenderTypes(stack, true)) {
                VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer((MultiBufferSource)buffer, (RenderType)rendertype, (boolean)true, (boolean)stack.hasFoil());
                this.renderModelLists(model, stack, combinedLight, combinedOverlay, matrixStack, vertexconsumer);
            }
        }
        float pct = MathUtils.clamp01((float)containedFluid.getAmount() / 16000.0f);
        boolean infinite = ((FluidJug)stack.getItem()).isInfinite();
        matrixStack.translate(0.5f, 0.186f, 0.3f);
        this.renderFluidBar(matrixStack, buffer, combinedLight, 0.0f, extension, pct, infinite);
        matrixStack.translate(0.0f, 0.0f, 0.4f);
        this.renderFluidBar(matrixStack, buffer, combinedLight, 0.0f, extension, pct, infinite);
        matrixStack.popPose();
    }

    public void renderModelLists(BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay, PoseStack pMatrixStack, VertexConsumer pBuffer) {
        RandomSource randomsource = RandomSource.create();
        for (Direction direction : Direction.values()) {
            randomsource.setSeed(42L);
            this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState)null, direction, randomsource), pStack, pCombinedLight, pCombinedOverlay);
        }
        randomsource.setSeed(42L);
        this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState)null, (Direction)null, randomsource), pStack, pCombinedLight, pCombinedOverlay);
    }

    public void renderQuadList(PoseStack pPoseStack, VertexConsumer pBuffer, List<BakedQuad> pQuads, ItemStack pItemStack, int pCombinedLight, int pCombinedOverlay) {
        boolean flag = !pItemStack.isEmpty();
        PoseStack.Pose posestack$pose = pPoseStack.last();
        for (BakedQuad bakedquad : pQuads) {
            int i = -1;
            if (flag && bakedquad.isTinted()) {
                i = this.itemColors.getColor(pItemStack, bakedquad.getTintIndex());
            }
            float f = (float)(i >> 16 & 0xFF) / 255.0f;
            float f1 = (float)(i >> 8 & 0xFF) / 255.0f;
            float f2 = (float)(i & 0xFF) / 255.0f;
            pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, 1.0f, pCombinedLight, pCombinedOverlay, true);
        }
    }

    private void renderFluidBar(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLight, float partialTicks, IClientFluidTypeExtensions attrs, float fillPct, boolean infinite) {
        float width = infinite ? 0.08f : 0.12f;
        float min = infinite ? 0.02f : 0.0f;
        float height = infinite ? 0.2f : 0.5f;
        ResourceLocation fluidTexBase = attrs.getStillTexture();
        if (fluidTexBase == null) {
        }
        TextureAtlasSprite sp = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTexBase);
        RenderType liquid = RenderType.armorCutoutNoCull((ResourceLocation)TextureAtlas.LOCATION_BLOCKS);
        VertexConsumer builder = bufferIn.getBuffer(liquid);
        int color = attrs.getTintColor();
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color >> 0 & 0xFF) / 255.0f;
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float[] rgba = new float[]{r, g, b, a};
        Matrix3f normal = ((PoseStack.Pose)matrixStackIn.last()).normal();
        Matrix4f pos = ((PoseStack.Pose)matrixStackIn.last()).pose();
        Vector3f nrm = new Vector3f(0.0f, 1.0f, 0.0f);
        nrm.mul((Matrix3fc)normal);
        float maxV = sp.getV0() + (sp.getV1() - sp.getV0()) * fillPct;
        float minU = sp.getU(0.0f);
        float maxU = sp.getU(8.0f);
        int light = 0xF000F0;
        builder.addVertex(pos, -width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(minU, sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(pos, width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(maxU, sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(pos, width, min + height * fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(maxU, maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(pos, -width, min + height * fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(minU, maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
    }
}


