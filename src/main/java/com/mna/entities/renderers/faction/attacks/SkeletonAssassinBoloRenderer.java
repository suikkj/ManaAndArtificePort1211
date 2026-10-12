/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package com.mna.entities.renderers.faction.attacks;

import com.mna.entities.models.projectile.EntitySkeletonAssassinBoloModel;
import com.mna.entities.projectile.SkeletonAssassinBolo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SkeletonAssassinBoloRenderer
extends GeoEntityRenderer<SkeletonAssassinBolo> {
    public SkeletonAssassinBoloRenderer(EntityRendererProvider.Context context) {
        super(context, (GeoModel)new EntitySkeletonAssassinBoloModel());
    }

    public void render(SkeletonAssassinBolo entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        float ticks = ((float)entity.tickCount + partialTicks) * 50.0f;
        stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp((float)partialTicks, (float)entity.yRotO, (float)entity.getYRot()) - 90.0f));
        stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp((float)partialTicks, (float)entity.xRotO, (float)entity.getXRot())));
        if (!entity.isInGround()) {
            stack.mulPose(Axis.YP.rotationDegrees(ticks));
        }
        super.render((Entity)entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
        stack.popPose();
    }
}

