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
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.cache.object.GeoBone
 */
package com.mna.entities.renderers.faction;

import com.mna.entities.faction.WitchHunter;
import com.mna.entities.models.faction.WitchHunterModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.GeoBone;

public class WitchHunterRenderer
extends MAGeckoRenderer<WitchHunter> {
    private static final ItemStack crossbow = new ItemStack((ItemLike)Items.CROSSBOW);
    private final Minecraft mc = Minecraft.getInstance();

    public WitchHunterRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchHunterModel());
    }

    @Override
    public void render(WitchHunter entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        stack.mulPose(Axis.YP.rotationDegrees(180.0f));
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
    }

    public void renderRecursively(PoseStack poseStack, WitchHunter animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (bone.getName().equals("right_hand")) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(15.0f));
            poseStack.translate(0.14, -0.13, 0.8);
            poseStack.scale(0.7f, 0.7f, 0.7f);
            this.mc.getItemRenderer().renderStatic(crossbow, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, packedOverlay, poseStack, bufferSource, (Level)this.mc.level, 0);
            poseStack.popPose();
            bufferSource.getBuffer(renderType);
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}

