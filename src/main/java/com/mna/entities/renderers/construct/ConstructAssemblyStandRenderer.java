/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.entities.renderers.construct;

import com.mna.ManaAndArtifice;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.entities.constructs.ConstructAssemblyStand;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.models.constructs.ConstructAssemblyStandModel;
import com.mna.entities.models.constructs.ConstructModel;
import com.mna.entities.renderers.construct.ConstructRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConstructAssemblyStandRenderer
extends LivingEntityRenderer<ConstructAssemblyStand, ConstructAssemblyStandModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = ResourceLocation.parse("textures/entity/armorstand/wood.png");

    public ConstructAssemblyStandRenderer(EntityRendererProvider.Context p_173915_) {
        super(p_173915_, (EntityModel)new ConstructAssemblyStandModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0f);
    }

    public void render(ConstructAssemblyStand stand, float p_115309_, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int combinedLight) {
        super.render((LivingEntity)stand, p_115309_, partialTick, stack, bufferSource, combinedLight);
        stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(180.0f));
        stack.mulPose(Axis.YP.rotationDegrees(180.0f - stand.getYRot()));
        stack.translate(0.0f, 0.0f, 0.2f);
        try {
            Construct dummy = ManaAndArtifice.instance.proxy.getDummyConstructForRender();
            ConstructRenderer geoRenderer = ConstructRenderer.instance;
            ConstructModel model = ConstructRenderer.constructModel;
            model.getBakedModel(model.getModelResource(dummy));
            for (ConstructMaterial matl : stand.getComposition()) {
                model.resetMutexVisibility();
                model.setActiveMaterial(matl);
                dummy.getConstructData().resetParts();
                for (ItemConstructPart part : stand.getPartsForMaterial(matl)) {
                    dummy.getConstructData().setPart(new ItemStack((ItemLike)part));
                    model.setMutexVisibility(part.getSlot(), part.getModelTypeMutex());
                }
                stack.pushPose();
                geoRenderer.render(dummy, 0.0f, partialTick, stack, bufferSource, combinedLight);
                stack.popPose();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        stack.popPose();
    }

    public ResourceLocation getTextureLocation(ConstructAssemblyStand stand) {
        return DEFAULT_SKIN_LOCATION;
    }

    protected void setupRotations(ConstructAssemblyStand stand, PoseStack stack, float bob, float yRotLerp, float xRotLerp) {
        stack.mulPose(Axis.YP.rotationDegrees(180.0f - stand.getYRot()));
        float f = (float)(stand.level().getGameTime() - stand.lastHit) + xRotLerp;
        if (f < 5.0f) {
            stack.mulPose(Axis.YP.rotationDegrees(Mth.sin((float)(f / 1.5f * (float)Math.PI)) * 3.0f));
        }
    }

    protected boolean shouldShowName(ConstructAssemblyStand p_113815_) {
        return false;
    }

    @Nullable
    protected RenderType getRenderType(ConstructAssemblyStand stand, boolean p_113807_, boolean p_113808_, boolean p_113809_) {
        ResourceLocation resourcelocation = this.getTextureLocation(stand);
        if (p_113808_) {
            return RenderType.entityTranslucent((ResourceLocation)resourcelocation, (boolean)false);
        }
        return p_113807_ ? RenderType.entityCutoutNoCull((ResourceLocation)resourcelocation, (boolean)false) : null;
    }
}

