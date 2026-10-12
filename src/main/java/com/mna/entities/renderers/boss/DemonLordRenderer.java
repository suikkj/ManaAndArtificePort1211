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

import com.mna.entities.boss.DemonLord;
import com.mna.entities.models.boss.DemonLordModel;
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

public class DemonLordRenderer
extends MAGeckoRenderer<DemonLord> {
    private static final ItemStack axe = new ItemStack((ItemLike)ItemInit.DEMON_LORD_AXE.get());
    private static final ItemStack sword = new ItemStack((ItemLike)ItemInit.DEMON_LORD_SWORD.get());
    private static final ItemStack staff = new ItemStack((ItemLike)ItemInit.HELLFIRE_STAFF.get());
    private final Minecraft mc = Minecraft.getInstance();

    public DemonLordRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonLordModel());
    }

    public void renderRecursively(PoseStack stack, DemonLord animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (bone.getName().equals("held_item_left")) {
            boolean render = false;
            ItemStack renderStack = ItemStack.EMPTY;
            switch (animatable.getWeaponState()) {
                case AXE: {
                    render = true;
                    renderStack = axe;
                    break;
                }
                case SWORDS: {
                    render = true;
                    renderStack = sword;
                    break;
                }
                case STAFF: {
                    render = true;
                    renderStack = staff;
                    break;
                }
            }
            if (render) {
                stack.pushPose();
                stack.mulPose(Axis.XP.rotationDegrees(-90.0f));
                stack.mulPose(Axis.YP.rotationDegrees(180.0f));
                stack.translate(1.0, 0.3, -1.7);
                stack.scale(2.0f, 2.0f, 2.0f);
                Minecraft.getInstance().getItemRenderer().renderStatic(renderStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                stack.popPose();
                bufferSource.getBuffer(renderType);
            }
        } else if (bone.getName().equals("held_item_right") && animatable.getWeaponState() == DemonLord.WeaponState.SWORDS) {
            stack.pushPose();
            stack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            stack.mulPose(Axis.YP.rotationDegrees(180.0f));
            stack.translate(-1.0, 0.3, -1.7);
            stack.scale(2.0f, 2.0f, 2.0f);
            Minecraft.getInstance().getItemRenderer().renderStatic(sword, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
            stack.popPose();
            bufferSource.getBuffer(renderType);
        }
        super.renderRecursively(stack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}

