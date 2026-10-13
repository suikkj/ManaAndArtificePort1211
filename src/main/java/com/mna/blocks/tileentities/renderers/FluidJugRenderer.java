 
package com.mna.blocks.tileentities.renderers;

import com.mna.blocks.artifice.FluidJugBlock;
import com.mna.blocks.tileentities.FluidJugTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FluidJugRenderer
implements BlockEntityRenderer<FluidJugTile> {
    public FluidJugRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(FluidJugTile jug, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = jug.getBlockState();
        FluidStack flStack = jug.getContainedFluid();
        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of((Fluid)flStack.getFluid());
        matrixStackIn.pushPose();
        if (state.getValue((Property)FluidJugBlock.FACING) == Direction.WEST || state.getValue((Property)FluidJugBlock.FACING) == Direction.EAST) {
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0f));
            matrixStackIn.translate(-0.5, 0.1875, 0.3125);
        } else {
            matrixStackIn.translate(0.5, 0.1875, 0.3125);
        }
        this.renderFluidBar(matrixStackIn, bufferIn, combinedLightIn, partialTicks, extension, jug.getFillPct(), jug.isInfinite());
        matrixStackIn.popPose();
        matrixStackIn.pushPose();
        if (state.getValue((Property)FluidJugBlock.FACING) == Direction.WEST || state.getValue((Property)FluidJugBlock.FACING) == Direction.EAST) {
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0f));
            matrixStackIn.translate(-0.5, 0.1875, 0.6875);
        } else {
            matrixStackIn.translate(0.5, 0.1875, 0.6875);
        }
        this.renderFluidBar(matrixStackIn, bufferIn, combinedLightIn, partialTicks, extension, jug.getFillPct(), jug.isInfinite());
        matrixStackIn.popPose();
    }

    private void renderFluidBar(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLight, float partialTicks, IClientFluidTypeExtensions attrs, float fillPct, boolean infinite) {
        float width = infinite ? 0.08f : 0.12f;
        float min = infinite ? 0.02f : 0.0f;
        float height = infinite ? 0.2f : 0.5f;
        ResourceLocation fluidTexBase = attrs.getStillTexture();
        if (fluidTexBase == null) {
            return;
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
        builder.addVertex(pos, -width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(minU, sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(pos, width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(maxU, sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(pos, width, min + height * fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(maxU, maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(pos, -width, min + height * fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(minU, maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
    }
}


