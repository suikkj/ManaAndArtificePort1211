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
 */
package com.mna.entities.renderers.boss;

import com.mna.entities.boss.Odin;
import com.mna.entities.models.boss.OdinModel;
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

public class OdinRenderer
extends MAGeckoRenderer<Odin> {
    private static final ItemStack axe = new ItemStack((ItemLike)ItemInit.ALLFATHER_AXE.get());
    private final Minecraft mc = Minecraft.getInstance();

    public OdinRenderer(EntityRendererProvider.Context context) {
        super(context, new OdinModel());
    }

    public void renderRecursively(PoseStack stack, Odin animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        byte axeSlot = animatable.getAxeSlot();
        if (axeSlot == 1 && bone.getName().equals("left_hand_grip")) {
            stack.pushPose();
            stack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            stack.mulPose(Axis.YP.rotationDegrees(180.0f));
            stack.translate(0.5, 0.0, -0.775);
            Minecraft.getInstance().getItemRenderer().renderStatic(axe, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
            stack.popPose();
            bufferSource.getBuffer(renderType);
        } else if (axeSlot == 0 && bone.getName().equals("backSlot")) {
            stack.pushPose();
            stack.translate(-0.3, 2.1, 0.3);
            stack.mulPose(Axis.YP.rotationDegrees(90.0f));
            stack.mulPose(Axis.XP.rotationDegrees(135.0f));
            Minecraft.getInstance().getItemRenderer().renderStatic(axe, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
            stack.popPose();
            bufferSource.getBuffer(renderType);
        }
        super.renderRecursively(stack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}

