 
package com.mna.interop;

import com.mna.items.artifice.FluidJug;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CurioRenderer
implements ICurioRenderer {
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        matrixStack.pushPose();
        ICurioRenderer.translateIfSneaking((PoseStack)matrixStack, (LivingEntity)slotContext.entity());
        ICurioRenderer.rotateIfSneaking((PoseStack)matrixStack, (LivingEntity)slotContext.entity());
        if (stack.getItem() instanceof FluidJug) {
            matrixStack.translate(0.0f, 0.3f, 0.2f);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            matrixStack.scale(0.75f, 0.75f, 0.75f);
        } else {
            matrixStack.translate(0.0f, 0.3f, 0.2f);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            matrixStack.scale(2.0f, 2.0f, 2.0f);
        }
        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer, (Level)mc.level, light);
        if (stack.getItem() instanceof FluidJug) {
            FluidStack flStack = ((FluidJug)stack.getItem()).getFluidTagData(stack);
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of((Fluid)flStack.getFluid());
            if (!flStack.isEmpty()) {
                float pct = MathUtils.clamp01((float)flStack.getAmount() / 16000.0f);
                matrixStack.translate(0.0f, -0.315f, 0.2f);
                this.renderFluidBar(matrixStack, renderTypeBuffer, light, partialTicks, extension, pct, ((FluidJug)stack.getItem()).isInfinite());
            }
        }
        matrixStack.popPose();
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


