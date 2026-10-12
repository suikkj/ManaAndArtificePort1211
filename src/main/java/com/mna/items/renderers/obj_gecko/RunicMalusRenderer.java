/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.NonNullList
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.items.renderers.obj_gecko;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.items.relic.RunicMalus;
import com.mna.items.renderers.models.ModelRunicMalus;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class RunicMalusRenderer
extends GeoItemRenderer<RunicMalus> {
    public static final ResourceLocation hammer_model = RLoc.create("item/special/malus_hammer");
    protected Minecraft mc = Minecraft.getInstance();
    protected NonNullList<ItemStack> runes;

    public RunicMalusRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super((GeoModel)new ModelRunicMalus());
    }

    public RenderType getRenderType(RunicMalus animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.solid();
    }

    public void renderByItem(ItemStack itemStack, ItemDisplayContext p_239207_2_, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int p_239207_6_) {
        this.runes = RunicMalus.getRunes(itemStack);
        super.renderByItem(itemStack, p_239207_2_, matrixStack, bufferIn, combinedLightIn, p_239207_6_);
    }

    public void renderRecursively(PoseStack stack, RunicMalus animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (this.mc == null) {
            return;
        }
        stack.pushPose();
        RenderUtil.translateMatrixToBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.translateToPivotPoint((PoseStack)stack, (GeoBone)bone);
        RenderUtil.rotateMatrixAroundBone((PoseStack)stack, (GeoBone)bone);
        RenderUtil.scaleMatrixForBone((PoseStack)stack, (GeoBone)bone);
        if (!bone.isHidden()) {
            stack.pushPose();
            switch (bone.getName()) {
                case "ROOT": {
                    ModelUtils.renderEntityModel(buffer, ManaAndArtifice.instance.proxy.getClientWorld(), hammer_model, stack, packedLight, OverlayTexture.NO_OVERLAY);
                    break;
                }
                case "RUNE_1": {
                    if (this.runes.size() < 1 || ((ItemStack)this.runes.get(0)).isEmpty()) break;
                    stack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    stack.scale(0.5f, 0.5f, 0.5f);
                    Minecraft.getInstance().getItemRenderer().renderStatic((ItemStack)this.runes.get(0), ItemDisplayContext.FIXED, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                    break;
                }
                case "RUNE_2": {
                    if (this.runes.size() < 1 || ((ItemStack)this.runes.get(1)).isEmpty()) break;
                    stack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    stack.scale(0.5f, 0.5f, 0.5f);
                    Minecraft.getInstance().getItemRenderer().renderStatic((ItemStack)this.runes.get(1), ItemDisplayContext.FIXED, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                    break;
                }
                case "RUNE_3": {
                    if (this.runes.size() < 2 || ((ItemStack)this.runes.get(2)).isEmpty()) break;
                    stack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    stack.scale(0.5f, 0.5f, 0.5f);
                    Minecraft.getInstance().getItemRenderer().renderStatic((ItemStack)this.runes.get(2), ItemDisplayContext.FIXED, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                    break;
                }
                case "RUNE_4": {
                    if (this.runes.size() < 3 || ((ItemStack)this.runes.get(3)).isEmpty()) break;
                    stack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    stack.scale(0.5f, 0.5f, 0.5f);
                    Minecraft.getInstance().getItemRenderer().renderStatic((ItemStack)this.runes.get(3), ItemDisplayContext.FIXED, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                    break;
                }
                case "RUNE_5": {
                    if (this.runes.size() < 4 || ((ItemStack)this.runes.get(4)).isEmpty()) break;
                    stack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    stack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                    stack.scale(0.5f, 0.5f, 0.5f);
                    Minecraft.getInstance().getItemRenderer().renderStatic((ItemStack)this.runes.get(4), ItemDisplayContext.FIXED, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                    break;
                }
                case "RUNE_6": {
                    if (this.runes.size() < 5 || ((ItemStack)this.runes.get(5)).isEmpty()) break;
                    stack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    stack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                    stack.scale(0.5f, 0.5f, 0.5f);
                    Minecraft.getInstance().getItemRenderer().renderStatic((ItemStack)this.runes.get(5), ItemDisplayContext.FIXED, packedLight, packedOverlay, stack, bufferSource, (Level)this.mc.level, 0);
                }
            }
            stack.popPose();
            for (GeoBone childBone : bone.getChildBones()) {
                this.renderRecursively(stack, animatable, childBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
            }
        }
        stack.popPose();
    }
}

