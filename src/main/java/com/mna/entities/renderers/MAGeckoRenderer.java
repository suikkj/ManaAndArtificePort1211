/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.util.Color
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package com.mna.entities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.util.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public abstract class MAGeckoRenderer<T extends LivingEntity & GeoAnimatable>
extends GeoEntityRenderer<T> {
    protected boolean invisible;
    protected boolean hasEmissivePass = false;
    protected boolean emissivePass = false;

    protected MAGeckoRenderer(EntityRendererProvider.Context context, GeoModel<T> modelProvider) {
        super(context, modelProvider);
    }

    public MAGeckoRenderer<T> enableEmissive() {
        this.hasEmissivePass = true;
        return this;
    }

    public void render(T entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        try {
            float curScale = entity.getPersistentData().getFloat("mna_entity_scale");
            float prevScale = entity.getPersistentData().getFloat("mna_entity_scale_prev");
            float scale = curScale + (curScale - prevScale) * partialTicks;
            if (!entity.isAddedToLevel()) {
                scale = 1.0f;
            }
            stack.pushPose();
            stack.scale(scale, scale, scale);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.invisible = entity.isInvisible();
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
        try {
            stack.popPose();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void defaultRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, RenderType renderType, VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        Color renderColor = this.getRenderColor(animatable, partialTick, packedLight);
        float red = renderColor.getRedFloat();
        float green = renderColor.getGreenFloat();
        float blue = renderColor.getBlueFloat();
        float alpha = renderColor.getAlphaFloat();
        int packedOverlay = this.getPackedOverlay(animatable, 0.0f, 0.0f);
        BakedGeoModel model = this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(animatable));
        this.renderPass(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight, false, model, com.mna.tools.render.MNAGeo.packColour(red, green, blue, alpha), packedOverlay);
        if (this.hasEmissivePass) {
            this.renderPass(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight, true, model, com.mna.tools.render.MNAGeo.packColour(red, green, blue, alpha), packedOverlay);
        }
    }

    private void renderPass(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, RenderType renderType, VertexConsumer buffer, float yaw, float partialTick, int packedLight, boolean emissive, BakedGeoModel model, int colour, int packedOverlay) {
        this.emissivePass = emissive;
        if (renderType == null) {
            renderType = this.getRenderType(animatable, this.getTextureLocation(animatable), bufferSource, partialTick);
        }
        if (buffer == null) {
            buffer = bufferSource.getBuffer(renderType);
        }
        poseStack.pushPose();
        this.preRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
        if (this.firePreRenderEvent(poseStack, model, bufferSource, partialTick, packedLight)) {
            this.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, packedLight, packedLight, packedOverlay);
            this.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
            this.applyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            this.postRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
            this.firePostRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
        }
        poseStack.popPose();
        this.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
    }

    public RenderType getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        if (this.emissivePass) {
            return RenderType.eyes((ResourceLocation)texture);
        }
        return RenderType.entityCutout((ResourceLocation)texture);
    }
}

