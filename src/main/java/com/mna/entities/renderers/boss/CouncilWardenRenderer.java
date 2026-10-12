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
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.entities.renderers.boss;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.entities.boss.CouncilWarden;
import com.mna.entities.models.boss.CouncilWardenModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mna.items.ItemInit;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

public class CouncilWardenRenderer
extends MAGeckoRenderer<CouncilWarden> {
    private static final ItemStack sword = new ItemStack((ItemLike)ItemInit.ASTRO_BLADE.get());
    private final Minecraft mc = Minecraft.getInstance();

    public CouncilWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new CouncilWardenModel());
    }

    public void renderRecursively(PoseStack stack, CouncilWarden animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (bone.getName().equals("right_grip_point")) {
            if (animatable.getWeaponState() == CouncilWarden.WeaponState.SWORD_IN_HAND) {
                stack.pushPose();
                RenderUtil.translateMatrixToBone((PoseStack)stack, (GeoBone)bone);
                RenderUtil.translateToPivotPoint((PoseStack)stack, (GeoBone)bone);
                RenderUtil.rotateMatrixAroundBone((PoseStack)stack, (GeoBone)bone);
                stack.mulPose(Axis.XP.rotationDegrees(-90.0f));
                stack.mulPose(Axis.YP.rotationDegrees(90.0f));
                RenderUtil.scaleMatrixForBone((PoseStack)stack, (GeoBone)bone);
                Minecraft.getInstance().getItemRenderer().renderStatic(sword, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                stack.popPose();
                bufferSource.getBuffer(renderType);
            }
        } else if (bone.getName().equals("back_anchor") && animatable.getWeaponState() == CouncilWarden.WeaponState.SWORD_ON_BACK) {
            stack.pushPose();
            stack.translate(0.4, 1.5, 0.25);
            stack.mulPose(Axis.ZP.rotationDegrees(-45.0f));
            stack.mulPose(Axis.YP.rotationDegrees(90.0f));
            stack.mulPose(Axis.XP.rotationDegrees(170.0f));
            Minecraft.getInstance().getItemRenderer().renderStatic(sword, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
            stack.popPose();
            bufferSource.getBuffer(renderType);
        }
        super.renderRecursively(stack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    static {
        sword.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("as_warden", true);
    }
}

